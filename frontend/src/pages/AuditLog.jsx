import { useState } from 'react'
import { ScrollText } from 'lucide-react'
import { Card, EmptyState, Badge } from '../components/UI.jsx'
import { OP_COLOR } from '../data/seed.js'

export default function AuditLog({ state }) {
    const [filter, setFilter] = useState('ALL')
    const ops = ['ALL', 'GET', 'PUT', 'DELETE', 'SYNC', 'HEALTH']

    const filtered = filter === 'ALL'
        ? state.auditLog
        : state.auditLog.filter(e => e.op === filter)

    return (
        <div className="fade-in">
            <div className="page-header">
                <div className="page-title">Audit Log</div>
                <div className="page-sub">Every request your dashboard sends is recorded here in real time</div>
            </div>

            <Card title="Request History" action={
                <div className="row">
                    {ops.map(op => (
                        <button
                            key={op}
                            className={`btn btn-sm ${filter === op ? 'btn-primary' : 'btn-ghost'}`}
                            onClick={() => setFilter(op)}
                        >
                            {op}
                        </button>
                    ))}
                </div>
            }>
                {filtered.length === 0 ? (
                    <EmptyState
                        icon={ScrollText}
                        title="No audit entries yet"
                        description="Entries appear here as soon as you perform PUT, GET, or DELETE operations from the Key Browser."
                    />
                ) : (
                    <div className="table-wrap">
                        <table>
                            <thead>
                            <tr><th>Time</th><th>Op</th><th>Key</th><th>Node</th><th>Status</th><th>Latency</th></tr>
                            </thead>
                            <tbody>
                            {filtered.slice(0, 100).map(e => (
                                <tr key={e.id}>
                                    <td>{e.time}</td>
                                    <td><Badge color={OP_COLOR[e.op] || 'muted'}>{e.op}</Badge></td>
                                    <td className="primary">{e.key}</td>
                                    <td>{e.node}</td>
                                    <td>
                                        <Badge color={e.status === 'OK' ? 'emerald' : 'crimson'}>{e.status}</Badge>
                                    </td>
                                    <td>{e.latencyMs}ms</td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </Card>
        </div>
    )
}
