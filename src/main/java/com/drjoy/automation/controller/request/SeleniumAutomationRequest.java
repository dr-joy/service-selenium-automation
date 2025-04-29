package com.drjoy.automation.controller.request;

import com.drjoy.automation.constants.Action;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SeleniumAutomationRequest {
    private boolean removeAllCheckingLog;
    private boolean addAllWorkingTimeType;
    private boolean addAllPreset;
    private boolean addWorkSchedule;
    private boolean addAllCheckingLogs;
    private boolean approveAllRequest;
    private boolean rejectAllRequest;
    private boolean removeAllDownloadTemplate;
    private boolean createNewDownloadTemplate;
    private boolean downloadTemplate;
}
