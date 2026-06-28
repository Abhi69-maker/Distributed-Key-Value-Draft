import React, { useRef, useEffect } from 'react'

const NODE_DEFS = [
    { id: 'gateway', label: 'API Gateway', sub: 'Spring Cloud', x: 0.5,  y: 0.12, color: '#8b5cf6', r: 28 },
    { id: 'node-1',  label: 'Node 1',      sub: ':8081 Leader', x: 0.2,  y: 0.48, color: '#22c55e', r: 24 },
    { id: 'node-2',  label: 'Node 2',      sub: ':8082',        x: 0.5,  y: 0.48, color: '#00d4ff', r: 24 },
    { id: 'node-3',  label: 'Node 3',      sub: ':8083',        x: 0.8,  y: 0.48, color: '#00d4ff', r: 24 },
    { id: 'kafka',   label: 'Kafka',       sub: '5 topics',     x: 0.3,  y: 0.82, color: '#f59e0b', r: 22 },
    { id: 'redis',   label: 'Redis',       sub: 'Cache',        x: 0.55, y: 0.82, color: '#ef4444', r: 22 },
    { id: 'pg',      label: 'PostgreSQL',  sub: 'Persistence',  x: 0.8,  y: 0.82, color: '#60a5fa', r: 22 },
]

const EDGES = [
    ['gateway','node-1'], ['gateway','node-2'], ['gateway','node-3'],
    ['node-1','kafka'], ['node-2','kafka'], ['node-3','kafka'],
    ['node-1','redis'], ['node-2','redis'],
    ['node-1','pg'],    ['node-2','pg'],    ['node-3','pg'],
]

export default function ClusterCanvas({ nodeStatus }) {
    const canvasRef = useRef(null)
    const animRef = useRef(0)
    const offsetRef = useRef(0)

    useEffect(() => {
        const canvas = canvasRef.current
        if (!canvas) return
        const ctx = canvas.getContext('2d')

        function draw() {
            const W = canvas.offsetWidth
            const H = canvas.offsetHeight
            canvas.width = W
            canvas.height = H

            ctx.clearRect(0, 0, W, H)

            const nodes = NODE_DEFS.map(n => ({
                ...n,
                px: n.x * W,
                py: n.y * H,
                alive: n.id.startsWith('node-') ? (nodeStatus[n.id]?.alive ?? true) : true,
                color: n.id === 'node-3'
                    ? (nodeStatus['node-3']?.alive ? '#00d4ff' : '#ef4444')
                    : n.color,
            }))

            // Draw edges
            offsetRef.current = (offsetRef.current + 0.4) % 40
            EDGES.forEach(([aId, bId]) => {
                const a = nodes.find(n => n.id === aId)
                const b = nodes.find(n => n.id === bId)
                if (!a || !b) return
                const inactive = (aId.startsWith('node-') && !a.alive) || (bId.startsWith('node-') && !b.alive)
                ctx.save()
                ctx.beginPath()
                ctx.moveTo(a.px, a.py)
                ctx.lineTo(b.px, b.py)
                ctx.strokeStyle = inactive ? 'rgba(239,68,68,0.15)' : 'rgba(255,255,255,0.08)'
                ctx.lineWidth = 1
                if (!inactive) {
                    ctx.setLineDash([4, 6])
                    ctx.lineDashOffset = -offsetRef.current
                } else {
                    ctx.setLineDash([2, 6])
                }
                ctx.stroke()
                ctx.restore()

                // Animated dot along edge
                if (!inactive) {
                    const t = ((offsetRef.current / 40) + (aId.length * 0.1)) % 1
                    const ex = a.px + (b.px - a.px) * t
                    const ey = a.py + (b.py - a.py) * t
                    ctx.beginPath()
                    ctx.arc(ex, ey, 2, 0, Math.PI * 2)
                    ctx.fillStyle = a.color
                    ctx.globalAlpha = 0.7
                    ctx.fill()
                    ctx.globalAlpha = 1
                }
            })

            // Draw nodes
            nodes.forEach(n => {
                // Glow
                if (n.alive) {
                    const grad = ctx.createRadialGradient(n.px, n.py, 0, n.px, n.py, n.r * 2)
                    grad.addColorStop(0, n.color + '33')
                    grad.addColorStop(1, 'transparent')
                    ctx.beginPath()
                    ctx.arc(n.px, n.py, n.r * 2, 0, Math.PI * 2)
                    ctx.fillStyle = grad
                    ctx.fill()
                }

                // Circle
                ctx.beginPath()
                ctx.arc(n.px, n.py, n.r, 0, Math.PI * 2)
                ctx.fillStyle = n.color + '18'
                ctx.fill()
                ctx.strokeStyle = n.alive ? n.color : '#ef4444'
                ctx.lineWidth = n.alive ? 1.5 : 1
                ctx.setLineDash([])
                ctx.stroke()

                // Label
                ctx.fillStyle = n.alive ? n.color : '#ef4444'
                ctx.font = `500 10px 'JetBrains Mono', monospace`
                ctx.textAlign = 'center'
                ctx.fillText(n.label, n.px, n.py + 3)

                // Sub
                ctx.fillStyle = 'rgba(148,163,184,0.7)'
                ctx.font = `400 9px 'JetBrains Mono', monospace`
                ctx.fillText(n.alive ? n.sub : 'OFFLINE', n.px, n.py + 14)
            })

            animRef.current = requestAnimationFrame(draw)
        }

        draw()
        return () => cancelAnimationFrame(animRef.current)
    }, [nodeStatus])

    return (
        <canvas
            ref={canvasRef}
            style={{ width: '100%', height: '100%', display: 'block' }}
        />
    )
}
