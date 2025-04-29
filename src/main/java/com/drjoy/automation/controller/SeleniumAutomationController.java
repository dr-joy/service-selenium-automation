package com.drjoy.automation.controller;

import com.drjoy.automation.controller.request.SeleniumAutomationRequest;
import com.drjoy.automation.execution.phase.PhaseProcessor;
import com.drjoy.automation.model.ExportTemplateFilterSetting;
import com.drjoy.automation.repository.ExcelReaderRepository;
import com.drjoy.automation.service.AttendanceService;
import com.drjoy.automation.service.SeleniumAutomationService;
import com.drjoy.automation.utils.AttendanceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@RestController
@RequestMapping("/api")
public class SeleniumAutomationController {
    private final SeleniumAutomationService seleniumAutomationService;

    @Autowired
    public SeleniumAutomationController(SeleniumAutomationService seleniumAutomationService) {
        this.seleniumAutomationService = seleniumAutomationService;
    }

    @PostMapping(value = "/selenium/create_month_payroll_data")
    public ResponseEntity<Void> createBeaconStayLogCsv(@RequestBody SeleniumAutomationRequest request) {
        seleniumAutomationService.processSeleniumAutomation(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
