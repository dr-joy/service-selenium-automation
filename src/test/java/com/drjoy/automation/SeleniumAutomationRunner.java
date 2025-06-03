package com.drjoy.automation;

import com.drjoy.automation.controller.request.ATTaskRequest;
import com.drjoy.automation.execution.StaticExecutionRunner;
import com.drjoy.automation.execution.phase.PhaseProcessor;
import com.drjoy.automation.logging.TaskLoggerManager;
import com.drjoy.automation.model.setting.ExportTemplateFilterSetting;
import com.drjoy.automation.repository.ExcelReaderRepository;
import com.drjoy.automation.service.AttendanceService;
import com.drjoy.automation.service.SeleniumAutomationService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SeleniumAutomationRunner {

    private final SeleniumAutomationService seleniumAutomationService;

    @Autowired
    public SeleniumAutomationRunner(SeleniumAutomationService seleniumAutomationService) {
        this.seleniumAutomationService = seleniumAutomationService;
    }

    public static void main(String[] args) {
        // Arrange
        ATTaskRequest request = new ATTaskRequest();
        request.setPhaseStart(1);
        request.setPhaseEnd(1);
        request.setAddWorkSchedule(false);
        request.setRemoveAllCheckingLog(true);
        request.setAddAllCheckingLogs(true);
        request.setApproveAllRequest(true);
        request.setRejectAllRequest(false);
        request.setRemoveAllDownloadTemplate(false);
        request.setCreateNewDownloadTemplate(false);
        request.setDownloadTemplate(false);

        String taskId = TaskLoggerManager.generateTaskId("attendance");

        TaskLoggerManager.init(taskId);
        int phaseStart = request.getPhaseStart();
        int phaseEnd = request.getPhaseEnd();
        List<ExportTemplateFilterSetting> allSettings = ExcelReaderRepository.findAllExportFilterSetting();

        if (phaseStart < 1 || phaseEnd > allSettings.size() || phaseStart > phaseEnd) {
            throw new IllegalArgumentException("Invalid phase range");
        }

        List<ExportTemplateFilterSetting> selectedSettings = IntStream.rangeClosed(phaseStart, phaseEnd)
            .mapToObj(i -> allSettings.get(i - 1)) // i - 1 vì danh sách index 0-based
            .collect(Collectors.toList());

        List<String> orderedSteps = Lists.newArrayList();
        if (request.isAddWorkSchedule())      orderedSteps.add("addWorkSchedule");
        if (request.isRemoveAllCheckingLog()) orderedSteps.add("removeCheckingLog");
        if (request.isAddAllCheckingLogs())   orderedSteps.add("addCheckingLogs");
        if (request.isApproveAllRequest())    orderedSteps.add("approveRequests");
        if (request.isRejectAllRequest())     orderedSteps.add("rejectRequests");
        if (request.isRemoveAllDownloadTemplate())     orderedSteps.add("removeAllDownloadTemplate");
        if (request.isCreateNewDownloadTemplate())     orderedSteps.add("createNewDownloadTemplate");
        if (request.isDownloadTemplate())     orderedSteps.add("downloadTemplate");

        PhaseProcessor.process(selectedSettings, setting ->
            StaticExecutionRunner.runSteps(AttendanceService.class, setting, orderedSteps)
        );
    }
}
