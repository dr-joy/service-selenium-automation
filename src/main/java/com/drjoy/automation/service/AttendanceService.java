package com.drjoy.automation.service;

import com.drjoy.automation.config.DriverFactory;
import com.drjoy.automation.model.CheckingLog;
import com.drjoy.automation.model.ExportTemplateFilterSetting;
import com.drjoy.automation.model.Request;
import com.drjoy.automation.repository.ExcelReaderRepository;
import com.drjoy.automation.utils.AttendanceUtils;
import com.drjoy.automation.utils.DateUtils;
import com.drjoy.automation.utils.WebUI;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.drjoy.automation.utils.AttendanceUtils.waitForLoadingElement;

public class AttendanceService {

    public static void addAllCheckingLogs(ExportTemplateFilterSetting setting) {
        AttendanceUtils.navigateToATPage("at0001");

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
            WebElement editButton = WebUI.findWebElement(xpathRow + "/td[last()-1]/button");
            if (editButton != null) {
                editButton.click();
                WebUI.sleep(1000);
            }

            // Nhập checking log
            for (int i = 0; i < logsInDay.size(); i++) {
                CheckingLog log = logsInDay.get(i);
                String xpathLogTime = String.format("//app-at0023//form[@id='checking-log']//table//tbody/tr[%d]/td[1]//input", i + 1);
                String xpathReason = String.format("//app-at0023//form[@id='checking-log']//table//tbody/tr[%d]/td[3]//textarea", i + 1);

                WebElement inputTime = WebUI.findWebElement(xpathLogTime);
                WebElement inputReason = WebUI.findWebElement(xpathReason);

                if (inputTime != null) inputTime.sendKeys(log.getLogTime());
                if (inputReason != null) inputReason.sendKeys(Optional.ofNullable(log.getReason()).orElse(""));

                if (i >= 1 && i < logsInDay.size()) {
                    WebElement btnAddLog = WebUI.findWebElement("//button[@id='btn_add_checking_log']");
                    if (btnAddLog != null) btnAddLog.click();
                }
            }

            // Submit checking log
            WebUI.findWebElement("//*[@id='checking-log']/div/table/tbody/tr[1]/td[6]/div/button").click();

            // Gửi request
            addRequestByDateIndex(dateIndex, setting.getSheetName(), setting.getPhase());

            // Quay lại AT0001
            WebElement backBtn = WebUI.findWebElement("//a[@class='page-head-backlink']");
            if (backBtn != null) backBtn.click();

            // Xử lý confirm popup nếu có
            int retry = 0;
            while (retry < 5) {
                WebElement confirmBtn = WebUI.waitForElementClickable(By.xpath("//app-modal//button[@id='positiveButton']"), 2);
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
            List<WebElement> allElements = WebUI.findWebElements(requestRowXpath);
            for (int i = 0; i < allElements.size(); i++) {
                try {
                    WebElement validBtn = WebUI.findWebElement(requestRowXpath + "[1]//button[normalize-space(text())='有効']");
                    if (validBtn != null) validBtn.click();
                } catch (Exception ignored) {}

                WebElement deleteBtn = WebUI.findWebElement(requestRowXpath + "[1]/td[last()]/button");
                deleteBtn.click();
                waitForLoadingElement();
            }

            // Tạo mới các request OT/research
            for (int i = 0; i < otRequestInTargetDate.size(); i++) {
                Request request = otRequestInTargetDate.get(i);
                String curRowXpath = String.format(requestRowXpath + "[%d]", i + 1);

                WebElement btnAddRequest = WebUI.findWebElement("//app-at0023//div[@class='ot-table-container']/button");
                btnAddRequest.click();

                WebElement inputStartTime = WebUI.findWebElement(curRowXpath + "//input[1][@ng-reflect-name='startTime']");
                inputStartTime.sendKeys(request.getStartTime());

                WebElement inputEndTime = WebUI.findWebElement(curRowXpath + "//input[2][@ng-reflect-name='endTime']");
                inputEndTime.sendKeys(request.getEndTime());

                WebElement selectReason = WebUI.findWebElement(curRowXpath + "//div[@class='reason-type']//select[1]");
                Select dropdown = new Select(selectReason);
                if ("ot".equalsIgnoreCase(request.getReasonCategory())) {
                    dropdown.selectByValue("0: RC_OVERTIME");
                } else if ("research".equalsIgnoreCase(request.getReasonCategory())) {
                    dropdown.selectByValue("1: RC_RESEARCH");
                }
            }

            WebElement btnSubmit = WebUI.findWebElement("//button[@id='btn_requestOT_RS']");
            btnSubmit.click();

            WebElement btnConfirm = WebUI.findWebElement("//app-modal//button[@id='positiveButton']");
            while (btnConfirm != null && btnConfirm.isDisplayed()) {
                btnConfirm.click();
                btnConfirm = WebUI.findWebElement("//app-modal//button[@id='positiveButton']");
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
            WebElement selectScreenBtn = WebUI.findWebElement("//app-at0023//div[contains(@class, 'select-screen')]//select");
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
        String applycationTypeXpath = "//app-at0024//div[text()='申請種類']/following-sibling::div//span[normalize-space()='休暇']";
        WebElement appTypeBtn = WebUI.findWebElement(applycationTypeXpath);
        appTypeBtn.click();

        // カテゴリ - Loại lý do nghỉ
        String reasonTypeBaseXPath = "//app-at0024//div[text()='カテゴリ']/following-sibling::div";
        String reasonType = dayoffRequest.getReasonType().trim();
        WebElement reasonTypeBtn = WebUI.findWebElement(String.format("%s//span[text()[normalize-space()='%s']]", reasonTypeBaseXPath, reasonType));
        WebUI.scrollToElementCenter(reasonTypeBtn);
        if (reasonTypeBtn.isEnabled()) {
            reasonTypeBtn.click();
        }

        // 休暇名 - Tên lý do nghỉ
        String reasonNameBaseXpath = "//app-at0024//div[text()='休暇名']/following-sibling::div";
        String reasonName = dayoffRequest.getReasonName().trim();
        WebElement reasonNameBtn = WebUI.findWebElement(String.format("%s//span[text()[normalize-space()='%s']]", reasonNameBaseXpath, reasonName));

        if (reasonNameBtn.isEnabled()) {
            reasonNameBtn.click();
        }

        String timeUnit = dayoffRequest.getRequestTimeUnit().trim();
        if (StringUtils.isEmpty(timeUnit)) return;

        String requestTimeUnitBaseXpath = "//app-day-off-type/div[contains(@class, 'block-form')]//select";
        WebElement selectTimeUnit = WebUI.findWebElement(requestTimeUnitBaseXpath);

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
                WebElement radioIPPeriodType = WebUI.findWebElement(periodTypeXpath + "/ancestor::label[contains(@class, 'custom-radio')]");
                radioIPPeriodType.click();
                break;
            case "timeperiod":
                dropdown.selectByValue("TIME_PERIOD");

                String xpathStartTime = "//app-day-off-type//table[@id='date-lbx']//tr[2]//td[2]//input[1]";
                WebElement ipStartTime = WebUI.findWebElement(xpathStartTime);
                ipStartTime.sendKeys(dayoffRequest.getStartTime().trim());

                String xpathEndTime = "//app-day-off-type//table[@id='date-lbx']//tr[2]//td[2]//input[2]";
                WebElement ipEndTime = WebUI.findWebElement(xpathEndTime);
                ipEndTime.sendKeys(dayoffRequest.getEndTime().trim());
                break;
            case "minuteperiod":
                dropdown.selectByValue("MINUTE_PERIOD");

                String xpathStartTimeMP = "//app-day-off-type//table[@id='date-lbx']//tr[2]//td[2]//input[1]";
                WebElement ipStartTimeMP = WebUI.findWebElement(xpathStartTimeMP);
                ipStartTimeMP.sendKeys(dayoffRequest.getStartTime().trim());

                String xpathEndTimeMP = "//app-day-off-type//table[@id='date-lbx']//tr[2]//td[2]//input[2]";
                WebElement ipEndTimeMP = WebUI.findWebElement(xpathEndTimeMP);
                ipEndTimeMP.sendKeys(dayoffRequest.getEndTime().trim());
                break;
            default:
                break;
        }

        WebElement btnApply = WebUI.findWebElement("//app-at0024//button[@id='btn_apply']");
        btnApply.click();
        waitForLoadingElement();

        int counter = 0;
        WebElement btnConfirm = WebUI.findWebElement("//app-modal//button[@id='positiveButton']");
        while (btnConfirm.isDisplayed()) {
            if (counter == 5) break;

            btnConfirm.click();
            counter++;
        }
    }

    public static void handleDayoffWorkingRequest(Request dayoffRequest) {
        // 申請種類 - Loại đơn: 休日出勤
        WebElement appTypeBtn = WebUI.findWebElement("//app-at0024//div[text()='申請種類']/following-sibling::div//span[normalize-space()='休日出勤']");
        appTypeBtn.click();

        waitForLoadingElement();

        // Lấy ngày nghỉ làm (休日出勤日)
        String holidayWorkDate = dayoffRequest.getHolidayWorkDate();
        String year1 = holidayWorkDate.substring(0, 4);
        String month1 = String.valueOf(Integer.parseInt(holidayWorkDate.substring(4, 6)));
        String day1 = String.valueOf(Integer.parseInt(holidayWorkDate.substring(6, 8)));

        WebElement openHolidayWorkBtn = WebUI.findWebElement("//app-at0024//span[text()='休日出勤日']//ancestor::tr//following-sibling::tr[1]//app-date-input-at");
        DateUtils.chooseDatePicker(openHolidayWorkBtn, year1, month1, day1);

        // Lấy ngày nghỉ bù (休暇取得日)
        String vacationTakenDate = dayoffRequest.getVacationTakenDate();
        String year2 = vacationTakenDate.substring(0, 4);
        String month2 = String.valueOf(Integer.parseInt(vacationTakenDate.substring(4, 6)));
        String day2 = String.valueOf(Integer.parseInt(vacationTakenDate.substring(6, 8)));

        WebElement openVacationTakenBtn = WebUI.findWebElement("//app-at0024//span[text()='休暇取得日']//ancestor::tr//following-sibling::tr[1]//app-date-input-at");
        WebUI.scrollToElementCenter(openVacationTakenBtn);
        DateUtils.chooseDatePicker(openVacationTakenBtn, year2, month2, day2);

        WebUI.sleep(500);

        // 申請ボタン
        WebElement btnApply = WebUI.findWebElement("//app-at0024//button[normalize-space()='申請']");
        if (btnApply != null) {
            btnApply.click();
            waitForLoadingElement();
        }

        // Confirm modal
        int counter = 0;
        WebElement btnConfirm = WebUI.findWebElement("//app-modal//button[@id='positiveButton']");
        if (btnConfirm != null) {
            btnConfirm.click();
//            while (WebUI.waitForElementPresent(btnConfirm) && counter < 5) {
//                WebUI.click(btnConfirm);
//                btnConfirm = WebUI.findWebElement("//app-modal//button[@id='positiveButton']");
//                counter++;
//            }
        }
    }

}
