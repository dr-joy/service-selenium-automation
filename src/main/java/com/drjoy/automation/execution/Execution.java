package com.drjoy.automation.execution;

import com.drjoy.automation.execution.phase.PhaseSetting;

@FunctionalInterface
public interface Execution<T extends PhaseSetting> {
    void run(T setting) throws Exception;
}
