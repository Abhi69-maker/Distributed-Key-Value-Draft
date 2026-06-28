export const INITIAL_KV_DATA = [
    { key: 'user:session:abc123',     value: '{"userId":"abc123","role":"admin","exp":1719619200}',      node: 'node-1', ttl: '14m 32s', cached: true  },
    { key: 'product:catalog:42',      value: '{"name":"Widget Pro","price":99.99,"stock":142}',          node: 'node-2', ttl: '5m 12s',  cached: true  },
    { key: 'order:status:8821',       value: '{"status":"shipped","eta":"2026-06-28","carrier":"DHL"}',  node: 'node-1', ttl: '1h 0s',   cached: false },
    { key: 'rate:limit:ip:1.2.3.4',  value: '{"count":42,"window":60,"blocked":false}',                 node: 'node-3', ttl: '48s',     cached: true  },
    { key: 'config:feature:darkmode', value: 'true',                                                     node: 'node-1', ttl: '\u221e',   cached: false },
    { key: 'cache:warm:popular',      value: '["user:1","user:2","user:3","user:99"]',                   node: 'node-2', ttl: '2m 10s',  cached: true  },
    { key: 'metrics:cpu:node-1',      value: '{"value":34,"unit":"percent","ts":1719532800}',            node: 'node-1', ttl: '30s',     cached: true  },
    { key: 'session:lock:txn-99',     value: '{"locked":true,"owner":"node-2","acquired":1719532790}',   node: 'node-2', ttl: '10s',     cached: false },
]

export const KAFKA_TOPICS = [
    { name: 'kv-create-events',   msgs: 4821,  rate: 12, partitions: 3, group: 'kv-replication-group' },
    { name: 'kv-update-events',   msgs: 6104,  rate: 8,  partitions: 3, group: 'kv-replication-group' },
    { name: 'kv-delete-events',   msgs: 1892,  rate: 2,  partitions: 3, group: 'kv-replication-group' },
    { name: 'node-status-events', msgs: 1204,  rate: 1,  partitions: 1, group: 'monitoring-group'      },
    { name: 'audit-events',       msgs: 14821, rate: 23, partitions: 6, group: 'audit-group'           },
]

export const KEY_NAMES = [
    'user:session', 'product:sku', 'order:status', 'rate:limit',
    'config:flag', 'cache:key', 'session:data', 'metric:count',
    'lock:txn', 'feature:toggle', 'inventory:item', 'payment:ref',
]
export const NODE_NAMES = ['node-1', 'node-2', 'node-3']
export const EVENT_TYPES = ['create', 'update', 'delete', 'sync', 'health']
export const OPS = ['CREATE', 'UPDATE', 'DELETE']
