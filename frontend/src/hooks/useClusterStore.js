import { useState, useEffect, useRef, useCallback } from 'react'
import { INITIAL_KV_DATA, KAFKA_TOPICS, KEY_NAMES, NODE_NAMES, EVENT_TYPES, OPS } from '../data/seed.js'

function nowStr() {
    const d = new Date()
    return [d.getHours(), d.getMinutes(), d.getSeconds()]
        .map(n => String(n).padStart(2, '0')).join(':')
}
function randOf(arr) { return arr[Math.floor(Math.random() * arr.length)] }
function randInt(min, max) { return Math.floor(Math.random() * (max - min + 1)) + min }

function makeEvent() {
    return {
        id: Math.random().toString(36).slice(2),
        type: randOf(EVENT_TYPES),
        key: randOf(KEY_NAMES) + ':' + randInt(1, 9999),
        node: randOf(NODE_NAMES),
        time: nowStr(),
        ts: Date.now(),
    }
}

function makeAuditEntry(op, key, node) {
    return {
        id: Math.random().toString(36).slice(2),
        op: op || randOf(OPS),
        key: key || randOf(KEY_NAMES) + ':' + randInt(1, 9999),
        node: node || randOf(NODE_NAMES),
        ts: nowStr(),
        full: new Date().toISOString().replace('T', ' ').substring(0, 19),
    }
}

function seedAudit() {
    return Array.from({ length: 40 }, (_, i) => {
        const d = new Date(Date.now() - i * 18000)
        const ts = d.toISOString().replace('T', ' ').substring(0, 19)
        return {
            id: i.toString(),
            op: OPS[i % 3],
            key: KEY_NAMES[i % KEY_NAMES.length] + ':' + (1000 + i),
            node: NODE_NAMES[i % 3],
            ts: ts.substring(11),
            full: ts,
        }
    })
}

export function useClusterStore() {
    const [kvData, setKvData] = useState(INITIAL_KV_DATA)
    const [events, setEvents] = useState([])
    const [auditLog, setAuditLog] = useState(seedAudit)
    const [topics, setTopics] = useState(KAFKA_TOPICS)
    const [rpsHistory, setRpsHistory] = useState(() => Array.from({ length: 30 }, () => randInt(900, 1500)))
    const [metrics, setMetrics] = useState({ keys: 2847, hitRate: 87, rps: 1204, lag: 4 })
    const [nodeStatus, setNodeStatus] = useState({
        'node-1': { alive: true, cpu: 34, mem: 342, keys: 2847, offset: 14821, role: 'Leader' },
        'node-2': { alive: true, cpu: 28, mem: 318, keys: 2847, offset: 14819, role: 'Follower' },
        'node-3': { alive: true, cpu: 22, mem: 305, keys: 2847, offset: 14817, role: 'Follower' },
    })

    const pushEvent = useCallback((ev) => {
        setEvents(prev => {
            const next = [...prev, ev]
            return next.length > 300 ? next.slice(-300) : next
        })
    }, [])

    const addKV = useCallback((key, value, node) => {
        if (!key.trim()) return
        const entry = { key: key.trim(), value: value.trim(), node, ttl: '\u221e', cached: false }
        setKvData(prev => [entry, ...prev.filter(r => r.key !== key.trim())])
        const ev = makeEvent()
        ev.type = 'create'; ev.key = key.trim(); ev.node = node; ev.time = nowStr()
        pushEvent(ev)
        setAuditLog(prev => [makeAuditEntry('CREATE', key.trim(), node), ...prev])
    }, [pushEvent])

    const deleteKV = useCallback((key) => {
        setKvData(prev => prev.filter(r => r.key !== key))
        const ev = makeEvent()
        ev.type = 'delete'; ev.key = key; ev.time = nowStr()
        pushEvent(ev)
        setAuditLog(prev => [makeAuditEntry('DELETE', key, 'node-1'), ...prev])
    }, [pushEvent])

    const toggleNode = useCallback((nodeId) => {
        setNodeStatus(prev => {
            const cur = prev[nodeId]
            const ev = makeEvent()
            ev.type = 'health'
            ev.key = nodeId + (cur.alive ? ' went offline' : ' came online')
            ev.node = nodeId; ev.time = nowStr()
            pushEvent(ev)
            return { ...prev, [nodeId]: { ...cur, alive: !cur.alive } }
        })
    }, [pushEvent])

    // Live tick
    useEffect(() => {
        const id = setInterval(() => {
            const ev = makeEvent()
            pushEvent(ev)
            setAuditLog(prev => [makeAuditEntry(), ...prev].slice(0, 200))
            setRpsHistory(prev => {
                const next = [...prev.slice(1), randInt(900, 1600)]
                return next
            })
            setMetrics({
                keys: randInt(2840, 2860),
                hitRate: randInt(80, 93),
                rps: randInt(900, 1600),
                lag: randInt(2, 12),
            })
            setTopics(prev => prev.map(t => ({ ...t, msgs: t.msgs + t.rate })))
            setNodeStatus(prev => ({
                'node-1': { ...prev['node-1'], cpu: randInt(28, 45), offset: prev['node-1'].offset + randInt(5, 15) },
                'node-2': { ...prev['node-2'], cpu: randInt(20, 38), offset: prev['node-2'].offset + randInt(3, 12) },
                'node-3': { ...prev['node-3'], cpu: prev['node-3'].alive ? randInt(15, 32) : 0, offset: prev['node-3'].alive ? prev['node-3'].offset + randInt(2, 10) : prev['node-3'].offset },
            }))
        }, 2000)
        return () => clearInterval(id)
    }, [pushEvent])

    const aliveCount = Object.values(nodeStatus).filter(n => n.alive).length

    return {
        kvData, events, auditLog, topics, rpsHistory,
        metrics, nodeStatus, aliveCount,
        addKV, deleteKV, toggleNode,
    }
}
