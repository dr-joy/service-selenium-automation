package com.drjoy.automation.execution;

import com.drjoy.automation.logging.TaskLoggerManager;

public class ExecutionHelper {
    public static void runStepWithLogging(String stepName, Runnable stepLogic) {
        ExecutionContext.pushStep(stepName);
        TaskLoggerManager.info("▶ {}", ExecutionContext.getStepTrace());
        stepLogic.run();
        ExecutionContext.popStep();
    }
}

