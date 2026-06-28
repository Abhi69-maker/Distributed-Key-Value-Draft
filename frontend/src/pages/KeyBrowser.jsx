import React, { useState } from 'react'
import { Search, Plus, Trash2, ChevronRight } from 'lucide-react'
import { Card, SectionLabel, Badge, Btn, Input, Select } from '../components/UI.jsx'

const NODE_OPTIONS = [
    { value: 'node-1', label: 'Node 1 :8081' },
    { value: 'node-2', label: 'Node 2 :8082' },
    { value: 'node-3', label: 'Node 3 :8083' },
]

export default function KeyBrowser({ store }) {
    const { kvData, addKV, deleteKV } = store
    const [keyIn,  setKeyIn]  = useState('user:session:new-key')
    const [valIn,  setValIn]  = useState('{"example":"value"}')
    const [nodeIn, setNodeIn] = useState('node-1')
    const [search, setSearch] = useState('')
    const [trace,  setTrace]  = useState([])
    const [getKey, setGetKey] = useState('')

    const filtered = kvData.filter(r => !search || r.key.toLowerCase().includes(search.toLowerCase()))

    function handleAdd() {
        if (!keyIn.trim()) return
        addKV(keyIn.trim(), valIn.trim(), nodeIn)
        setKeyIn(''); setValIn('')
    }

    function simulateGet() {
        const k = getKey || 'user:session:abc123'
        const found = kvData.find(r => r.key === k)
        const steps = [
            { ok: true,  text: `→ GET /store/${k}` },
            ...(found && found.cached ? [
                { ok: true,  text: '→ Redis LOOKUP: key found ✓' },
                { ok: true,  text: '→ Cache HIT — returning immediately (< 1ms)' },
                { ok: true,  text: `← 200 OK  ${(found?.value || '{}').substring(0, 60)}` },
            ] : [
                { ok: false, text: '→ Redis LOOKUP: key not found' },
                { ok: false, text: '→ Cache MISS — querying PostgreSQL' },
                { ok: true,  text: `→ SELECT * FROM key_value WHERE key = '${k}'` },
                { ok: true,  text: `→ Redis SET '${k}' EX 300` },
                { ok: true,  text: `← 200 OK  ${(found?.value || '{}').substring(0, 60)}` },
            ]),
        ]
        setTrace([])
        steps.forEach((s, i) => {
            setTimeout(() => setTrace(prev => [...prev, s]), i * 200)
        })
    }

    return (
        <div>
            {/* PUT form */}
            <Card style={{ marginBottom: 12 }}>
                <SectionLabel>PUT / DELETE Keys</SectionLabel>
                <div style={{ display: 'flex', gap: 8, marginBottom: 8 }}>
                    <Input value={keyIn} onChange={setKeyIn} placeholder="key name" style={{ flex: 1 }} />
                    <Input value={valIn} onChange={setValIn} placeholder='value or JSON'    style={{ flex: 2 }} />
                    <Select value={nodeIn} onChange={setNodeIn} options={NODE_OPTIONS} style={{ width: 140 }} />
                    <Btn variant="accent" onClick={handleAdd}><Plus size={13} /> PUT</Btn>
                    <Btn variant="danger" onClick={() => deleteKV(keyIn)}><Trash2 size={13} /> DEL</Btn>
                </div>

                {/* Search */}
                <div style={{ position: 'relative' }}>
                    <Search size={13} style={{ position: 'absolute', left: 10, top: '50%', transform: 'translateY(-50%)', color: 'var(--text3)' }} />
                    <input
                        value={search} onChange={e => setSearch(e.target.value)}
                        placeholder="Search keys..."
                        style={{
                            width: '100%', height: 32, padding: '0 10px 0 30px',
                            background: 'var(--bg3)', border: '0.5px solid var(--border)',
                            borderRadius: 'var(--radius)', color: 'var(--text)',
                            fontSize: 12, fontFamily: 'var(--mono)',
                        }}
                    />
                </div>
            </Card>

            {/* Table */}
            <Card style={{ marginBottom: 12, padding: 0 }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 12, fontFamily: 'var(--mono)' }}>
                    <thead>
                    <tr style={{ borderBottom: '0.5px solid var(--border)' }}>
                        {['Key', 'Value', 'Node', 'TTL', 'Cache', 'Action'].map(h => (
                            <th key={h} style={{
                                padding: '10px 14px', textAlign: 'left',
                                fontSize: 10, color: 'var(--text3)', fontWeight: 500,
                                textTransform: 'uppercase', letterSpacing: '0.06em',
                            }}>{h}</th>
                        ))}
                    </tr>
                    </thead>
                    <tbody>
                    {filtered.map(row => (
                        <tr key={row.key} style={{ borderBottom: '0.5px solid var(--border)' }}>
                            <td style={{ padding: '9px 14px', color: '#00d4ff' }}>{row.key}</td>
                            <td style={{ padding: '9px 14px', color: 'var(--text2)', maxWidth: 220, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                {row.value}
                            </td>
                            <td style={{ padding: '9px 14px' }}>
                                <Badge color="cyan" style={{ fontSize: 9 }}>{row.node}</Badge>
                            </td>
                            <td style={{ padding: '9px 14px', color: 'var(--text3)' }}>{row.ttl}</td>
                            <td style={{ padding: '9px 14px' }}>
                                {row.cached
                                    ? <Badge color="green" style={{ fontSize: 9 }}>HIT</Badge>
                                    : <span style={{ color: 'var(--text3)', fontSize: 10 }}>—</span>
                                }
                            </td>
                            <td style={{ padding: '9px 14px' }}>
                                <Btn size="sm" variant="danger" onClick={() => deleteKV(row.key)}>
                                    <Trash2 size={10} />
                                </Btn>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
                {filtered.length === 0 && (
                    <div style={{ padding: '20px', textAlign: 'center', color: 'var(--text3)', fontSize: 12, fontFamily: 'var(--mono)' }}>
                        No keys match "{search}"
                    </div>
                )}
            </Card>

            {/* Cache flow simulator */}
            <Card>
                <SectionLabel>Cache Flow Simulator</SectionLabel>
                <div style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
                    <Input value={getKey} onChange={setGetKey} placeholder="GET /store/..." style={{ flex: 1 }} />
                    <Btn variant="accent" onClick={simulateGet}><ChevronRight size={13} /> Simulate GET</Btn>
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
                    {trace.map((s, i) => (
                        <div key={i} style={{
                            fontSize: 11, fontFamily: 'var(--mono)', padding: '2px 0',
                            color: s.ok ? '#22c55e' : '#f59e0b',
                            animation: 'fadeInUp 0.2s ease',
                        }}>{s.text}</div>
                    ))}
                    {trace.length === 0 && (
                        <div style={{ fontSize: 11, fontFamily: 'var(--mono)', color: 'var(--text3)' }}>
                            Enter a key above and click Simulate GET to trace the cache flow
                        </div>
                    )}
                </div>
            </Card>
        </div>
    )
}
