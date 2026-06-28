import React from 'react'
import { AlertTriangle, RefreshCw } from 'lucide-react'
import { Card, SectionLabel, Badge, NodeStatusBadge, ProgressBar, EventRow, LiveDot, Btn } from '../components/UI.jsx'

function NodeCard({ id, node, onToggle }) {
    const cpu = node.cpu
    return (
        <Card style={{ position: 'relative' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
                <div style={{
                    width: 8, height: 8, borderRadius: '50%',
                    background: node.alive ? '#22c55e' : '#ef4444',
                    animation: node.alive ? 'pulse-dot 2s infinite' : 'none',
                }} />
                <span style={{ fontSize: 13, fontWeight: 600, fontFamily: 'var(--mono)', color: 'var(--text)', flex: 1 }}>{id}</span>
                {node.role === 'Leader' && <Badge color="purple">Leader</Badge>}
                <NodeStatusBadge alive={node.alive} />
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 7, marginBottom: 12, fontSize: 12, fontFamily: 'var(--mono)' }}>
                {[
                    ['Keys stored',   node.keys.toLocaleString()],
                    ['Memory',        node.mem + ' MB / 1 GB'],
                    ['CPU usage',     cpu + '%'],
                    ['Kafka offset',  node.offset.toLocaleString()],
                ].map(([k, v]) => (
                    <div key={k} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <span style={{ color: 'var(--text3)' }}>{k}</span>
                        <span style={{ color: 'var(--text)' }}>{v}</span>
                    </div>
                ))}
            </div>

            <ProgressBar value={cpu} max={100} height={5} />
            <div style={{ fontSize: 9, color: 'var(--text3)', fontFamily: 'var(--mono)', marginTop: 4, marginBottom: 12 }}>
                CPU utilization
            </div>

            <div style={{ display: 'flex', gap: 6 }}>
                <Btn size="sm" variant="danger" onClick={() => onToggle(id)} style={{ flex: 1 }}>
                    {node.alive ? <><AlertTriangle size={11} /> Simulate failure</> : <><RefreshCw size={11} /> Recover node</>}
                </Btn>
            </div>
        </Card>
    )
}

export default function Cluster({ store }) {
    const { nodeStatus, events, toggleNode } = store
    const recent = events.filter(e => e.type === 'sync' || e.type === 'health' || e.type === 'create').slice(-12).reverse()

    return (
        <div>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3,1fr)', gap: 12, marginBottom: 14 }}>
                {Object.entries(nodeStatus).map(([id, node]) => (
                    <NodeCard key={id} id={id} node={node} onToggle={toggleNode} />
                ))}
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
                <Card>
                    <SectionLabel style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        Replication Events <LiveDot />
                    </SectionLabel>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 4, maxHeight: 200, overflowY: 'auto' }}>
                        {recent.length === 0
                            ? <div style={{ fontSize: 11, fontFamily: 'var(--mono)', color: 'var(--text3)' }}>No replication events yet</div>
                            : recent.map(ev => <EventRow key={ev.id} event={ev} />)
                        }
                    </div>
                </Card>

                <Card>
                    <SectionLabel>Service Status</SectionLabel>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 8, fontSize: 12, fontFamily: 'var(--mono)' }}>
                        {[
                            { name: 'Kafka Broker',     status: 'Online',    color: '#22c55e', detail: '5 topics · 3 partitions' },
                            { name: 'Redis Cache',      status: 'Online',    color: '#22c55e', detail: '87% hit rate · 1,204 keys' },
                            { name: 'PostgreSQL',       status: 'Online',    color: '#22c55e', detail: '24/100 connections' },
                            { name: 'API Gateway',      status: 'Online',    color: '#22c55e', detail: 'Spring Cloud Gateway' },
                            { name: 'Prometheus',       status: 'Scraping',  color: '#00d4ff', detail: '15s interval' },
                            { name: 'Spring Actuator',  status: 'Active',    color: '#00d4ff', detail: '/actuator/health' },
                        ].map(s => (
                            <div key={s.name} style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '6px 8px', background: 'var(--bg3)', borderRadius: 6 }}>
                                <div style={{ width: 6, height: 6, borderRadius: '50%', background: s.color, flexShrink: 0 }} />
                                <span style={{ flex: 1, color: 'var(--text)' }}>{s.name}</span>
                                <span style={{ color: s.color, fontSize: 10 }}>{s.status}</span>
                                <span style={{ color: 'var(--text3)', fontSize: 10 }}>{s.detail}</span>
                            </div>
                        ))}
                    </div>
                </Card>
            </div>
        </div>
    )
}
