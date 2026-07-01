import {
    LayoutDashboard, Server, BarChart2, Database, Activity, ScrollText, Cpu
} from 'lucide-react'

const NAV = [
    { id: 'dashboard',  label: 'Dashboard',    icon: LayoutDashboard },
    { id: 'cluster',    label: 'Cluster',       icon: Server },
    { id: 'keybrowser', label: 'Key Browser',   icon: Database },
    { id: 'kafka',      label: 'Kafka Events',  icon: Activity },
    { id: 'metrics',    label: 'Metrics',       icon: BarChart2 },
    { id: 'auditlog',   label: 'Audit Log',     icon: ScrollText },
]

export default function Sidebar({ page, setPage, nodes }) {
    return (
        <nav className="sidebar">
            <span className="sidebar-section-label">Navigation</span>

            {NAV.map(({ id, label, icon: Icon }) => (
                <div
                    key={id}
                    className={`nav-item ${page === id ? 'active' : ''}`}
                    onClick={() => setPage(id)}
                >
                    <Icon size={16} />
                    {label}
                </div>
            ))}

            <div className="sidebar-nodes">
        <span className="sidebar-section-label" style={{ padding: '0 0 8px', display: 'block' }}>
          Node Status
        </span>
                {nodes.map(n => (
                    <div key={n.id} className="node-status-row">
                        <span className="node-status-name"><Cpu size={10} style={{ marginRight: 4, verticalAlign: 'middle' }} />{n.id}</span>
                        <span className={`status-dot ${n.status}`} />
                    </div>
                ))}
            </div>
        </nav>
    )
}
