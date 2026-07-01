// ──────────────────────────────────────────────
//  Configure your real backend URLs here
// ──────────────────────────────────────────────
export const GATEWAY_URL = 'http://localhost:8000'

export const NODE_URLS = {
    'node-1': 'http://localhost:8081',
    'node-2': 'http://localhost:8085',
    'node-3': 'http://localhost:8086',
}

export const KAFKA_URL    = 'http://localhost:9092'
export const EUREKA_URL   = 'http://localhost:8761'

// Poll interval in ms
export const POLL_INTERVAL = 1000

// Operation types for colour coding
export const OP_COLOR = {
    GET:    'cyan',
    PUT:    'emerald',
    DELETE: 'crimson',
    SYNC:   'violet',
    HEALTH: 'amber',
}
