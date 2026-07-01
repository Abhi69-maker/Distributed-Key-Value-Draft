import { useEffect, useRef } from 'react'

const NODE_DEFS = [
    { id: 'gateway',    label: 'API Gateway', role: 'gateway', color: '#00C8FF' },
    { id: 'node-1',     label: 'KV Node 1',   role: 'kv',      color: '#00E87A' },
    { id: 'node-2',     label: 'KV Node 2',   role: 'kv',      color: '#00E87A' },
    { id: 'node-3',     label: 'KV Node 3',   role: 'kv',      color: '#00E87A' },
    { id: 'kafka',      label: 'Kafka',        role: 'infra',   color: '#FFB347' },
    { id: 'redis',      label: 'Redis',        role: 'infra',   color: '#A78BFA' },
    { id: 'postgres',   label: 'PostgreSQL',   role: 'infra',   color: '#60A5FA' },
]

const EDGES = [
    ['gateway','node-1'],
    ['gateway','node-2'],
    ['gateway','node-3'],
    ['node-1','kafka'],
    ['node-2','kafka'],
    ['node-3','kafka'],
    ['node-1','redis'],
    ['node-2','redis'],
    ['node-3','redis'],
    ['node-1','postgres'],
    ['node-2','postgres'],
    ['node-3','postgres'],
]

function layout(W, H) {
    const cx = W / 2
    const py = 64
    const kvY = H / 2
    const infraY = H - 64
    const kvXs = [W * 0.2, cx, W * 0.8]
    const infraXs = [W * 0.2, cx, W * 0.8]

    const pos = {
        'gateway':  { x: cx,         y: py     },
        'node-1':   { x: kvXs[0],    y: kvY    },
        'node-2':   { x: kvXs[1],    y: kvY    },
        'node-3':   { x: kvXs[2],    y: kvY    },
        'kafka':    { x: infraXs[0], y: infraY },
        'redis':    { x: infraXs[1], y: infraY },
        'postgres': { x: infraXs[2], y: infraY },
    }
    return pos
}

export default function ClusterCanvas({ nodes }) {
    const canvasRef = useRef(null)
    const rafRef    = useRef(null)
    const particles = useRef([])   // { from, to, t, speed }

    // spawn travel particles along edges
    useEffect(() => {
        const interval = setInterval(() => {
            const edge = EDGES[Math.floor(Math.random() * EDGES.length)]
            particles.current.push({ from: edge[0], to: edge[1], t: 0, speed: 0.008 + Math.random() * 0.006 })
        }, 600)
        return () => clearInterval(interval)
    }, [])

    useEffect(() => {
        const canvas = canvasRef.current
        if (!canvas) return
        const ctx = canvas.getContext('2d')

        const resize = () => {
            const rect = canvas.parentElement.getBoundingClientRect()
            canvas.width  = rect.width  * window.devicePixelRatio
            canvas.height = rect.height * window.devicePixelRatio
            canvas.style.width  = rect.width  + 'px'
            canvas.style.height = rect.height + 'px'
            ctx.scale(window.devicePixelRatio, window.devicePixelRatio)
        }

        resize()
        const ro = new ResizeObserver(resize)
        ro.observe(canvas.parentElement)

        const nodeStatus = {}
        nodes.forEach(n => { nodeStatus[n.id] = n.status })

        const draw = () => {
            const W = canvas.width  / window.devicePixelRatio
            const H = canvas.height / window.devicePixelRatio
            ctx.clearRect(0, 0, W, H)

            const pos = layout(W, H)

            // ── edges ──────────────────────────────────────────────────
            EDGES.forEach(([a, b]) => {
                const pa = pos[a]; const pb = pos[b]
                ctx.beginPath()
                ctx.moveTo(pa.x, pa.y)
                ctx.lineTo(pb.x, pb.y)
                ctx.strokeStyle = 'rgba(30,45,74,0.8)'
                ctx.lineWidth   = 1.5
                ctx.stroke()
            })

            // ── particles ──────────────────────────────────────────────
            particles.current = particles.current.filter(p => p.t <= 1)
            particles.current.forEach(p => {
                p.t += p.speed
                const pa  = pos[p.from]; const pb = pos[p.to]
                const x   = pa.x + (pb.x - pa.x) * p.t
                const y   = pa.y + (pb.y - pa.y) * p.t
                const col = p.from === 'gateway' || p.to === 'gateway' ? '#00C8FF' : '#00E87A'
                ctx.beginPath()
                ctx.arc(x, y, 3, 0, Math.PI * 2)
                ctx.fillStyle   = col
                ctx.shadowColor = col
                ctx.shadowBlur  = 8
                ctx.fill()
                ctx.shadowBlur  = 0
            })

            // ── nodes ──────────────────────────────────────────────────
            const now = Date.now()
            NODE_DEFS.forEach(nd => {
                const p       = pos[nd.id]
                const status  = nd.role === 'gateway' ? (nodeStatus['gateway'] || 'unknown') : nodeStatus[nd.id] || 'unknown'
                const offline = status === 'offline'
                const radius  = nd.role === 'gateway' ? 22 : 16

                // glow ring for online nodes
                if (!offline) {
                    const phase = (Math.sin(now / 1200 + nd.id.charCodeAt(0)) + 1) / 2
                    ctx.beginPath()
                    ctx.arc(p.x, p.y, radius + 8 + phase * 4, 0, Math.PI * 2)
                    ctx.strokeStyle = nd.color + '22'
                    ctx.lineWidth   = 6
                    ctx.stroke()
                }

                // circle fill
                ctx.beginPath()
                ctx.arc(p.x, p.y, radius, 0, Math.PI * 2)
                ctx.fillStyle   = offline ? '#1a1a2e' : nd.color + '22'
                ctx.strokeStyle = offline ? '#FF4560' : nd.color
                ctx.lineWidth   = 2
                ctx.fill()
                ctx.stroke()

                // label
                ctx.fillStyle = offline ? '#FF4560' : nd.color
                ctx.font      = `600 10px 'JetBrains Mono', monospace`
                ctx.textAlign = 'center'
                ctx.fillText(nd.label, p.x, p.y + radius + 16)

                if (offline) {
                    ctx.fillStyle = '#FF4560'
                    ctx.font      = 'bold 9px monospace'
                    ctx.fillText('OFFLINE', p.x, p.y + radius + 27)
                }

                // icon letter
                ctx.fillStyle = offline ? '#FF4560' : nd.color
                ctx.font      = `bold ${nd.role === 'gateway' ? 14 : 11}px monospace`
                ctx.textAlign = 'center'
                ctx.textBaseline = 'middle'
                ctx.fillText(nd.id === 'kafka' ? 'K' : nd.id === 'redis' ? 'R' : nd.id === 'postgres' ? 'PG' : nd.id === 'gateway' ? 'GW' : nd.id.slice(-1), p.x, p.y)
                ctx.textBaseline = 'alphabetic'
            })

            rafRef.current = requestAnimationFrame(draw)
        }

        draw()

        return () => {
            cancelAnimationFrame(rafRef.current)
            ro.disconnect()
        }
    }, [nodes])

    return (
        <div className="topology-wrap">
            <canvas ref={canvasRef} className="topology-canvas" />
        </div>
    )
}
