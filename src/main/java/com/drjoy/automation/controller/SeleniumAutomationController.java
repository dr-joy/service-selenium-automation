package com.drjoy.automation.controller;

import com.drjoy.automation.controller.request.SeleniumAutomationRequest;
import com.drjoy.automation.service.AttendanceService;
import com.drjoy.automation.utils.AttendanceUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SeleniumAutomationController {

    @PostMapping(value = "/selenium/automation")
    public ResponseEntity<Void> createBeaconStayLogCsv(@RequestBody SeleniumAutomationRequest request) {
        AttendanceUtils.processPhases(1, 1, setting -> {
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
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
