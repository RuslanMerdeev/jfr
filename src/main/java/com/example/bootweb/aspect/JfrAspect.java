package com.example.bootweb.aspect;

import com.example.bootweb.event.RequestEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;

@Aspect
@Component
public class JfrAspect {

    @Around("@annotation(com.example.bootweb.annotation.JfrRequest)")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        RequestEvent requestEvent = new RequestEvent();

        requestEvent.begin();

        Method method = MethodSignature.class.cast(joinPoint.getSignature()).getMethod();
        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        if (getMapping != null) {
            requestEvent.method = "GET";
            requestEvent.path = getMapping.value()[0];
        } else {
            PostMapping postMapping = method.getAnnotation(PostMapping.class);
            if (postMapping != null) {
                requestEvent.method = "POST";
                requestEvent.path = postMapping.value()[0];
            }
        }
        Object[] args = joinPoint.getArgs();
        requestEvent.size = (new GsonBuilder().create()).toJson(args[0]).length();;
        Object proceed = joinPoint.proceed();

        requestEvent.end();
        requestEvent.commit();

        return proceed;
    }
}
