import { Activity, Layers } from 'lucide-react'
import { Card, EmptyState, EventRow, Badge } from '../components/UI.jsx'

export default function KafkaEvents({ state }) {
    const { kafka, events } = state

    return (
        <div className="fade-in">
            <div className="page-header">
                <div className="page-title">Kafka Events</div>
                <div className="page-sub">Topic stats and replication events — sourced from /api/kafka/stats</div>
            </div>

            <div className="metrics-grid">
                <div className="metric-card amber">
                    <div className="metric-label">Total Messages</div>
                    <div className="metric-value">{kafka.totalMessages || 0}</div>
                </div>
                <div className="metric-card cyan">
                    <div className="metric-label">Throughput</div>
                    <div className="metric-value">{kafka.totalRate || 0}<span style={{ fontSize: 13, color: 'var(--text-muted)' }}>/s</span></div>
                </div>
                <div className="metric-card emerald">
                    <div className="metric-label">Active Topics</div>
                    <div className="metric-value">{kafka.topics.length}</div>
                </div>
                <div className="metric-card violet">
                    <div className="metric-label">Events Captured</div>
                    <div className="metric-value">{events.filter(e => e.op === 'SYNC').length}</div>
                </div>
            </div>

            <div className="grid-2">
                <Card title="Topics">
                    {kafka.topics.length === 0 ? (
                        <EmptyState
                            icon={Layers}
                            title="No topics reported"
                            description="Topic stats will appear once your gateway responds with data from /api/kafka/stats."
                        />
                    ) : (
                        <div className="table-wrap">
                            <table>
                                <thead><tr><th>Topic</th><th>Messages</th><th>Rate</th><th>Partitions</th></tr></thead>
                                <tbody>
                                {kafka.topics.map(t => (
                                    <tr key={t.name}>
                                        <td className="primary">{t.name}</td>
                                        <td>{t.messages}</td>
                                        <td>{t.rate}/s</td>
                                        <td>{t.partitions}</td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </Card>

                <Card title="Replication Events">
                    {events.filter(e => e.op === 'SYNC').length === 0 ? (
                        <EmptyState
                            icon={Activity}
                            title="No replication events yet"
                            description="Sync events appear here as your cluster replicates writes across nodes."
                        />
                    ) : (
                        <div className="event-list">
                            {events.filter(e => e.op === 'SYNC').slice(0, 14).map(ev => <EventRow key={ev.id} ev={ev} />)}
                        </div>
                    )}
                </Card>
            </div>
        </div>
    )
}
