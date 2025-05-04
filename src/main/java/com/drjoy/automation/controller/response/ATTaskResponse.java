package com.drjoy.automation.controller.response;

public class ATTaskResponse {
    private String taskId;

    public ATTaskResponse(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}
