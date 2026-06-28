import React, { useState } from 'react'
import Sidebar from './components/Sidebar.jsx'
import Topbar  from './components/Topbar.jsx'
import { useClusterStore } from './hooks/useClusterStore.js'

import Dashboard  from './pages/Dashboard.jsx'
import Cluster    from './pages/Cluster.jsx'
import Metrics    from './pages/Metrics.jsx'
import KeyBrowser from './pages/KeyBrowser.jsx'
import KafkaEvents from './pages/KafkaEvents.jsx'
import AuditLog   from './pages/AuditLog.jsx'

export default function App() {
    const [page, setPage] = useState('dashboard')
    const store = useClusterStore()

    const pages = {
        dashboard: <Dashboard  store={store} />,
        cluster:   <Cluster    store={store} />,
        metrics:   <Metrics    store={store} />,
        browser:   <KeyBrowser store={store} />,
        kafka:     <KafkaEvents store={store} />,
        logs:      <AuditLog   store={store} />,
    }

    return (
        <div style={{
            display: 'flex', height: '100vh', overflow: 'hidden',
            background: 'var(--bg)',
        }}>
            <Sidebar active={page} onNav={setPage} nodeStatus={store.nodeStatus} />

            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden', minWidth: 0 }}>
                <Topbar page={page} aliveCount={store.aliveCount} metrics={store.metrics} />

                <main style={{ flex: 1, overflowY: 'auto', padding: '16px 20px' }}>
                    {pages[page] || pages.dashboard}
                </main>
            </div>
        </div>
    )
}
