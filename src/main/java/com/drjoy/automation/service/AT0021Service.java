package com.drjoy.automation.service;

import com.drjoy.automation.execution.ExecutionHelper;
import com.drjoy.automation.execution.ExecutionStep;
import com.drjoy.automation.model.ExportTemplateFilterSetting;
import com.drjoy.automation.utils.AttendanceUtils;
import com.drjoy.automation.utils.DateUtils;
import com.drjoy.automation.utils.WebUI;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.springframework.stereotype.Service;

import static com.drjoy.automation.utils.AttendanceUtils.waitForLoadingElement;

@Service
public class AT0021Service {

    @ExecutionStep(value = "filterRequestsOnAT0021")
    public static void filterRequestsOnAT0021(ExportTemplateFilterSetting setting) {

        String yearStart = setting.getYearStart();
        String monthStart = setting.getMonthStart();
        String dateStart = setting.getDateStart();

        String yearEnd = setting.getYearEnd();
        String monthEnd = setting.getMonthEnd();
        String dateEnd = setting.getDateEnd();

        String requestType = setting.getRequestType();
        String requestStatus = setting.getRequestStatus();

        ExecutionHelper.runStepWithLogging("Navigate to AT0021 and apply filters", () -> {
            AttendanceUtils.navigateToATPage("at0021"); // Corresponds to WebUI.callTestCase with uri '/at/at0021'
            waitForLoadingElement(); // Ensure page is loaded

            // Define XPaths based on Katalon variables
            String xpathSlDatePickerStart = "//span[normalize-space()='〜']/preceding-sibling::div[@class='input-time']";
            String xpathSlDatePickerEnd = "//span[normalize-space()='〜']/following-sibling::div[@class='input-time']";

            // XPaths for dropdowns and their labels
            String xpathSelectFilterFormat = "//p[text()='%s']/following-sibling::select";
            String requestTypeLabelFromKatalon = "種類を選択";
            String requestStatusLabelFromKatalon = "ステータスを選択";

            // Handle Start Date selection
            WebElement datePickerStartInput = WebUI.findWebElementIfVisible(By.xpath(xpathSlDatePickerStart));
            // DateUtils.chooseDatePicker should handle clicking the input, then selecting year, month, and day
            DateUtils.chooseDatePicker(datePickerStartInput, yearStart, monthStart, dateStart);
            WebUI.sleep(500); // Short pause if needed for UI to update

            // Handle End Date selection
            WebElement datePickerEndInput = WebUI.findWebElementIfVisible(By.xpath(xpathSlDatePickerEnd));
            DateUtils.chooseDatePicker(datePickerEndInput, yearEnd, monthEnd, dateEnd);
            WebUI.sleep(500);

            // Select Request Type
            // Katalon: WebUI.selectOptionByLabel(getTestObject(xpathSelectFilter, requestTypeLabel), requestType, false)
            String xpathRequestTypeDropdown = String.format(xpathSelectFilterFormat, requestTypeLabelFromKatalon);
            WebElement requestTypeDropdownElement = WebUI.findWebElementIfVisible(By.xpath(xpathRequestTypeDropdown));
            Select selectRequestType = new Select(requestTypeDropdownElement);
            selectRequestType.selectByVisibleText(requestType); // selectByVisibleText is equivalent to Katalon's selectOptionByLabel
            WebUI.sleep(500);

            // Select Request Status
            // Katalon: WebUI.selectOptionByLabel(getTestObject(xpathSelectFilter, requestStatusLabel), requestStatus, false)
            String xpathRequestStatusDropdown = String.format(xpathSelectFilterFormat, requestStatusLabelFromKatalon);
            WebElement requestStatusDropdownElement = WebUI.findWebElementIfVisible(By.xpath(xpathRequestStatusDropdown));
            Select selectRequestStatus = new Select(requestStatusDropdownElement);
            selectRequestStatus.selectByVisibleText(requestStatus);
            WebUI.sleep(500);

            // If there's a search or filter button to click after setting these values, add it here.
            // For example:
            // WebElement searchButton = WebUI.findWebElementIfVisible(By.xpath("//button[normalize-space()='検索']"));
            // if (searchButton != null) {
            //     searchButton.click();
            //     waitForLoadingElement();
            // }
        });
    }

    @ExecutionStep(value = "navigateToRequestCreationPageFromAT0021")
    public static void navigateToRequestCreationPageFromAT0021() {
        ExecutionHelper.runStepWithLogging("Navigate to AT0021 and click '休暇｜当直等の申請' button", () -> {
            AttendanceUtils.navigateToATPage("at0021"); // Corresponds to WebUI.callTestCase with uri '/at/at0021'
            waitForLoadingElement(); // Ensure page is loaded

            // Define XPath for the button based on Katalon
            String xpathButtonRequestCreation = "//button[normalize-space()=\"休暇｜当直等の申請\"]";

            // Click the button
            // Katalon: WebUI.click(getTestObject('//button[normalize-space()="休暇｜当直等の申請"]'))
            WebElement requestCreationButton = WebUI.findWebElementIfVisible(By.xpath(xpathButtonRequestCreation));
            if (requestCreationButton != null) {
                requestCreationButton.click();
                waitForLoadingElement(); // Wait for the next page/modal to load if necessary
            } else {
                // Handle the case where the button is not found, e.g., throw an exception or log an error
                System.err.println("Button '休暇｜当直等の申請' not found on page AT0021.");
                // throw new RuntimeException("Button '休暇｜当直等の申請' not found on page AT0021.");
            }
        });
    }
}
