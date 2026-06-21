package com.dist.api_gateway.config;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.text.DecimalFormat;
import java.util.List;

@Configuration
public class KvServiceLoadBalancerConfig {

    /**
     * Creates and supplies the list of KV service instances that Spring Cloud
     * LoadBalancer can distribute requests across. The node addresses are loaded
     * from application configuration and converted into ServiceInstance objects.
     * This enables client-side load balancing, allowing the gateway to route
     * requests evenly among multiple KV nodes for scalability and high availability.
     */
    @Bean
    public ServiceInstanceListSupplier kvServiceInstanceListSupplier(Environment env){
        List<String>nodeUrls=List.of(
                env.getProperty("app.kv-nodes[0]", "http://kv-node-1:8081"),
                env.getProperty("app.kv-nodes[1]", "http://kv-node-2:8081"),
                env.getProperty("app.kv-nodes[2]", "http://kv-node-3:8081")
        );

        List<ServiceInstance> instances=nodeUrls.stream()
                .map(url->{
                    URI uri=URI.create(url);
                    return (ServiceInstance)new DefaultServiceInstance(
                            uri.getHost(),"kv-service",uri.getHost(),uri.getPort(),false);
                }).toList();

        return new ServiceInstanceListSupplier() {

            /**
             * Returns the unique service identifier associated with this load balancer
             * configuration. Spring Cloud Gateway uses this name to match incoming
             * requests targeting the KV service and retrieve the corresponding list
             * of available instances. This method acts as the link between routing
             * configuration and the custom load balancing mechanism.
             */
            @Override
            public String getServiceId() {
                return "kv-service";
            }

            /**
             * Supplies the current list of available KV service instances to the
             * Spring Cloud LoadBalancer framework. The instances are emitted as a
             * reactive Flux so that the gateway can dynamically select a target node
             * for each request. This method is a core part of request distribution
             * and enables load balancing across all configured KV nodes.
             */
            @Override
            public Flux<List<ServiceInstance>> get() {
                return Flux.just(instances);
            }
        };
    }
}
