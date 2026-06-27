package com.dist.cluster_service.config;


public final class ClusterRedisKeys {
    private ClusterRedisKeys() {}

    public static final String HEARTBEAT_PREFIX = "cluster:heartbeat:";
    public static final String NODE_REGISTRY = "cluster:nodes:registry";

    public static String heartbeatKey(String nodeId) {
        return HEARTBEAT_PREFIX + nodeId; }
}