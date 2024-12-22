package com.example.item.tracker.aspect;

import com.example.item.tracker.model.CustomException;
import com.example.item.tracker.model.ErrorCodes;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.Arrays;

/**
 * Aspect for logging execution of service and repository Spring components.
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

    /**
     * Pointcut that matches all repositories, services and Web REST endpoints.
     */
    @Pointcut("within(@org.springframework.stereotype.Repository *)" + " || within(@org.springframework.stereotype.Service *)" + " || within(@org.springframework.stereotype.Component *)" + " || within(@org.springframework.web.bind.annotation.RestController *)")
    public void springBeanPointcut() {
        // Method is empty as this is just a Pointcut, the implementations are in the advices.
    }

    /**
     * Pointcut that matches all Spring beans in the application's main packages.
     */
    @Pointcut("execution(* com.example.item.tracker..*(..))")
    public void applicationPackagePointcut() {
        // Method is empty as this is just a Pointcut, the implementations are in the advices.
    }

    /**
     * Advice that logs methods throwing exceptions.
     *
     * @param joinPoint join point for advice
     * @param e         exception
     */
    @AfterThrowing(pointcut = "applicationPackagePointcut() && springBeanPointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        log.error("Exception in {}.{}() with cause: {}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), e.getCause() != null ? e.getCause() : "NULL");
    }

    /**
     * Advice that logs when a method is entered and exited.
     *
     * @param joinPoint join point for advice
     * @return result
     * @throws Throwable throws IllegalArgumentException
     */
    @Around("applicationPackagePointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws CustomException {
        log.debug("Enter: {}.{}() with argument[s] = {}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));

        try {
            Object result = joinPoint.proceed();
            log.debug("Exit: {}.{}() with result = {}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), result);

            return result;
        } catch (Throwable e) {
            log.error("Illegal argument: {} in {}.{}()", Arrays.toString(joinPoint.getArgs()), joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
            throw new CustomException(ErrorCodes.TEC001.getCode(), ErrorCodes.TEC001.getDesc(), "", "Illegal argument: " + e.getMessage());
        }
    }

    //AOP expression for which methods shall be intercepted
    @Around("execution(* com.example.item.tracker.component..*(..)))")
    public Object profileAllMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();

        //Get intercepted method details
        String className = methodSignature.getDeclaringType().getSimpleName();
        String methodName = methodSignature.getName();

        final StopWatch stopWatch = new StopWatch();

        //Measure method execution time
        stopWatch.start();
        Object result = joinPoint.proceed();
        stopWatch.stop();

        //Log method execution time
        if (stopWatch.getTotalTimeMillis() >= 10)
            log.error("Execution time (PERF LOG) of {}.{} :: {} ms", className, methodName, stopWatch.getTotalTimeMillis());

        return result;
    }
}
