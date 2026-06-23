package com.gym.crm.common.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;

import java.util.Arrays;

@Slf4j
public abstract class AbstractServiceLoggingAspect {

    protected static final String TRANSACTION_ID_KEY = "transactionId";

    public Object logServiceOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        Object[] args = joinPoint.getArgs();
        String transactionId = MDC.get(TRANSACTION_ID_KEY);

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