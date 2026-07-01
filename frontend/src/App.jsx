import { useState } from 'react'
import Sidebar from './components/Sidebar.jsx'
import Topbar from './components/Topbar.jsx'
import Dashboard from './pages/Dashboard.jsx'
import Cluster from './pages/Cluster.jsx'
import KeyBrowser from './pages/KeyBrowser.jsx'
import KafkaEvents from './pages/KafkaEvents.jsx'
import Metrics from './pages/Metrics.jsx'
import AuditLog from './pages/AuditLog.jsx'
import { useClusterStore } from './hooks/useClusterStore.js'

export default function App() {
    const [page, setPage] = useState('dashboard')
    const { state, actions } = useClusterStore()

    return (
        <div className="app-shell">
            <Topbar state={state} />
            <Sidebar page={page} setPage={setPage} nodes={state.nodes} />
            <main className="main-content">
                {page === 'dashboard'  && <Dashboard  state={state} />}
                {page === 'cluster'    && <Cluster    state={state} />}
                {page === 'keybrowser' && <KeyBrowser state={state} actions={actions} />}
                {page === 'kafka'      && <KafkaEvents state={state} />}
                {page === 'metrics'    && <Metrics    state={state} />}
                {page === 'auditlog'   && <AuditLog   state={state} />}
            </main>
        </div>
    )
}
