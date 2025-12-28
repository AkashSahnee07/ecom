package com.ecommerce.order.config;

import brave.Tracing;
import brave.context.slf4j.MDCScopeDecorator;
import brave.propagation.B3Propagation;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.sampler.Sampler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.Span;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Custom tracing configuration for Order Service.
 * Provides additional tracing capabilities and custom span management.
 */
@Configuration
public class TracingConfig implements WebMvcConfigurer {

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${tracing.sampling.probability:1.0}")
    private float samplingProbability;

    /**
     * Custom sampler configuration
     */
    @Bean
    public Sampler customSampler() {
        return Sampler.create(samplingProbability);
    }

    /**
     * Custom tracing configuration with MDC support
     */
    @Bean
    public Tracing customTracing() {
        return Tracing.newBuilder()
                .localServiceName(serviceName)
                .sampler(customSampler())
                .currentTraceContext(
                    ThreadLocalCurrentTraceContext.newBuilder()
                        .addScopeDecorator(MDCScopeDecorator.get())
                        .build()
                )
                .build();
    }

    /**
     * Register custom tracing interceptor
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Temporarily disabled due to Spring Cloud Sleuth compatibility issues
        // registry.addInterceptor(new CustomTracingInterceptor());
    }

    /**
     * Custom HTTP request interceptor for tracing
     * Temporarily disabled due to Spring Cloud Sleuth compatibility issues
     */
    /*
    public static class CustomTracingInterceptor implements HandlerInterceptor {

        @Autowired
        private Tracer tracer;

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
                               Object handler) throws Exception {
            
            Span currentSpan = tracer.nextSpan().name("http-request");
            
            // Add custom tags
            currentSpan.tag("http.method", request.getMethod());
            currentSpan.tag("http.url", request.getRequestURL().toString());
            currentSpan.tag("http.user_agent", request.getHeader("User-Agent"));
            currentSpan.tag("service.name", "order-service");
            
            // Add correlation headers if present
            String correlationId = request.getHeader("X-Correlation-ID");
            if (correlationId != null) {
                currentSpan.tag("correlation.id", correlationId);
            }
            
            String userId = request.getHeader("X-User-ID");
            if (userId != null) {
                currentSpan.tag("user.id", userId);
            }
            
            String sessionId = request.getHeader("X-Session-ID");
            if (sessionId != null) {
                currentSpan.tag("session.id", sessionId);
            }
            
            currentSpan.start();
            
            // Store span in request attributes for later use
            request.setAttribute("currentSpan", currentSpan);
            
            return true;
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                  Object handler, Exception ex) throws Exception {
            
            Span currentSpan = (Span) request.getAttribute("currentSpan");
            if (currentSpan != null) {
                // Add response information
                currentSpan.tag("http.status_code", String.valueOf(response.getStatus()));
                
                // Add error information if present
                if (ex != null) {
                    currentSpan.tag("error", "true");
                    currentSpan.tag("error.message", ex.getMessage());
                    currentSpan.tag("error.class", ex.getClass().getSimpleName());
                }
                
                // Mark as error if status code indicates error
                if (response.getStatus() >= 400) {
                    currentSpan.tag("error", "true");
                }
                
                currentSpan.end();
            }
        }
    }
    */
}

/**
 * Custom tracing aspect for business logic methods
 * Temporarily disabled due to Spring Cloud Sleuth compatibility issues
 */
// @org.aspectj.lang.annotation.Aspect
// @org.springframework.stereotype.Component
class OrderTracingAspect {

    @Autowired
    private Tracer tracer;

    /**
     * Trace all service methods
     */
    @org.aspectj.lang.annotation.Around("execution(* com.ecommerce.order.service..*(..))")
    public Object traceServiceMethods(org.aspectj.lang.ProceedingJoinPoint joinPoint) throws Throwable {
        
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        Span span = tracer.nextSpan()
                .name(className + "." + methodName)
                .tag("component", "service")
                .tag("class", className)
                .tag("method", methodName);
        
        span.start();
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            // Add method parameters as tags (be careful with sensitive data)
            Object[] args = joinPoint.getArgs();
            for (int i = 0; i < args.length && i < 3; i++) { // Limit to first 3 args
                if (args[i] != null) {
                    span.tag("arg." + i, args[i].toString());
                }
            }
            
            Object result = joinPoint.proceed();
            
            // Add success tag
            span.tag("success", "true");
            
            return result;
            
        } catch (Exception e) {
            // Add error information
            span.tag("error", "true");
            span.tag("error.message", e.getMessage());
            span.tag("error.class", e.getClass().getSimpleName());
            
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * Trace repository methods
     */
    @org.aspectj.lang.annotation.Around("execution(* com.ecommerce.order.repository..*(..))")
    public Object traceRepositoryMethods(org.aspectj.lang.ProceedingJoinPoint joinPoint) throws Throwable {
        
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        Span span = tracer.nextSpan()
                .name("db." + methodName)
                .tag("component", "database")
                .tag("db.type", "postgresql")
                .tag("db.operation", methodName)
                .tag("repository", className);
        
        span.start();
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            Object result = joinPoint.proceed();
            span.tag("success", "true");
            return result;
            
        } catch (Exception e) {
            span.tag("error", "true");
            span.tag("error.message", e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }
}