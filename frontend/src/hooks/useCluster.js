
import { useState, useEffect, useCallback, useRef } from 'react';
import {
    INITIAL_KV_DATA, seedAuditLog,
    makeEvent, makeAuditEntry, nowStr,
} from '../utils/store';

export function useCluster() {
    const [kvData, setKvData] = useState(INITIAL_KV_DATA);
    const [events, setEvents] = useState([]);
    const [auditLog, setAuditLog] = useState(seedAuditLog(40));
    const [rpsHistory, setRpsHistory] = useState(() =>
        Array.from({ length: 30 }, () => Math.floor(900 + Math.random() * 600))
    );
    const [metrics, setMetrics] = useState({
        keys: 2847, hitRate: 87.3, rps: 1204, lag: 4,
        totalReq: 84210, replEvents: 14821, cacheHits: 73511,
        node1: { cpu: 34, mem: 342, keys: 2847, offset: 14821, status: 'healthy' },
        node2: { cpu: 28, mem: 318, keys: 2847, offset: 14819, status: 'healthy' },
        node3: { cpu: 22, mem: 305, keys: 2847, offset: 14817, status: 'healthy' },
    });
    const [node3Alive, setNode3Alive] = useState(true);
    const node3AliveRef = useRef(true);

    const addEvent = useCallback((ev) => {
        setEvents(prev => {
            const next = [...prev, ev];
            return next.length > 200 ? next.slice(-200) : next;
        });
        setAuditLog(prev => {
            const entry = makeAuditEntry();
            return [entry, ...prev].slice(0, 100);
        });
    }, []);

    const simulateNodeFailure = useCallback((nodeNum) => {
        if (nodeNum === 3) {
            const next = !node3AliveRef.current;
            node3AliveRef.current = next;
            setNode3Alive(next);
            setMetrics(prev => ({
                ...prev,
                node3: { ...prev.node3, status: next ? 'healthy' : 'offline' },
            }));
            addEvent({
                type: 'health',
                key: `node-3 ${next ? 'recovered' : 'went offline'}`,
                node: 'node-3',
                time: nowStr(),
            });
        }
    }, [addEvent]);

    const putKV = useCallback((key, val, node) => {
        const entry = { key, val, node, ttl: '∞', cached: false };
        setKvData(prev => [entry, ...prev.filter(r => r.key !== key)]);
        addEvent({ type: 'create', key, node: node.toLowerCase().replace(' ', '-'), time: nowStr() });
    }, [addEvent]);

    const deleteKV = useCallback((key) => {
        setKvData(prev => prev.filter(r => r.key !== key));
        addEvent({ type: 'delete', key, node: 'node-1', time: nowStr() });
    }, [addEvent]);

    // Live tick every 2 seconds
    useEffect(() => {
        const id = setInterval(() => {
            const ev = makeEvent();
            addEvent(ev);

            const newRps = Math.floor(900 + Math.random() * 600);
            setRpsHistory(prev => [...prev.slice(1), newRps]);

            const hitRate = +(80 + Math.random() * 12).toFixed(1);
            setMetrics(prev => ({
                ...prev,
                rps: newRps,
                keys: 2840 + Math.floor(Math.random() * 20),
                hitRate,
                lag: 2 + Math.floor(Math.random() * 8),
                totalReq: prev.totalReq + Math.floor(Math.random() * 40),
                replEvents: prev.replEvents + Math.floor(Math.random() * 5),
                cacheHits: prev.cacheHits + Math.floor(hitRate / 100 * newRps * 2),
                node1: { ...prev.node1, cpu: 28 + Math.floor(Math.random() * 18), offset: prev.node1.offset + 3 },
                node2: { ...prev.node2, cpu: 20 + Math.floor(Math.random() * 14), offset: prev.node2.offset + 2 },
                node3: {
                    ...prev.node3,
                    cpu: node3AliveRef.current ? 15 + Math.floor(Math.random() * 12) : 0,
                    offset: node3AliveRef.current ? prev.node3.offset + 2 : prev.node3.offset,
                },
            }));
        }, 2000);
        return () => clearInterval(id);
    }, [addEvent]);

    return { kvData, events, auditLog, rpsHistory, metrics, node3Alive, simulateNodeFailure, putKV, deleteKV };
}
