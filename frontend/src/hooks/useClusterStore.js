import { useState, useEffect, useCallback, useRef } from 'react'
import { NODE_URLS, GATEWAY_URL, POLL_INTERVAL } from '../data/seed.js'

const ts = () => new Date().toLocaleTimeString('en-US', { hour12: false })

async function safeFetch(url, opts = {}) {
    try {
        const r = await fetch(url, { signal: AbortSignal.timeout(4000), ...opts })
        if (r.status === 404) return null
        if (!r.ok) throw new Error(`HTTP ${r.status}`)
        return await r.json()
    } catch {
        return null
    }
}

const INITIAL = {
    nodes: Object.keys(NODE_URLS).map(id => ({
        id, url: NODE_URLS[id], status: 'unknown',
        cpu: null, memoryMb: null, keys: null,
        kafkaOffset: null, storageBytes: null, lastSeen: null,
    })),
    gatewayOnline: false, totalKeys: 0, cacheHitRate: null,
    requestsPerSec: null, replicationLag: null,
    kvEntries: [], events: [],
    kafka: { topics: [], totalMessages: 0, totalRate: 0 },
    auditLog: [], rpsHistory: [], cacheHistory: [],
}

export function useClusterStore() {
    const [state, setState] = useState(INITIAL)
    const pollRef = useRef(null)

    const pushEvent = useCallback((ev) => {
        setState(s => ({
            ...s,
            events: [{ id: Date.now() + Math.random(), time: ts(), ...ev }, ...s.events].slice(0, 200),
        }))
    }, [])

    const pushAudit = useCallback((entry) => {
        setState(s => ({
            ...s,
            auditLog: [{ id: Date.now() + Math.random(), time: ts(), ...entry }, ...s.auditLog].slice(0, 500),
        }))
    }, [])

    const pollHealth = useCallback(async () => {
        const results = await Promise.all(
            Object.entries(NODE_URLS).map(async ([id, url]) => {
                const health  = await safeFetch(`${url}/actuator/health`)
                const metrics = await safeFetch(`${url}/actuator/metrics/jvm.memory.used`)
                const cpu     = await safeFetch(`${url}/actuator/metrics/process.cpu.usage`)
                const online  = health?.status === 'UP'
                return {
                    id,
                    status: online ? 'online' : (health === null ? 'offline' : 'degraded'),
                    memoryMb: metrics?.measurements?.[0]?.value
                        ? Math.round(metrics.measurements[0].value / 1024 / 1024) : null,
                    cpu: cpu?.measurements?.[0]?.value != null
                        ? Math.round(cpu.measurements[0].value * 100) : null,
                    lastSeen: online ? ts() : null,
                }
            })
        )
        setState(s => ({
            ...s,
            nodes: s.nodes.map(n => {
                const r = results.find(x => x.id === n.id)
                return r ? { ...n, ...r } : n
            }),
        }))
    }, [])

    const pollGateway = useCallback(async () => {
        const health  = await safeFetch(`${GATEWAY_URL}/actuator/health`)
        const cache   = await safeFetch(`${GATEWAY_URL}/api/v1/dashboard/cache-stats`)
        const kvMeta  = await safeFetch(`${GATEWAY_URL}/api/v1/dashboard/store-meta`)
        const kafkaSt = await safeFetch(`${GATEWAY_URL}/api/v1/dashboard/kafka-stats`)
        const rps     = await safeFetch(`${GATEWAY_URL}/api/v1/dashboard/rps`)

        setState(s => {
            const now = ts()
            const rpsVal   = rps?.value ?? null
            const cacheVal = cache?.hitRate != null ? Math.round(cache.hitRate * 100) : null
            return {
                ...s,
                gatewayOnline:  health?.status === 'UP',
                totalKeys:      kvMeta?.totalKeys ?? s.totalKeys,
                cacheHitRate:   cacheVal ?? s.cacheHitRate,
                requestsPerSec: rpsVal   ?? s.requestsPerSec,
                replicationLag: kvMeta?.replicationLagMs ?? s.replicationLag,
                kafka: kafkaSt ? {
                    topics:        kafkaSt.topics ?? [],
                    totalMessages: kafkaSt.totalMessages ?? 0,
                    totalRate:     kafkaSt.totalRate ?? 0,
                } : s.kafka,
                rpsHistory:   rpsVal   != null ? [...s.rpsHistory,   { t: now, v: rpsVal }].slice(-40) : s.rpsHistory,
                cacheHistory: cacheVal != null ? [...s.cacheHistory, { t: now, v: cacheVal }].slice(-40) : s.cacheHistory,
            }
        })
    }, [])

    // Auto-fetches all keys every poll cycle — no manual refresh needed
    const pollKeys = useCallback(async () => {
        const res = await safeFetch(`${GATEWAY_URL}/api/v1/keys?page=0&pageSize=100`)
        if (res) {
            const raw = Array.isArray(res) ? res : (res.entries ?? [])
            const kvEntries = raw.map(e => ({
                key:       e.key,
                value:     e.value,
                node:      e.nodeId  || 'gateway',
                ttl:       e.ttl     ?? null,
                updatedAt: e.updatedAt || e.createdAt || ts(),
            }))
            setState(s => ({ ...s, kvEntries, totalKeys: kvEntries.length }))
        }
    }, [])

    useEffect(() => {
        const run = () => { pollHealth(); pollGateway(); pollKeys() }
        run()
        pollRef.current = setInterval(run, POLL_INTERVAL)
        return () => clearInterval(pollRef.current)
    }, [pollHealth, pollGateway, pollKeys])

    // KVRequest only has key + value — no ttl field in backend DTO
    const kvPut = useCallback(async ({ key, value, node }) => {
        const url = node ? NODE_URLS[node] : GATEWAY_URL
        const t0 = Date.now()
        const res = await safeFetch(`${url}/api/v1/keys/create`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ key, value }),
        })
        const latencyMs = Date.now() - t0
        const ok = res !== null
        const entry = {
            key, value,
            node:      res?.nodeId || node || 'gateway',
            ttl:       null,
            updatedAt: res?.updatedAt || res?.createdAt || ts(),
        }
        if (ok) {
            setState(s => {
                const existing = s.kvEntries.findIndex(e => e.key === key)
                const kvEntries = existing >= 0
                    ? s.kvEntries.map((e, i) => i === existing ? entry : e)
                    : [entry, ...s.kvEntries]
                return { ...s, kvEntries, totalKeys: kvEntries.length }
            })
        }
        pushEvent({ op: 'PUT', key, node: entry.node, hit: null })
        pushAudit({ op: 'PUT', key, node: entry.node, status: ok ? 'OK' : 'ERROR', latencyMs })
        return { ok, res }
    }, [pushEvent, pushAudit])

    const kvGet = useCallback(async ({ key, node }) => {
        const url = node ? NODE_URLS[node] : GATEWAY_URL
        const t0 = Date.now()
        const res = await safeFetch(`${url}/api/v1/keys/${encodeURIComponent(key)}`)
        const latencyMs = Date.now() - t0
        const ok  = res !== null
        const hit = res?.source === 'cache' || res?.cacheHit === true
        pushEvent({ op: 'GET', key, node: res?.nodeId || node || 'gateway', hit })
        pushAudit({ op: 'GET', key, node: res?.nodeId || node || 'gateway', status: ok ? 'OK' : 'MISS', latencyMs })
        return { ok, hit, res }
    }, [pushEvent, pushAudit])

    const kvDelete = useCallback(async ({ key, node }) => {
        const url = node ? NODE_URLS[node] : GATEWAY_URL
        const t0 = Date.now()
        const res = await safeFetch(`${url}/api/v1/keys/delete/${encodeURIComponent(key)}`, { method: 'DELETE' })
        const latencyMs = Date.now() - t0
        const ok = res !== null
        if (ok) {
            setState(s => {
                const kvEntries = s.kvEntries.filter(e => e.key !== key)
                return { ...s, kvEntries, totalKeys: kvEntries.length }
            })
        }
        pushEvent({ op: 'DELETE', key, node: node || 'gateway', hit: null })
        pushAudit({ op: 'DELETE', key, node: node || 'gateway', status: ok ? 'OK' : 'ERROR', latencyMs })
        return { ok, res }
    }, [pushEvent, pushAudit])

    const refreshKeys = useCallback(async () => {
        const res = await safeFetch(`${GATEWAY_URL}/api/v1/keys?page=0&pageSize=100`)
        if (res) {
            const raw = Array.isArray(res) ? res : (res.entries ?? [])
            const kvEntries = raw.map(e => ({
                key: e.key, value: e.value,
                node: e.nodeId || 'gateway',
                ttl: e.ttl ?? null,
                updatedAt: e.updatedAt || e.createdAt || ts(),
            }))
            setState(s => ({ ...s, kvEntries, totalKeys: kvEntries.length }))
        }
        return res
    }, [])

    return { state, actions: { kvPut, kvGet, kvDelete, refreshKeys } }
}