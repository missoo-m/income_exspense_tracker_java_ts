package com.example.expensetracker.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
public class LoggableAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggableAspect.class);

    @Around("@annotation(com.example.expensetracker.aop.Loggable)")
    public Object aroundLoggableMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Loggable annotation = method.getAnnotation(Loggable.class);

        String action = annotation != null && !annotation.value().isBlank()
                ? annotation.value()
                : method.getName();

        String argsSummary = Arrays.stream(joinPoint.getArgs())
                .map(arg -> arg == null ? "null" : arg.getClass().getSimpleName())
                .collect(Collectors.joining(", "));

        String genericTypes = resolveGenericTypes(joinPoint.getTarget().getClass());

        log.info("[AOP] start action='{}', method={}.{}, args=[{}], genericTypes={}",
                action, signature.getDeclaringTypeName(), signature.getName(), argsSummary, genericTypes);

        long startedAt = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            log.info("[AOP] success action='{}', method={}.{}, tookMs={}",
                    action, signature.getDeclaringTypeName(), signature.getName(), System.currentTimeMillis() - startedAt);
            return result;
        } catch (Throwable ex) {
            log.error("[AOP] fail action='{}', method={}.{}, tookMs={}, reason={}",
                    action, signature.getDeclaringTypeName(), signature.getName(), System.currentTimeMillis() - startedAt, ex.getMessage());
            throw ex;
        }
    }

    private String resolveGenericTypes(Class<?> clazz) {
        Type genericSuperclass = clazz.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType parameterizedType) {
            return Arrays.stream(parameterizedType.getActualTypeArguments())
                    .map(Type::getTypeName)
                    .collect(Collectors.joining(", "));
        }
        return "n/a";
    }
}
