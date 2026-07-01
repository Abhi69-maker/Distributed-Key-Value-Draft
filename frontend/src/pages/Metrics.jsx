import { Line, Doughnut } from 'react-chartjs-2'
import {
    Chart as ChartJS, LineElement, PointElement, LinearScale, CategoryScale,
    ArcElement, Tooltip, Legend, Filler
} from 'chart.js'
import { Gauge, PieChart } from 'lucide-react'
import { Card, EmptyState } from '../components/UI.jsx'

ChartJS.register(LineElement, PointElement, LinearScale, CategoryScale, ArcElement, Tooltip, Legend, Filler)

const chartTextColor = '#7A90B8'
const gridColor = '#1E2D4A'

export default function Metrics({ state }) {
    const { rpsHistory, cacheHistory, nodes } = state

    const lineData = {
        labels: rpsHistory.map(d => d.t),
        datasets: [{
            label: 'Requests / sec',
            data: rpsHistory.map(d => d.v),
            borderColor: '#00C8FF',
            backgroundColor: 'rgba(0,200,255,0.08)',
            tension: 0.35,
            fill: true,
            pointRadius: 0,
            borderWidth: 2,
        }],
    }

    const lineOpts = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: {
            x: { ticks: { color: chartTextColor, maxTicksLimit: 6 }, grid: { color: gridColor } },
            y: { ticks: { color: chartTextColor }, grid: { color: gridColor }, beginAtZero: true },
        },
    }

    const latestCache = cacheHistory.length ? cacheHistory[cacheHistory.length - 1].v : null
    const donutData = {
        labels: ['Hit', 'Miss'],
        datasets: [{
            data: latestCache != null ? [latestCache, 100 - latestCache] : [0, 0],
            backgroundColor: ['#00E87A', '#1E2D4A'],
            borderWidth: 0,
        }],
    }

    const donutOpts = {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '72%',
        plugins: { legend: { labels: { color: chartTextColor, font: { size: 11 } } } },
    }

    const memData = {
        labels: nodes.map(n => n.id),
        datasets: [{
            label: 'Memory (MB)',
            data: nodes.map(n => n.memoryMb ?? 0),
            backgroundColor: '#A78BFA',
            borderRadius: 4,
        }],
    }

    return (
        <div className="fade-in">
            <div className="page-header">
                <div className="page-title">Metrics</div>
                <div className="page-sub">Charted directly from polled backend metrics — empty until data arrives</div>
            </div>

            <div className="grid-2" style={{ marginBottom: 16 }}>
                <Card title="Throughput (req/s)">
                    {rpsHistory.length === 0 ? (
                        <EmptyState icon={Gauge} title="No throughput data" description="Will populate once /api/metrics/rps starts responding." />
                    ) : (
                        <div style={{ height: 220 }}><Line data={lineData} options={lineOpts} /></div>
                    )}
                </Card>

                <Card title="Cache Hit / Miss Ratio">
                    {latestCache == null ? (
                        <EmptyState icon={PieChart} title="No cache data" description="Will populate once /api/cache/stats starts responding." />
                    ) : (
                        <div style={{ height: 220 }}><Doughnut data={donutData} options={donutOpts} /></div>
                    )}
                </Card>
            </div>

            <Card title="Node Memory Usage">
                {nodes.every(n => n.memoryMb == null) ? (
                    <EmptyState icon={Gauge} title="No memory data" description="Will populate once each node's /actuator/metrics endpoint responds." />
                ) : (
                    <div style={{ height: 200 }}>
                        <Line data={memData} options={{ ...lineOpts, scales: { ...lineOpts.scales } }} />
                    </div>
                )}
            </Card>
        </div>
    )
}
