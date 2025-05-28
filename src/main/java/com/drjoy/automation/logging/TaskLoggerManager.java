package com.drjoy.automation.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class TaskLoggerManager {
    private static final String TASK_ID_KEY = "taskId";
    private static final String logDir = System.getProperty("user.dir").concat("/resources/selenium/logs");

    public static String generateTaskId(String prefix) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String shortUUID = UUID.randomUUID().toString().substring(0, 5);
        return prefix + "_" + timestamp + "_" + shortUUID;
    }

    public static void init(String taskId) {
        MDC.put(TASK_ID_KEY, taskId);
    }
    
    public static void openLogTail(String taskId) {
        String path = logDir + "/task-" + taskId + ".log";
        long timeOutMillis = 30000L;
        CompletableFuture.runAsync(() -> {
            Path log = Paths.get(path);
            if (Files.exists(log)) {
                return;
            }
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                log.getParent().register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
                long startTime = System.currentTimeMillis();
                
                while (true) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    long remaining = timeOutMillis - elapsed;
                    if (remaining <= 0) {
                        throw new RuntimeException("Timed out waiting for log file: " + log);
                    }
                    WatchKey key;
                    try {
                        key = watchService.poll(remaining, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // Restore interrupt status
                        throw new RuntimeException("Thread interrupted while waiting for file", e);
                    }

                    if (key == null) {
                        // Timeout occurred
                        throw new RuntimeException("Timed out waiting for log file: " + log);
                    }
                    
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            continue; // Skip and keep watching
                        }

                        @SuppressWarnings("unchecked")
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path createdFile = ev.context();

                        if (createdFile.getFileName().toString().equals(log.getFileName().toString())) {
                            System.out.println("Log file appeared: " + log);
                            return;
                        }
                    }

                    boolean valid = key.reset();
                    if (!valid) {
                        throw new RuntimeException("WatchKey no longer valid. Directory may be inaccessible.");
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("IO error while setting up WatchService", e);
            }
        }).thenAccept(unused -> {
            try {
                Runtime.getRuntime().exec("cmd /c start powershell.exe -NoExit \"Get-Content " + path +  " -Wait -Tail 30\"");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
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

    public static void error(String message, Throwable t, Object... args) {
        getLogger().error(message, args);
        getLogger().error("DETAILS: ", t);
    }

    public static void debug(String message, Object... args) {
        getLogger().debug(message, args);
    }
}

