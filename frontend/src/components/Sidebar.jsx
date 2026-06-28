import React from 'react'
import {
    LayoutDashboard, Network, BarChart2, Database,
    Radio, FileText, Circle
} from 'lucide-react'
import { LiveDot } from './UI.jsx'

const NAV = [
    { id: 'dashboard', label: 'Dashboard',     icon: LayoutDashboard, section: 'Monitor' },
    { id: 'cluster',   label: 'Cluster',       icon: Network,         section: null      },
    { id: 'metrics',   label: 'Metrics',       icon: BarChart2,       section: null      },
    { id: 'browser',   label: 'Key Browser',   icon: Database,        section: 'Data'    },
    { id: 'kafka',     label: 'Kafka Events',  icon: Radio,           section: null      },
    { id: 'logs',      label: 'Audit Log',     icon: FileText,        section: 'Ops'     },
]

export default function Sidebar({ active, onNav, nodeStatus }) {
    const items = []
    let lastSection = null
    NAV.forEach(item => {
        if (item.section && item.section !== lastSection) {
            items.push({ type: 'section', label: item.section, key: 'sec-' + item.section })
            lastSection = item.section
        }
        items.push({ type: 'item', ...item })
    })

    return (
        <aside style={{
            width: 200, minWidth: 200, display: 'flex', flexDirection: 'column',
            background: 'var(--bg2)', borderRight: '0.5px solid var(--border)',
        }}>
            {/* Logo */}
            <div style={{ padding: '18px 16px 14px', borderBottom: '0.5px solid var(--border)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                    <div style={{
                        width: 28, height: 28, borderRadius: 6,
                        background: 'linear-gradient(135deg,rgba(0,212,255,0.3),rgba(139,92,246,0.3))',
                        border: '0.5px solid rgba(0,212,255,0.4)',
                        display: 'flex', alignItems: 'center', justifyContent: 'center',
                        fontSize: 12, fontWeight: 700, fontFamily: 'var(--mono)', color: '#00d4ff',
                    }}>KV</div>
                    <div>
                        <div style={{ fontSize: 13, fontWeight: 600, color: 'var(--text)' }}>KV Store</div>
                        <div style={{ fontSize: 9, color: 'var(--text3)', fontFamily: 'var(--mono)' }}>distributed cluster</div>
                    </div>
                </div>
            </div>

            {/* Nav */}
            <nav style={{ flex: 1, padding: '8px 0', overflowY: 'auto' }}>
                {items.map(item => {
                    if (item.type === 'section') return (
                        <div key={item.key} style={{
                            padding: '10px 16px 4px',
                            fontSize: 9, fontFamily: 'var(--mono)', fontWeight: 500,
                            color: 'var(--text3)', textTransform: 'uppercase', letterSpacing: '0.1em',
                        }}>{item.label}</div>
                    )
                    const Icon = item.icon
                    const isActive = active === item.id
                    return (
                        <button key={item.id} onClick={() => onNav(item.id)}
                                style={{
                                    display: 'flex', alignItems: 'center', gap: 9,
                                    width: '100%', padding: '8px 16px',
                                    background: isActive ? 'rgba(0,212,255,0.08)' : 'transparent',
                                    border: 'none',
                                    borderLeft: isActive ? '2px solid #00d4ff' : '2px solid transparent',
                                    color: isActive ? '#00d4ff' : 'var(--text2)',
                                    fontSize: 12, fontFamily: 'var(--sans)', cursor: 'pointer',
                                    transition: 'all 0.15s', textAlign: 'left',
                                }}>
                            <Icon size={14} />
                            {item.label}
                        </button>
                    )
                })}
            </nav>

            {/* Cluster status strip */}
            <div style={{ padding: '12px 16px', borderTop: '0.5px solid var(--border)' }}>
                <div style={{ fontSize: 9, fontFamily: 'var(--mono)', color: 'var(--text3)', textTransform: 'uppercase', letterSpacing: '0.08em', marginBottom: 8 }}>
                    Cluster Nodes
                </div>
                {Object.entries(nodeStatus).map(([id, node]) => (
                    <div key={id} style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 5, fontSize: 11, color: 'var(--text2)' }}>
                        <LiveDot color={node.alive ? '#22c55e' : '#ef4444'} />
                        <span style={{ fontFamily: 'var(--mono)', flex: 1 }}>{id}</span>
                        <span style={{ fontSize: 9, color: node.alive ? 'var(--text3)' : '#ef4444', fontFamily: 'var(--mono)' }}>
              {node.alive ? `${node.cpu}%` : 'offline'}
            </span>
                    </div>
                ))}
            </div>
        </aside>
    )
}
