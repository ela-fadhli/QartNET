package tn.enicarthage.qartnet.shared.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);
    private static final long SLOW_THRESHOLD_MS = 200;

    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void serviceLayer() {}

    @Around("serviceLayer()")
    public Object logServiceCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        long start = System.currentTimeMillis();

        log.debug("→ {}", method);
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            if (elapsed > SLOW_THRESHOLD_MS) {
                log.warn("SLOW {} ({}ms)", method, elapsed);
            } else {
                log.debug("← {} ({}ms)", method, elapsed);
            }
            return result;
        } catch (Exception ex) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("✗ {} threw {} after {}ms: {}", method,
                    ex.getClass().getSimpleName(), elapsed, ex.getMessage());
            throw ex;
        }
    }
}
