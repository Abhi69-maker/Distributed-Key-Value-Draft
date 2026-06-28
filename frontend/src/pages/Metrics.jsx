import React, { useRef, useEffect } from 'react'
import {
    Chart as ChartJS,
    CategoryScale, LinearScale, PointElement, LineElement,
    ArcElement, BarElement, Tooltip, Filler, Legend
} from 'chart.js'
import { Line, Doughnut, Bar } from 'react-chartjs-2'
import { Card, SectionLabel, MetricCard } from '../components/UI.jsx'
import { Activity, Zap, Database, AlertCircle } from 'lucide-react'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, ArcElement, BarElement, Tooltip, Filler, Legend)

const GRID = 'rgba(255,255,255,0.05)'
const TICK = '#4a5568'

export default function Metrics({ store }) {
    const { rpsHistory, metrics } = store

    const rpsData = {
        labels: rpsHistory.map((_, i) => i % 5 === 0 ? `-${30 - i}s` : ''),
        datasets: [{
            label: 'RPS',
            data: rpsHistory,
            borderColor: '#00d4ff',
            backgroundColor: 'rgba(0,212,255,0.08)',
            borderWidth: 1.5, pointRadius: 0, fill: true, tension: 0.4,
        }],
    }
    const rpsOpts = {
        responsive: true, maintainAspectRatio: false, animation: false,
        plugins: { legend: { display: false } },
        scales: {
            x: { grid: { color: GRID }, ticks: { color: TICK, font: { size: 9, family: 'JetBrains Mono' } } },
            y: { grid: { color: GRID }, ticks: { color: TICK, font: { size: 9, family: 'JetBrains Mono' } } },
        },
    }

    const cacheData = {
        labels: ['Cache Hits', 'Cache Misses'],
        datasets: [{
            data: [metrics.hitRate, 100 - metrics.hitRate],
            backgroundColor: ['#22c55e', 'rgba(255,255,255,0.07)'],
            borderWidth: 0, hoverOffset: 4,
        }],
    }
    const cacheOpts = {
        responsive: true, maintainAspectRatio: false, animation: false,
        plugins: { legend: { position: 'right', labels: { color: '#94a3b8', font: { size: 10, family: 'JetBrains Mono' }, boxWidth: 10, padding: 12 } } },
    }

    const nodeBarData = {
        labels: ['node-1', 'node-2', 'node-3'],
        datasets: [
            { label: 'Memory MB', data: [342, 318, 305], backgroundColor: 'rgba(0,212,255,0.5)', borderRadius: 4 },
            { label: 'CPU %',     data: [34,  28,  22],  backgroundColor: 'rgba(139,92,246,0.5)', borderRadius: 4 },
        ],
    }
    const barOpts = {
        responsive: true, maintainAspectRatio: false, animation: false,
        plugins: { legend: { labels: { color: '#94a3b8', font: { size: 10, family: 'JetBrains Mono' }, boxWidth: 10 } } },
        scales: {
            x: { grid: { color: GRID }, ticks: { color: TICK, font: { size: 10, family: 'JetBrains Mono' } } },
            y: { grid: { color: GRID }, ticks: { color: TICK, font: { size: 9, family: 'JetBrains Mono' } } },
        },
    }

    const latencyData = {
        labels: ['p50','p75','p90','p95','p99','p999'],
        datasets: [{
            label: 'Latency (ms)',
            data: [2, 4, 8, 14, 28, 62],
            borderColor: '#f59e0b',
            backgroundColor: 'rgba(245,158,11,0.08)',
            borderWidth: 1.5, pointRadius: 3, fill: true, tension: 0.3,
            pointBackgroundColor: '#f59e0b',
        }],
    }
    const latOpts = {
        responsive: true, maintainAspectRatio: false, animation: false,
        plugins: { legend: { display: false } },
        scales: {
            x: { grid: { color: GRID }, ticks: { color: TICK, font: { size: 10, family: 'JetBrains Mono' } } },
            y: { grid: { color: GRID }, ticks: { color: TICK, font: { size: 9, family: 'JetBrains Mono' } } },
        },
    }

    return (
        <div>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4,1fr)', gap: 12, marginBottom: 14 }}>
                <MetricCard label="Total Requests"      value="84,210"  sub="Since startup"      icon={Activity}     accentColor="#00d4ff" />
                <MetricCard label="Replication Events"  value="14,821"  sub="Across all topics"  icon={Database}     accentColor="#8b5cf6" />
                <MetricCard label="Cache Hits"          value="73,511"  sub="Redis read-through" icon={Zap}          accentColor="#22c55e" />
                <MetricCard label="Error Rate"          value="0.02%"   sub="Well below SLA"     icon={AlertCircle}  accentColor="#22c55e" subColor="#22c55e" />
            </div>

            <Card style={{ marginBottom: 12 }}>
                <SectionLabel>Requests Per Second — Live</SectionLabel>
                <div style={{ height: 140 }}>
                    <Line data={rpsData} options={rpsOpts} />
                </div>
            </Card>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, marginBottom: 12 }}>
                <Card>
                    <SectionLabel>Cache Hit / Miss Ratio</SectionLabel>
                    <div style={{ height: 150 }}>
                        <Doughnut data={cacheData} options={cacheOpts} />
                    </div>
                </Card>
                <Card>
                    <SectionLabel>Read Latency Percentiles</SectionLabel>
                    <div style={{ height: 150 }}>
                        <Line data={latencyData} options={latOpts} />
                    </div>
                </Card>
            </div>

            <Card>
                <SectionLabel>Node Resource Comparison</SectionLabel>
                <div style={{ height: 160 }}>
                    <Bar data={nodeBarData} options={barOpts} />
                </div>
            </Card>
        </div>
    )
}
