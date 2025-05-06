package com.drjoy.automation.service;

import com.drjoy.automation.controller.request.ATTaskRequest;
import com.drjoy.automation.execution.StaticExecutionRunner;
import com.drjoy.automation.execution.phase.PhaseProcessor;
import com.drjoy.automation.logging.TaskLoggerManager;
import com.drjoy.automation.model.ExportTemplateFilterSetting;
import com.drjoy.automation.repository.ExcelReaderRepository;
import com.google.common.collect.Lists;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class SeleniumAutomationService {

    @Async
    public void processAttendanceSteps(ATTaskRequest request, String taskId) {
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

        PhaseProcessor.process(selectedSettings, setting ->
            StaticExecutionRunner.runSteps(AttendanceService.class, setting, orderedSteps)
        );
    }
}

