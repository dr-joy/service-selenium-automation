package com.drjoy.automation.service;

import com.drjoy.automation.controller.request.ATTaskRequest;
import com.drjoy.automation.logging.TaskLoggerManager;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MCPAutomationService {
    private final SeleniumAutomationService seleniumAutomationService;

    @Autowired
    public MCPAutomationService(SeleniumAutomationService seleniumAutomationService) {
        this.seleniumAutomationService = seleniumAutomationService;
    }

    @Tool(name = "processAttendanceSteps", description = "Thực hiện chạy tiến trình automation tính lương")
    public String processAttendanceSteps(int phaseStart, int phaseEnd,
                                     boolean removeAllCheckingLog,
                                     boolean addAllWorkingTimeType,
                                     boolean addAllPreset,
                                     boolean addWorkSchedule,
                                     boolean addAllCheckingLogs,
                                     boolean approveAllRequest,
                                     boolean rejectAllRequest,
                                     boolean removeAllDownloadTemplate,
                                     boolean createNewDownloadTemplate,
                                     boolean downloadTemplate) {
        ATTaskRequest request = new ATTaskRequest();
        request.setPhaseStart(phaseStart);
        request.setPhaseEnd(phaseEnd);
        request.setRemoveAllCheckingLog(removeAllCheckingLog);
        request.setAddAllWorkingTimeType(addAllWorkingTimeType);
        request.setAddAllPreset(addAllPreset);
        request.setAddWorkSchedule(addWorkSchedule);
        request.setAddAllCheckingLogs(addAllCheckingLogs);
        request.setApproveAllRequest(approveAllRequest);
        request.setRejectAllRequest(rejectAllRequest);
        request.setRemoveAllDownloadTemplate(removeAllDownloadTemplate);
        request.setCreateNewDownloadTemplate(createNewDownloadTemplate);
        request.setDownloadTemplate(downloadTemplate);

        String taskId = TaskLoggerManager.generateTaskId("attendance");
        seleniumAutomationService.processAttendanceSteps(request, taskId);
        return taskId;
    }
} 