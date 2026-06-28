import React from 'react'
import { Database, Zap, Activity, Clock } from 'lucide-react'
import { MetricCard, Card, SectionLabel, EventRow, LiveDot } from '../components/UI.jsx'
import ClusterCanvas from '../components/ClusterCanvas.jsx'

function Sparkline({ data }) {
    const max = Math.max(...data, 1)
    return (
        <div style={{ display: 'flex', alignItems: 'flex-end', gap: 2, height: 36 }}>
            {data.map((v, i) => (
                <div key={i} style={{
                    flex: 1, borderRadius: '1px 1px 0 0', minHeight: 2,
                    height: `${Math.max(2, Math.round((v / max) * 36))}px`,
                    background: i === data.length - 1 ? '#00d4ff' : 'rgba(0,212,255,0.35)',
                    transition: 'height 0.3s ease',
                }} />
            ))}
        </div>
    )
}

export default function Dashboard({ store }) {
    const { metrics, events, rpsHistory, nodeStatus } = store
    const recent = events.slice(-10).reverse()
    return (
        <div>
            {/* Metrics row */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4,1fr)', gap: 12, marginBottom: 16 }}>
                <MetricCard label="Total Keys"      value={metrics.keys.toLocaleString()} sub="↑ 12 this minute"   subColor="#22c55e" icon={Database}  accentColor="#00d4ff" />
                <MetricCard label="Cache Hit Rate"  value={metrics.hitRate + '%'}         sub="Redis read-through"                     icon={Zap}       accentColor="#22c55e" />
                <MetricCard label="Requests / sec"  value={metrics.rps.toLocaleString()}  sub="↑ 8% vs last min"   subColor="#22c55e" icon={Activity}   accentColor="#8b5cf6" />
                <MetricCard label="Replication Lag" value={metrics.lag + 'ms'}            sub="Within threshold"   subColor="#22c55e" icon={Clock}      accentColor="#f59e0b" />
            </div>

            {/* Topology + Event feed */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, marginBottom: 12 }}>
                <Card style={{ height: 260 }}>
                    <SectionLabel>Cluster Topology</SectionLabel>
                    <div style={{ height: 220 }}>
                        <ClusterCanvas nodeStatus={nodeStatus} />
                    </div>
                </Card>
                <Card>
                    <SectionLabel style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 10 }}>
                        <span>Live Events</span><LiveDot />
                    </SectionLabel>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 4, maxHeight: 210, overflowY: 'auto' }}>
                        {recent.length === 0
                            ? <div style={{ color: 'var(--text3)', fontSize: 11, fontFamily: 'var(--mono)' }}>Waiting for events...</div>
                            : recent.map(ev => <EventRow key={ev.id} event={ev} />)
                        }
                    </div>
                </Card>
            </div>

            {/* Sparkline */}
            <Card>
                <SectionLabel>Requests Per Second — Last 30s</SectionLabel>
                <Sparkline data={rpsHistory} />
                <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 4 }}>
                    <span style={{ fontSize: 10, fontFamily: 'var(--mono)', color: 'var(--text3)' }}>30s ago</span>
                    <span style={{ fontSize: 10, fontFamily: 'var(--mono)', color: '#00d4ff' }}>now — {metrics.rps.toLocaleString()} rps</span>
                </div>
            </Card>
        </div>
    )
}
