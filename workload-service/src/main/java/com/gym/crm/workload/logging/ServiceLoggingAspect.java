package com.gym.crm.workload.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class ServiceLoggingAspect {

    @Around("execution(public * com.gym.crm.workload.service..*(..))")
    public Object logServiceOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className  = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        Object[] args     = joinPoint.getArgs();
        String transactionId = MDC.get(TransactionIdFilter.TRANSACTION_ID_KEY);

        log.debug("OPERATION START  transactionId={} {}.{}() args={}",
                transactionId, className, methodName, Arrays.toString(args));

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;

            log.debug("OPERATION SUCCESS transactionId={} {}.{}() duration={}ms result={}",
                    transactionId, className, methodName, duration, result);

            return result;
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - start;

            log.error("OPERATION ERROR  transactionId={} {}.{}() duration={}ms error={}: {}",
                    transactionId, className, methodName, duration,
                    ex.getClass().getSimpleName(), ex.getMessage());

            throw ex;
        }
    }
}
