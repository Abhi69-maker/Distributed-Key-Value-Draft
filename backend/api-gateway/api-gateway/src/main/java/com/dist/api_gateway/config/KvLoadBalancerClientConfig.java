package com.dist.api_gateway.config;

import com.dist.api_gateway.lb.KvLoadBalancerConfig;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.context.annotation.Configuration;

@Configuration
@LoadBalancerClient(name = "kv-service", configuration = KvLoadBalancerConfig.class)
public class KvLoadBalancerClientConfig {
}