package com.drjoy.automation.execution.steps;

import com.drjoy.automation.execution.Execution;
import com.drjoy.automation.execution.phase.PhaseSetting;
import com.drjoy.automation.model.ExportTemplateFilterSetting;
import com.drjoy.automation.service.AttendanceService;
import com.drjoy.automation.execution.Step;
import org.apache.poi.ss.formula.functions.T;

public class AddCheckingLogs implements Execution<PhaseSetting> {
    @Override
    public void run(PhaseSetting setting) throws Exception {
        AttendanceService.removeAllCheckingLogInTimeSheetPage((ExportTemplateFilterSetting) setting);
        AttendanceService.addAllCheckingLogs((ExportTemplateFilterSetting) setting);
    }
}
