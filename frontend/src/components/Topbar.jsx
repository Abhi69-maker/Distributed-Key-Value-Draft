import React from 'react'
import { Badge, LiveDot } from './UI.jsx'

const PAGE_TITLES = {
    dashboard: 'Dashboard',
    cluster:   'Cluster Status',
    metrics:   'Metrics',
    browser:   'Key Browser',
    kafka:     'Kafka Events',
    logs:      'Audit Log',
}

export default function Topbar({ page, aliveCount, metrics }) {
    const healthy = aliveCount === 3
    const degraded = aliveCount === 2
    return (
        <header style={{
            display: 'flex', alignItems: 'center', gap: 12,
            padding: '10px 20px', borderBottom: '0.5px solid var(--border)',
            background: 'var(--bg2)', flexShrink: 0,
        }}>
            <h1 style={{ fontSize: 14, fontWeight: 600, color: 'var(--text)', flex: 1 }}>
                {PAGE_TITLES[page] || page}
            </h1>
            <div style={{ display: 'flex', alignItems: 'center', gap: 5 }}>
                <LiveDot />
                <span style={{ fontSize: 10, color: 'var(--text3)', fontFamily: 'var(--mono)' }}>LIVE</span>
            </div>
            <Badge color={healthy ? 'green' : degraded ? 'amber' : 'red'}>
                {healthy ? 'Cluster Healthy' : degraded ? 'Degraded' : 'Critical'}
            </Badge>
            <Badge color="cyan">{aliveCount} / 3 Nodes</Badge>
            <Badge color="gray">{metrics.rps.toLocaleString()} rps</Badge>
        </header>
    )
}
