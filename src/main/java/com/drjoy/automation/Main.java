package com.drjoy.automation;

import com.drjoy.automation.service.AttendanceService;
import com.drjoy.automation.utils.AttendanceUtils;

public class Main {
    public static void main(String[] args) {
        AttendanceUtils.processPhases(1, 1, setting -> {
            AttendanceService.removeAllCheckingLogInTimeSheetPage(setting);
            AttendanceService.addAllCheckingLogs(setting);
        });
    }
}