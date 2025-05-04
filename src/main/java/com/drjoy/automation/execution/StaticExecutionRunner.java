package com.drjoy.automation.execution;

import com.drjoy.automation.logging.TaskLoggerManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StaticExecutionRunner {

    private static final Map<Class<?>, Map<String, Method>> methodMapCache = new HashMap<>();

    public static void runSteps(Class<?> serviceClass, Object setting, List<String> orderedStepNames) {
        Map<String, Method> stepMethodMap = getCachedStepMethods(serviceClass, setting);

        for (String stepName : orderedStepNames) {
            Method method = stepMethodMap.get(stepName);
            if (method == null) continue;

            try {
                ExecutionContext.pushStep(stepName);
                TaskLoggerManager.info("▶ Running step: " + stepName);
                method.invoke(null, setting);
                ExecutionContext.popStep();
            } catch (InvocationTargetException e) {
                TaskLoggerManager.error("Detail: {}", e.getCause().getMessage());
                TaskLoggerManager.error("❌ Lỗi ở bước: {}", stepName);
                TaskLoggerManager.error("▶ Stack logic: " + String.join(" > ", ExecutionContext.getStepTrace()));
                ExecutionContext.clear();
                break;
            } catch (Exception e) {
                TaskLoggerManager.error("❌ Lỗi ở bước: {}", stepName);
                TaskLoggerManager.error("▶ Stack logic: " + String.join(" > ", ExecutionContext.getStepTrace()));
                TaskLoggerManager.error("Detail: {}", e.getCause().getMessage());
                ExecutionContext.clear();
                break;
            }
        }

        ExecutionContext.clear(); // sau cùng, dọn dẹp stack
    }

    public static void runStepWithLogging(String stepName, Runnable stepLogic) {
        ExecutionContext.pushStep(stepName);
        TaskLoggerManager.info("▶ Running step: " + stepName);

        stepLogic.run();
        ExecutionContext.popStep();
    }

    private static Map<String, Method> getCachedStepMethods(Class<?> serviceClass, Object setting) {
        return methodMapCache.computeIfAbsent(serviceClass, cls -> {
            Map<String, Method> map = new HashMap<>();
            for (Method method : cls.getDeclaredMethods()) {
                if (Modifier.isStatic(method.getModifiers())
                        && method.isAnnotationPresent(ExecutionStep.class)
                        && method.getParameterCount() == 1
                        && method.getParameterTypes()[0].isAssignableFrom(setting.getClass())) {
                    String name = method.getAnnotation(ExecutionStep.class).value();
                    map.put(name, method);
                }
            }
            return map;
        });
    }
}

