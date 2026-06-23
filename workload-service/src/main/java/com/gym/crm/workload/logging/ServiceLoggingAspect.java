package com.gym.crm.workload.logging;

import com.gym.crm.common.logging.AbstractServiceLoggingAspect;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceLoggingAspect extends AbstractServiceLoggingAspect {

    @Around("execution(public * com.gym.crm.workload.service..*(..))")
    @Override
    public Object logServiceOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        return super.logServiceOperation(joinPoint);
    }
}