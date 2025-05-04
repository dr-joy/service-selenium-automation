package com.drjoy.automation.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class TaskLoggerManager {
    private static final String TASK_ID_KEY = "taskId";

    public static String generateTaskId(String prefix) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String shortUUID = UUID.randomUUID().toString().substring(0, 5);
        String taskId =  prefix + "_" + timestamp + "_" + shortUUID;

        return taskId;
    }

    public static void init(String taskId) {
        MDC.put(TASK_ID_KEY, taskId);
    }

    public static void clear() {
        MDC.clear();
    }

    public static Logger getLogger() {
        return LoggerFactory.getLogger("taskLogger");
    }

    public static void info(String message, Object... args) {
        getLogger().info(message, args);
    }

    public static void error(String message, Object... args) {
        getLogger().error(message, args);
    }

    public static void debug(String message, Object... args) {
        getLogger().debug(message, args);
    }
}

