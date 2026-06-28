//package com.dist.api_gateway.config;
//
//import org.springframework.cloud.client.DefaultServiceInstance;
//import org.springframework.cloud.client.ServiceInstance;
//import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
//import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.env.Environment;
//import reactor.core.publisher.Flux;
//
//import java.net.URI;
//import java.util.List;
//
//@Configuration
//@LoadBalancerClient(name = "kv-service", configuration = KvServiceLoadBalancerConfig.class)
//public class KvServiceLoadBalancerConfig {
//
//    @Bean
//    public ServiceInstanceListSupplier kvServiceInstanceListSupplier(Environment env) {
//        List<String> nodeUrls = List.of(
//                env.getProperty("app.kv-nodes[0]", "http://localhost:8081"),
//                env.getProperty("app.kv-nodes[1]", "http://localhost:8085"),
//                env.getProperty("app.kv-nodes[2]", "http://localhost:8086")
//        );
//
//        List<ServiceInstance> instances = nodeUrls.stream()
//                .map(url -> {
//                    URI uri = URI.create(url);
//                    return (ServiceInstance) new DefaultServiceInstance(
//                            uri.getHost() + ":" + uri.getPort(),
//                            "kv-service",
//                            uri.getHost(),
//                            uri.getPort(),
//                            false
//                    );
//                })
//                .toList();
//
//        return new ServiceInstanceListSupplier() {
//            @Override
//            public String getServiceId() { return "kv-service"; }
//
//            @Override
//            public Flux<List<ServiceInstance>> get() { return Flux.just(instances); }
//        };
//    }
//}