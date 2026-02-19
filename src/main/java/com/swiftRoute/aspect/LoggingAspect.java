package com.swiftRoute.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("execution(* com.swiftRoute.service..*(..))" +
            "|| execution(* com.swiftRoute.controller..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {

        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        log.info("➡️ Starting execution: {} with args {}", methodName, args);

        long startTime = System.currentTimeMillis();

        Object result = joinPoint.proceed(); // method executes here

        long endTime = System.currentTimeMillis();

        log.info("⬅️ Finished execution: {} in {} ms",
                methodName,
                (endTime - startTime));

        return result;
    }

    @Before("execution(* com.swiftRoute.service..*(..))" +
            "|| execution(* com.swiftRoute.controller..*(..))")
    public void logBefore(JoinPoint joinPoint) {
        log.info("➡️ Starting: {}", joinPoint.getSignature());
    }
    @After("execution(* com.swiftRoute.service..*(..))" +
            "|| execution(* com.swiftRoute.controller..*(..))")
    public void logAfter(JoinPoint joinPoint) {
        log.info("⬅️ Finished: {}", joinPoint.getSignature());
    }


}
