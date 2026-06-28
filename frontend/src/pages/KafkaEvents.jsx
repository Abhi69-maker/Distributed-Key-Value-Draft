import React from 'react'
import { Card, SectionLabel, Badge, EventRow, LiveDot } from '../components/UI.jsx'

const REPLICATION_STEPS = [
    { label: 'Client PUT /store',           color: '#00d4ff', type: 'accent'  },
    { label: 'Node 1 → PostgreSQL',         color: '#8b5cf6', type: 'purple'  },
    { label: 'Node 1 → Redis SET',          color: '#22c55e', type: 'green'   },
    { label: 'Publish kv-create-events',    color: '#f59e0b', type: 'amber'   },
    { label: 'Node 2 consumes',             color: '#00d4ff', type: 'accent'  },
    { label: 'Node 3 consumes',             color: '#00d4ff', type: 'accent'  },
    { label: 'All nodes synchronized ✓',    color: '#22c55e', type: 'green'   },
]

export default function KafkaEvents({ store }) {
    const { topics, events } = store
    const recent = events.slice(-20).reverse()

    return (
        <div>
            {/* Topics table */}
            <Card style={{ marginBottom: 12, padding: 0 }}>
                <div style={{ padding: '14px 16px 10px' }}>
                    <SectionLabel style={{ marginBottom: 0 }}>Kafka Topics</SectionLabel>
                </div>
                <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 12, fontFamily: 'var(--mono)' }}>
                    <thead>
                    <tr style={{ borderBottom: '0.5px solid var(--border)' }}>
                        {['Topic', 'Messages', 'Rate', 'Partitions', 'Consumer Group'].map(h => (
                            <th key={h} style={{ padding: '8px 16px', textAlign: 'left', fontSize: 10, color: 'var(--text3)', fontWeight: 500, textTransform: 'uppercase', letterSpacing: '0.06em' }}>{h}</th>
                        ))}
                    </tr>
                    </thead>
                    <tbody>
                    {topics.map(t => (
                        <tr key={t.name} style={{ borderBottom: '0.5px solid var(--border)' }}>
                            <td style={{ padding: '9px 16px', color: '#00d4ff' }}>{t.name}</td>
                            <td style={{ padding: '9px 16px', color: 'var(--text)' }}>{t.msgs.toLocaleString()}</td>
                            <td style={{ padding: '9px 16px' }}>
                                <Badge color="amber">{t.rate}/s</Badge>
                            </td>
                            <td style={{ padding: '9px 16px', color: 'var(--text2)' }}>{t.partitions}</td>
                            <td style={{ padding: '9px 16px', color: 'var(--text3)', fontSize: 11 }}>{t.group}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </Card>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, marginBottom: 12 }}>
                {/* Live event stream */}
                <Card>
                    <SectionLabel style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 10 }}>
                        <span>Event Stream</span><LiveDot />
                    </SectionLabel>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 4, maxHeight: 280, overflowY: 'auto' }}>
                        {recent.length === 0
                            ? <div style={{ fontSize: 11, fontFamily: 'var(--mono)', color: 'var(--text3)' }}>Waiting for events…</div>
                            : recent.map(ev => <EventRow key={ev.id} event={ev} />)
                        }
                    </div>
                </Card>

                {/* Topic stats mini-cards */}
                <Card>
                    <SectionLabel>Topic Activity Breakdown</SectionLabel>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                        {topics.map(t => {
                            const total = topics.reduce((s, x) => s + x.msgs, 0)
                            const pct = Math.round((t.msgs / total) * 100)
                            return (
                                <div key={t.name}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4, fontSize: 11, fontFamily: 'var(--mono)' }}>
                                        <span style={{ color: 'var(--text2)' }}>{t.name}</span>
                                        <span style={{ color: 'var(--text3)' }}>{pct}%</span>
                                    </div>
                                    <div style={{ height: 4, background: 'var(--bg3)', borderRadius: 2, overflow: 'hidden' }}>
                                        <div style={{ height: '100%', width: pct + '%', background: '#00d4ff', borderRadius: 2, opacity: 0.7 }} />
                                    </div>
                                </div>
                            )
                        })}
                    </div>
                </Card>
            </div>

            {/* Replication workflow */}
            <Card>
                <SectionLabel>Replication Workflow</SectionLabel>
                <div style={{ display: 'flex', alignItems: 'center', flexWrap: 'wrap', gap: 6 }}>
                    {REPLICATION_STEPS.map((s, i) => (
                        <React.Fragment key={s.label}>
                            <div style={{
                                padding: '5px 10px', borderRadius: 6,
                                fontSize: 11, fontFamily: 'var(--mono)',
                                background: s.color + '18', color: s.color,
                                border: `0.5px solid ${s.color}44`,
                            }}>{s.label}</div>
                            {i < REPLICATION_STEPS.length - 1 && (
                                <span style={{ color: 'var(--text3)', fontSize: 14 }}>→</span>
                            )}
                        </React.Fragment>
                    ))}
                </div>
                <div style={{ marginTop: 12, fontSize: 11, fontFamily: 'var(--mono)', color: 'var(--text3)', lineHeight: 1.8 }}>
                    <div>Topics: <span style={{ color: '#f59e0b' }}>kv-create-events</span>, <span style={{ color: '#00d4ff' }}>kv-update-events</span>, <span style={{ color: '#ef4444' }}>kv-delete-events</span>, node-status-events, audit-events</div>
                    <div>Consumer group: <span style={{ color: 'var(--text2)' }}>kv-replication-group</span> · Offset commit: <span style={{ color: 'var(--text2)' }}>auto (5s)</span> · Serialization: <span style={{ color: 'var(--text2)' }}>JSON</span></div>
                </div>
            </Card>
        </div>
    )
}
