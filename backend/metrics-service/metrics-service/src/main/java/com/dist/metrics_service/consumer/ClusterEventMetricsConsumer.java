//package com.dist.metrics_service.consumer;
//
//import com.dist.metrics_service.event.ClusterEvent;
//import com.dist.metrics_service.service.MetricService;
//import io.micrometer.core.instrument.Counter;
//import io.micrometer.core.instrument.MeterRegistry;
//import io.micrometer.core.instrument.Tags;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.support.Acknowledgment;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class ClusterEventMetricsConsumer {
//
//    private final MetricService metricService;
//    private final MeterRegistry meterRegistry;
//
//    @KafkaListener(
//            topics = "${app.kafka.topics.cluster-events:cluster-events}",
//            groupId = "metrics-group",
//            containerFactory = "clusterEventListenerContainerFactory"
//    )
//    public void consume(ClusterEvent event,
//                        Acknowledgment acknowledgment) {
//
//        try {
//
//            metricService.recordClusterEvent(event);
//
//            Counter.builder("cluster.node.status.transitions")
//                    .tags(
//                            Tags.of(
//                                    "node",
//                                    event.nodeId(),
//                                    "status",
//                                    event.status().name()
//                            )
//                    )
//                    .register(meterRegistry)
//                    .increment();
//
//            acknowledgment.acknowledge();
//
//        } catch (Exception e) {
//
//            log.error(
//                    "Failed to record cluster event for nodeId={}",
//                    event.nodeId(),
//                    e
//            );
//        }
//    }
//}