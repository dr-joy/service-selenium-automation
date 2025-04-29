package com.drjoy.automation.service;

import com.drjoy.automation.controller.request.SeleniumAutomationRequest;
import com.drjoy.automation.execution.phase.PhaseProcessor;
import com.drjoy.automation.execution.phase.PhaseSetting;
import com.drjoy.automation.model.ExportTemplateFilterSetting;
import com.drjoy.automation.repository.ExcelReaderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class SeleniumAutomationService {

    public void processSeleniumAutomation(SeleniumAutomationRequest request) {
        int phaseStart = request.getPhaseStart();
        int phaseEnd = request.getPhaseEnd();
        List<ExportTemplateFilterSetting> allSettings = ExcelReaderRepository.findAllExportFilterSetting();

        if (phaseStart < 1 || phaseEnd > allSettings.size() || phaseStart > phaseEnd) {
            throw new IllegalArgumentException("Invalid phase range");
        }

        List<ExportTemplateFilterSetting> selectedSettings = IntStream.rangeClosed(phaseStart, phaseEnd)
            .mapToObj(i -> allSettings.get(i - 1)) // i - 1 vì danh sách index 0-based
            .collect(Collectors.toList());

        // Sử dụng PhaseProcessor để thực thi các hành động đã chuẩn bị
        PhaseProcessor.process(selectedSettings, setting -> {
            if (request.isRemoveAllCheckingLog()) {
                AttendanceService.removeAllCheckingLogInTimeSheetPage(setting);
            }
            if (request.isAddAllWorkingTimeType()) {
                //AttendanceService.addAllWorkingTimeType(setting);
            }
            if (request.isAddAllPreset()) {
                //AttendanceService.addAllPreset(setting);
            }
            if (request.isAddWorkSchedule()) {
                AttendanceService.addWorkSchedule(setting);
            }
            if (request.isAddAllCheckingLogs()) {
                AttendanceService.addAllCheckingLogs(setting);
            }
            if (request.isApproveAllRequest()) {
                AttendanceService.approveAllRequest(setting);
            }
            if (request.isRejectAllRequest()) {
                AttendanceService.rejectAllRequest(setting);
            }
            if (request.isRemoveAllDownloadTemplate()) {
                //AttendanceService.removeAllDownloadTemplate(setting);
            }
            if (request.isCreateNewDownloadTemplate()) {
                //AttendanceService.createNewDownloadTemplate(setting);
            }
            if (request.isDownloadTemplate()) {
                //AttendanceService.downloadTemplate(setting);
            }
        });
    }
}

