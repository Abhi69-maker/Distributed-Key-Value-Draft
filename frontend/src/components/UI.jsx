import { OP_COLOR } from '../data/seed.js'

export function Badge({ color = 'muted', children, dot }) {
    return (
        <span className={`badge ${color}`}>
      {dot && <span className="status-dot" style={{
          background: color === 'emerald' ? 'var(--emerald)'
              : color === 'crimson' ? 'var(--crimson)'
                  : color === 'amber'   ? 'var(--amber)'
                      : 'var(--text-muted)',
          boxShadow: color === 'emerald' ? '0 0 5px var(--emerald)' : 'none'
      }} />}
            {children}
    </span>
    )
}

export function MetricCard({ label, value, sub, accent = 'cyan', loading }) {
    return (
        <div className={`metric-card ${accent}`}>
            <div className="metric-label">{label}</div>
            {loading
                ? <div className="metric-value" style={{ color: 'var(--text-muted)', fontSize: 16 }}>—</div>
                : <div className="metric-value">{value ?? '—'}</div>
            }
            {sub && <div className="metric-sub">{sub}</div>}
        </div>
    )
}

export function Card({ title, action, children, style }) {
    return (
        <div className="card" style={style}>
            {(title || action) && (
                <div className="card-header">
                    {title && <span className="card-title">{title}</span>}
                    {action}
                </div>
            )}
            {children}
        </div>
    )
}

export function EmptyState({ icon: Icon, title, description }) {
    return (
        <div className="empty-state">
            {Icon && <Icon size={40} />}
            <h3>{title}</h3>
            {description && <p>{description}</p>}
        </div>
    )
}

export function Spinner() {
    return <div className="spinner" />
}

export function EventRow({ ev }) {
    const color = OP_COLOR[ev.op] || 'muted'
    return (
        <div className="event-row fade-in">
            <span className="event-time">{ev.time}</span>
            <span className="event-key">{ev.key || '—'}</span>
            <span className="event-node">{ev.node}</span>
            <Badge color={color}>{ev.op}</Badge>
        </div>
    )
}

