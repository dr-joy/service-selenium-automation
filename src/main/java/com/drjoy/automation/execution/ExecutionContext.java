package com.drjoy.automation.execution;

import java.util.*;

public class ExecutionContext {
    private static final ThreadLocal<Deque<String>> stepStack = ThreadLocal.withInitial(ArrayDeque::new);

    public static void pushStep(String step) {
        stepStack.get().push(step);
    }

    public static void popStep() {
        stepStack.get().pop();
    }

    public static String getCurrentStep() {
        return stepStack.get().peek();
    }

    public static List<String> getStepTrace() {
        List<String> trace = new ArrayList<>(stepStack.get());
        Collections.reverse(trace);
        return trace;
    }

    public static void clear() {
        stepStack.get().clear();
    }
}

