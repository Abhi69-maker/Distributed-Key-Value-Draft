import React, { useState } from 'react'
import { Card, SectionLabel, Badge, Select } from '../components/UI.jsx'

const OP_COLORS = {
    CREATE: { bg: 'rgba(34,197,94,0.12)',  color: '#22c55e' },
    UPDATE: { bg: 'rgba(0,212,255,0.12)',  color: '#00d4ff' },
    DELETE: { bg: 'rgba(239,68,68,0.12)',  color: '#ef4444' },
}

export default function AuditLog({ store }) {
    const { auditLog } = store
    const [opFilter,   setOpFilter]   = useState('all')
    const [nodeFilter, setNodeFilter] = useState('all')

    const filtered = auditLog.filter(e => {
        if (opFilter !== 'all'   && e.op   !== opFilter)   return false
        if (nodeFilter !== 'all' && e.node !== nodeFilter)  return false
        return true
    })

    return (
        <div>
            <Card style={{ marginBottom: 12 }}>
                <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                    <SectionLabel style={{ marginBottom: 0, flex: 1 }}>Audit Log — {filtered.length} entries</SectionLabel>
                    <Select
                        value={opFilter} onChange={setOpFilter}
                        options={[
                            { value: 'all',    label: 'All operations' },
                            { value: 'CREATE', label: 'CREATE'          },
                            { value: 'UPDATE', label: 'UPDATE'          },
                            { value: 'DELETE', label: 'DELETE'          },
                        ]}
                    />
                    <Select
                        value={nodeFilter} onChange={setNodeFilter}
                        options={[
                            { value: 'all',    label: 'All nodes' },
                            { value: 'node-1', label: 'Node 1'    },
                            { value: 'node-2', label: 'Node 2'    },
                            { value: 'node-3', label: 'Node 3'    },
                        ]}
                    />
                </div>
            </Card>

            <Card style={{ padding: 0 }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 12, fontFamily: 'var(--mono)' }}>
                    <thead>
                    <tr style={{ borderBottom: '0.5px solid var(--border)' }}>
                        {['Timestamp', 'Operation', 'Key', 'Node'].map(h => (
                            <th key={h} style={{ padding: '10px 16px', textAlign: 'left', fontSize: 10, color: 'var(--text3)', fontWeight: 500, textTransform: 'uppercase', letterSpacing: '0.06em' }}>{h}</th>
                        ))}
                    </tr>
                    </thead>
                    <tbody>
                    {filtered.slice(0, 80).map(e => {
                        const c = OP_COLORS[e.op] || OP_COLORS.CREATE
                        return (
                            <tr key={e.id} style={{ borderBottom: '0.5px solid var(--border)' }}>
                                <td style={{ padding: '8px 16px', color: 'var(--text3)' }}>{e.full}</td>
                                <td style={{ padding: '8px 16px' }}>
                    <span style={{ padding: '2px 7px', borderRadius: 4, fontSize: 10, fontWeight: 600, background: c.bg, color: c.color }}>
                      {e.op}
                    </span>
                                </td>
                                <td style={{ padding: '8px 16px', color: '#00d4ff' }}>{e.key}</td>
                                <td style={{ padding: '8px 16px' }}>
                                    <Badge color="gray" style={{ fontSize: 9 }}>{e.node}</Badge>
                                </td>
                            </tr>
                        )
                    })}
                    </tbody>
                </table>
                {filtered.length === 0 && (
                    <div style={{ padding: 24, textAlign: 'center', color: 'var(--text3)', fontSize: 12 }}>
                        No entries match the selected filters
                    </div>
                )}
                {filtered.length > 80 && (
                    <div style={{ padding: '12px 16px', fontSize: 11, color: 'var(--text3)', fontFamily: 'var(--mono)', borderTop: '0.5px solid var(--border)' }}>
                        Showing 80 of {filtered.length} entries
                    </div>
                )}
            </Card>
        </div>
    )
}
