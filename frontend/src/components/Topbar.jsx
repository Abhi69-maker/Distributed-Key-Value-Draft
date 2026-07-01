import { Badge } from './UI.jsx'

export default function Topbar({ state }) {
    const onlineNodes = state.nodes.filter(n => n.status === 'online').length
    const total       = state.nodes.length

    return (
        <header className="topbar">
            <div className="topbar-logo">
                <div className="topbar-logo-mark">⚡</div>
                <span>KV Control Plane</span>
            </div>

            <div className="topbar-divider" />

            <div className="topbar-badges">
                <Badge color={state.gatewayOnline ? 'emerald' : 'crimson'} dot>
                    Gateway {state.gatewayOnline ? 'Online' : 'Offline'}
                </Badge>
                <Badge color={onlineNodes === total ? 'emerald' : onlineNodes > 0 ? 'amber' : 'crimson'}>
                    {onlineNodes}/{total} Nodes
                </Badge>
                {state.kafka.topics.length > 0 && (
                    <Badge color="amber">Kafka Connected</Badge>
                )}
            </div>

            <div className="topbar-right">
                <div className="live-pulse">
                    <div className="pulse-dot" />
                    <span>
            {state.requestsPerSec != null
                ? `${state.requestsPerSec} req/s`
                : 'Awaiting data'}
          </span>
                </div>
            </div>
        </header>
    )
}
