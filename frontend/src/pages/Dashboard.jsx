import { Activity, Database, Gauge, Layers, Radio } from 'lucide-react'
import { Card, MetricCard, EmptyState, EventRow } from '../components/UI.jsx'
import ClusterCanvas from '../components/ClusterCanvas.jsx'

export default function Dashboard({ state }) {
    const onlineNodes = state.nodes.filter(n => n.status === 'online').length

    return (
        <div className="fade-in">
            <div className="page-header">
                <div className="page-title">Dashboard</div>
                <div className="page-sub">Live cluster overview — updates only from real backend responses</div>
            </div>

            <div className="metrics-grid">
                <MetricCard
                    label="Total Keys"
                    value={state.totalKeys}
                    sub={`${state.kvEntries.length} loaded in browser`}
                    accent="cyan"
                />
                <MetricCard
                    label="Cache Hit Rate"
                    value={state.cacheHitRate != null ? `${state.cacheHitRate}%` : null}
                    sub={state.cacheHitRate == null ? 'No data yet' : 'Redis cache'}
                    accent="emerald"
                />
                <MetricCard
                    label="Requests / sec"
                    value={state.requestsPerSec}
                    sub={state.requestsPerSec == null ? 'No data yet' : 'Gateway throughput'}
                    accent="amber"
                />
                <MetricCard
                    label="Replication Lag"
                    value={state.replicationLag != null ? `${state.replicationLag}ms` : null}
                    sub={state.replicationLag == null ? 'No data yet' : 'Cross-node sync'}
                    accent="violet"
                />
            </div>

            <div className="grid-2" style={{ gridTemplateColumns: '1.6fr 1fr', marginBottom: 16, alignItems: 'start' }}>
                <Card title="Cluster Topology" action={
                    <span style={{ fontSize: 11, color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>
            {onlineNodes}/{state.nodes.length} online
          </span>
                }>
                    <ClusterCanvas nodes={state.nodes} />
                </Card>

                <Card title="Live Events" action={<Radio size={14} color="var(--text-muted)" />}>
                    {state.events.length === 0 ? (
                        <EmptyState
                            icon={Activity}
                            title="No events yet"
                            description="Events appear here when you PUT, GET, or DELETE keys, or when a node's health status changes."
                        />
                    ) : (
                        <div className="event-list">
                            {state.events.slice(0, 12).map(ev => <EventRow key={ev.id} ev={ev} />)}
                        </div>
                    )}
                </Card>
            </div>

            <div className="grid-2">
                <Card title="Node Summary">
                    <div className="table-wrap">
                        <table>
                            <thead>
                            <tr>
                                <th>Node</th><th>Status</th><th>CPU</th><th>Memory</th>
                            </tr>
                            </thead>
                            <tbody>
                            {state.nodes.map(n => (
                                <tr key={n.id}>
                                    <td className="primary">{n.id}</td>
                                    <td>
                                        <span className={`status-dot ${n.status}`} style={{ marginRight: 6, display: 'inline-block' }} />
                                        {n.status}
                                    </td>
                                    <td>{n.cpu != null ? `${n.cpu}%` : '—'}</td>
                                    <td>{n.memoryMb != null ? `${n.memoryMb} MB` : '—'}</td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                </Card>

                <Card title="Throughput History">
                    {state.rpsHistory.length === 0 ? (
                        <EmptyState
                            icon={Gauge}
                            title="Awaiting throughput data"
                            description="The sparkline will populate once your gateway starts reporting requests-per-second."
                        />
                    ) : (
                        <Sparkline data={state.rpsHistory} />
                    )}
                </Card>
            </div>
        </div>
    )
}

function Sparkline({ data }) {
    const max = Math.max(...data.map(d => d.v), 1)
    const w = 100 / Math.max(data.length - 1, 1)
    return (
        <svg viewBox="0 0 100 40" preserveAspectRatio="none" style={{ width: '100%', height: 100 }}>
            <polyline
                points={data.map((d, i) => `${i * w},${40 - (d.v / max) * 36}`).join(' ')}
                fill="none"
                stroke="var(--cyan)"
                strokeWidth="1.5"
                vectorEffect="non-scaling-stroke"
            />
        </svg>
    )
}
