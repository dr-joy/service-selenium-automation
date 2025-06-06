package com.drjoy.automation.execution;

import com.drjoy.automation.config.Configuration;
import com.drjoy.automation.config.DriverFactory;
import com.drjoy.automation.execution.phase.PhaseSetting;
import com.drjoy.automation.logging.TaskLoggerManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import com.drjoy.automation.utils.WebUI;
import com.drjoy.automation.utils.xpath.common.XpathCommon;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@Slf4j
public class StaticExecutionRunner {

    private static final Map<Class<?>, Map<String, Method>> methodMapCache = new HashMap<>();
    private static final int TOTAL_ATTEMPT = 2;

    private StaticExecutionRunner() {}

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

                    // Xử lý confirm popup nếu có
                    int retry = 0;
                    while (retry < 5) {
                        WebElement confirmBtn = WebUI.waitForElementClickable(By.xpath(XpathCommon.MODAL_CONFIRM_BTN.value), 1);
                        if (confirmBtn == null) break;
                        confirmBtn.click();
                        retry++;
                    }
                }
                ExecutionContext.clear();
            }
        }
    }

    private static void detectUnexpectedError() {
        boolean isNotFound = WebUI.isMeetNotFoundPage();
        boolean isError = WebUI.isMeetErrorPage();
        if (isNotFound) {
            TaskLoggerManager.error("Detected NOT FOUND page!");
        } else if (isError) {
            TaskLoggerManager.error("Page is ERROR, maybe upstream or other reason. Please check it out!");
        }

        // Reload page when the screen locked
        if (isError || isNotFound) {
            WebDriver driver = DriverFactory.getDriver();
            String targetURL = Configuration.getBaseUrl();

            driver.get(targetURL);
            WebUI.waitForConditionSucceed(driver.getCurrentUrl().equals(targetURL));

            WebUI.waitForLoadingElement();
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

