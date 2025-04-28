package com.drjoy.automation.service;

import com.drjoy.automation.config.DriverFactory;
import com.drjoy.automation.model.CheckingLog;
import com.drjoy.automation.model.ExportTemplateFilterSetting;
import com.drjoy.automation.model.Request;
import com.drjoy.automation.model.WorkSchedule;
import com.drjoy.automation.repository.ExcelReaderRepository;
import com.drjoy.automation.utils.AttendanceUtils;
import com.drjoy.automation.utils.DateUtils;
import com.drjoy.automation.utils.WebUI;
import com.drjoy.automation.utils.xpath.common.XpathCommon;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.drjoy.automation.utils.AttendanceUtils.waitForLoadingElement;
import static com.drjoy.automation.utils.AttendanceUtils.waitForLoadingOverlayElement;

public class AttendanceService {

    public static void removeAllCheckingLogInTimeSheetPage(ExportTemplateFilterSetting setting) {
        AttendanceUtils.navigateToATPage("at0001");
        waitForLoadingElement();

        AttendanceUtils.selectUserAndMonthOnTimesheetPage(setting.getTargetUser(), setting.getTargetMonth());

        String xpathRowAt0001 = "//app-at0001//div[@id='tbl-sheet']/table/tbody//tr";
        List<WebElement> dayElements = WebUI.findWebElementsIfPresent(By.xpath(xpathRowAt0001));

        for (int i = 0; i < dayElements.size(); i++) {
            String xpathRow = String.format("%s[%d]", xpathRowAt0001, i + 1);

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
            removeAllCheckingLogInPage();

            waitForLoadingOverlayElement();

            // Quay lại AT0001
            WebElement backButton = WebUI.findWebElementIfVisible(By.xpath(XpathCommon.PAGE_BACK_BTN.value));
            backButton.click();
        }
    }

    public static void removeAllCheckingLogInPage() {
        String acceptCheckingLogButtonXpath = "//app-at0023//form[@id='checking-log']//table//tbody/tr[1]/td[last()-1]//button[normalize-space(text())='確定']";

        if (WebUI.waitForElementPresent(By.xpath(acceptCheckingLogButtonXpath), 5) != null) {
            return;
        }

        WebElement requestButton = WebUI.findWebElementIfVisible(By.xpath("//*[@id='checking-log']/div/table/tbody/tr[1]/td[6]/div/button"));
        requestButton.click();

        while (WebUI.waitForElementPresent(By.xpath(XpathCommon.MODAL_CONFIRM_WITH_JP_TEXT_BTN.value), 2) != null) {
            WebElement confirmButton = WebUI.findWebElementIfVisible(By.xpath(XpathCommon.MODAL_CONFIRM_WITH_JP_TEXT_BTN.value));
            confirmButton.click();
            waitForLoadingElement();
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

    public static void addAllCheckingLogs(ExportTemplateFilterSetting setting) {
        AttendanceUtils.navigateToATPage("at0001");
        waitForLoadingElement();

        AttendanceUtils.selectUserAndMonthOnTimesheetPage(setting.getTargetUser(), setting.getTargetMonth());

        List<CheckingLog> allLogs = ExcelReaderRepository.findAllCheckingLog();
        // Lọc theo phase
        Map<String, List<CheckingLog>> logsGroupedByDay = allLogs.stream()
            .filter(log -> setting.getPhase().equals(log.getPhase()))
            .filter(log -> log.getDateIndex() != null && !log.getDateIndex().isEmpty())
            .collect(Collectors.groupingBy(CheckingLog::getDateIndex));

        String xpathRowAT0001 = "//app-at0001//div[@id='tbl-sheet']/table/tbody//tr[%s]";
        for (Map.Entry<String, List<CheckingLog>> entry : logsGroupedByDay.entrySet()) {
            String dateIndex = entry.getKey();
            List<CheckingLog> logsInDay = entry.getValue();

            String xpathRow = String.format(xpathRowAT0001, dateIndex);
            WebElement editButton = WebUI.findWebElementIfVisible(By.xpath(xpathRow + "/td[last()-1]/button"));
            if (editButton != null) {
                editButton.click();
                WebUI.sleep(1000);
            }

            // Nhập checking log
            for (int i = 0; i < logsInDay.size(); i++) {
                CheckingLog log = logsInDay.get(i);
                String xpathLogTime = String.format("//app-at0023//form[@id='checking-log']//table//tbody/tr[%d]/td[1]//input", i + 1);
                String xpathReason = String.format("//app-at0023//form[@id='checking-log']//table//tbody/tr[%d]/td[3]//textarea", i + 1);

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

            // Gửi request
            addRequestByDateIndex(dateIndex, setting.getSheetName(), setting.getPhase());

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

    public static void addRequestByDateIndex(String dateIndex, String sheet, String phaseTest) {
        if (dateIndex == null || dateIndex.isEmpty()) return;

        waitForLoadingElement();

        List<Request> requestData = ExcelReaderRepository.findAllRequest(sheet); // sheet == null thì xử lý trong hàm này

        // Group by dateIndex
        Map<String, List<Request>> mapGroupingByDay = requestData.stream()
            .filter(row -> phaseTest.equals(row.getPhase()))
            .filter(row -> row.getDateIndex() != null && !row.getDateIndex().isEmpty())
            .collect(Collectors.groupingBy(Request::getDateIndex));

        handleOTAndResearchRequestByDateIndex(mapGroupingByDay, dateIndex);
        handleDayOffRequestByDateIndex(mapGroupingByDay, dateIndex);
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
                String curRowXpath = String.format("%s[%d]", requestRowXpath, i + 1);

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
        String reasonBtnXpath = String.format("%s//span[text()[normalize-space()='%s']]", reasonTypeBaseXPath, reasonType);
        WebElement reasonTypeBtn = WebUI.findWebElementIfVisible(By.xpath(reasonBtnXpath));
        WebUI.scrollToElementCenter(reasonTypeBtn);
        if (reasonTypeBtn.isEnabled()) {
            reasonTypeBtn.click();
        }

        // 休暇名 - Tên lý do nghỉ
        String reasonNameBaseXpath = "//app-at0024//div[text()='休暇名']/following-sibling::div";
        String reasonName = dayoffRequest.getReasonName().trim();
        String reasonNameXpath = String.format("%s//span[text()[normalize-space()='%s']]", reasonNameBaseXpath, reasonName);
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

    public static void addWorkSchedule(ExportTemplateFilterSetting setting) {
        AttendanceUtils.navigateToATPage("at0033");

        Map<String, List<WorkSchedule>> mapGroupingByPhase = new HashMap<>();

        List<WorkSchedule> workScheduleData = ExcelReaderRepository.findAllWorkSchedule(setting.getSheetName()); // sheet == null thì xử lý trong hàm này

        for (WorkSchedule row : workScheduleData) {
            if (row.getPhase() != null && row.getPhase().equals(setting.getPhase()) && row.getDayIndex() != null && !row.getDayIndex().toString().isEmpty()) {
                String phase = row.getPhase().toString();
                if (!mapGroupingByPhase.containsKey(phase)) {
                    mapGroupingByPhase.put(phase, new ArrayList<>());
                }
                mapGroupingByPhase.get(phase).add(row);
            }
        }

        waitForLoadingElement();
        for (Map.Entry<String, List<WorkSchedule>> entry : mapGroupingByPhase.entrySet()) {
            WebElement inputElementAT0033SearchName = DriverFactory.getDriver().findElement(By.xpath("//app-at0033//input[@placeholder=\"ユーザー名を検索\"]"));
            inputElementAT0033SearchName.clear();
            inputElementAT0033SearchName.sendKeys(setting.getTargetUser());

            WebUI.findWebElementIfVisible(By.xpath("//app-at0033//input[@placeholder=\"ユーザー名を検索\"]/following-sibling::button")).click();

            waitForLoadingElement();

            String xpathEditButton = String.format(
                    "//app-at0033//div[@id='tbl-sheet']//table/tbody//span[normalize-space(text())='%s']/ancestor::tr//button[normalize-space(text())='編集']",
                    setting.getTargetUser()
            );
            WebUI.findWebElementIfVisible(By.xpath(xpathEditButton)).click();
            waitForLoadingElement();

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

            String xpathDateInMonth = "//div[@id='tbl-sheet']//table/tbody/tr[@ng-reflect-name and @ng-reflect-ng-class]";
            waitForLoadingElement();
            List<WebElement> dateElements = WebUI.findWebElementsIfVisible(By.xpath(xpathDateInMonth));

            String curURL = "";
            try {
                curURL = DriverFactory.getDriver().getCurrentUrl();
            } catch (Exception e) {
                // Handle or log the error if needed
            }
            boolean isDiscretionaryScreen = curURL != null && curURL.contains("/at/at0036b/");
            if (isDiscretionaryScreen) {
                handleDiscretionarySchedule(entry.getValue(), dateElements, xpathDateInMonth);
            } else {
                handleIndividualAndVariableSchedule(entry.getValue(), dateElements, xpathDateInMonth);
            }

            if (WebUI.waitForElementClickable(By.xpath("//button[normalize-space(text())='保存' and not(@disabled)]"), 1) != null) {
                WebUI.findWebElementIfVisible(By.xpath("//button[normalize-space(text())='保存' and not(@disabled)]")).click();
                WebUI.findWebElementIfVisible(By.xpath("//button[@id=\"positiveButton\" and normalize-space(text())='はい']")).click();

                while (WebUI.findWebElementIfVisible(By.xpath("//button[@id='positiveButton']")) != null) {
                    WebUI.findWebElementIfVisible(By.xpath("//button[@id='positiveButton']")).click();
                    waitForLoadingElement();
                }
            }

            WebUI.findWebElementIfVisible(By.xpath("//a[@class='page-head-backlink']")).click();
        }
    }

    public static void approveAllRequest(ExportTemplateFilterSetting setting) {
        AttendanceUtils.navigateToATPage("at0022");

        // Check OT Request
        WebUI.findWebElementIfVisible(By.xpath("//*[@id='tab-content4']//app-at0022//button[normalize-space(text())='残業・研鑽申請一覧']")).click();

        String btnMemberFilterXpath = "//*[@id='tab-content4']//app-at0022//app-destination-member-filter";
        WebUI.findWebElementIfVisible(By.xpath(btnMemberFilterXpath)).click();

        WebElement inputElementMemberFilter = DriverFactory.getDriver().findElement(By.xpath(btnMemberFilterXpath + "//ngb-popover-window//input[@placeholder='メンバーを検索']"));
        inputElementMemberFilter.clear();
        inputElementMemberFilter.sendKeys(setting.getTargetUser());

        WebUI.findWebElementIfVisible(By.xpath(btnMemberFilterXpath + "//virtual-scroll//span[text()='" + setting.getTargetUser() + "']//ancestor::div[contains(@class, 'destination-popover-profile-wrap')]")).click();
        WebUI.findWebElementIfVisible(By.xpath("//app-at0022-filter//div[contains(@class, 'search popup-member-filter')]//button[@type='submit']")).click();

        String xpathSearchApplicationType = "//*[@id='tab-content4']//app-at0022//p[normalize-space(text())='申請種類を選択']/following-sibling::select";
        WebElement dropdownElementSearchApplicationType = DriverFactory.getDriver().findElement(By.xpath(xpathSearchApplicationType));
        Select dropdownSearchApplicationType = new Select(dropdownElementSearchApplicationType);
        dropdownSearchApplicationType.selectByVisibleText("すべて");

        String xpathSearchStatusButton = "//*[@id='tab-content4']//app-at0022//p[normalize-space(text())='ステータスを選択']/following-sibling::select";
        WebElement dropdownElementSearchStatusButton  = DriverFactory.getDriver().findElement(By.xpath(xpathSearchStatusButton));
        Select dropdownSearchStatusButton  = new Select(dropdownElementSearchStatusButton );
        dropdownSearchStatusButton.selectByValue("RS_NEW");

        String xpathAllRecords = "//app-at0022//div[@id='table-content']/table/tbody/tr";
        if (WebUI.waitForElementPresent(By.xpath(xpathAllRecords), 5) != null) {
            String checkboxChooseAllRecordXpath = "//app-at0022//div[@id='table-header']//tr[1]/th[1]";
            WebUI.findWebElementIfVisible(By.xpath(checkboxChooseAllRecordXpath)).click();

            String buttonApproveAllRequestXpath = "//app-at0022//button[normalize-space(text())='一括承認']";
            AttendanceUtils.clickAndConfirm(By.xpath(buttonApproveAllRequestXpath), 0);

            waitForLoadingElement();

            String xpathAT0022HeaderSelectStatus = "//*[@id=\"tab-content4\"]//app-at0022//p[normalize-space(text())='ステータスを選択']/following-sibling::select";
            WebElement dropdownElementAT0022HeaderSelectStatus  = DriverFactory.getDriver().findElement(By.xpath(xpathAT0022HeaderSelectStatus));
            Select dropdownAT0022HeaderSelectStatus  = new Select(dropdownElementAT0022HeaderSelectStatus );
            dropdownAT0022HeaderSelectStatus.selectByValue("RS_ACCEPTED");
        }

        // Check DayOff Request
        WebUI.findWebElementIfVisible(By.xpath("//*[@id='tab-content4']//app-at0022//button[normalize-space(text())='各種申請一覧']")).click();

        WebUI.findWebElementIfVisible(By.xpath(btnMemberFilterXpath)).click();

        inputElementMemberFilter.clear();
        inputElementMemberFilter.sendKeys(setting.getTargetUser());

        WebUI.findWebElementIfVisible(By.xpath(btnMemberFilterXpath + "//virtual-scroll//span[text()='" + setting.getTargetUser() + "']//ancestor::div[contains(@class, 'destination-popover-profile-wrap')]")).click();
        WebUI.findWebElementIfVisible(By.xpath("//app-at0022-filter//div[contains(@class, 'search popup-member-filter')]//button[@type='submit']")).click();

        dropdownSearchApplicationType.selectByVisibleText("すべて");
        dropdownSearchStatusButton.selectByValue("RS_NEW");

        if (WebUI.waitForElementPresent(By.xpath(xpathAllRecords), 3) != null) {
            String checkboxChooseAllRecordXpath = "//app-at0022//div[@id='table-header']//tr[1]/th[1]";
            WebUI.findWebElementIfVisible(By.xpath(checkboxChooseAllRecordXpath)).click();

            String buttonApproveAllRequestXpath = "//app-at0022//button[normalize-space(text())='一括承認']";
            AttendanceUtils.clickAndConfirm(By.xpath(buttonApproveAllRequestXpath), 0);

            waitForLoadingElement();

            String xpathAT0022HeaderSelectStatus = "//*[@id=\"tab-content4\"]//app-at0022//p[normalize-space(text())='ステータスを選択']/following-sibling::select";
            WebElement dropdownElementAT0022HeaderSelectStatus  = DriverFactory.getDriver().findElement(By.xpath(xpathAT0022HeaderSelectStatus));
            Select dropdownAT0022HeaderSelectStatus  = new Select(dropdownElementAT0022HeaderSelectStatus );
            dropdownAT0022HeaderSelectStatus.selectByValue("RS_ACCEPTED");
        }
    }

    public static void rejectAllRequest(ExportTemplateFilterSetting setting) {
        AttendanceUtils.navigateToATPage("at0022");

        // Handle OT request
        WebUI.findWebElementIfVisible(By.xpath("//*[@id='tab-content4']//app-at0022//button[normalize-space(text())='残業・研鑽申請一覧']")).click();

        String btnMemberFilterXpath = "//*[@id='tab-content4']//app-at0022//app-destination-member-filter";
        WebUI.findWebElementIfVisible(By.xpath(btnMemberFilterXpath)).click();

        WebElement inputElementMemberFilter = DriverFactory.getDriver().findElement(By.xpath(btnMemberFilterXpath + "//ngb-popover-window//input[@placeholder='メンバーを検索']"));
        inputElementMemberFilter.clear();
        inputElementMemberFilter.sendKeys(setting.getTargetUser());

        WebUI.findWebElementIfVisible(By.xpath(btnMemberFilterXpath + "//virtual-scroll//span[text()='" + setting.getTargetUser() + "']//ancestor::div[contains(@class, 'destination-popover-profile-wrap')]")).click();
        WebUI.findWebElementIfVisible(By.xpath("//app-at0022-filter//div[contains(@class, 'search popup-member-filter')]//button[@type='submit']")).click();

        String xpathSearchApplicationType = "//*[@id='tab-content4']//app-at0022//p[normalize-space(text())='申請種類を選択']/following-sibling::select";
        WebElement dropdownElementSearchApplicationType = DriverFactory.getDriver().findElement(By.xpath(xpathSearchApplicationType));
        Select dropdownSearchApplicationType = new Select(dropdownElementSearchApplicationType);
        dropdownSearchApplicationType.selectByVisibleText("すべて");

        String xpathSearchStatusButton = "//*[@id='tab-content4']//app-at0022//p[normalize-space(text())='ステータスを選択']/following-sibling::select";
        WebElement dropdownElementSearchStatusButton  = DriverFactory.getDriver().findElement(By.xpath(xpathSearchStatusButton));
        Select dropdownSearchStatusButton  = new Select(dropdownElementSearchStatusButton );
        dropdownSearchStatusButton.selectByValue("RS_ACCEPTED");

        String xpathAllRecords = "//app-at0022//div[@id='table-content']/table/tbody/tr";
        if (WebUI.waitForElementPresent(By.xpath(xpathAllRecords), 5) != null) {
            List<WebElement> requestRowElements = WebUI.findWebElementsIfVisible(By.xpath(xpathAllRecords));
            for (WebElement requestElement : requestRowElements) {
                By rejectButton = By.xpath(xpathAllRecords + "/td[10]//span[normalize-space(text())='非承認']/ancestor::button");

                WebUI.waitForElementClickable(rejectButton, 10);
                WebUI.click(rejectButton);

                WebUI.findWebElementIfVisible(By.xpath(XpathCommon.MODAL_CONFIRM_BTN.value)).click();

                WebDriverWait wait = new WebDriverWait(DriverFactory.getDriver(), 5);
                WebElement loader = DriverFactory.getDriver().findElement(By.xpath("//app-loader-empty")); // cần đúng xpath tương ứng với 'app-loader-empty'
                wait.until(ExpectedConditions.attributeToBe(loader, "ng-reflect-is-show-loading", "false"));
            }

            String xpathAT0022HeaderSelectStatus = "//*[@id=\"tab-content4\"]//app-at0022//p[normalize-space(text())='ステータスを選択']/following-sibling::select";
            WebElement dropdownElementAT0022HeaderSelectStatus  = DriverFactory.getDriver().findElement(By.xpath(xpathAT0022HeaderSelectStatus));
            Select dropdownAT0022HeaderSelectStatus  = new Select(dropdownElementAT0022HeaderSelectStatus );
            dropdownAT0022HeaderSelectStatus.selectByValue("RS_REJECTED");
        }

        // Handle DayOff request
        WebUI.findWebElementIfVisible(By.xpath("//*[@id='tab-content4']//app-at0022//button[normalize-space(text())='各種申請一覧']")).click();

        dropdownSearchApplicationType.selectByVisibleText("すべて");
        dropdownSearchStatusButton.selectByValue("RS_ACCEPTED");

        WebUI.findWebElementIfVisible(By.xpath(btnMemberFilterXpath)).click();

        inputElementMemberFilter.clear();
        inputElementMemberFilter.sendKeys(setting.getTargetUser());

        WebUI.findWebElementIfVisible(By.xpath(btnMemberFilterXpath + "//virtual-scroll//span[text()='" + setting.getTargetUser() + "']//ancestor::div[contains(@class, 'destination-popover-profile-wrap')]")).click();
        WebUI.findWebElementIfVisible(By.xpath("//app-at0022-filter//div[contains(@class, 'search popup-member-filter')]//button[@type='submit']")).click();

        if (WebUI.waitForElementPresent(By.xpath(xpathAllRecords), 5) != null) {
            List<WebElement> requestRowElements = WebUI.findWebElementsIfVisible(By.xpath(xpathAllRecords));
            for (WebElement requestElement : requestRowElements) {
                By rejectButton = By.xpath(xpathAllRecords + "/td[9]//button[normalize-space(text())='非承認']");

                WebUI.waitForElementClickable(rejectButton, 10);
                WebUI.click(rejectButton);
                
                WebUI.findWebElementIfVisible(By.xpath(XpathCommon.MODAL_CONFIRM_BTN.value)).click();

                WebDriverWait wait = new WebDriverWait(DriverFactory.getDriver(), 5);
                WebElement loader = DriverFactory.getDriver().findElement(By.xpath("//app-loader-empty")); // cần đúng xpath tương ứng với 'app-loader-empty'
                wait.until(ExpectedConditions.attributeToBe(loader, "ng-reflect-is-show-loading", "false"));
            }

            String xpathAT0022HeaderSelectStatus = "//*[@id=\"tab-content4\"]//app-at0022//p[normalize-space(text())='ステータスを選択']/following-sibling::select";
            WebElement dropdownElementAT0022HeaderSelectStatus  = DriverFactory.getDriver().findElement(By.xpath(xpathAT0022HeaderSelectStatus));
            Select dropdownAT0022HeaderSelectStatus  = new Select(dropdownElementAT0022HeaderSelectStatus );
            dropdownAT0022HeaderSelectStatus.selectByValue("RS_REJECTED");
        }
    }

    public static void handleDiscretionarySchedule(List<WorkSchedule> workScheduleList, List<WebElement> dateElements, String baseXpath) {
        WebDriverWait wait = new WebDriverWait(DriverFactory.getDriver(), 5);

        for (int j = 0; j < workScheduleList.size(); j++) {
            String dateIndexStr = workScheduleList.get(j).getDayIndex();
            int dateIndex = Integer.parseInt(dateIndexStr);

            if (dateIndex > dateElements.size()) {
                continue;
            }

            // Handle DayType
            String dayType = workScheduleList.get(j).getDayType();
            if (StringUtils.isNotBlank(dayType)) {
                By dayTypeSelectBox = By.xpath(baseXpath + "[" + dateIndex + "]/td[3]/select");

                try {
                    WebElement selectElement = wait.until(ExpectedConditions.presenceOfElementLocated(dayTypeSelectBox));
                    Select select = new Select(selectElement);
                    select.selectByVisibleText(dayType);
                } catch (TimeoutException e) {
                    // not found, skip
                }
            }

            boolean needToAddNewDuration = false;

            // Handle WorkingTimeType
            String workingTimeType = workScheduleList.get(j).getWorkingTimeType();
            if (StringUtils.isNotBlank(workingTimeType)) {
                By wttSelectBox = By.xpath(baseXpath + "[" + dateIndex + "]/td[4]//select");

                if (DriverFactory.getDriver().findElements(wttSelectBox).isEmpty()) {
                    // Button to add WorkingTime
                    By buttonAddTimeDuration = By.xpath(baseXpath + "[" + dateIndex + "]/td[4]//i[contains(@class, 'fa-plus-square')]/ancestor::button[contains(@class, 'btn btn-link add-link')]");
                    DriverFactory.getDriver().findElement(buttonAddTimeDuration).click();
                    needToAddNewDuration = true;
                }

                WebElement wttElement = wait.until(ExpectedConditions.presenceOfElementLocated(wttSelectBox));
                Select select = new Select(wttElement);
                select.selectByVisibleText(workingTimeType);
            }

            // Handle StartTime
            String startTime = workScheduleList.get(j).getStartTime();
            if (StringUtils.isNotBlank(startTime)) {
                By startTimeSelectBox = By.xpath(baseXpath + "[" + dateIndex + "]/td[4]//input[1]");

                if (!DriverFactory.getDriver().findElements(startTimeSelectBox).isEmpty()) {
                    DriverFactory.getDriver().findElement(startTimeSelectBox).click();
                    String formattedTime = startTime.substring(0, 2) + ":" + startTime.substring(2);
                    By timeOption = By.xpath(String.format("//app-at0036b-time-picker//ul//li[@data-value='%s']", formattedTime));
                    DriverFactory.getDriver().findElement(timeOption).click();
                }
            } else {
                // Delete startTime input
                By buttonDeleteTimeDuration = By.xpath(baseXpath + "[" + dateIndex + "]/td[4]//i[contains(@class, 'fa fa-times')]/ancestor::button");
                if (!DriverFactory.getDriver().findElements(buttonDeleteTimeDuration).isEmpty()) {
                    DriverFactory.getDriver().findElement(buttonDeleteTimeDuration).click();
                }
            }

            // Handle EndTime
            String endTime = workScheduleList.get(j).getEndTime();
            if (StringUtils.isNotBlank(endTime)) {
                By endTimeSelectBox = By.xpath(baseXpath + "[" + dateIndex + "]/td[4]//input[2]");
                DriverFactory.getDriver().findElement(endTimeSelectBox).click();
                String formattedTime = endTime.substring(0, 2) + ":" + endTime.substring(2);
                By timeOption = By.xpath(String.format("//app-at0036b-time-picker//ul//li[@data-value='%s']", formattedTime));
                DriverFactory.getDriver().findElement(timeOption).click();
            }

            // Handle StartBreakTime
            String startBreakTime = workScheduleList.get(j).getStartBreakTime();
            if (StringUtils.isNotBlank(startBreakTime)) {
                By startBreakTimeSelectBox = By.xpath(baseXpath + "[" + dateIndex + "]/td[5]//input[1]");

                if (needToAddNewDuration && DriverFactory.getDriver().findElements(startBreakTimeSelectBox).isEmpty()) {
                    By buttonAddBreakTimeDuration = By.xpath(baseXpath + "[" + dateIndex + "]/td[5]//i[contains(@class, 'fa-plus-square')]/ancestor::button");
                    DriverFactory.getDriver().findElement(buttonAddBreakTimeDuration).click();
                }

                DriverFactory.getDriver().findElement(startBreakTimeSelectBox).click();
                String formattedTime = startBreakTime.substring(0, 2) + ":" + startBreakTime.substring(2);
                By timeOption = By.xpath(String.format("//app-at0036b-time-picker//ul//li[@data-value='%s']", formattedTime));
                DriverFactory.getDriver().findElement(timeOption).click();
            } else {
                // Delete startBreakTime
                By buttonDeleteTimeDuration = By.xpath(baseXpath + "[" + dateIndex + "]/td[5]//i[contains(@class, 'fa fa-times')]/ancestor::button");
                if (!DriverFactory.getDriver().findElements(buttonDeleteTimeDuration).isEmpty()) {
                    DriverFactory.getDriver().findElement(buttonDeleteTimeDuration).click();
                }
            }

            // Handle EndBreakTime
            String endBreakTime = workScheduleList.get(j).getEndBreakTime();
            if (StringUtils.isNotBlank(endBreakTime)) {
                By endBreakTimeSelectBox = By.xpath(baseXpath + "[" + dateIndex + "]/td[5]//input[2]");
                DriverFactory.getDriver().findElement(endBreakTimeSelectBox).click();
                String formattedTime = endBreakTime.substring(0, 2) + ":" + endBreakTime.substring(2);
                By timeOption = By.xpath(String.format("//app-at0036b-time-picker//ul//li[@data-value='%s']", formattedTime));
                DriverFactory.getDriver().findElement(timeOption).click();
            }
        }
    }

    public static void handleIndividualAndVariableSchedule(
            List<WorkSchedule> workScheduleList,
            List<WebElement> dateElements,
            String baseXpath
    ) {
        WebDriverWait wait = new WebDriverWait(DriverFactory.getDriver(), 5);

        for (int j = 0; j < workScheduleList.size(); j++) {
            String dateIndex = workScheduleList.get(j).getDayIndex();
            if (Integer.parseInt(dateIndex) > dateElements.size()) continue;

            String presetName = workScheduleList.get(j).getPresetName();
            if (presetName != null && !presetName.isEmpty()) {
                By presetSelectLocator = By.xpath(baseXpath + "[" + dateIndex + "]/td[3]/select");
                try {
                    WebElement presetSelectBox = wait.until(ExpectedConditions.presenceOfElementLocated(presetSelectLocator));
                    Select presetSelect = new Select(presetSelectBox);
                    presetSelect.selectByVisibleText(presetName);
                    continue;
                } catch (TimeoutException e) {
                    // Ignore if not found
                }
            }

            String dayType = workScheduleList.get(j).getDayType();
            if (dayType != null && !dayType.isEmpty()) {
                By dayTypeSelectLocator = By.xpath(baseXpath + "[" + dateIndex + "]/td[4]/select");
                try {
                    WebElement dayTypeSelectBox = wait.until(ExpectedConditions.presenceOfElementLocated(dayTypeSelectLocator));
                    Select dayTypeSelect = new Select(dayTypeSelectBox);
                    for (WebElement option : dayTypeSelect.getOptions()) {
                        if (option.getText().contains(": " + dayType)) {
                            dayTypeSelect.selectByVisibleText(option.getText());
                            break;
                        }
                    }
                } catch (TimeoutException e) {
                    // Ignore
                }
            }

            boolean needToAddNewDuration = false;
            String workingTimeType = workScheduleList.get(j).getWorkingTimeType();
            if (workingTimeType != null && !workingTimeType.isEmpty()) {
                By wttSelectLocator = By.xpath(baseXpath + "[" + dateIndex + "]/td[5]//select");
                List<WebElement> wttSelectElements = DriverFactory.getDriver().findElements(wttSelectLocator);

                if (wttSelectElements.isEmpty()) {
                    String ancestorPath = workingTimeType.equals("WORKING")
                            ? "/ancestor::td/div/div[1]//div[contains(@class, 'tbl-select-time')]/button"
                            : "/ancestor::td/div/div[contains(@class, 'working-time-controls-not-working')]/button";

                    By addButtonLocator = By.xpath(baseXpath + "[" + dateIndex + "]/td[5]//i[contains(@class, 'fa-plus-square')]" + ancestorPath);
                    DriverFactory.getDriver().findElement(addButtonLocator).click();
                    needToAddNewDuration = true;
                }
                else {
                    Select wttSelect = new Select(wttSelectElements.get(0));
                    wttSelect.selectByVisibleText(workingTimeType);
                }
            }

            String startTime = workScheduleList.get(j).getStartTime();
            String endTime = workScheduleList.get(j).getEndTime();
            if (startTime != null && !startTime.isEmpty() && endTime != null && !endTime.isEmpty()) {
                String[] startTimesArr = startTime.split(",");
                String[] endTimesArr = endTime.split(",");

                int loopCount = Math.min(startTimesArr.length, endTimesArr.length);

                for (int i = 0; i < loopCount; i++) {
                    String st = startTimesArr[i];
                    String et = endTimesArr[i];

                    if (i > 0) {
                        String ancestorPath = dayType.equals("WORKING")
                                ? "/ancestor::td/div/div[1]//div[contains(@class, 'tbl-select-time')]/button"
                                : "/ancestor::td/div/div[contains(@class, 'working-time-controls-not-working')]/button";

                        By addButtonLocator = By.xpath(baseXpath + "[" + dateIndex + "]/td[5]//i[contains(@class, 'fa-plus-square')]" + ancestorPath);
                        DriverFactory.getDriver().findElement(addButtonLocator).click();
                    }

                    WebElement startTimeInput = DriverFactory.getDriver().findElement(By.xpath(baseXpath + "[" + dateIndex + "]/td[5]//div[@ng-reflect-name='" + i + "']//input[1]"));
                    startTimeInput.click();
                    String formattedSTime = st.substring(0, 2) + ":" + st.substring(2);
                    WebElement stTimeOption = DriverFactory.getDriver().findElement(By.xpath("//app-at0035b-time-picker//ul//li[@data-value='" + formattedSTime + "']"));
                    stTimeOption.click();

                    WebElement endTimeInput = DriverFactory.getDriver().findElement(By.xpath(baseXpath + "[" + dateIndex + "]/td[5]//div[@ng-reflect-name='" + i + "']//input[2]"));
                    endTimeInput.click();
                    String formattedETime = et.substring(0, 2) + ":" + et.substring(2);
                    WebElement etTimeOption = DriverFactory.getDriver().findElement(By.xpath("//app-at0035b-time-picker//ul//li[@data-value='" + formattedETime + "']"));
                    etTimeOption.click();
                }
            } else {
                By deleteButtonLocator = By.xpath(baseXpath + "[" + dateIndex + "]/td[5]//i[contains(@class, 'fa fa-times')]/ancestor::button");
                List<WebElement> deleteButtons = DriverFactory.getDriver().findElements(deleteButtonLocator);
                if (!deleteButtons.isEmpty()) {
                    deleteButtons.get(0).click();
                }
            }

            String startBreakTime = workScheduleList.get(j).getStartBreakTime();
            String endBreakTime = workScheduleList.get(j).getEndBreakTime();

            By deleteBreakLocator = By.xpath(baseXpath + "[" + dateIndex + "]/td[6]//i[contains(@class, 'fa fa-times')]/ancestor::button");
            List<WebElement> deleteBreakButtons = DriverFactory.getDriver().findElements(deleteBreakLocator);
            for (WebElement button : deleteBreakButtons) {
                button.click();
            }

            if (startBreakTime != null && !startBreakTime.isEmpty() && endBreakTime != null && !endBreakTime.isEmpty()) {
                String[] startBTimesArr = startBreakTime.split(",");
                String[] endBTimesArr = endBreakTime.split(",");

                int loopCount = Math.min(startBTimesArr.length, endBTimesArr.length);

                for (int i = 0; i < loopCount; i++) {
                    String st = startBTimesArr[i];
                    String et = endBTimesArr[i];

                    By startBreakInputLocator = By.xpath(baseXpath + "[" + dateIndex + "]/td[6]//div[@ng-reflect-name='" + i + "']//input[1]");
                    List<WebElement> startBreakInputList = DriverFactory.getDriver().findElements(startBreakInputLocator);

                    if (startBreakInputList.isEmpty() || i > 0) {
                        boolean isAT0034 = DriverFactory.getDriver().getCurrentUrl().contains("/at/at0034b/");

                        String ancestorPath = "";
                        if (isAT0034) {
                            ancestorPath = dayType.equals("WORKING")
                                    ? "/ancestor::div[contains(@class, 'tbl-select-time')]/button"
                                    : "/ancestor::div/button[2]";
                        } else {
                            ancestorPath = i > 0
                                    ? "/ancestor::div[@class='tbl-select-time d-flex']/button"
                                    : "/ancestor::button[contains(@class, 'ml-auto')]";
                        }

                        By addBreakLocator = By.xpath(baseXpath + "[" + dateIndex + "]/td[6]//i[contains(@class, 'fa-plus-square')]" + ancestorPath);
                        DriverFactory.getDriver().findElement(addBreakLocator).click();
                    }

                    WebElement startBreakInput = DriverFactory.getDriver().findElement(startBreakInputLocator);
                    startBreakInput.click();
                    String formattedTime = st.substring(0, 2) + ":" + st.substring(2);
                    WebElement stBreakTimeOption = DriverFactory.getDriver().findElement(By.xpath("//app-at0035b-time-picker//ul//li[@data-value='" + formattedTime + "']"));
                    stBreakTimeOption.click();

                    WebElement endBreakInput = DriverFactory.getDriver().findElement(By.xpath(baseXpath + "[" + dateIndex + "]/td[6]//div[@ng-reflect-name='" + i + "']//input[2]"));
                    endBreakInput.click();
                    String formattedEBTime = et.substring(0, 2) + ":" + et.substring(2);
                    WebElement etBreakTimeOption = DriverFactory.getDriver().findElement(By.xpath("//app-at0035b-time-picker//ul//li[@data-value='" + formattedEBTime + "']"));
                    etBreakTimeOption.click();
                }
            }
        }
    }
}
