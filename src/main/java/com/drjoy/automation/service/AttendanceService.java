package com.drjoy.automation.service;

import com.drjoy.automation.config.DriverFactory;
import com.drjoy.automation.constants.AttendanceConstants;
import com.drjoy.automation.execution.ExecutionHelper;
import com.drjoy.automation.execution.ExecutionStep;
import com.drjoy.automation.logging.TaskLoggerManager;
import com.drjoy.automation.model.CheckingLog;
import com.drjoy.automation.model.DownloadTemplate;
import com.drjoy.automation.model.setting.ExportTemplateFilterSetting;
import com.drjoy.automation.model.Request;
import com.drjoy.automation.model.WorkSchedule;
import com.drjoy.automation.repository.ExcelReaderRepository;
import com.drjoy.automation.utils.AttendanceUtils;
import com.drjoy.automation.utils.DateUtils;
import com.drjoy.automation.utils.WebUI;
import com.drjoy.automation.utils.xpath.common.XpathCommon;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.drjoy.automation.utils.AttendanceUtils.waitForLoadingElement;
import static com.drjoy.automation.utils.AttendanceUtils.waitForLoadingOverlayElement;
import static java.lang.String.format;

@Service
public class AttendanceService {

    @ExecutionStep(value = "removeCheckingLog")
    public static void removeAllCheckingLogInTimeSheetPage(ExportTemplateFilterSetting setting) {
        ExecutionHelper.runStepWithLoggingByPhase(setting, "Navigate to AT0001",
            () -> AttendanceUtils.navigateToATPage("at0001")
        );
        waitForLoadingElement();

        AttendanceUtils.selectUserAndMonthOnTimesheetPage(setting);

        String xpathRowAt0001 = "//app-at0001//div[@id='tbl-sheet']/table/tbody//tr";
        List<WebElement> dayElements = WebUI.findWebElementsIfPresent(By.xpath(xpathRowAt0001));

        for (int i = 0; i < dayElements.size(); i++) {
            String xpathRow = format("%s[%d]", xpathRowAt0001, i + 1);

            WebElement startTimeCol = WebUI.findWebElementIfPresent(By.xpath(xpathRow + "/td[4]"));
            String startTimeValue = startTimeCol.getText();
            if (startTimeValue == null || startTimeValue.isEmpty()) {
                continue;
            }

            // Thực hiện bỏ chấm công cho từng ngày
            WebElement editCheckingLogButton = WebUI.findWebElementIfVisible(By.xpath(xpathRow + "/td[last()-1]/button"));
            editCheckingLogButton.click();
            waitForLoadingElement();

            // AT0023: Remove checking logs
            ExecutionHelper.runStepWithLoggingByPhase(setting,"Remove all checking logs in AT0023",
                AttendanceService::removeAllCheckingLogInPage
            );

            waitForLoadingOverlayElement();

            // Quay lại AT0001
            WebElement backButton = WebUI.findWebElementIfVisible(By.xpath(XpathCommon.PAGE_BACK_BTN.value));
            backButton.click();
        }
    }

    public static void removeAllCheckingLogInPage() {
        String acceptCheckingLogButtonXpath = "//app-at0023//form[@id='checking-log']//table//tbody/tr[1]/td[last()-1]//button[normalize-space(text())='確定']";

        waitForLoadingElement();
        WebUI.sleep(200);
        if (WebUI.waitForElementPresent(By.xpath(acceptCheckingLogButtonXpath), 1) == null) {
            WebUI.sleep(200);
            WebElement requestButton = WebUI.findWebElementIfVisible(By.xpath("//*[@id='checking-log']/div/table/tbody/tr[1]/td[6]/div/button"));
            WebUI.clickByJS(requestButton);

            waitForLoadingElement();
            while (WebUI.waitForElementPresent(By.xpath(XpathCommon.MODAL_CONFIRM_WITH_JP_TEXT_BTN.value), 2) != null) {
                WebElement confirmButton = WebUI.findWebElementIfVisible(By.xpath(XpathCommon.MODAL_CONFIRM_WITH_JP_TEXT_BTN.value));
                WebUI.sleep(200);
                WebUI.clickByJS(confirmButton);
                waitForLoadingElement();
            }
        }

        List<WebElement> checkingLogsElement = WebUI.findWebElementsIfVisible(By.xpath("//app-at0023//form[@id='checking-log']//table//tbody/tr"));

        for (WebElement el : checkingLogsElement) {
            WebElement buttonDeleteCheckingLog = WebUI.findWebElementIfVisible(By.xpath(
                "//app-at0023//form[@id='checking-log']//table//tbody/tr[1]/td[7]/button"
            ));
            buttonDeleteCheckingLog.click();

            WebElement confirmButton = WebUI.findWebElementIfVisible(By.xpath(XpathCommon.MODAL_CONFIRM_WITH_JP_TEXT_BTN.value));
            confirmButton.click();
        }

        try {
            WebElement submitButton = WebUI.findWebElementIfVisible(By.xpath("//*[@id='checking-log']//button[normalize-space(text())='確定']"));
            submitButton.click();

            WebElement reEditButton = WebUI.findWebElementIfVisible(By.xpath(
                "//app-at0023//button[contains(@class, 'btn-edit-del') and normalize-space(text())='編集']"
            ));
            reEditButton.click();

        } catch (Exception ex) {
            WebElement altSubmitButton = WebUI.findWebElementIfVisible(By.xpath("//*[@id='checking-log']/div/table/tbody/tr[1]/td[6]/div/button"));
            altSubmitButton.click();

            WebElement reEditButton = WebUI.findWebElementIfVisible(By.xpath(
                "//app-at0023//button[contains(@class, 'btn-edit-del') and normalize-space(text())='編集']"
            ));
            reEditButton.click();
        }
    }

    @ExecutionStep(value = "addCheckingLogs")
    public static void addAllCheckingLogs(ExportTemplateFilterSetting setting) {
        AttendanceUtils.navigateToATPage("at0001");
        waitForLoadingElement();

        WebUI.sleep(500);
        AttendanceUtils.selectUserAndMonthOnTimesheetPage(setting);

        List<CheckingLog> allLogs = ExcelReaderRepository.findAllCheckingLog(setting.getSheetName());
        // Lọc theo phase
        Map<String, List<CheckingLog>> logsGroupedByDay = allLogs.stream()
            .filter(log -> setting.getPhase().equals(log.getPhase()))
            .filter(log -> log.getDateIndex() != null && !log.getDateIndex().isEmpty())
            .collect(Collectors.groupingBy(CheckingLog::getDateIndex));

        String xpathRowAT0001 = "//app-at0001//div[@id='tbl-sheet']/table/tbody//tr[%s]";
        for (Map.Entry<String, List<CheckingLog>> entry : logsGroupedByDay.entrySet()) {
            String dateIndex = entry.getKey();
            List<CheckingLog> logsInDay = entry.getValue();

            String xpathRow = format(xpathRowAT0001, dateIndex);
            WebElement editButton = WebUI.findWebElementIfVisible(By.xpath(xpathRow + "/td[last()-1]/button"));
            if (editButton != null) {
                editButton.click();
                waitForLoadingElement();
            }

            Map<String, List<Request>> mapRequestsByDay = mapGroupingRequestByDay(dateIndex, setting.getSheetName(), setting.getPhase());

            // Xử lý day off request
            ExecutionHelper.runStepWithLoggingByPhase(setting, format("Make day off request: dateIndex:%s", dateIndex), () ->
                handleDayOffRequestByDateIndex(mapRequestsByDay, dateIndex)
            );

            // Nhập checking log
            ExecutionHelper.runStepWithLoggingByPhase(setting, format("Enter the checking log: dateIndex:%s", dateIndex), () -> {
                for (int i = 0; i < logsInDay.size(); i++) {
                    CheckingLog log = logsInDay.get(i);
                    String xpathLogTime = format("//app-at0023//form[@id='checking-log']//table//tbody/tr[%d]/td[1]//input", i + 1);
                    String xpathReason = format("//app-at0023//form[@id='checking-log']//table//tbody/tr[%d]/td[3]//textarea", i + 1);

                    WebElement inputTime = WebUI.findWebElementIfVisible(By.xpath(xpathLogTime));
                    WebElement inputReason = WebUI.findWebElementIfVisible(By.xpath(xpathReason));

                    if (inputTime != null) inputTime.sendKeys(log.getLogTime());
                    if (inputReason != null) inputReason.sendKeys(Optional.ofNullable(log.getReason()).orElse(""));

                    if (i >= 1 && i < logsInDay.size()) {
                        WebElement btnAddLog = WebUI.findWebElementIfVisible(By.cssSelector("#checking-log div div button.add-link"));
                        if (WebUI.waitForElementClickable(By.cssSelector("#checking-log div div button.add-link")) != null) WebUI.clickWithScrollTo(btnAddLog);
                    }
                }

                // Submit checking log
                WebUI.findWebElementIfPresent(By.xpath("//*[@id='checking-log']/div/table/tbody/tr[1]/td[6]/div/button")).click();
            });

            // Xử lý OT request
            ExecutionHelper.runStepWithLoggingByPhase(setting, format("Make OT request: dateIndex:%s", dateIndex), () ->
                handleOTAndResearchRequestByDateIndex(mapRequestsByDay, dateIndex)
            );

            // Quay lại AT0001
            WebElement backBtn = WebUI.findWebElementIfVisible(By.xpath("//a[@class='page-head-backlink']"));
            if (backBtn != null) WebUI.clickByJS(backBtn);

            // Xử lý confirm popup nếu có
            int retry = 0;
            while (retry < 5) {
                WebElement confirmBtn = WebUI.waitForElementClickable(By.xpath(XpathCommon.MODAL_CONFIRM_BTN.value), 2);
                if (confirmBtn == null) break;
                confirmBtn.click();
                retry++;
            }
        }
    }

    public static Map<String, List<Request>> mapGroupingRequestByDay(String dateIndex, String sheet, String phaseTest) {
        if (dateIndex == null || dateIndex.isEmpty()) return Maps.newHashMap();

        waitForLoadingElement();

        List<Request> requestData = ExcelReaderRepository.findAllRequest(sheet); // sheet == null thì xử lý trong hàm này

        return requestData.stream()
            .filter(row -> phaseTest.equals(row.getPhase()))
            .filter(row -> row.getDateIndex() != null && !row.getDateIndex().isEmpty())
            .collect(Collectors.groupingBy(Request::getDateIndex));
    }

    public static void handleOTAndResearchRequestByDateIndex(Map<String, List<Request>> mapGroupingByDay, String dateIndex) {
        List<Request> otRequestInTargetDate = mapGroupingByDay.get(dateIndex);
        if (otRequestInTargetDate == null) return;

        // Lọc các request loại "ot" hoặc "research"
        otRequestInTargetDate = otRequestInTargetDate.stream()
            .filter(it -> it.getReasonCategory().equalsIgnoreCase("ot") || it.getReasonCategory().equalsIgnoreCase("research"))
            .collect(Collectors.toList());

        if (!otRequestInTargetDate.isEmpty()) {
            String requestRowXpath = "//app-at0023//div[@class='ot-table-container']/table/tbody/tr";

            // Xoá hết các request cũ
            List<WebElement> allElements = WebUI.findWebElementsIfPresent(By.xpath(requestRowXpath));
            for (int i = 0; i < allElements.size(); i++) {
                WebElement validBtn = WebUI.waitForElementPresent(By.xpath(requestRowXpath + "[1]//button[normalize-space(text())='有効']"), 1);
                if (validBtn != null) validBtn.click();

                WebElement deleteBtn = WebUI.findWebElementIfPresent(By.xpath(requestRowXpath + "[1]/td[last()]/button"));
                WebUI.clickWithScrollTo(deleteBtn);
                waitForLoadingElement();
            }

            // Tạo mới các request OT/research
            for (int i = 0; i < otRequestInTargetDate.size(); i++) {
                Request request = otRequestInTargetDate.get(i);
                String curRowXpath = format("%s[%d]", requestRowXpath, i + 1);

                WebElement btnAddRequest = WebUI.findWebElementIfVisible(By.xpath("//app-at0023//div[@class='ot-table-container']/button"));
                btnAddRequest.click();

                WebElement inputStartTime = WebUI.findWebElementIfVisible(By.xpath(curRowXpath + "//input[1][@ng-reflect-name='startTime']"));
                inputStartTime.sendKeys(request.getStartTime());

                WebElement inputEndTime = WebUI.findWebElementIfVisible(By.xpath(curRowXpath + "//input[2][@ng-reflect-name='endTime']"));
                inputEndTime.sendKeys(request.getEndTime());

                WebElement selectReason = WebUI.findWebElementIfVisible(By.xpath(curRowXpath + "//div[@class='reason-type']//select[1]"));
                Select dropdown = new Select(selectReason);
                if ("ot".equalsIgnoreCase(request.getReasonCategory())) {
                    dropdown.selectByValue("0: RC_OVERTIME");
                } else if ("research".equalsIgnoreCase(request.getReasonCategory())) {
                    dropdown.selectByValue("1: RC_RESEARCH");
                }
            }

            WebElement btnSubmit = WebUI.findWebElementIfPresent(By.xpath("//*[@id='btn-submit-ot']"));
            WebUI.clickWithScrollTo(btnSubmit);

            WebElement btnConfirm = WebUI.findWebElementIfPresent(By.xpath(XpathCommon.MODAL_CONFIRM_BTN.value));
            while (btnConfirm != null && btnConfirm.isDisplayed()) {
                btnConfirm.click();
                btnConfirm = WebUI.waitForElementPresent(By.xpath(XpathCommon.MODAL_CONFIRM_BTN.value), 1);
            }
        }
    }

    public static void handleDayOffRequestByDateIndex(Map<String, List<Request>> mapGroupingByDay, String dateIndex) {
        // Lấy danh sách yêu cầu ngày nghỉ theo dateIndex
        List<Request> dayOffRequestInTargetDate = mapGroupingByDay.get(dateIndex);

        if (dayOffRequestInTargetDate == null || dayOffRequestInTargetDate.isEmpty()) {
            return;  // Nếu không có yêu cầu nào, thoát ra
        }

        // Lọc ra những yêu cầu có loại "dayoff" hoặc "dayoffworking"
        dayOffRequestInTargetDate = dayOffRequestInTargetDate.stream()
            .filter(request -> "dayoff".equals(request.getReasonCategory()) || "dayoffworking".equals(request.getReasonCategory()))
            .collect(Collectors.toList());

        if (!dayOffRequestInTargetDate.isEmpty()) {
            // Lựa chọn màn hình "AT0024F"
            WebElement selectScreenBtn = WebUI.findWebElementIfVisible(By.xpath("//app-at0023//div[contains(@class, 'select-screen')]//select"));
            Select dropdown = new Select(selectScreenBtn);
            dropdown.selectByValue("AT0024F");

            // Chờ tải lại trang
            waitForLoadingElement();

            // Lặp qua các yêu cầu và xử lý
            for (Request dayoffRequest : dayOffRequestInTargetDate) {
                String requestType = dayoffRequest.getReasonCategory();

                // Xử lý từng loại yêu cầu
                switch (requestType) {
                    case "dayoff":
                        handleDayoffRequest(dayoffRequest);
                        break;
                    case "dayoffworking":
                        handleDayoffWorkingRequest(dayoffRequest);
                        break;
                    default:
                        break;
                }
            }

            // Lựa chọn màn hình "AT0023C"
            selectScreenBtn = WebUI.findWebElementIfVisible(By.xpath("//app-at0024//div[contains(@class, 'select-screen')]//select"));
            dropdown = new Select(selectScreenBtn);
            dropdown.selectByValue("AT0023C");

            // Chờ tải lại trang
            waitForLoadingElement();

            // Xử lý confirm popup nếu có
            int retry = 0;
            while (retry < 5) {
                WebElement confirmBtn = WebUI.waitForElementClickable(By.xpath(XpathCommon.MODAL_CONFIRM_BTN.value), 2);
                if (confirmBtn == null) break;
                confirmBtn.click();
                retry++;
            }
        }
    }

    public static void handleDayoffRequest(Request dayoffRequest) {
        // 申請種類
        waitForLoadingOverlayElement();
        String applycationTypeXpath = "//app-at0024//div[text()='申請種類']/following-sibling::div//span[normalize-space()='休暇']";
        WebElement appTypeBtn = WebUI.findWebElementIfVisible(By.xpath(applycationTypeXpath));
        appTypeBtn.click();

        // カテゴリ - Loại lý do nghỉ
        String reasonTypeBaseXPath = "//app-at0024//div[text()='カテゴリ']/following-sibling::div";
        String reasonType = dayoffRequest.getReasonType().trim();
        String reasonBtnXpath = format("%s//span[text()[normalize-space()='%s']]", reasonTypeBaseXPath, reasonType);
        WebElement reasonTypeBtn = WebUI.findWebElementIfVisible(By.xpath(reasonBtnXpath));
        WebUI.scrollToElementCenter(reasonTypeBtn);
        if (reasonTypeBtn.isEnabled()) {
            reasonTypeBtn.click();
        }

        // 休暇名 - Tên lý do nghỉ
        String reasonNameBaseXpath = "//app-at0024//div[text()='休暇名']/following-sibling::div";
        String reasonName = dayoffRequest.getReasonName().trim();
        String reasonNameXpath = format("%s//span[text()[normalize-space()='%s']]", reasonNameBaseXpath, reasonName);
        WebElement reasonNameBtn = WebUI.findWebElementIfVisible(By.xpath(reasonNameXpath));

        if (reasonNameBtn.isEnabled()) {
            reasonNameBtn.click();
        }

        String timeUnit = dayoffRequest.getRequestTimeUnit().trim();
        if (StringUtils.isEmpty(timeUnit)) return;

        String requestTimeUnitBaseXpath = "//app-day-off-type/div[contains(@class, 'block-form')]//select";
        WebElement selectTimeUnit = WebUI.findWebElementIfVisible(By.xpath(requestTimeUnitBaseXpath));

        Select dropdown = new Select(selectTimeUnit);
        switch (timeUnit.toLowerCase().trim()) {
            case "allday":
                dropdown.selectByValue("ALL_DAY");
                break;
            case "halfday":
                dropdown.selectByValue("HALF_DAY");
                // Xin nghỉ dựa trên periodType - morning/afternoon
                String periodType = dayoffRequest.getPeriodType().trim();
                String periodTypeXpath = "";
                if ("morning".equalsIgnoreCase(periodType)) {
                    periodTypeXpath = "//app-day-off-type//input[@ng-reflect-value='AT_MORNING']";
                } else if ("afternoon".equalsIgnoreCase(periodType)) {
                    periodTypeXpath = "//app-day-off-type//input[@ng-reflect-value='AT_AFTERNOON']";
                }
                WebElement radioIPPeriodType = WebUI.findWebElementIfVisible(By.xpath(periodTypeXpath + "/ancestor::label[contains(@class, 'custom-radio')]"));
                radioIPPeriodType.click();
                break;
            case "timeperiod":
                dropdown.selectByValue("TIME_PERIOD");

                String xpathStartTime = "//app-day-off-type//table[@id='date-lbx']//tr[2]//td[2]//input[1]";
                WebElement ipStartTime = WebUI.findWebElementIfVisible(By.xpath(xpathStartTime));
                ipStartTime.sendKeys(dayoffRequest.getStartTime().trim());

                String xpathEndTime = "//app-day-off-type//table[@id='date-lbx']//tr[2]//td[2]//input[2]";
                WebElement ipEndTime = WebUI.findWebElementIfVisible(By.xpath(xpathEndTime));
                ipEndTime.sendKeys(dayoffRequest.getEndTime().trim());
                break;
            case "minuteperiod":
                dropdown.selectByValue("MINUTE_PERIOD");

                String xpathStartTimeMP = "//app-day-off-type//table[@id='date-lbx']//tr[2]//td[2]//input[1]";
                WebElement ipStartTimeMP = WebUI.findWebElementIfVisible(By.xpath(xpathStartTimeMP));
                ipStartTimeMP.sendKeys(dayoffRequest.getStartTime().trim());

                String xpathEndTimeMP = "//app-day-off-type//table[@id='date-lbx']//tr[2]//td[2]//input[2]";
                WebElement ipEndTimeMP = WebUI.findWebElementIfVisible(By.xpath(xpathEndTimeMP));
                ipEndTimeMP.sendKeys(dayoffRequest.getEndTime().trim());
                break;
            default:
                break;
        }

        WebElement btnApply = WebUI.findWebElementIfVisible(By.xpath("//app-at0024//button[text()[normalize-space() = '申請']]"));
        WebUI.clickWithScrollTo(btnApply);
        waitForLoadingElement();

        int counter = 0;
        WebElement btnConfirm = WebUI.findWebElementIfVisible(By.xpath(XpathCommon.MODAL_CONFIRM_BTN.value));
        while (btnConfirm != null && btnConfirm.isDisplayed()) {
            if (counter == 5) break;
            btnConfirm.click();
            btnConfirm = WebUI.waitForElementPresent(By.xpath(XpathCommon.MODAL_CONFIRM_BTN.value), 2);
            counter++;
        }
    }

    public static void handleDayoffWorkingRequest(Request dayoffRequest) {
        WebUI.scrollToTop();

        // 申請種類 - Loại đơn: 休日出勤
        WebElement appTypeBtn = WebUI.findWebElementIfVisible(By.xpath("//app-at0024//div[text()='申請種類']/following-sibling::div//span[normalize-space()='休日出勤']"));
        WebUI.clickByJS(appTypeBtn);

        waitForLoadingElement();

        // Lấy ngày nghỉ làm (休日出勤日)
        String holidayWorkDate = dayoffRequest.getHolidayWorkDate();
        String year1 = holidayWorkDate.substring(0, 4);
        String month1 = String.valueOf(Integer.parseInt(holidayWorkDate.substring(4, 6)));
        String day1 = String.valueOf(Integer.parseInt(holidayWorkDate.substring(6, 8)));

        WebElement openHolidayWorkBtn = WebUI.findWebElementIfVisible(By.xpath("//app-at0024//span[text()='休日出勤日']//ancestor::tr//following-sibling::tr[1]//app-date-input-at"));
        DateUtils.chooseDatePicker(openHolidayWorkBtn, year1, month1, day1);

        // Lấy ngày nghỉ bù (休暇取得日)
        String vacationTakenDate = dayoffRequest.getVacationTakenDate();
        String year2 = vacationTakenDate.substring(0, 4);
        String month2 = String.valueOf(Integer.parseInt(vacationTakenDate.substring(4, 6)));
        String day2 = String.valueOf(Integer.parseInt(vacationTakenDate.substring(6, 8)));

        WebElement openVacationTakenBtn = WebUI.findWebElementIfVisible(By.xpath("//app-at0024//span[text()='休暇取得日']//ancestor::tr//following-sibling::tr[1]//app-date-input-at"));
        WebUI.scrollToElementCenter(openVacationTakenBtn);
        DateUtils.chooseDatePicker(openVacationTakenBtn, year2, month2, day2);

        WebUI.sleep(500);

        // 申請ボタン
        WebElement btnApply = WebUI.findWebElementIfVisible(By.xpath("//app-at0024//button[normalize-space()='申請']"));
        if (btnApply != null) {
            btnApply.click();
            waitForLoadingElement();
        }

        // Confirm modal
        int counter = 0;
        By positiveButton = By.xpath(XpathCommon.MODAL_CONFIRM_BTN.value);
        WebElement btnConfirm = WebUI.findWebElementIfVisible(positiveButton);
        if (btnConfirm != null) {
            btnConfirm.click();
            while (WebUI.waitForElementPresent(By.xpath("//app-modal//button[@id='positiveButton']"), 1) != null && counter < 5) {
                WebUI.click(By.xpath("//app-modal//button[@id='positiveButton']"));
                counter++;
            }
        }
    }

    @ExecutionStep(value = "addWorkSchedule")
    public static void addWorkSchedule(ExportTemplateFilterSetting setting) {
        AttendanceUtils.navigateToATPage("at0033");

        List<WorkSchedule> workScheduleData = ExcelReaderRepository.findAllWorkSchedule(setting.getSheetName()); // sheet == null thì xử lý trong hàm này

        List<WorkSchedule> filteredRows = new ArrayList<>();
        for (WorkSchedule row : workScheduleData) {
            if (row.getPhase() != null
                && row.getPhase().equals(setting.getPhase())
                && row.getDayIndex() != null
                && !row.getDayIndex().isEmpty()) {
                filteredRows.add(row);
            }
        }

        waitForLoadingElement();
        if (!filteredRows.isEmpty()) {
            filteredRows.sort(Comparator.comparingInt(row -> Integer.parseInt(row.getDayIndex())));

            ExecutionHelper.runStepWithLoggingByPhase(setting, format("Access to the target user: %s", setting.getTargetUser()), () -> {
                WebElement inputElementAT0033SearchName = WebUI.findWebElementIfVisible(By.xpath("//app-at0033//input[@placeholder=\"ユーザー名を検索\"]"));
                inputElementAT0033SearchName.clear();
                inputElementAT0033SearchName.sendKeys(setting.getTargetUser());

                WebElement searchNameBtn = WebUI.findWebElementIfVisible(
                    By.xpath("//app-at0033//input[@placeholder=\"ユーザー名を検索\"]/following-sibling::button")
                );
                WebUI.clickByJS(searchNameBtn);

                waitForLoadingElement();
            });

            ExecutionHelper.runStepWithLoggingByPhase(setting, "Access the edit work schedule screen [編集]", () -> {
                String xpathEditButton = format(
                    "//app-at0033//div[@id='tbl-sheet']//table/tbody//span[normalize-space(text())='%s']/ancestor::tr//button[normalize-space(text())='編集']",
                    setting.getTargetUser()
                );
                WebElement editBtn = WebUI.findWebElementIfVisible(By.xpath(xpathEditButton));
                WebUI.clickByJS(editBtn);
                waitForLoadingElement();
            });

            ExecutionHelper.runStepWithLoggingByPhase(setting, format("Access the schedule by target month: %s", setting.getTargetMonth()), () -> {
                String[] splitedMonthYear = DateUtils.splitMonthYear(setting.getTargetMonth());
                String targetYear = splitedMonthYear[0];
                String targetMonth = splitedMonthYear[1];

                String currMonthYearText = DateUtils.convertToYYYYMM(WebUI.findWebElementIfVisible(By.xpath("//div[contains(@class, 'text-center')]//h2")).getText());
                if (setting.getTargetMonth() != null && !setting.getTargetMonth().equals(currMonthYearText)) {
                    WebUI.findWebElementIfVisible(By.xpath("//app-at0035b-date-picker//button[normalize-space(text())='月選択']")).click();

                    String baseDatePickerXpath = "//app-at0035b-date-picker//div[contains(@class, 'date-picker')]";
                    String textYearCenterXpath = baseDatePickerXpath + "//div[contains(@class, 'date-tab-bar')]//div[contains(@class, 'text-center')]//span";
                    String currYear = WebUI.findWebElementIfVisible(By.xpath(textYearCenterXpath)).getText();

                    while (Integer.parseInt(targetYear) < Integer.parseInt(currYear)) {
                        String backLeftYearBtnXpath = baseDatePickerXpath + "//div[contains(@class, 'date-tab-bar')]//div[contains(@class, 'text-left')]//i";
                        WebUI.findWebElementIfVisible(By.xpath(backLeftYearBtnXpath)).click();
                        currYear = WebUI.findWebElementIfVisible(By.xpath(textYearCenterXpath)).getText();
                    }

                    String textMonthCenterXpath = baseDatePickerXpath + "//div[contains(@class, 'month text-center')]//button[" + targetMonth + "]";
                    WebUI.findWebElementIfVisible(By.xpath(textMonthCenterXpath)).click();
                }
            });

            String xpathDateInMonth = "//div[@id='tbl-sheet']//table/tbody/tr[not(contains(@class, 'box-time')) and not(contains(@class, 'no-border')) and not (contains(@class, 'bottom-tbl'))]";
            waitForLoadingElement();
            List<WebElement> dateElements = WebUI.findWebElementsIfVisible(By.xpath(xpathDateInMonth));

            String curURL = "";
            String curScreen = "";
            try {
                curURL = DriverFactory.getDriver().getCurrentUrl();

                Pattern pattern = Pattern.compile("https?://[^/]+(/[^/]+/[^/]+/)");
                Matcher matcher = pattern.matcher(curURL);
                if (matcher.find()) {
                    curScreen = matcher.group(1);
                }
            } catch (Exception e) {
                // Handle or log the error if needed
            }


            boolean isDiscretionaryScreen = curURL != null && curURL.contains("/at/at0036b/");
            if (isDiscretionaryScreen) {
                ExecutionHelper.runStepWithLoggingByPhase(setting, format("Handle discretionary schedule [%s]", curScreen), () ->
                    handleDiscretionarySchedule(setting, filteredRows, dateElements, xpathDateInMonth)
                );
            } else {
                String workPattern = curScreen.contains("/at/at0035") ? "variable" : "individual";
                ExecutionHelper.runStepWithLoggingByPhase(setting, format("Handle %s schedule [%s]", workPattern, curScreen), () ->
                    handleIndividualAndVariableSchedule(setting, filteredRows, dateElements, xpathDateInMonth)
                );
            }

            if (WebUI.waitForElementClickable(By.xpath("//button[normalize-space(text())='保存' and not(@disabled)]"), 1) != null) {
                WebUI.findWebElementIfVisible(By.xpath("//button[normalize-space(text())='保存' and not(@disabled)]")).click();
                WebUI.findWebElementIfVisible(By.xpath("//button[@id=\"positiveButton\" and normalize-space(text())='はい']")).click();

                while (WebUI.waitForElementPresent(By.xpath("//button[@id='positiveButton']"), 1) != null) {
                    WebUI.findWebElementIfVisible(By.xpath("//button[@id='positiveButton']")).click();
                    waitForLoadingElement();
                }
            }

            waitForLoadingElement();
            WebUI.sleep(1000);

            WebUI.scrollToTop();
            WebElement backBtn = WebUI.findWebElementIfVisible(By.xpath("//a[@class='page-head-backlink']"));
            WebUI.clickByJS(backBtn);
        }
    }

    @ExecutionStep(value = "approveRequests")
    public static void approveAllRequest(ExportTemplateFilterSetting setting) {
        AttendanceUtils.navigateToATPage("at0022");

        // Check OT Request
        WebUI.findWebElementIfVisible(By.xpath("//*[@id='tab-content4']//app-at0022//button[normalize-space(text())='残業・研鑽申請一覧']")).click();
        waitForLoadingElement();

        String btnMemberFilterXpath = "//*[@id='tab-content4']//app-at0022//app-destination-member-filter";
        WebUI.click(By.xpath(btnMemberFilterXpath));

        By btnChooseAllItem = By.xpath(btnMemberFilterXpath + "//ngb-popover-window//button[text()='全選択']");
        int clickCounter = 0;
        while (WebUI.waitForElementPresent(btnChooseAllItem, 1) != null) {
            if (clickCounter >= 5) break;
            WebUI.sleep(500);
            // Click first time - choose all
            WebUI.clickByJS(WebUI.findWebElementIfVisible(btnChooseAllItem));
            WebUI.sleep(500);

            clickCounter++;
        }

        // Click seconds time - reset all
        By btnResetAllItem = By.xpath(btnMemberFilterXpath + "//ngb-popover-window//button[text()='全解除']");
        WebUI.click(btnResetAllItem);
        WebUI.sleep(500);

        By searchUserNameInput = By.xpath(btnMemberFilterXpath + "//ngb-popover-window//input[@placeholder='メンバーを検索']");
        WebElement inputElementMemberFilter = DriverFactory.getDriver().findElement(searchUserNameInput);

        String filterUserNameText = WebUI.findWebElementIfPresent(By.xpath(btnMemberFilterXpath + "//span[@class='tooltip-users']")).getText();
        if (!filterUserNameText.contains(setting.getTargetUser())) {
            inputElementMemberFilter.sendKeys(setting.getTargetUser());

            WebUI.findWebElementIfVisible(By.xpath(btnMemberFilterXpath + "//virtual-scroll//span[text()='" + setting.getTargetUser() + "']//ancestor::div[contains(@class, 'destination-popover-profile-wrap')]")).click();
            WebUI.findWebElementIfVisible(By.xpath("//app-at0022-filter//div[contains(@class, 'search popup-member-filter')]//button[@type='submit']")).click();

            WebUI.sleep(500);
        }

        String xpathSearchApplicationType = "//*[@id='tab-content4']//app-at0022//p[normalize-space(text())='申請種類を選択']/following-sibling::select";
        WebElement dropdownElementSearchApplicationType = DriverFactory.getDriver().findElement(By.xpath(xpathSearchApplicationType));
        Select dropdownSearchApplicationType = new Select(dropdownElementSearchApplicationType);
        dropdownSearchApplicationType.selectByVisibleText("すべて");

        String xpathSearchStatusButton = "//*[@id='tab-content4']//app-at0022//p[normalize-space(text())='ステータスを選択']/following-sibling::select";
        WebElement dropdownElementSearchStatusButton  = DriverFactory.getDriver().findElement(By.xpath(xpathSearchStatusButton));
        Select dropdownSearchStatusButton  = new Select(dropdownElementSearchStatusButton );
        dropdownSearchStatusButton.selectByValue("RS_NEW");

        String xpathAllRecords = "//app-at0022//div[@id='table-content']/table/tbody/tr";
        ExecutionHelper.runStepWithLoggingByPhase(setting, "Approve request OT", () -> {
            if (WebUI.waitForElementPresent(By.xpath(xpathAllRecords), 5) != null) {
                String checkboxChooseAllRecordXpath = "//app-at0022//div[@id='table-header']//tr[1]/th[1]";
                WebUI.findWebElementIfVisible(By.xpath(checkboxChooseAllRecordXpath)).click();
                WebUI.sleep(400);

                String buttonApproveAllRequestXpath = "//app-at0022//button[normalize-space(text())='一括承認']";
                ExecutionHelper.runStepWithLoggingByPhase(setting, "AT0022 - Check OT request -> Click & Confirm ", () ->
                    AttendanceUtils.clickAndConfirm(By.xpath(buttonApproveAllRequestXpath), 0)
                );

                waitForLoadingElement();

                String xpathAT0022HeaderSelectStatus = "//*[@id=\"tab-content4\"]//app-at0022//p[normalize-space(text())='ステータスを選択']/following-sibling::select";
                WebElement dropdownElementAT0022HeaderSelectStatus = DriverFactory.getDriver().findElement(By.xpath(xpathAT0022HeaderSelectStatus));
                Select dropdownAT0022HeaderSelectStatus = new Select(dropdownElementAT0022HeaderSelectStatus);
                dropdownAT0022HeaderSelectStatus.selectByValue("RS_ACCEPTED");

                WebUI.sleep(400);
            }
        });

        // Check DayOff Request
        WebUI.findWebElementIfVisible(By.xpath("//*[@id='tab-content4']//app-at0022//button[normalize-space(text())='各種申請一覧']")).click();

         filterUserNameText = WebUI.findWebElementIfPresent(By.xpath(btnMemberFilterXpath + "//span[@class='tooltip-users']")).getText();
        if (filterUserNameText.contains(setting.getTargetUser())) {
            WebUI.findWebElementIfVisible(By.xpath(btnMemberFilterXpath)).click();

            inputElementMemberFilter = WebUI.findWebElementIfPresent(By.xpath(btnMemberFilterXpath + "//ngb-popover-window//input[@placeholder='メンバーを検索']"));
            inputElementMemberFilter.clear();
            inputElementMemberFilter.sendKeys(setting.getTargetUser());

            WebUI.findWebElementIfVisible(By.xpath(btnMemberFilterXpath + "//virtual-scroll//span[text()='" + setting.getTargetUser() + "']//ancestor::div[contains(@class, 'destination-popover-profile-wrap')]")).click();
            WebUI.findWebElementIfVisible(By.xpath("//app-at0022-filter//div[contains(@class, 'search popup-member-filter')]//button[@type='submit']")).click();

            WebUI.sleep(500);
        }

        dropdownElementSearchApplicationType = DriverFactory.getDriver().findElement(By.xpath(xpathSearchApplicationType));
        dropdownSearchApplicationType = new Select(dropdownElementSearchApplicationType);
        dropdownSearchApplicationType.selectByVisibleText("すべて");

        dropdownElementSearchStatusButton = DriverFactory.getDriver().findElement(By.xpath(xpathSearchStatusButton));
        dropdownSearchStatusButton  = new Select(dropdownElementSearchStatusButton );
        dropdownSearchStatusButton.selectByValue("RS_NEW");

        ExecutionHelper.runStepWithLoggingByPhase(setting, "Approve request day off", () -> {
            if (WebUI.waitForElementPresent(By.xpath(xpathAllRecords), 5) != null) {
                String checkboxChooseAllRecordXpath = "//app-at0022//div[@id='table-header']//tr[1]/th[1]";
                WebUI.findWebElementIfVisible(By.xpath(checkboxChooseAllRecordXpath)).click();
                WebUI.sleep(400);

                String buttonApproveAllRequestXpath = "//app-at0022//button[normalize-space(text())='一括承認']";
                ExecutionHelper.runStepWithLoggingByPhase(setting, "AT0022 - Check DayOff request -> Click & Confirm ", () ->
                    AttendanceUtils.clickAndConfirm(By.xpath(buttonApproveAllRequestXpath), 0)
                );

                waitForLoadingElement();

                String xpathAT0022HeaderSelectStatus = "//*[@id=\"tab-content4\"]//app-at0022//p[normalize-space(text())='ステータスを選択']/following-sibling::select";
                WebElement dropdownElementAT0022HeaderSelectStatus = DriverFactory.getDriver().findElement(By.xpath(xpathAT0022HeaderSelectStatus));
                Select dropdownAT0022HeaderSelectStatus = new Select(dropdownElementAT0022HeaderSelectStatus);
                dropdownAT0022HeaderSelectStatus.selectByValue("RS_ACCEPTED");

                WebUI.sleep(500);
            }
        });
    }

    @ExecutionStep(value = "rejectRequests")
    public static void rejectAllRequest(ExportTemplateFilterSetting setting) {
        AttendanceUtils.navigateToATPage("at0022");

        WebUI.clickAtCoordinates(0, 0);

        // Handle OT request
        WebUI.findWebElementIfVisible(By.xpath("//*[@id='tab-content4']//app-at0022//button[normalize-space(text())='残業・研鑽申請一覧']")).click();
        waitForLoadingElement();

        String btnMemberFilterXpath = "//*[@id='tab-content4']//app-at0022//app-destination-member-filter";
        WebUI.click(By.xpath(btnMemberFilterXpath));

        By btnChooseAllItem = By.xpath(btnMemberFilterXpath + "//ngb-popover-window//button[text()='全選択']");
        int clickCounter = 0;
        while (WebUI.waitForElementPresent(btnChooseAllItem, 1) != null) {
            if (clickCounter >= 5) break;
            WebUI.sleep(200);
            // Click first time - choose all
            WebUI.clickByJS(WebUI.findWebElementIfVisible(btnChooseAllItem));
            // Delay in 1 sec
            WebUI.sleep(200);

            clickCounter++;
        }

        // Click seconds time - reset all
        By btnResetAllItem = By.xpath(btnMemberFilterXpath + "//ngb-popover-window//button[text()='全解除']");
        WebUI.click(btnResetAllItem);
        WebUI.sleep(500);

        By searchUserNameInput = By.xpath(btnMemberFilterXpath + "//ngb-popover-window//input[@placeholder='メンバーを検索']");
        WebElement inputElementMemberFilter = DriverFactory.getDriver().findElement(searchUserNameInput);

        String filterUserNameText = WebUI.findWebElementIfPresent(By.xpath(btnMemberFilterXpath + "//span[@class='tooltip-users']")).getText();
        if (!filterUserNameText.contains(setting.getTargetUser())) {
            inputElementMemberFilter.clear();
            inputElementMemberFilter.sendKeys(setting.getTargetUser());

            WebUI.findWebElementIfVisible(By.xpath(btnMemberFilterXpath + "//virtual-scroll//span[text()='" + setting.getTargetUser() + "']//ancestor::div[contains(@class, 'destination-popover-profile-wrap')]")).click();
            WebUI.findWebElementIfVisible(By.xpath("//app-at0022-filter//div[contains(@class, 'search popup-member-filter')]//button[@type='submit']")).click();

            WebUI.sleep(500);
        }

        String xpathSearchApplicationType = "//*[@id='tab-content4']//app-at0022//p[normalize-space(text())='申請種類を選択']/following-sibling::select";
        WebElement dropdownElementSearchApplicationType = DriverFactory.getDriver().findElement(By.xpath(xpathSearchApplicationType));
        Select dropdownSearchApplicationType = new Select(dropdownElementSearchApplicationType);
        dropdownSearchApplicationType.selectByVisibleText("すべて");

        String xpathSearchStatusButton = "//*[@id='tab-content4']//app-at0022//p[normalize-space(text())='ステータスを選択']/following-sibling::select";
        WebElement dropdownElementSearchStatusButton  = DriverFactory.getDriver().findElement(By.xpath(xpathSearchStatusButton));
        Select dropdownSearchStatusButton  = new Select(dropdownElementSearchStatusButton );
        dropdownSearchStatusButton.selectByValue("RS_ACCEPTED");
        WebUI.sleep(500);

        String xpathAllRecords = "//app-at0022//div[@id='table-content']/table/tbody/tr";
        if (WebUI.waitForElementPresent(By.xpath(xpathAllRecords), 5) != null) {
            ExecutionHelper.runStepWithLoggingByPhase(setting, "AT0022 - Reject OT Request -> Click & Confirm ", () ->{
                List<WebElement> requestRowElements = WebUI.findWebElementsIfVisible(By.xpath(xpathAllRecords));

                for (int i = 0; i < requestRowElements.size(); i++) {
                    WebUI.sleep(500);
                    By rejectButton = By.xpath(xpathAllRecords + "[1]//span[normalize-space(text())='非承認']/ancestor::button");

                    WebUI.waitForElementClickable(rejectButton, 10);
                    WebUI.click(rejectButton);

                    WebUI.findWebElementIfVisible(By.xpath(XpathCommon.MODAL_CONFIRM_BTN.value)).click();

                    WebDriverWait wait = new WebDriverWait(DriverFactory.getDriver(), Duration.ofSeconds(5));
                    WebElement loader = DriverFactory.getDriver().findElement(By.xpath("//app-loader-empty")); // cần đúng xpath tương ứng với 'app-loader-empty'
                    wait.until(ExpectedConditions.attributeToBe(loader, "ng-reflect-is-show-loading", "false"));
                }

                String xpathAT0022HeaderSelectStatus = "//*[@id=\"tab-content4\"]//app-at0022//p[normalize-space(text())='ステータスを選択']/following-sibling::select";
                WebElement dropdownElementAT0022HeaderSelectStatus  = DriverFactory.getDriver().findElement(By.xpath(xpathAT0022HeaderSelectStatus));
                Select dropdownAT0022HeaderSelectStatus  = new Select(dropdownElementAT0022HeaderSelectStatus );
                dropdownAT0022HeaderSelectStatus.selectByValue("RS_REJECTED");
            });
        }

        // Handle DayOff request
        WebUI.findWebElementIfVisible(By.xpath("//*[@id='tab-content4']//app-at0022//button[normalize-space(text())='各種申請一覧']")).click();

        dropdownElementSearchApplicationType = DriverFactory.getDriver().findElement(By.xpath(xpathSearchApplicationType));
        dropdownSearchApplicationType = new Select(dropdownElementSearchApplicationType);
        dropdownSearchApplicationType.selectByVisibleText("すべて");

        dropdownElementSearchStatusButton  = DriverFactory.getDriver().findElement(By.xpath(xpathSearchStatusButton));
        dropdownSearchStatusButton  = new Select(dropdownElementSearchStatusButton);
        dropdownSearchStatusButton.selectByValue("RS_ACCEPTED");
        WebUI.sleep(500);

        filterUserNameText = WebUI.findWebElementIfPresent(By.xpath(btnMemberFilterXpath + "//span[@class='tooltip-users']")).getText();
        if (filterUserNameText.contains(setting.getTargetUser())) {
            WebUI.findWebElementIfVisible(By.xpath(btnMemberFilterXpath)).click();

            inputElementMemberFilter = WebUI.findWebElementIfPresent(By.xpath(btnMemberFilterXpath + "//ngb-popover-window//input[@placeholder='メンバーを検索']"));
            inputElementMemberFilter.clear();
            inputElementMemberFilter.sendKeys(setting.getTargetUser());

            WebUI.findWebElementIfVisible(By.xpath(btnMemberFilterXpath + "//virtual-scroll//span[text()='" + setting.getTargetUser() + "']//ancestor::div[contains(@class, 'destination-popover-profile-wrap')]")).click();
            WebUI.findWebElementIfVisible(By.xpath("//app-at0022-filter//div[contains(@class, 'search popup-member-filter')]//button[@type='submit']")).click();
        }

        if (WebUI.waitForElementPresent(By.xpath(xpathAllRecords), 5) != null) {
            ExecutionHelper.runStepWithLoggingByPhase(setting, "AT0022 - Reject Day off Request -> Click & Confirm ", () ->{
                List<WebElement> requestRowElements = WebUI.findWebElementsIfVisible(By.xpath(xpathAllRecords));
                for (int i = 0; i < requestRowElements.size(); i++) {
                    By rejectButton = By.xpath(xpathAllRecords + "[1]//span[normalize-space(text())='非承認']/ancestor::button");

                    WebElement rejectElm = WebUI.waitForElementPresent(rejectButton, 3);
                    if (rejectElm == null) continue;
                    WebUI.clickWithScrollTo(rejectElm);

                    WebUI.findWebElementIfVisible(By.xpath(XpathCommon.MODAL_CONFIRM_BTN.value)).click();

                    WebDriverWait wait = new WebDriverWait(DriverFactory.getDriver(), Duration.ofSeconds(5));
                    WebElement loader = DriverFactory.getDriver().findElement(By.xpath("//app-loader-empty")); // cần đúng xpath tương ứng với 'app-loader-empty'
                    wait.until(ExpectedConditions.attributeToBe(loader, "ng-reflect-is-show-loading", "false"));
                }

                String xpathAT0022HeaderSelectStatus = "//*[@id=\"tab-content4\"]//app-at0022//p[normalize-space(text())='ステータスを選択']/following-sibling::select";
                WebElement dropdownElementAT0022HeaderSelectStatus  = DriverFactory.getDriver().findElement(By.xpath(xpathAT0022HeaderSelectStatus));
                Select dropdownAT0022HeaderSelectStatus  = new Select(dropdownElementAT0022HeaderSelectStatus );
                dropdownAT0022HeaderSelectStatus.selectByValue("RS_REJECTED");
            });
        }
    }

    @ExecutionStep(value = "removeAllDownloadTemplate")
    public static void removeAllDownloadTemplate(ExportTemplateFilterSetting setting) {
        AttendanceUtils.navigateToATPage("at0059");

        String xpathRow = "//app-at0059//div[@id='tbl-sheet']/table//tr";
        try {
            List<WebElement> templateRecord = WebUI.findWebElementsIfVisible(By.xpath(xpathRow), 3);

            for (int i = 1; i <= templateRecord.size(); i++) {
                String xpathRemoveBtn = xpathRow + "[1]/td[7]/button";
                WebUI.findWebElementIfVisible(By.xpath(xpathRemoveBtn)).click();
                WebUI.sleep(500);
                WebUI.findWebElementIfVisible(By.xpath(XpathCommon.MODAL_CONFIRM_BTN.value)).click();
                WebUI.sleep(500);
            }
        } catch (TimeoutException timeoutException) {
            System.out.println("Không tìm thấy element download template màn AT0059 để xóa, tiếp tục chạy...");
        }
    }

    @ExecutionStep(value = "createNewDownloadTemplate")
    public static void createNewDownloadTemplate(ExportTemplateFilterSetting setting) {
        AttendanceUtils.navigateToATPage("at0059");
        waitForLoadingElement();

        // Xóa toàn bộ template hiện có
        removeAllDownloadTemplate(setting);

        List<DownloadTemplate> downloadTemplate = ExcelReaderRepository.findAllDownloadTemplate(setting.getSheetName());

        // Lọc và nhóm dữ liệu theo mode
        Map<String, List<DownloadTemplate>> mapGroupingByTemplateMode = downloadTemplate.stream()
                .collect(Collectors.groupingBy(DownloadTemplate::getMode));

        // Duyệt từng group để tạo template
        for (Map.Entry<String, List<DownloadTemplate>> entry : mapGroupingByTemplateMode.entrySet()) {

            // Vào màn AT0060 tạo mới template
            WebUI.findWebElementIfVisible(By.xpath("//button[@class='btn btn-success' and text()='新規作成']")).click();

            waitForLoadingElement();

            String mode = entry.getKey();
            WebUI.findWebElementIfVisible(By.xpath("//input[@placeholder=\"テンプレート名称\"]"))
                    .sendKeys(format(AttendanceConstants.TEMPLATE_NAME, mode.toUpperCase()));

            switch (mode.toLowerCase()) {
                case "day":
                    WebUI.findWebElementIfVisible(By.xpath("//button[normalize-space(text())='日単位']")).click();
                    break;
                case "month":
                    WebUI.findWebElementIfVisible(By.xpath("//button[normalize-space(text())='月単位']")).click();
                    break;
                default:
                    return;
            }
            boolean option1 = "TRUE".equals(setting.getTemplateOp1());
            boolean option2 = "TRUE".equals(setting.getTemplateOp2());
            boolean option3 = "TRUE".equals(setting.getTemplateOp3());

            if (option1 || option2 || option3) {
                String openSettingBtnXpath = "//span[text()='出力時の詳細条件']/ancestor::div[contains(@class, 'cal-setting')]";
                By openSettingBtn = By.xpath(openSettingBtnXpath);
                WebUI.waitForElementClickable(openSettingBtn, 1);
                WebUI.click(openSettingBtn);
            }

            if (option1) {
                System.out.println("option1: " + option1 + " -> Turn on 所定時間に満たない残業（勤務）申請時間を割増に含める");
                String op1Xpath = "//span[text()='所定時間に満たない残業（勤務）申請時間を割増に含める']/ancestor::label";
                WebUI.findWebElementIfVisible(By.xpath(op1Xpath)).click();
            }

            if (option2) {
                System.out.println("option2: " + option2 + " -> Turn on 日をまたいだ労働時間を翌日分に計上する");
                String op2Xpath = "//span[text()='日をまたいだ労働時間を翌日分に計上する']/ancestor::label";
                WebUI.findWebElementIfVisible(By.xpath(op2Xpath)).click();
            }

            if (option3) {
                System.out.println("option3: " + option3 + " -> Turn on 週残業を割増に含める");
                String op3Xpath = "//span[text()='週残業を割増に含める']/ancestor::label";
                WebUI.findWebElementIfVisible(By.xpath(op3Xpath)).click();
            }

            String queryXpathByTitle = "//app-at0060//div[@class='select-col1']//ul//div[@class='text-title' and text()='%s']";

            List<DownloadTemplate> templateItemList = entry.getValue();
            for (DownloadTemplate item : templateItemList) {
                String optionLabel = item.getOption();
                WebElement optionElement = WebUI.findWebElementIfVisible(
                        By.xpath("//app-at0060//div[@class='select-col1']//select"));
                Select optionSelect = new Select(optionElement);
                optionSelect.selectByVisibleText(optionLabel);

                String itemTitle = org.apache.commons.lang3.StringUtils.trimToNull(item.getTitle());
                if (itemTitle != null) {
                    WebElement targetItem = WebUI.findWebElementIfVisible(
                            By.xpath(format(queryXpathByTitle, itemTitle)));
                    targetItem.click();
                }
            }

            WebUI.findWebElementIfVisible(By.xpath("//button[@type=\"button\" and @class=\"btn btn-primary\" and text()='作成']")).click();
            WebUI.findWebElementIfVisible(By.xpath("//app-modal//button[@id='positiveButton']")).click();
            waitForLoadingElement();
        }
        WebUI.sleep(1000);
    }

    @ExecutionStep(value = "downloadTemplate")
    public static void downloadTemplate(ExportTemplateFilterSetting setting) {
        AttendanceUtils.navigateToATPage("at0029");

        waitForLoadingElement();
        WebElement selectBoxDownloadType = WebUI.findWebElementIfVisible(
                By.xpath("//app-at0029//div[@class=\"type-export-csv\"]//div[@class='col-12 row']//select"));
        Select optionSelect = new Select(selectBoxDownloadType);
        optionSelect.selectByVisibleText("テンプレート");

        List<DownloadTemplate> downloadTemplate = ExcelReaderRepository.findAllDownloadTemplate(setting.getSheetName());

        Map<String, List<DownloadTemplate>> mapGroupingByTemplateMode = downloadTemplate.stream()
                .collect(Collectors.groupingBy(DownloadTemplate::getMode));

        boolean isSelectedDepartment = false;
        for (Map.Entry<String, List<DownloadTemplate>> entry : mapGroupingByTemplateMode.entrySet()) {
            String mode = entry.getKey();
            String templateName = format(AttendanceConstants.TEMPLATE_NAME, mode.toUpperCase());

            waitForLoadingElement();
            WebElement templateSelectBox = WebUI.findWebElementIfPresent(
                    By.xpath("//app-at0029//div[@class='type-export-csv']//div[@class='row']/div[3]/select"));
            Select selectTemplate = new Select(templateSelectBox);
            selectTemplate.selectByVisibleText(templateName);

            String[] splitedMonthYear = DateUtils.splitMonthYear(setting.getTargetMonth());
            String targetSYear = splitedMonthYear[0];
            String targetEYear = targetSYear;

            String targetSMonth = String.valueOf(Integer.parseInt(splitedMonthYear[1]));
            String targetEMonth = targetSMonth;

            switch (mode.toLowerCase()) {
                case "day":
                    LocalDate firstDay = LocalDate.of(Integer.parseInt(targetSYear), Integer.parseInt(targetSMonth), 1);
                    LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());
                    String minDay = String.valueOf(firstDay.getDayOfMonth());
                    String maxDay = String.valueOf(lastDay.getDayOfMonth());
                    String startDate = setting.getStartDate();
                    String endDate = setting.getEndDate();

                    if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
                        targetSYear = startDate.substring(0, 4);
                        targetEYear = endDate.substring(0, 4);

                        targetSMonth = String.valueOf(Integer.parseInt(startDate.substring(4, 6)));
                        targetEMonth = String.valueOf(Integer.parseInt(endDate.substring(4, 6)));

                        minDay = String.valueOf(Integer.parseInt(startDate.substring(6, 8)));
                        maxDay = String.valueOf(Integer.parseInt(endDate.substring(6, 8)));
                    }

                    WebElement inputStartTime = WebUI.findWebElementIfVisible(
                            By.xpath("//div[@class='time-request']/div[@class='time'][1]/app-date-input-at"));
                    DateUtils.chooseDatePicker(inputStartTime, targetSYear, targetSMonth, minDay);

                    WebElement inputEndTime = WebUI.findWebElementIfVisible(
                            By.xpath("//div[@class='time-request']/div[@class='time'][2]/app-date-input-at"));
                    DateUtils.chooseDatePicker(inputEndTime, targetEYear, targetEMonth, maxDay);
                    break;
                case "month":
                    WebElement selectMonth = WebUI.findWebElementIfVisible(
                            By.xpath("//app-at0029//form//select[@formcontrolname='month' and @ng-reflect-name='month']"));
                    WebUI.sleep(2000);
                    Select selectMonthBox = new Select(selectMonth);
                    selectMonthBox.selectByVisibleText(DateUtils.convertToYearMonthAT0029(setting.getTargetMonth()));
                    break;
                default:
                    return;
            }

            String department = setting.getTargetUserDepartment();
            if (StringUtils.isNotBlank(department) && !isSelectedDepartment) {
                WebElement deptSelectBox = WebUI.findWebElementIfVisible(
                        By.xpath("//app-multi-select-department//div[contains(@class, 'dept-name')]"));
                deptSelectBox.click();
                WebUI.sleep(500);

                String deptItemXpath = "//app-multi-select-department//div[contains(@class, 'popup-list-department')]//span[contains(text(), '%s')]/ancestor::div/label";
                WebElement allDeptItem = WebUI.findWebElementIfVisible(
                        By.xpath(format(deptItemXpath + "/input[@type='checkbox' and @ng-reflect-model='true']/ancestor::label", "すべて")));
                allDeptItem.click();
                WebUI.sleep(500);

                WebElement targetDeptItem = WebUI.findWebElementIfVisible(
                        By.xpath(format(deptItemXpath + "/input[@type='checkbox' and " +
                                "@ng-reflect-model='false']/ancestor::label", department)));
                targetDeptItem.click();
                WebUI.sleep(500);

                deptSelectBox.click();
                isSelectedDepartment = true;
            }

            String jobType = setting.getTargetUserJobType();
            if (StringUtils.isNotBlank(jobType)) {
                WebElement jobTypeSelectBox = WebUI.findWebElementIfVisible(
                        By.xpath("//app-at0029//select[@ng-reflect-name='jobTypeId']"));
                Select jobtypeSelect = new Select(jobTypeSelectBox);
                jobtypeSelect.selectByVisibleText(AttendanceUtils.getJobTypeName(jobType.toUpperCase()));
            }

            String workForm = setting.getTargetUserWorkForm();
            if (StringUtils.isNotBlank(workForm)) {
                WebElement workFormSelect = WebUI.findWebElementIfVisible(
                        By.xpath("//app-at0029//select[@ng-reflect-name='contractType']"));
                Select workFormSelectBox = new Select(workFormSelect);
                workFormSelectBox.selectByVisibleText(AttendanceUtils.getWorkFormName(workForm.toUpperCase()));
            }

            String workPattern = setting.getTargetUserWorkPattern();
            if (StringUtils.isNotBlank(workPattern)) {
                WebElement workPatternSelect = WebUI.findWebElementIfVisible(
                        By.xpath("//app-at0029//select[@ng-reflect-name='workingPattern']"));
                Select workPatternSelectBox = new Select(workPatternSelect);
                workPatternSelectBox.selectByVisibleText(AttendanceUtils.getWorkPatternName(workPattern.toUpperCase()));
            }

            WebElement btnCreate = WebUI.findWebElementIfVisible(
                    By.xpath("//button[@class=\"btn btn-primary btn-create-csv\" and normalize-space(text())='作成']"));
            btnCreate.click();
            WebUI.sleep(2000);
            WebElement btnReload = WebUI.findWebElementIfVisible(
                    By.xpath("//button[@class=\"btn btn-secondary btn-repeat\" and @data-original-title='更新']"));
            btnReload.click();

            int count = 0;
            By downloadButton = By.xpath("//div[@id='tbl-sheet']//table//tr[1]/td[1]/button[@type='button' and normalize-space(text())='ダウンロード']");
            while (!WebUI.isElementPresent(downloadButton, 200)) {
                if (count >= 5) break;
                WebElement btnReloadTmp = WebUI.findWebElementIfVisible(
                        By.xpath("//button[@class=\"btn btn-secondary btn-repeat\" and @data-original-title='更新']"));
                btnReloadTmp.click();
                WebUI.sleep(1000);
                count++;
            }

            WebUI.waitForElementClickable(downloadButton, 2);
            WebUI.click(downloadButton);
            WebUI.sleep(5000);
        }
    }

    public static void handleDiscretionarySchedule(ExportTemplateFilterSetting setting, List<WorkSchedule> workScheduleList, List<WebElement> dateElements, String baseXpath) {
        WebDriverWait wait = new WebDriverWait(DriverFactory.getDriver(), Duration.ofSeconds(5));

        String ancestorPathButtonAddLink = "/ancestor::button[contains(@class, 'btn btn-link add-link') and not(contains(@class, 'd-none'))]";
        String timePickerXpath = "//app-at0036b-time-picker//ul//li[@data-value='%s']";

        for (WorkSchedule workSchedule : workScheduleList) {
            String dateIndexStr = workSchedule.getDayIndex();
            int dateIndex = Integer.parseInt(dateIndexStr);

            if (dateIndex > dateElements.size()) {
                continue;
            }

            // Handle DayType
            String dayType = workSchedule.getDayType();
            ExecutionHelper.runStepWithLoggingByPhase(setting, format("Day %s, dayType %s", dateIndexStr, dayType), () -> {
                if (StringUtils.isNotBlank(dayType)) {
                    By dayTypeSelectBox = By.xpath(baseXpath + "[" + dateIndex + "]/td[3]/select");

                    try {
                        WebElement selectElement = wait.until(ExpectedConditions.presenceOfElementLocated(dayTypeSelectBox));
                        Select select = new Select(selectElement);
                        Pattern pattern = Pattern.compile(".*" + Pattern.quote(dayType) + ".*", Pattern.CASE_INSENSITIVE);

                        for (WebElement option : select.getOptions()) {
                            String text = option.getText().trim();
                            if (pattern.matcher(text).matches()) {
                                select.selectByVisibleText(text);
                                break;
                            }
                        }
                    } catch (TimeoutException e) {
                        // not found, skip
                        TaskLoggerManager.error("Xpath: {}", baseXpath + "[" + dateIndex + "]/td[3]/select");
                    }
                }
            });

            // WorkingTimeType - Handle StartTime - EndTime
            ExecutionHelper.runStepWithLoggingByPhase(setting, "Remove all working time before handle", () -> {
                By deleteLocator = By.xpath(
                    baseXpath
                        + "[" + dateIndex + "]"
                        + "/td[4]//i[contains(@class, 'fa fa-times')]/ancestor::button[not(@disabled)][1]"
                );
                while (WebUI.isElementPresent(deleteLocator, 500L)) {
                    WebUI.findWebElementIfVisible(deleteLocator).click();
                }
            });
            String workingTimeType = workSchedule.getWorkingTimeType();
            String startTime = workSchedule.getStartTime();
            String endTime = workSchedule.getEndTime();
            if (workingTimeType != null && !workingTimeType.isEmpty()
                && startTime != null && !startTime.isEmpty()
                && endTime != null && !endTime.isEmpty()) {
                ExecutionHelper.runStepWithLoggingByPhase(setting,
                    format("Day %s - WorkingTimeType: [%s], Working time: [%s-%s]",
                        dateIndex, workingTimeType, startTime, endTime),
                    () -> {
                        String[] wttArr = workingTimeType.split(",");
                        String[] startTimesArr = startTime.split(",");
                        String[] endTimesArr = endTime.split(",");

                        int loopCount = wttArr.length;
                        for (int i = 0; i < loopCount; i++) {
                            String workingTimeTypeRowText = wttArr[i];
                            String startTimeRowText = startTimesArr[i];
                            String endTimeRowText = endTimesArr[i];

                            String baseRowFormat = "%s[%s]/td[4]/div/div[%d]%s";
                            By wttBy = By.xpath(format(baseRowFormat, baseXpath, dateIndex, (i + 1), "//select"));

                            if (i > 0 || WebUI.isElementNotPresent(wttBy, 500L)) {
                                By addButtonLocator = By.xpath(
                                    format("%s[%s]/td[4]//i[contains(@class, 'fa-plus-square')]%s", baseXpath, dateIndex, ancestorPathButtonAddLink)
                                );
                                WebElement addBtn = WebUI.findWebElementIfPresent(addButtonLocator);
                                WebUI.clickWithScrollTo(addBtn);
                            }

                            // Handle working time type
                            WebElement wttSelectElement = WebUI.findWebElementIfPresent(wttBy);
                            Select wttSelect = new Select(wttSelectElement);
                            wttSelect.selectByVisibleText(workingTimeTypeRowText);

                            // Handle start time
                            WebElement startTimeInput = WebUI.findWebElementIfPresent(
                                By.xpath(format(baseRowFormat, baseXpath, dateIndex, (i + 1), "//div[contains(@class, 'tbl-select-time')]//input[1]"))
                            );
                            WebUI.scrollToElementCenter(startTimeInput);
                            startTimeInput.click();

                            String formattedSTime = startTimeRowText.substring(0, 2) + ":" + startTimeRowText.substring(2);
                            WebElement stTimeOption = WebUI.findWebElementIfPresent(By.xpath(format(timePickerXpath, formattedSTime)));
                            stTimeOption.click();

                            // Handle end time
                            WebElement endTimeInput = WebUI.findWebElementIfPresent(
                                By.xpath(format(baseRowFormat, baseXpath, dateIndex, (i + 1), "//div[contains(@class, 'tbl-select-time')]//input[2]"))
                            );
                            endTimeInput.click();
                            String formattedETime = endTimeRowText.substring(0, 2) + ":" + endTimeRowText.substring(2);
                            WebElement etTimeOption = WebUI.findWebElementIfPresent(By.xpath(format(timePickerXpath, formattedETime)));
                            etTimeOption.click();
                        }
                    });
            }

            // Handle StartBreakTime
            String startBreakTime = workSchedule.getStartBreakTime();
            String endBreakTime = workSchedule.getEndBreakTime();
            ExecutionHelper.runStepWithLoggingByPhase(setting, "Remove all break time before handle", () -> {
                By deleteBreakLocator = By.xpath(
                    baseXpath
                        + "[" + dateIndex + "]"
                        + "/td[5]//i[contains(@class, 'fa fa-times')]/ancestor::button"
                );
                while (WebUI.isElementPresent(deleteBreakLocator, 1)) {
                    WebUI.findWebElementIfVisible(deleteBreakLocator).click();
                }
            });
            if (startBreakTime != null && !startBreakTime.isEmpty() && endBreakTime != null && !endBreakTime.isEmpty()) {
                ExecutionHelper.runStepWithLoggingByPhase(setting, format("Day %s - Break time: [%s-%s]", dateIndex, startBreakTime, endBreakTime), () -> {
                    String[] startBTimesArr = startBreakTime.split(",");
                    String[] endBTimesArr = endBreakTime.split(",");

                    int loopCount = Math.min(startBTimesArr.length, endBTimesArr.length);
                    String baseFormat = "%s[%s]/td[5]/div/div[%d]%s";

                    for (int i = 0; i < loopCount; i++) {
                        String st = startBTimesArr[i];
                        String et = endBTimesArr[i];

                        By startBreakInputLocator = By.xpath(format(baseFormat, baseXpath, dateIndex, i+1, "//input[1]"));
                        if (i > 0 || WebUI.isElementNotPresent(startBreakInputLocator, 500)) {
                            By addBreakLocator = By.xpath(
                                baseXpath
                                    + "[" + (dateIndex) + "]"
                                    + "/td[5]//i[contains(@class, 'fa-plus-square')]"
                                    + ancestorPathButtonAddLink
                            );
                            WebUI.findWebElementIfVisible(addBreakLocator).click();
                        }

                        WebElement startBreakInput = DriverFactory.getDriver().findElement(startBreakInputLocator);
                        startBreakInput.click();
                        String formattedTime = st.substring(0, 2) + ":" + st.substring(2);
                        WebElement stBreakTimeOption = WebUI.findWebElementIfVisible(
                            By.xpath(format(timePickerXpath, formattedTime))
                        );
                        stBreakTimeOption.click();

                        WebElement endBreakInput = WebUI.findWebElementIfVisible(By.xpath(format(baseFormat, baseXpath, dateIndex, i+1, "//input[1]")));
                        endBreakInput.click();
                        String formattedEBTime = et.substring(0, 2) + ":" + et.substring(2);
                        WebElement etBreakTimeOption = WebUI.findWebElementIfVisible(
                            By.xpath(format(timePickerXpath, formattedEBTime))
                        );
                        etBreakTimeOption.click();
                    }
                });
            }
        }
    }

    public static void handleIndividualAndVariableSchedule(
            ExportTemplateFilterSetting setting,
            List<WorkSchedule> workScheduleList,
            List<WebElement> dateElements,
            String baseXpath
    ) {
        WebDriverWait wait = new WebDriverWait(DriverFactory.getDriver(), Duration.ofSeconds(5));

        for (WorkSchedule workSchedule : workScheduleList) {
            String dateIndex = workSchedule.getDayIndex();
            if (Integer.parseInt(dateIndex) > dateElements.size()) continue;

            String presetName = workSchedule.getPresetName();
            if (presetName != null && !presetName.isEmpty()) {
                ExecutionHelper.runStepWithLoggingByPhase(setting, format("Day %s - Preset name: %s", dateIndex, presetName), () -> {
                    By presetSelectLocator = By.xpath(baseXpath + "[" + dateIndex + "]/td[3]/select");
                    try {
                        WebElement presetSelectBox = wait.until(ExpectedConditions.presenceOfElementLocated(presetSelectLocator));
                        Select presetSelect = new Select(presetSelectBox);
                        presetSelect.selectByVisibleText(presetName);
                    } catch (TimeoutException e) {
                        // Ignore if not found
                    }
                });

                continue;
            }

            String dayType = workSchedule.getDayType();
            if (dayType != null && !dayType.isEmpty()) {
                ExecutionHelper.runStepWithLoggingByPhase(setting, format("Day %s - Day Type: %s", dateIndex, dayType), () -> {
                    By dayTypeSelectLocator = By.xpath(baseXpath + "[" + dateIndex + "]/td[4]/select");
                    try {
                        WebElement dayTypeSelectBox = wait.until(ExpectedConditions.presenceOfElementLocated(dayTypeSelectLocator));
                        Select dayTypeSelect = new Select(dayTypeSelectBox);
                        for (WebElement option : dayTypeSelect.getOptions()) {
                            if (option.getAttribute("value").contains(": " + dayType)) {
                                dayTypeSelect.selectByVisibleText(option.getText());
                                break;
                            }
                        }
                    } catch (TimeoutException e) {
                        // Ignore
                    }
                });
            }

            ExecutionHelper.runStepWithLoggingByPhase(setting, "Remove all working time before handle", () -> {
                By deleteLocator = By.xpath(
                    baseXpath
                        + "[" + dateIndex + "]"
                        + "/td[5]//i[contains(@class, 'fa fa-times')]/ancestor::button[not(@disabled)][1]"
                );
                while (WebUI.isElementPresent(deleteLocator, 1)) {
                    WebUI.findWebElementIfVisible(deleteLocator).click();
                }
            });

            String workingTimeType = workSchedule.getWorkingTimeType();
            String startTime = workSchedule.getStartTime();
            String endTime = workSchedule.getEndTime();
            if (workingTimeType != null && !workingTimeType.isEmpty()
                && startTime != null && !startTime.isEmpty()
                && endTime != null && !endTime.isEmpty()) {
                ExecutionHelper.runStepWithLoggingByPhase(setting,
                    format("Day %s - WorkingTimeType: [%s], Working time: [%s-%s]",
                    dateIndex, workingTimeType, startTime, endTime),
                    () -> {
                    String[] wttArr = workingTimeType.split(",");
                    String[] startTimesArr = startTime.split(",");
                    String[] endTimesArr = endTime.split(",");

                    int loopCount = wttArr.length;
                    for (int i = 0; i < loopCount; i++) {
                        String workingTimeTypeRowText = wttArr[i];
                        String startTimeRowText = startTimesArr[i];
                        String endTimeRowText = endTimesArr[i];

                        String baseRowFormat = "%s[%s]/td[5]/div/div[%d]%s";
                        By wttBy = By.xpath(format(baseRowFormat, baseXpath, dateIndex, (i + 1), "//select"));

                        if (i > 0 || WebUI.isElementNotPresent(wttBy, 500L)) {
                            String ancestorPath = "WORKING".equals(dayType)
                                ? "/ancestor::td/div/div[1]//div[contains(@class, 'tbl-select-time')]/button"
                                : "/ancestor::td/div/div[contains(@class, 'working-time-controls-not-working')]/button";

                            By addButtonLocator = By.xpath(baseXpath + "[" + dateIndex + "]/td[5]//i[contains(@class, 'fa-plus-square')]" + ancestorPath);
                            WebUI.findWebElementIfPresent(addButtonLocator).click();
                        }

                        // Handle working time type
                        WebElement wttSelectElement = WebUI.findWebElementIfPresent(wttBy);
                        Select wttSelect = new Select(wttSelectElement);
                        wttSelect.selectByVisibleText(workingTimeTypeRowText);

                        // Handle start time
                        WebElement startTimeInput = WebUI.findWebElementIfPresent(
                            By.xpath(format(baseRowFormat, baseXpath, dateIndex, (i + 1), "//div[contains(@class, 'tbl-select-time')]//input[1]"))
                        );
                        WebUI.scrollToElementCenter(startTimeInput);
                        startTimeInput.click();

                        String formattedSTime = startTimeRowText.substring(0, 2) + ":" + startTimeRowText.substring(2);
                        WebElement stTimeOption = WebUI.findWebElementIfPresent(By.xpath(format("//app-at0035b-time-picker//ul//li[@data-value='%s']", formattedSTime)));
                        stTimeOption.click();

                        // Handle end time
                        WebElement endTimeInput = WebUI.findWebElementIfPresent(
                            By.xpath(format(baseRowFormat, baseXpath, dateIndex, (i + 1), "//div[contains(@class, 'tbl-select-time')]//input[2]"))
                        );
                        endTimeInput.click();
                        String formattedETime = endTimeRowText.substring(0, 2) + ":" + endTimeRowText.substring(2);
                        WebElement etTimeOption = WebUI.findWebElementIfPresent(By.xpath(format("//app-at0035b-time-picker//ul//li[@data-value='%s']", formattedETime)));
                        etTimeOption.click();
                    }
                });
            } else {
                By deleteButtonLocator = By.xpath(baseXpath + "[" + dateIndex + "]/td[5]//i[contains(@class, 'fa fa-times')]/ancestor::button");
                List<WebElement> deleteButtons = DriverFactory.getDriver().findElements(deleteButtonLocator);
                if (!deleteButtons.isEmpty()) {
                    deleteButtons.get(0).click();
                }
            }

            String startBreakTime = workSchedule.getStartBreakTime();
            String endBreakTime = workSchedule.getEndBreakTime();
            ExecutionHelper.runStepWithLoggingByPhase(setting, "Remove all break time before handle", () -> {
                By deleteBreakLocator = By.xpath(
                    baseXpath
                        + "[" + dateIndex + "]"
                        + "/td[6]//i[contains(@class, 'fa fa-times')]/ancestor::button"
                );
                while (WebUI.isElementPresent(deleteBreakLocator, 1)) {
                    WebUI.findWebElementIfVisible(deleteBreakLocator).click();
                }
            });
            if (startBreakTime != null && !startBreakTime.isEmpty() && endBreakTime != null && !endBreakTime.isEmpty()) {
                ExecutionHelper.runStepWithLoggingByPhase(setting, format("Day %s - Break time: [%s-%s]", dateIndex, startBreakTime, endBreakTime), () -> {
                    String[] startBTimesArr = startBreakTime.split(",");
                    String[] endBTimesArr = endBreakTime.split(",");

                    int loopCount = Math.min(startBTimesArr.length, endBTimesArr.length);

                    for (int i = 0; i < loopCount; i++) {
                        String st = startBTimesArr[i];
                        String et = endBTimesArr[i];

                        By startBreakInputLocator = By.xpath(baseXpath + "[" + (dateIndex) + "]/td[6]/div/div[" + (i + 1) + "]//input[1]");
                        if (!WebUI.isElementPresent(startBreakInputLocator, 1) || i > 0) {
                            boolean isAT0034 = DriverFactory.getDriver().getCurrentUrl().contains("/at/at0034b/");

                            String ancestorPath = "";
                            if (isAT0034) {
                                ancestorPath = i > 0
                                    ? "/ancestor::div[contains(@class, 'tbl-select-time')]/button"
                                    : "/ancestor::div/button[2]";
                            } else {
                                ancestorPath = i > 0
                                    ? "/ancestor::div[@class='tbl-select-time d-flex']/button"
                                    : "/ancestor::button[contains(@class, 'ml-auto')]";
                            }

                            By addBreakLocator = By.xpath(
                                baseXpath
                                    + "[" + (dateIndex) + "]"
                                    + "/td[6]//i[contains(@class, 'fa-plus-square')]"
                                    + ancestorPath
                            );
                            WebUI.findWebElementIfVisible(addBreakLocator).click();
                        }

                        WebElement startBreakInput = DriverFactory.getDriver().findElement(startBreakInputLocator);
                        startBreakInput.click();
                        String formattedTime = st.substring(0, 2) + ":" + st.substring(2);
                        WebElement stBreakTimeOption = WebUI.findWebElementIfVisible(
                            By.xpath(format("//app-at0035b-time-picker//ul//li[@data-value='%s']", formattedTime))
                        );
                        stBreakTimeOption.click();

                        WebElement endBreakInput = WebUI.findWebElementIfVisible(By.xpath(baseXpath + "[" + dateIndex + "]/td[6]//div[@ng-reflect-name='" + i + "']//input[2]"));
                        endBreakInput.click();
                        String formattedEBTime = et.substring(0, 2) + ":" + et.substring(2);
                        WebElement etBreakTimeOption = WebUI.findWebElementIfVisible(
                            By.xpath(format("//app-at0035b-time-picker//ul//li[@data-value='%s']", formattedEBTime))
                        );
                        etBreakTimeOption.click();
                    }
                });
            }
        }
    }
}
