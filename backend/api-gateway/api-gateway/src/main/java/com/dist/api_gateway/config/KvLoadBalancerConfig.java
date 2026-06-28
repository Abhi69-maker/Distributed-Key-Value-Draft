//package com.dist.api_gateway.config;
//
//import org.springframework.cloud.client.DefaultServiceInstance;
//import org.springframework.cloud.client.ServiceInstance;
//import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
//import org.springframework.context.annotation.Bean;
//import reactor.core.publisher.Flux;
//
//import java.util.List;
//
//public class KvLoadBalancerConfig {
//
//    @Bean
//    ServiceInstanceListSupplier staticSupplier() {
//        List<ServiceInstance> instances = List.of(
//                new DefaultServiceInstance("node1", "kv-service", "localhost", 8081, false),
//                new DefaultServiceInstance("node2", "kv-service", "localhost", 8085, false),
//                new DefaultServiceInstance("node3", "kv-service", "localhost", 8086, false)
//        );
//
//        return new ServiceInstanceListSupplier() {
//            @Override
//            public String getServiceId() {
//                return "kv-service";
//            }
//
//            @Override
//            public Flux<List<ServiceInstance>> get() {
//                return Flux.just(instances);
//            }
//        };
//    }
//}