package com.sarthak.webapp.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class MetricsAspect {
    private final MeterRegistry meterRegistry;
    private static final Logger logger = LoggerFactory.getLogger(MetricsAspect.class);

    public MetricsAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        logger.info("MetricsAspect initialized");
    }

    private String getHttpMethod(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

        if (method.isAnnotationPresent(GetMapping.class)) return "GET";
        if (method.isAnnotationPresent(PostMapping.class)) return "POST";
        if (method.isAnnotationPresent(PutMapping.class)) return "PUT";
        if (method.isAnnotationPresent(DeleteMapping.class)) return "DELETE";
        if (method.isAnnotationPresent(PatchMapping.class)) return "PATCH";

        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        if (requestMapping != null && requestMapping.method().length > 0) {
            return requestMapping.method()[0].name();
        }

        logger.warn("Unknown HTTP method for endpoint: {}", method.getName());
        return "UNKNOWN";
    }

    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public Object measureApiTiming(ProceedingJoinPoint joinPoint) throws Throwable {
        String httpMethod = getHttpMethod(joinPoint);
        String methodName = joinPoint.getSignature().getName();
        String path = getPath(joinPoint);

        logger.debug("Starting request measurement - Method: {} Path: {}", httpMethod, path);
        long startTime = System.currentTimeMillis();

        try {
            // Record request start
            meterRegistry.counter("http.requests",
                    "method", httpMethod,
                    "path", path,
                    "status", "started").increment();

            Object result = joinPoint.proceed();

            // Record successful completion
            long duration = System.currentTimeMillis() - startTime;
            Timer.builder("http.response.time")
                    .tag("method", httpMethod)
                    .tag("path", path)
                    .tag("status", "success")
                    .register(meterRegistry)
                    .record(duration, TimeUnit.MILLISECONDS);

            logger.debug("Completed request - Method: {} Path: {} Duration: {}ms",
                    httpMethod, path, duration);

            meterRegistry.counter("http.requests",
                    "method", httpMethod,
                    "path", path,
                    "status", "success").increment();

            return result;
        } catch (Exception e) {
            // Record error metrics
            long duration = System.currentTimeMillis() - startTime;

            logger.error("Request failed - Method: {} Path: {} Error: {} Duration: {}ms",
                    httpMethod, path, e.getClass().getSimpleName(), duration);

            meterRegistry.counter("http.errors",
                    "method", httpMethod,
                    "path", path,
                    "error", e.getClass().getSimpleName()).increment();

            Timer.builder("http.response.time")
                    .tag("method", httpMethod)
                    .tag("path", path)
                    .tag("status", "error")
                    .register(meterRegistry)
                    .record(duration, TimeUnit.MILLISECONDS);

            throw e;
        }
    }

    private String getPath(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String path = "";

        if (method.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping mapping = method.getAnnotation(RequestMapping.class);
            path = mapping.value().length > 0 ? mapping.value()[0] : "";
        } else if (method.isAnnotationPresent(GetMapping.class)) {
            GetMapping mapping = method.getAnnotation(GetMapping.class);
            path = mapping.value().length > 0 ? mapping.value()[0] : "";
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            PostMapping mapping = method.getAnnotation(PostMapping.class);
            path = mapping.value().length > 0 ? mapping.value()[0] : "";
        } else if (method.isAnnotationPresent(PutMapping.class)) {
            PutMapping mapping = method.getAnnotation(PutMapping.class);
            path = mapping.value().length > 0 ? mapping.value()[0] : "";
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            DeleteMapping mapping = method.getAnnotation(DeleteMapping.class);
            path = mapping.value().length > 0 ? mapping.value()[0] : "";
        }

        if (path.isEmpty()) {
            path = joinPoint.getSignature().getName();
            logger.debug("No explicit path found, using method name: {}", path);
        }

        return path;
    }
}