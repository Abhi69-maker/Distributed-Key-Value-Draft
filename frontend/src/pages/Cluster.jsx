import { Cpu, HardDrive, Server } from 'lucide-react'
import { Card, Badge } from '../components/UI.jsx'

export default function Cluster({ state }) {
    return (
        <div className="fade-in">
            <div className="page-header">
                <div className="page-title">Cluster</div>
                <div className="page-sub">Per-node health, polled every few seconds from each service's actuator endpoint</div>
            </div>

            <div className="grid-3">
                {state.nodes.map(n => (
                    <Card key={n.id} title={n.id} action={
                        <Badge color={n.status === 'online' ? 'emerald' : n.status === 'offline' ? 'crimson' : 'muted'} dot>
                            {n.status}
                        </Badge>
                    }>
                        <div className="stack">
                            <Row icon={Cpu} label="CPU Usage" value={n.cpu != null ? `${n.cpu}%` : '—'} />
                            <Row icon={HardDrive} label="Memory" value={n.memoryMb != null ? `${n.memoryMb} MB` : '—'} />
                            <Row icon={Server} label="URL" value={n.url} mono />
                            <Row icon={Server} label="Last Seen" value={n.lastSeen ?? '—'} />
                        </div>
                    </Card>
                ))}
            </div>

            {state.nodes.every(n => n.status === 'unknown') && (
                <div className="card" style={{ marginTop: 16, textAlign: 'center', color: 'var(--text-muted)' }}>
                    Waiting for first health poll. Make sure your Spring Boot services are running and
                    expose <code>/actuator/health</code> on the ports configured in <code>src/data/seed.js</code>.
                </div>
            )}
        </div>
    )
}

function Row({ icon: Icon, label, value, mono }) {
    return (
        <div className="row" style={{ justifyContent: 'space-between', fontSize: 12 }}>
      <span className="row" style={{ color: 'var(--text-muted)', gap: 6 }}>
        <Icon size={13} /> {label}
      </span>
            <span style={{ fontFamily: mono ? 'var(--font-mono)' : 'inherit', color: 'var(--text-primary)', fontSize: mono ? 11 : 12 }}>
        {value}
      </span>
        </div>
    )
}
