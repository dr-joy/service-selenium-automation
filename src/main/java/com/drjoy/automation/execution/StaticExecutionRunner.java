package com.drjoy.automation.execution;

import com.drjoy.automation.execution.phase.PhaseSetting;
import com.drjoy.automation.logging.TaskLoggerManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import com.drjoy.automation.utils.WebUI;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StaticExecutionRunner {

    private static final Map<Class<?>, Map<String, Method>> methodMapCache = new HashMap<>();
    private static final int TOTAL_ATTEMPT = 2;

    public static void runSteps(Class<?> serviceClass, PhaseSetting setting, List<String> orderedStepNames) {
        Map<String, Method> stepMethodMap = getCachedStepMethods(serviceClass, setting);

        for (String stepName : orderedStepNames) {
            Method method = stepMethodMap.get(stepName);
            if (method == null) continue;

            executeStepWithRetry(stepName, method, setting);
        }

        // sau cùng, dọn dẹp stack
        ExecutionContext.clear();
    }

    private static void executeStepWithRetry(String stepName, Method method, PhaseSetting setting) {
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                ExecutionContext.pushStep(stepName);
                TaskLoggerManager.info("[{}] Running step: {}", setting.getPhase(), stepName);
                method.invoke(null, setting);
                ExecutionContext.popStep();

                break;
            } catch (Exception e) {
                TaskLoggerManager.error("Error in step: {}", stepName);
                TaskLoggerManager.error("=> Stack logic: " + String.join(" > ", ExecutionContext.getStepTrace()));
                TaskLoggerManager.error("Detail: {}", e.getCause().getMessage());

                detectUnexpectedError();

                TaskLoggerManager.error("Phase {} - Step {}: attempt {}/{} failed", setting.getPhase(), stepName, attempt, TOTAL_ATTEMPT);
                if (attempt == 2) {
                    TaskLoggerManager.error("Phase {} failed after {} attempts", setting.getPhase(), TOTAL_ATTEMPT);
                } else {
                    TaskLoggerManager.info("Retrying step {}...", stepName);
                }
                ExecutionContext.clear();
            }
        }
    }

    private static void detectUnexpectedError() {
        if (WebUI.isMeetNotFoundPage()) {
            TaskLoggerManager.error("Detected NOT FOUND page!");
        } else if (WebUI.isMeetErrorPage()) {
            TaskLoggerManager.error("Page is ERROR, maybe upstream or other reason. Please try again later!");
        }
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

