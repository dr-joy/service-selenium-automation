package com.drjoy.automation.execution;

public interface Step {
    void execute() throws Exception;
    void rollback();
}
