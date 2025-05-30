package com.drjoy.automation.execution.phase;

import com.drjoy.automation.config.DriverFactory;
import com.drjoy.automation.execution.Execution;
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

            log.info("Processing phase {}: {}", i + 1, username);

            // Attempt login and execute with retries
            if (loginIfNeeded(username, password, previousUsername)) {
                executePhaseWithRetry(execution, current, i);
            }
        }

        LoginService.logout();
    }

    // Check login and handle login/logout if needed
    private static boolean loginIfNeeded(String username, String password, String previousUsername) {
        if (!username.equals(previousUsername)) {
            try {
                LoginService.login(username, password);
                log.info("Logged in as {}", username);
                return true; // Login successful
            } catch (Exception e) {
                log.error("Login failed for user {}: {}", username, e.getMessage());
                return false; // Login failed
            }
        }
        return true; // Already logged in
    }

    // Execute phase with retry logic
    private static <T extends PhaseSetting> void executePhaseWithRetry(Execution<T> execution, T current, int phaseIndex) {
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                execution.run(current);
                log.info("Phase {} completed successfully", phaseIndex + 1);
                break; // Exit after successful execution
            } catch (Exception e) {
                log.error("Phase {} attempt {}/2 failed: {}", phaseIndex + 1, attempt, e.getMessage(), e);
                if (attempt == 2) {
                    log.error("Phase {} failed after 2 attempts", phaseIndex + 1);
                } else {
                    log.info("Retrying phase {}...", phaseIndex + 1);
                }
            }
        }
    }
}
