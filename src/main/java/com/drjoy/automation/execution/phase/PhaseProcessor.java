package com.drjoy.automation.execution.phase;

import com.drjoy.automation.config.DriverFactory;
import com.drjoy.automation.execution.Execution;
import com.drjoy.automation.logging.TaskLoggerManager;
import com.drjoy.automation.service.LoginService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class PhaseProcessor {

    public static <T extends PhaseSetting> void process(List<T> phaseSetting, Execution<T> execution) {
        String previousUsername = "";

        for (int i = 0; i < phaseSetting.size(); i++) {
            T current = phaseSetting.get(i);
            String username = current.getUserName();
            String password = current.getPassword();

            TaskLoggerManager.info("Processing phase {}: {}", current.getPhase(), username);

            // Attempt login and execute with retries
            if (loginIfNeeded(username, password, previousUsername)) {
                executePhaseWithRetry(execution, current);

                previousUsername = username;
            }
        }

        LoginService.logout();
    }

    // Check login and handle login/logout if needed
    private static boolean loginIfNeeded(String username, String password, String previousUsername) {
//        TaskLoggerManager.info(DriverFactory.getCACHE_PATH());
        if (!username.equals(previousUsername)) {
            try {
                LoginService.login(username, password);
                TaskLoggerManager.info("Logged in as {}", username);
                return true; // Login successful
            } catch (Exception e) {
                TaskLoggerManager.error("Login failed for user: {}", e, username);
                return false; // Login failed
            }
        }
        return true; // Already logged in
    }

    // Execute phase with retry logic
    private static <T extends PhaseSetting> void executePhaseWithRetry(Execution<T> execution, T current) {
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                execution.run(current);
                TaskLoggerManager.info("Phase {} completed successfully", current.getPhase());
                break; // Exit after successful execution
            } catch (Exception e) {
                TaskLoggerManager.error("Phase {} attempt {}/2 failed: {}", current.getPhase(), attempt, e.getMessage(), e);
                if (attempt == 2) {
                    TaskLoggerManager.error("Phase {} failed after 2 attempts", current.getPhase());
                } else {
                    TaskLoggerManager.info("Retrying phase {}...", current.getPhase());
                }
            }
        }
    }
}
