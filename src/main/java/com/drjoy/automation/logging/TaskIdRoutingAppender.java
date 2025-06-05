package com.drjoy.automation.logging;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.FileAppender;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TaskIdRoutingAppender extends AppenderBase<ILoggingEvent> {
    private final Map<String, FileAppender<ILoggingEvent>> appenders = new ConcurrentHashMap<>();

    private final String logDir = System.getProperty("user.dir").concat("/resources/selenium/logs");

    @Override
    protected void append(ILoggingEvent event) {
        String taskId = event.getMDCPropertyMap().get("taskId");
        if (taskId == null) return; // hoặc log vào file mặc định

        FileAppender<ILoggingEvent> appender = appenders.computeIfAbsent(taskId, id -> {
            FileAppender<ILoggingEvent> fa = new FileAppender<>();
            fa.setName("TASK_" + id);
            fa.setFile(logDir + "/task-" + id + ".log");

            PatternLayoutEncoder encoder = new PatternLayoutEncoder();
            encoder.setContext(getContext());
            encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level - %msg%n");
            encoder.start();

            fa.setEncoder(encoder);
            fa.setContext(getContext());
            fa.start();
            return fa;
        });

        appender.doAppend(event);
    }
}

