package de.svg.spring_boot_sse_manager.aop;

import de.svg.spring_boot_sse_manager.SSEManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SSEManagerAspect.
 */
@Aspect
@Component
public class SSEManagerAspect {

    @Around("@annotation(de.svg.spring_boot_sse_manager.aop.SSEStream)")
    public SseEmitter run(final ProceedingJoinPoint joinPoint) throws Throwable {
        final SSEManager manager;


        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final Method method = signature.getMethod();

        final SSEStream myAnnotation = method.getAnnotation(SSEStream.class);

        if (myAnnotation.timeout().isEmpty()) {
            manager = new SSEManager();
        } else {
            manager = new SSEManager(Long.parseLong(myAnnotation.timeout()));
        }

        final ExecutorService sseMvcExecutor = Executors.newSingleThreadExecutor();

        sseMvcExecutor.execute(() -> {
            final Object proceed;
            try {
                proceed = joinPoint.proceed(new Object[]{manager});
                manager.done(proceed);
            } catch (Throwable throwable) {
                manager.error(500, throwable);
            }
        });

        return manager.getEmitter();
    }
}
