package com.dist.api_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * Logs method, path, which downstream node actually served the request
 * (resolved AFTER load balancing), and total latency. Best way to prove
 * load balancing works: PUT a few keys and watch the target node rotate.
 */
@Component
@Slf4j
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    /**
     * Intercepts every request passing through the API Gateway and records
     * important information such as HTTP method, request path, response status,
     * target downstream node, and total processing time. After routing completes,
     * it retrieves the actual node selected by the load balancer and logs it.
     * This is extremely useful for monitoring, debugging, performance analysis,
     * and verifying that requests are being distributed across multiple nodes.
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = System.currentTimeMillis();
        String method = request.getMethod().name();
        String path = request.getURI().getPath();

        log.info(">> Incoming request: {} {}", method, path);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            URI routedUri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
            String targetNode = routedUri != null ? routedUri.getHost() + ":" + routedUri.getPort() : "unknown";
            int status = exchange.getResponse().getStatusCode() != null
                    ? exchange.getResponse().getStatusCode().value() : 0;

            log.info("<< {} {} -> node={} status={} duration={}ms",
                    method, path, targetNode, status, duration);
        }));
    }

    /**
     * Defines the execution priority of this global filter within the Gateway
     * filter chain. A lower value gives higher precedence, ensuring request
     * logging starts before most other filters execute. This guarantees that
     * timing measurements include the complete request lifecycle from entry
     * into the gateway until the response is returned to the client.
     */
    @Override
    public int getOrder() { return -1; }
}
