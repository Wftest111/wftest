//package com.sarthak.webapp.config;
//
//import io.micrometer.core.aop.TimedAspect;
//import io.micrometer.core.instrument.MeterRegistry;
//import io.micrometer.statsd.StatsdMeterRegistry;
//import io.micrometer.statsd.StatsdConfig;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.EnableAspectJAutoProxy;
//
//@Configuration
//@EnableAspectJAutoProxy
//public class MetricsConfig {
//
//    @Bean
//    public TimedAspect timedAspect(MeterRegistry registry) {
//        return new TimedAspect(registry);
//    }
//
//    @Bean
//    public StatsdMeterRegistry statsdMeterRegistry() {
//        StatsdConfig config = new StatsdConfig() {
//            @Override
//            public String get(String key) {
//                return null;
//            }
//
//            @Override
//            public String prefix() {
//                return "csye6225";
//            }
//
//            @Override
//            public int port() {
//                return 8126;
//            }
//        };
//        return new StatsdMeterRegistry(config, io.micrometer.core.instrument.Clock.SYSTEM);
//    }
//}