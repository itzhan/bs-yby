package com.campus.recruitment.aspect;

import com.campus.recruitment.entity.OperationLog;
import com.campus.recruitment.security.SecurityUtils;
import com.campus.recruitment.service.OperationLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService operationLogService;
    private final ObjectMapper objectMapper;

    @Pointcut("execution(* com.campus.recruitment.controller.*.*(..)) " +
              "&& !execution(* com.campus.recruitment.controller.AuthController.*(..)) " +
              "&& !execution(* com.campus.recruitment.controller.DashboardController.*(..)) " +
              "&& !execution(* com.campus.recruitment.controller.FileController.*(..)) " +
              "&& (@annotation(org.springframework.web.bind.annotation.PostMapping) " +
              "|| @annotation(org.springframework.web.bind.annotation.PutMapping) " +
              "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping))")
    public void logPointcut() {}

    @Around("logPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result;
        String resultStr = "success";
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            resultStr = "error: " + e.getMessage();
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            try {
                saveLog(joinPoint, resultStr, duration);
            } catch (Exception e) {
                log.error("保存操作日志失败", e);
            }
        }
        return result;
    }

    private void saveLog(ProceedingJoinPoint joinPoint, String result, long duration) {
        OperationLog opLog = new OperationLog();
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId != null) {
            opLog.setUserId(userId);
            opLog.setUsername(SecurityUtils.getCurrentUser().getUsername());
        }
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        opLog.setOperation(className + "." + methodName);
        opLog.setMethod(joinPoint.getSignature().toShortString());
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                opLog.setParams(objectMapper.writeValueAsString(args));
            }
        } catch (Exception e) {
            opLog.setParams("序列化参数失败");
        }
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            opLog.setIp(request.getRemoteAddr());
        }
        opLog.setResult(result);
        opLog.setDuration(duration);
        opLog.setCreatedAt(LocalDateTime.now());
        operationLogService.saveLog(opLog);
    }
}
