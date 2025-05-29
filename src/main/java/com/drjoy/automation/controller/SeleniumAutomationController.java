package com.drjoy.automation.controller;

import com.drjoy.automation.controller.request.ATTaskRequest;
import com.drjoy.automation.controller.request.ATTeireiRequest;
import com.drjoy.automation.controller.response.ATTaskResponse;
import com.drjoy.automation.logging.TaskLoggerManager;
import com.drjoy.automation.service.SeleniumAutomationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SeleniumAutomationController {
    private final SeleniumAutomationService seleniumAutomationService;

    @Autowired
    public SeleniumAutomationController(SeleniumAutomationService seleniumAutomationService) {
        this.seleniumAutomationService = seleniumAutomationService;
    }

    @PostMapping(value = "/selenium/process_attendance_steps")
    public ResponseEntity<ATTaskResponse> processAttendanceSteps(@RequestBody ATTaskRequest request) {
        String taskId = TaskLoggerManager.generateTaskId("attendance");

        seleniumAutomationService.processAttendanceSteps(request, taskId);
        return ResponseEntity.ok(new ATTaskResponse(taskId));
    }

    @PostMapping(value = "/selenium/teirei-screens")
    public ResponseEntity<ATTaskResponse> processAttendanceSteps1(@RequestBody ATTeireiRequest request) throws ClassNotFoundException {
        String taskId = TaskLoggerManager.generateTaskId("attendance");

        seleniumAutomationService.processTeireiScreen(request, taskId);
        return ResponseEntity.ok(new ATTaskResponse(taskId));
    }
}
