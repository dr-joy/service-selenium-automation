package com.drjoy.automation.service;

import com.drjoy.automation.controller.request.ATTaskRequest;
import com.drjoy.automation.controller.request.ATTeireiRequest;
import com.drjoy.automation.execution.StaticExecutionRunner;
import com.drjoy.automation.execution.phase.PhaseProcessor;
import com.drjoy.automation.logging.TaskLoggerManager;
import com.drjoy.automation.model.setting.ExportTemplateFilterSetting;
import com.drjoy.automation.model.setting.TeireiSetting;
import com.drjoy.automation.repository.ExcelReaderRepository;
import com.google.common.collect.Lists;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class SeleniumAutomationService {
    @Value("${powershell-log:false}")
    private boolean isLogPowerShell;

    @Async
    public void processAttendanceSteps(ATTaskRequest request, String taskId) {
        TaskLoggerManager.init(taskId);
        if (isLogPowerShell) {
            TaskLoggerManager.openLogTail(taskId);
        }
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

    @Async
    public void processTeireiScreen(ATTeireiRequest request, String taskId) throws ClassNotFoundException {
        TaskLoggerManager.init(taskId);
        int phaseStart = request.getPhaseStart();
        int phaseEnd = request.getPhaseEnd();
        List<TeireiSetting> allSettings = ExcelReaderRepository.findAllTeireiSetting();

        if (phaseStart < 1 || phaseEnd > allSettings.size() || phaseStart > phaseEnd) {
            throw new IllegalArgumentException("Invalid phase range");
        }

        List<TeireiSetting> selectedSettings = IntStream.rangeClosed(phaseStart, phaseEnd)
                .mapToObj(i -> allSettings.get(i - 1)) // i - 1 vì danh sách index 0-based
                .collect(Collectors.toList());

        var className = "com.drjoy.automation.service.".concat(request.getScreen()).concat("Service");
        handle(Class.forName(className), selectedSettings);
    }

    private static <T> void handle(Class<T> clazz, List<TeireiSetting> selectedSettings) {
        var orderedSteps = switch (clazz.getSimpleName()) {
            // DOING
            case "AT0021Service" -> getAt0021Methods();
            case "AT0024Service" -> getAt0024Methods();
            case "AT0026Service" -> getAt0026Methods();
            case "AT0029Service" -> getAt0029Methods();
            // DONE
            case "AT0037Service" -> getAt0037Methods();
            case "AT0038Service" -> getAt0038Methods();
            case "AT0047Service" -> getAt0047Methods();
            case "AT0048Service" -> getAt0048Methods();
            case "AT0049Service" -> getAt0049Methods();
            case "AT0050Service" -> getAt0050Methods();
            case "AT0051Service" -> getAt0051Methods();
            case "AT0052Service" -> getAt0052Methods();
            case "AT0053Service" -> getAt0053Methods();
            case "AT0064Service" -> getAt0064Methods();
            case "AT0065Service" -> getAt0065Methods();
            default -> new ArrayList<String>();
        };
        PhaseProcessor.process(selectedSettings, setting ->
                StaticExecutionRunner.runSteps(clazz, setting, orderedSteps)
        );
    }

    private static List<String> getAt0021Methods() {
        return List.of("filterRequestsOnAT0021");
    }

    private static List<String> getAt0024Methods() {
        return List.of("createSingleDayRequestOnAT0024B", "createPeriodRequestOnAT0024B");
    }

    private static List<String> getAt0026Methods() {
        return List.of("editDayOffRequestReason", "addDayOffReason", "editOvertimeReason", "editResearchReason", "editWatchReason", "editDayOffWorkingReason", "editPreOvertimeReason", "editOtherReason");
    }

    private static List<String> getAt0029Methods() {
        return List.of("exportLeaveBalance");
    }

    private static List<String> getAt0037Methods() {
        return List.of("exportCSV");
    }

    private static List<String> getAt0038Methods() {
        return List.of(
                "editUserDetailAndSave",
                "editUserDetailCol3AndSave"
        );
    }

    private static List<String> getAt0047Methods() {
        return List.of(
                "AT_AT0047_1_2_5",
                "AT_AT0047_1_3_6",
                "half_time"
        );
    }

    private static List<String> getAt0048Methods() {
        return List.of("electUserForTimeOff");
    }

    private static List<String> getAt0049Methods() {
        return List.of(
                "searchByCurrentUsername",
                "clickVacationHistoryButton"
        );
    }

    private static List<String> getAt0050Methods() {
        return List.of(
                "grantLeaveType1",
                "grantLeaveType2"
        );
    }

    private static List<String> getAt0051Methods() {
        return List.of(
                "viewLeaveHistory",
                "editDayOffAllocation",
                "confirmFirstLeaveHistoryRow"
        );
    }

    private static List<String> getAt0052Methods() {
        return List.of(
                "executeAT0052Flow",
                "runHourlyPaidLeaveManagement"
        );
    }

    private static List<String> getAt0053Methods() {
        return List.of(
                "checkForgetRequestOptions",
                "checkLack5DayOffOptions"
        );
    }

    private static List<String> getAt0064Methods() {
        return List.of(
                "searchAndClickUserDetail"
        );
    }

    private static List<String> getAt0065Methods() {
        return List.of(
                "editUserDetailCol1AndSave1",
                "editUserDetailCol1AndSave2"
        );
    }
}

