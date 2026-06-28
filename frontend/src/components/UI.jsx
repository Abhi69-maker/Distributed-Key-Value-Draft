import React from 'react'

export function Badge({ color = 'cyan', children, style }) {
    const map = {
        cyan:   { background: 'rgba(0,212,255,0.12)',   color: '#00d4ff', border: 'rgba(0,212,255,0.25)'   },
        green:  { background: 'rgba(34,197,94,0.12)',   color: '#22c55e', border: 'rgba(34,197,94,0.25)'   },
        amber:  { background: 'rgba(245,158,11,0.12)',  color: '#f59e0b', border: 'rgba(245,158,11,0.25)'  },
        red:    { background: 'rgba(239,68,68,0.12)',   color: '#ef4444', border: 'rgba(239,68,68,0.25)'   },
        purple: { background: 'rgba(139,92,246,0.12)',  color: '#8b5cf6', border: 'rgba(139,92,246,0.25)'  },
        gray:   { background: 'rgba(255,255,255,0.06)', color: '#94a3b8', border: 'rgba(255,255,255,0.1)'  },
    }
    const c = map[color] || map.cyan
    return (
        <span style={{
            display: 'inline-flex', alignItems: 'center', gap: 4,
            padding: '2px 8px', borderRadius: 4,
            fontSize: 10, fontFamily: 'var(--mono)', fontWeight: 500,
            letterSpacing: '0.04em', textTransform: 'uppercase',
            background: c.background, color: c.color,
            border: `0.5px solid ${c.border}`,
            ...style,
        }}>{children}</span>
    )
}

export function LiveDot({ color = '#22c55e' }) {
    return (
        <span style={{
            display: 'inline-block', width: 7, height: 7, borderRadius: '50%',
            background: color, flexShrink: 0,
            animation: 'pulse-dot 1.6s ease-in-out infinite',
        }} />
    )
}

export function Card({ children, style, className }) {
    return (
        <div className={className} style={{
            background: 'var(--bg2)',
            border: '0.5px solid var(--border)',
            borderRadius: 'var(--radius-lg)',
            padding: '16px',
            ...style,
        }}>{children}</div>
    )
}

export function SectionLabel({ children, style }) {
    return (
        <div style={{
            fontSize: 10, fontFamily: 'var(--mono)', fontWeight: 500,
            color: 'var(--text3)', textTransform: 'uppercase', letterSpacing: '0.08em',
            marginBottom: 10, ...style,
        }}>{children}</div>
    )
}

export function MetricCard({ label, value, sub, subColor, icon: Icon, accentColor = '#00d4ff' }) {
    return (
        <Card style={{ position: 'relative', overflow: 'hidden' }}>
            <div style={{
                position: 'absolute', top: 0, left: 0, right: 0, height: 2,
                background: accentColor, borderRadius: '12px 12px 0 0',
            }} />
            <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginBottom: 8 }}>
        <span style={{ fontSize: 10, fontFamily: 'var(--mono)', color: 'var(--text3)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
          {label}
        </span>
                {Icon && <Icon size={14} color={accentColor} style={{ opacity: 0.7 }} />}
            </div>
            <div style={{ fontSize: 26, fontWeight: 600, fontFamily: 'var(--mono)', color: 'var(--text)', lineHeight: 1 }}>
                {value}
            </div>
            {sub && (
                <div style={{ fontSize: 11, color: subColor || 'var(--text3)', marginTop: 6, fontFamily: 'var(--mono)' }}>
                    {sub}
                </div>
            )}
        </Card>
    )
}

const EVENT_COLORS = {
    create: { bg: 'rgba(34,197,94,0.12)',  color: '#22c55e' },
    update: { bg: 'rgba(0,212,255,0.12)',  color: '#00d4ff' },
    delete: { bg: 'rgba(239,68,68,0.12)',  color: '#ef4444' },
    sync:   { bg: 'rgba(139,92,246,0.12)', color: '#8b5cf6' },
    health: { bg: 'rgba(245,158,11,0.12)', color: '#f59e0b' },
}
export function EventTypeBadge({ type }) {
    const c = EVENT_COLORS[type] || EVENT_COLORS.sync
    return (
        <span style={{
            display: 'inline-block', padding: '2px 7px', borderRadius: 4,
            fontSize: 9, fontFamily: 'var(--mono)', fontWeight: 600,
            textTransform: 'uppercase', letterSpacing: '0.06em',
            background: c.bg, color: c.color, minWidth: 52, textAlign: 'center',
        }}>{type}</span>
    )
}

export function EventRow({ event }) {
    return (
        <div style={{
            display: 'flex', alignItems: 'center', gap: 8,
            padding: '6px 8px', borderRadius: 6,
            background: 'var(--bg3)', border: '0.5px solid var(--border)',
            fontSize: 11, fontFamily: 'var(--mono)',
            animation: 'fadeInUp 0.25s ease',
        }}>
            <EventTypeBadge type={event.type} />
            <span style={{ color: '#00d4ff', flex: 1, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
        {event.key}
      </span>
            <span style={{ color: 'var(--text3)', minWidth: 60 }}>{event.node}</span>
            <span style={{ color: 'var(--text3)', fontSize: 10, minWidth: 40, textAlign: 'right' }}>{event.time}</span>
        </div>
    )
}

export function ProgressBar({ value, max = 100, color = '#00d4ff', height = 4 }) {
    const pct = Math.min(100, Math.round((value / max) * 100))
    const c = pct > 80 ? '#ef4444' : pct > 60 ? '#f59e0b' : color
    return (
        <div style={{ height, background: 'var(--bg3)', borderRadius: 2, overflow: 'hidden' }}>
            <div style={{ height: '100%', width: `${pct}%`, background: c, borderRadius: 2, transition: 'width 0.5s ease' }} />
        </div>
    )
}

export function NodeStatusBadge({ alive }) {
    return alive ? <Badge color="green">Healthy</Badge> : <Badge color="red">Offline</Badge>
}

export function Btn({ children, onClick, variant = 'default', size = 'md', style, disabled }) {
    const base = {
        display: 'inline-flex', alignItems: 'center', gap: 6,
        borderRadius: 'var(--radius)', fontFamily: 'var(--mono)',
        fontWeight: 500, cursor: disabled ? 'not-allowed' : 'pointer',
        transition: 'all 0.15s', border: '0.5px solid',
        opacity: disabled ? 0.5 : 1,
    }
    const sizes = {
        sm: { fontSize: 10, padding: '4px 10px', height: 26 },
        md: { fontSize: 12, padding: '6px 14px', height: 32 },
        lg: { fontSize: 13, padding: '8px 18px', height: 38 },
    }
    const variants = {
        default: { background: 'var(--bg3)',                     color: 'var(--text2)', borderColor: 'var(--border2)'            },
        accent:  { background: 'rgba(0,212,255,0.15)',           color: '#00d4ff',      borderColor: 'rgba(0,212,255,0.3)'       },
        danger:  { background: 'rgba(239,68,68,0.1)',            color: '#ef4444',      borderColor: 'rgba(239,68,68,0.3)'       },
        ghost:   { background: 'transparent',                    color: 'var(--text2)', borderColor: 'transparent'               },
    }
    return (
        <button onClick={onClick} disabled={disabled}
                style={{ ...base, ...sizes[size], ...variants[variant], ...style }}>
            {children}
        </button>
    )
}

export function Input({ value, onChange, placeholder, style }) {
    return (
        <input value={value} onChange={e => onChange(e.target.value)} placeholder={placeholder}
               style={{
                   height: 32, padding: '0 10px',
                   background: 'var(--bg3)', border: '0.5px solid var(--border2)',
                   borderRadius: 'var(--radius)', color: 'var(--text)',
                   fontSize: 12, fontFamily: 'var(--mono)', width: '100%', ...style,
               }}
        />
    )
}

export function Select({ value, onChange, options, style }) {
    return (
        <select value={value} onChange={e => onChange(e.target.value)}
                style={{
                    height: 32, padding: '0 8px',
                    background: 'var(--bg3)', border: '0.5px solid var(--border2)',
                    borderRadius: 'var(--radius)', color: 'var(--text)',
                    fontSize: 12, fontFamily: 'var(--mono)', ...style,
                }}>
            {options.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
        </select>
    )
}
