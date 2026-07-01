import { useState } from 'react'
import { useClusterStore } from '../hooks/useClusterStore.js'

const NODES = ['gateway', 'node-1', 'node-2', 'node-3']

function StatusPill({ ok, label }) {
    return (
        <span className={`kb-pill ${ok ? 'kb-pill-ok' : 'kb-pill-err'}`}>
      <span className="kb-pill-dot" />
            {label}
    </span>
    )
}

function RequestPreview({ method, path }) {
    return (
        <span className="kb-req-preview">
      <span className={`kb-method kb-method-${method.toLowerCase()}`}>{method}</span>
      <span className="kb-path">{path}</span>
    </span>
    )
}

export default function KeyBrowser() {
    const { state, actions } = useClusterStore()
    const { kvEntries } = state
    const { kvPut, kvGet, kvDelete, refreshKeys } = actions

    const [putKey, setPutKey] = useState('')
    const [putValue, setPutValue] = useState('')
    const [putNode, setPutNode] = useState('gateway')
    const [putResult, setPutResult] = useState(null)
    const [putLoading, setPutLoading] = useState(false)

    const [getKey, setGetKey] = useState('')
    const [getNode, setGetNode] = useState('gateway')
    const [getResult, setGetResult] = useState(null)
    const [getLoading, setGetLoading] = useState(false)

    const [refreshing, setRefreshing] = useState(false)
    const [deletingKey, setDeletingKey] = useState(null)

    const handlePut = async (e) => {
        e.preventDefault()
        if (!putKey.trim() || !putValue.trim()) return
        setPutLoading(true)
        const node = putNode === 'gateway' ? null : putNode
        const { ok, res } = await kvPut({ key: putKey.trim(), value: putValue.trim(), node })
        setPutResult({ ok, res })
        setPutLoading(false)
        if (ok) {
            setPutKey('')
            setPutValue('')
        }
    }

    const handleGet = async (e) => {
        e.preventDefault()
        if (!getKey.trim()) return
        setGetLoading(true)
        const node = getNode === 'gateway' ? null : getNode
        const { ok, hit, res } = await kvGet({ key: getKey.trim(), node })
        setGetResult({ ok, hit, res })
        setGetLoading(false)
    }

    const handleDelete = async (key, node) => {
        setDeletingKey(key)
        await kvDelete({ key, node: node === 'gateway' ? null : node })
        setDeletingKey(null)
    }

    const handleRefresh = async () => {
        setRefreshing(true)
        await refreshKeys()
        setRefreshing(false)
    }

    return (
        <div className="kb-page">
            <style>{KB_STYLES}</style>

            <div className="kb-header">
                <div>
                    <h1 className="kb-title">Key Browser</h1>
                    <p className="kb-subtitle">Read and write directly against the cluster — every action here is a live HTTP call.</p>
                </div>
                <button className="kb-btn kb-btn-ghost" onClick={handleRefresh} disabled={refreshing}>
                    <RefreshIcon spinning={refreshing} />
                    {refreshing ? 'Refreshing' : 'Refresh'}
                </button>
            </div>

            <div className="kb-grid-2">
                {/* ── Create / Update ─────────────────────────── */}
                <section className="kb-card">
                    <div className="kb-card-head">
                        <h3>Create / update a key</h3>
                        <RequestPreview method="POST" path="/api/v1/keys/create" />
                    </div>

                    <form onSubmit={handlePut} className="kb-form">
                        <div className="kb-field">
                            <label htmlFor="put-key">Key</label>
                            <input
                                id="put-key"
                                type="text"
                                className="kb-input kb-mono"
                                value={putKey}
                                onChange={(e) => setPutKey(e.target.value)}
                                placeholder="order:status:6219"
                                required
                            />
                        </div>
                        <div className="kb-field">
                            <label htmlFor="put-value">Value</label>
                            <input
                                id="put-value"
                                type="text"
                                className="kb-input kb-mono"
                                value={putValue}
                                onChange={(e) => setPutValue(e.target.value)}
                                placeholder="SHIPPED"
                                required
                            />
                        </div>
                        <div className="kb-field kb-field-target">
                            <label htmlFor="put-node">Target node</label>
                            <select id="put-node" className="kb-input kb-select" value={putNode} onChange={(e) => setPutNode(e.target.value)}>
                                {NODES.map((n) => <option key={n} value={n}>{n}</option>)}
                            </select>
                        </div>

                        <button className="kb-btn kb-btn-primary" type="submit" disabled={putLoading}>
                            {putLoading ? 'Sending…' : 'Send request'}
                        </button>
                    </form>

                    {putResult && (
                        <div className={`kb-response ${putResult.ok ? 'kb-response-ok' : 'kb-response-err'}`}>
                            <div className="kb-response-head">
                                <StatusPill ok={putResult.ok} label={putResult.ok ? '201 CREATED' : 'ERROR'} />
                            </div>
                            <pre className="kb-code">{JSON.stringify(putResult.res, null, 2)}</pre>
                        </div>
                    )}
                </section>

                {/* ── Get trace ────────────────────────────────── */}
                <section className="kb-card">
                    <div className="kb-card-head">
                        <h3>Get trace</h3>
                        <RequestPreview method="GET" path="/api/v1/keys/:key" />
                    </div>

                    <form onSubmit={handleGet} className="kb-form kb-form-row">
                        <div className="kb-field" style={{ flex: 1 }}>
                            <label htmlFor="get-key">Key</label>
                            <input
                                id="get-key"
                                type="text"
                                className="kb-input kb-mono"
                                value={getKey}
                                onChange={(e) => setGetKey(e.target.value)}
                                placeholder="order:status:6219"
                                required
                            />
                        </div>
                        <div className="kb-field kb-field-target">
                            <label htmlFor="get-node">Target node</label>
                            <select id="get-node" className="kb-input kb-select" value={getNode} onChange={(e) => setGetNode(e.target.value)}>
                                {NODES.map((n) => <option key={n} value={n}>{n}</option>)}
                            </select>
                        </div>
                        <button className="kb-btn kb-btn-primary" type="submit" disabled={getLoading} style={{ alignSelf: 'flex-end' }}>
                            {getLoading ? 'Fetching…' : 'Send request'}
                        </button>
                    </form>

                    {getResult && (
                        <div className={`kb-response ${getResult.ok ? 'kb-response-ok' : 'kb-response-err'}`}>
                            <div className="kb-response-head">
                                <StatusPill ok={getResult.ok} label={getResult.ok ? '200 OK' : '404 NOT FOUND'} />
                                {getResult.ok && (
                                    <span className={`kb-pill ${getResult.hit ? 'kb-pill-hit' : 'kb-pill-miss'}`}>
                    {getResult.hit ? 'cache hit' : 'cache miss'}
                  </span>
                                )}
                            </div>
                            {getResult.res ? (
                                <table className="kb-kv-table">
                                    <tbody>
                                    <tr><td className="kb-kv-label">key</td><td className="kb-mono">{getResult.res.key}</td></tr>
                                    <tr><td className="kb-kv-label">value</td><td className="kb-mono">{getResult.res.value}</td></tr>
                                    <tr><td className="kb-kv-label">nodeId</td><td className="kb-mono">{getResult.res.nodeId}</td></tr>
                                    <tr><td className="kb-kv-label">version</td><td className="kb-mono">{getResult.res.version}</td></tr>
                                    <tr><td className="kb-kv-label">createdAt</td><td className="kb-mono">{getResult.res.createdAt}</td></tr>
                                    <tr><td className="kb-kv-label">updatedAt</td><td className="kb-mono">{getResult.res.updatedAt}</td></tr>
                                    </tbody>
                                </table>
                            ) : (
                                <p className="kb-muted">No response — key not found, or the target node is unreachable.</p>
                            )}
                        </div>
                    )}
                </section>
            </div>

            {/* ── Stored keys ────────────────────────────────── */}
            <section className="kb-card kb-card-wide">
                <div className="kb-card-head">
                    <h3>Stored keys</h3>
                    <span className="kb-count-badge">{kvEntries.length}</span>
                </div>

                {kvEntries.length === 0 ? (
                    <div className="kb-empty">
                        <div className="kb-empty-icon"><EmptyIcon /></div>
                        <p className="kb-empty-title">No keys yet</p>
                        <p className="kb-muted">Create one above — this list stays in sync with <code className="kb-mono">GET /api/v1/keys</code> automatically.</p>
                    </div>
                ) : (
                    <div className="kb-table-wrap">
                        <table className="kb-data-table">
                            <thead>
                            <tr>
                                <th>Key</th>
                                <th>Value</th>
                                <th>Node</th>
                                <th>Updated</th>
                                <th aria-label="actions" />
                            </tr>
                            </thead>
                            <tbody>
                            {kvEntries.map((e) => (
                                <tr key={e.key}>
                                    <td className="kb-mono kb-key-cell">{e.key}</td>
                                    <td className="kb-mono kb-value-cell">{e.value}</td>
                                    <td><span className="kb-node-badge">{e.node}</span></td>
                                    <td className="kb-mono kb-muted">{e.updatedAt}</td>
                                    <td>
                                        <button
                                            className="kb-icon-btn"
                                            onClick={() => handleDelete(e.key, e.node)}
                                            disabled={deletingKey === e.key}
                                            aria-label={`Delete ${e.key}`}
                                            title="Delete key"
                                        >
                                            {deletingKey === e.key ? '…' : <TrashIcon />}
                                        </button>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </section>
        </div>
    )
}

/* ── inline icons — no external deps ─────────────────── */
function RefreshIcon({ spinning }) {
    return (
        <svg className={spinning ? 'kb-spin' : ''} width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M23 4v6h-6M1 20v-6h6" />
            <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15" />
        </svg>
    )
}
function TrashIcon() {
    return (
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M3 6h18M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2m3 0-1 14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2L4 6" />
        </svg>
    )
}
function EmptyIcon() {
    return (
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6">
            <rect x="3" y="7" width="18" height="13" rx="2" />
            <path d="M8 7V5a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
        </svg>
    )
}

/* ── scoped styles ────────────────────────────────────── */
const KB_STYLES = `
.kb-page { --kb-bg: #0b0f17; --kb-card: #101623; --kb-border: #1e2734; --kb-border-soft: #171f2b;
  --kb-text: #e5e9f0; --kb-muted: #6b7688; --kb-accent: #38bdf8; --kb-accent-dim: #0e2d3d;
  --kb-ok: #34d399; --kb-ok-dim: #0f2f24; --kb-err: #fb7185; --kb-err-dim: #34141a;
  --kb-radius: 10px; color: var(--kb-text); font-family: Inter, system-ui, sans-serif; }

.kb-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px; gap: 12px; }
.kb-title { font-size: 22px; font-weight: 650; margin: 0 0 4px; letter-spacing: -0.01em; }
.kb-subtitle { font-size: 13px; color: var(--kb-muted); margin: 0; max-width: 480px; }

.kb-grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; align-items: start; margin-bottom: 16px; }
@media (max-width: 900px) { .kb-grid-2 { grid-template-columns: 1fr; } }

.kb-card { background: var(--kb-card); border: 1px solid var(--kb-border); border-radius: var(--kb-radius);
  padding: 18px 20px; min-width: 0; }
.kb-card-wide { width: 100%; }
.kb-card-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; flex-wrap: wrap; gap: 8px; }
.kb-card-head h3 { font-size: 14px; font-weight: 600; margin: 0; color: var(--kb-text); }

.kb-req-preview { display: inline-flex; align-items: center; gap: 6px; font-family: 'JetBrains Mono', monospace;
  font-size: 11px; padding: 3px 8px; border-radius: 6px; background: var(--kb-border-soft); color: var(--kb-muted); }
.kb-method { font-weight: 700; letter-spacing: 0.02em; }
.kb-method-post { color: var(--kb-accent); }
.kb-method-get { color: var(--kb-ok); }

.kb-form { display: flex; flex-direction: column; gap: 12px; }
.kb-form-row { flex-direction: row; align-items: flex-end; flex-wrap: wrap; }
.kb-field { display: flex; flex-direction: column; gap: 6px; min-width: 0; }
.kb-field-target { min-width: 120px; }
.kb-field label { font-size: 12px; color: var(--kb-muted); font-weight: 500; }

.kb-input { background: var(--kb-bg); border: 1px solid var(--kb-border); border-radius: 7px;
  padding: 9px 11px; font-size: 13px; color: var(--kb-text); outline: none; width: 100%; box-sizing: border-box;
  transition: border-color .15s; }
.kb-input:focus { border-color: var(--kb-accent); box-shadow: 0 0 0 3px var(--kb-accent-dim); }
.kb-input::placeholder { color: #3d4759; }
.kb-mono { font-family: 'JetBrains Mono', monospace; }
.kb-select { cursor: pointer; }

.kb-btn { display: inline-flex; align-items: center; gap: 7px; justify-content: center; border-radius: 7px;
  padding: 9px 16px; font-size: 13px; font-weight: 600; cursor: pointer; border: 1px solid transparent;
  transition: opacity .15s, transform .1s; }
.kb-btn:active { transform: scale(0.98); }
.kb-btn:disabled { opacity: 0.55; cursor: not-allowed; }
.kb-btn-primary { background: var(--kb-accent); color: #04121c; align-self: flex-start; }
.kb-btn-primary:hover:not(:disabled) { opacity: 0.9; }
.kb-btn-ghost { background: transparent; border-color: var(--kb-border); color: var(--kb-text); }
.kb-btn-ghost:hover:not(:disabled) { border-color: var(--kb-accent); color: var(--kb-accent); }

.kb-spin { animation: kb-spin 0.9s linear infinite; }
@keyframes kb-spin { to { transform: rotate(360deg); } }

.kb-response { margin-top: 16px; border-top: 1px solid var(--kb-border); padding-top: 14px; }
.kb-response-head { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
.kb-pill { display: inline-flex; align-items: center; gap: 6px; font-size: 11px; font-weight: 700;
  letter-spacing: 0.03em; padding: 3px 9px; border-radius: 20px; font-family: 'JetBrains Mono', monospace; }
.kb-pill-dot { width: 6px; height: 6px; border-radius: 50%; }
.kb-pill-ok { background: var(--kb-ok-dim); color: var(--kb-ok); }
.kb-pill-ok .kb-pill-dot { background: var(--kb-ok); }
.kb-pill-err { background: var(--kb-err-dim); color: var(--kb-err); }
.kb-pill-err .kb-pill-dot { background: var(--kb-err); }
.kb-pill-hit { background: var(--kb-ok-dim); color: var(--kb-ok); }
.kb-pill-miss { background: var(--kb-border-soft); color: var(--kb-muted); }

.kb-code { background: var(--kb-bg); border: 1px solid var(--kb-border); border-radius: 7px; padding: 12px 14px;
  font-family: 'JetBrains Mono', monospace; font-size: 12px; color: #a8e6c1; overflow-x: auto; margin: 0; max-width: 100%; }

.kb-kv-table { width: 100%; border-collapse: collapse; font-size: 12.5px; }
.kb-kv-table td { padding: 6px 0; border-bottom: 1px solid var(--kb-border-soft); }
.kb-kv-label { color: var(--kb-muted); width: 100px; }
.kb-muted { color: var(--kb-muted); font-size: 12.5px; line-height: 1.5; }

.kb-count-badge { font-family: 'JetBrains Mono', monospace; font-size: 12px; color: var(--kb-muted);
  background: var(--kb-border-soft); padding: 2px 9px; border-radius: 20px; }

.kb-empty { display: flex; flex-direction: column; align-items: center; text-align: center; padding: 48px 20px;
  color: var(--kb-muted); }
.kb-empty-icon { color: #3d4759; margin-bottom: 12px; }
.kb-empty-title { font-size: 14px; font-weight: 600; color: var(--kb-text); margin: 0 0 6px; }

.kb-table-wrap { overflow-x: auto; }
.kb-data-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.kb-data-table th { text-align: left; font-size: 11px; font-weight: 600; color: var(--kb-muted);
  text-transform: uppercase; letter-spacing: 0.04em; padding: 0 10px 10px; border-bottom: 1px solid var(--kb-border); }
.kb-data-table td { padding: 10px; border-bottom: 1px solid var(--kb-border-soft); vertical-align: middle; }
.kb-data-table tbody tr { transition: background .12s; }
.kb-data-table tbody tr:hover { background: var(--kb-border-soft); }
.kb-key-cell { color: var(--kb-accent); }
.kb-value-cell { max-width: 240px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.kb-node-badge { font-family: 'JetBrains Mono', monospace; font-size: 11px; background: var(--kb-border-soft);
  color: var(--kb-text); padding: 3px 8px; border-radius: 6px; }

.kb-icon-btn { display: inline-flex; align-items: center; justify-content: center; width: 28px; height: 28px;
  border-radius: 6px; border: 1px solid var(--kb-border); background: transparent; color: var(--kb-muted);
  cursor: pointer; transition: all .15s; }
.kb-icon-btn:hover:not(:disabled) { border-color: var(--kb-err); color: var(--kb-err); background: var(--kb-err-dim); }
.kb-icon-btn:disabled { opacity: 0.5; cursor: not-allowed; }
`