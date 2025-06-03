package com.drjoy.automation.service;

import com.drjoy.automation.config.DriverFactory; // Assuming DriverFactory provides WebDriver
import com.drjoy.automation.execution.ExecutionHelper;
import com.drjoy.automation.execution.ExecutionStep;
import com.drjoy.automation.model.setting.ExportTemplateFilterSetting;
import com.drjoy.automation.model.setting.TeireiSetting;
import com.drjoy.automation.utils.AttendanceUtils;
import com.drjoy.automation.utils.DateUtils;
import com.drjoy.automation.utils.WebUI; // Still used for other actions
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor; // For scrolling
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver; // For WebDriverWait and JavascriptExecutor
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions; // For explicit waits
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait; // For explicit waits
import org.springframework.stereotype.Service;

import java.time.Duration; // For WebDriverWait timeout

import static com.drjoy.automation.utils.AttendanceUtils.waitForLoadingElement;
import static com.drjoy.automation.utils.AttendanceUtils.waitForLoadingOverlayElement;


@Service
public class AT0024Service {

    @ExecutionStep(value = "createSingleDayRequestOnAT0024B")
    public static void createSingleDayRequestOnAT0024B(TeireiSetting setting) {
        String unitTime = setting.getUnitTime();
        String year = setting.getYear();
        String month = setting.getMonth();
        String date = setting.getDate();
        String requestDescription = setting.getRequestDescription();

        ExecutionHelper.runStepWithLogging("Create single day request on AT0024B", () -> {
            WebDriver driver = DriverFactory.getDriver(); // Get WebDriver instance
            AttendanceUtils.navigateToATPage("at0024b");
            waitForLoadingElement();

            String xpathSlUnitTime = "//option[@value='ALL_DAY']/..";
            String xpathRbSingleDay = "//div[@class='specify-block']//span[normalize-space()='日付で指定']";
            String xpathSlDatePicker = "//*[@id='at-start-time-0']";
            String xpathTxtRequestDescription = "//textarea";
            String xpathBtnSave = "//button[normalize-space()='申請']";
            String xpathBtnConfirmAT0026 = "//app-at0026//button[normalize-space()='はい']";
            String xpathAT0021Title = "//h1[normalize-space()='申請の承認状況']";

            WebElement unitTimeDropdown = WebUI.findWebElementIfVisible(By.xpath(xpathSlUnitTime));
            if (unitTimeDropdown != null) {
                Select selectUnitTime = new Select(unitTimeDropdown);
                selectUnitTime.selectByValue(unitTime);
                WebUI.sleep(300);
            } else {
                System.err.println("Unit time dropdown not found with XPath: " + xpathSlUnitTime);
            }

            WebElement saveButtonForScroll = WebUI.findWebElementIfVisible(By.xpath(xpathBtnSave));
            if (saveButtonForScroll != null) {
                // Alternative for WebUI.scrollToElement(saveButtonForScroll);
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", saveButtonForScroll);
                WebUI.sleep(300); // Consider if this sleep is still needed after scrolling
            }

            WebElement singleDayRadioButton = WebUI.findWebElementIfVisible(By.xpath(xpathRbSingleDay));
            if (singleDayRadioButton != null) {
                singleDayRadioButton.click();
                WebUI.sleep(300);
            } else {
                System.err.println("Single day radio button not found with XPath: " + xpathRbSingleDay);
            }

            WebElement datePickerInput = WebUI.findWebElementIfVisible(By.xpath(xpathSlDatePicker));
            if (datePickerInput != null) {
                DateUtils.chooseDatePicker(datePickerInput, year, month, date);
                WebUI.sleep(500);
            } else {
                 System.err.println("Date picker input not found with XPath: " + xpathSlDatePicker);
            }

            WebElement requestDescriptionTextarea = WebUI.findWebElementIfVisible(By.xpath(xpathTxtRequestDescription));
            if (requestDescriptionTextarea != null) {
                requestDescriptionTextarea.sendKeys(requestDescription);
                WebUI.sleep(300);
            } else {
                System.err.println("Request description textarea not found with XPath: " + xpathTxtRequestDescription);
            }

            WebElement saveButton = WebUI.findWebElementIfVisible(By.xpath(xpathBtnSave));
            if (saveButton != null) {
                saveButton.click();
                waitForLoadingOverlayElement();
            } else {
                System.err.println("Save button not found with XPath: " + xpathBtnSave);
            }

            // Wait for the confirm button to be clickable before clicking
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement confirmButton = null;
            try {
                confirmButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpathBtnConfirmAT0026)));
            } catch (TimeoutException e) {
                System.err.println("Confirm button (AT0026) not found or not clickable within 10 seconds: " + xpathBtnConfirmAT0026);
            }

            if (confirmButton != null) {
                confirmButton.click();
                waitForLoadingElement();
            } else {
                // Error already logged by the catch block, or you can throw an exception
                // throw new RuntimeException("Confirm button (AT0026) not found or not clickable.");
            }

            try {
                // Alternative for WebUI.waitForElementVisible(By.xpath(xpathAT0021Title), 5);
                WebDriverWait titleWait = new WebDriverWait(driver, Duration.ofSeconds(5));
                titleWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpathAT0021Title)));
                System.out.println("Successfully navigated to AT0021 (Approval Status page).");
            } catch (TimeoutException e) {
                System.err.println("Failed to verify navigation to AT0021. Title not found: " + xpathAT0021Title);
            }
        });
    }

    @ExecutionStep(value = "createPeriodRequestOnAT0024B")
    public static void createPeriodRequestOnAT0024B(TeireiSetting setting) {
        String unitTime = setting.getUnitTime();
        String yearStart = setting.getYearStart();
        String monthStart = setting.getMonthStart();
        String dateStart = setting.getDateStart();
        String yearEnd = setting.getYearEnd();
        String monthEnd = setting.getMonthEnd();
        String dateEnd = setting.getDateEnd();
        String requestDescription = setting.getRequestDescription();

        ExecutionHelper.runStepWithLogging("Create period of day request on AT0024B", () -> {
            WebDriver driver = DriverFactory.getDriver();
            AttendanceUtils.navigateToATPage("at0024b");
            waitForLoadingElement();

            // XPaths from Katalon script
            String xpathSlUnitTime = "//option[@value='ALL_DAY']/.."; // Or more specific select tag
            String xpathRbPeriodOfDay = "//div[@class='specify-block']//span[normalize-space()='期間で指定']";
            String xpathSlDatePickerFormat = "//div[contains(@class,'block-date-period')]/div/div[%s]//input"; // %s will be 1 for start, 2 for end
            String xpathTxtRequestDescription = "//textarea";
            String xpathBtnSave = "//button[normalize-space()='申請']";
            String xpathBtnConfirmAT0026 = "//app-at0026//button[normalize-space()='はい']"; // Assuming from previous context
            String xpathAT0021Title = "//h1[normalize-space()='申請の承認状況']";

            // Select Unit Time
            WebElement unitTimeDropdown = WebUI.findWebElementIfVisible(By.xpath(xpathSlUnitTime));
            if (unitTimeDropdown != null) {
                Select selectUnitTime = new Select(unitTimeDropdown);
                selectUnitTime.selectByValue(unitTime);
                WebUI.sleep(300);
            } else {
                System.err.println("Unit time dropdown not found with XPath: " + xpathSlUnitTime);
            }

            // Scroll to Save button to ensure other elements are in view if needed
            WebElement saveButtonForScroll = WebUI.findWebElementIfVisible(By.xpath(xpathBtnSave));
            if (saveButtonForScroll != null) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", saveButtonForScroll);
                WebUI.sleep(300);
            }

            // Click 'Period of Day' radio button
            WebElement periodOfDayRadioButton = WebUI.findWebElementIfVisible(By.xpath(xpathRbPeriodOfDay));
            if (periodOfDayRadioButton != null) {
                periodOfDayRadioButton.click();
                WebUI.sleep(300); // Allow UI to update if date pickers appear/change
            } else {
                System.err.println("Period of Day radio button not found with XPath: " + xpathRbPeriodOfDay);
            }

            // Select Start Date
            String xpathStartDatePickerInput = String.format(xpathSlDatePickerFormat, "1");
            WebElement startDatePickerInput = WebUI.findWebElementIfVisible(By.xpath(xpathStartDatePickerInput));
            if (startDatePickerInput != null) {
                DateUtils.chooseDatePicker(startDatePickerInput, yearStart, monthStart, dateStart);
                WebUI.sleep(500);
            } else {
                System.err.println("Start date picker input not found with XPath: " + xpathStartDatePickerInput);
            }

            // Select End Date
            String xpathEndDatePickerInput = String.format(xpathSlDatePickerFormat, "2");
            WebElement endDatePickerInput = WebUI.findWebElementIfVisible(By.xpath(xpathEndDatePickerInput));
            if (endDatePickerInput != null) {
                DateUtils.chooseDatePicker(endDatePickerInput, yearEnd, monthEnd, dateEnd);
                WebUI.sleep(500);
            } else {
                System.err.println("End date picker input not found with XPath: " + xpathEndDatePickerInput);
            }

            // Set Request Description
            WebElement requestDescriptionTextarea = WebUI.findWebElementIfVisible(By.xpath(xpathTxtRequestDescription));
            if (requestDescriptionTextarea != null) {
                requestDescriptionTextarea.sendKeys(requestDescription);
                WebUI.sleep(300);
            } else {
                System.err.println("Request description textarea not found with XPath: " + xpathTxtRequestDescription);
            }

            // Click Save Button
            WebElement saveButton = WebUI.findWebElementIfVisible(By.xpath(xpathBtnSave));
            if (saveButton != null) {
                saveButton.click();
                waitForLoadingOverlayElement();
            } else {
                System.err.println("Save button not found with XPath: " + xpathBtnSave);
                // Consider throwing an exception if this is critical
            }

            // Click Confirm Button on AT0026 dialog/modal
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement confirmButton = null;
            try {
                confirmButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpathBtnConfirmAT0026)));
            } catch (TimeoutException e) {
                System.err.println("Confirm button (AT0026) not found or not clickable within 10 seconds: " + xpathBtnConfirmAT0026);
            }

            if (confirmButton != null) {
                confirmButton.click();
                waitForLoadingElement(); // Wait for navigation or page update
            } else {
                // Error already logged, or throw new RuntimeException("Confirm button (AT0026) not found or not clickable.");
            }

            // Verify navigation to AT0021
            try {
                WebDriverWait titleWait = new WebDriverWait(driver, Duration.ofSeconds(5));
                titleWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpathAT0021Title)));
                System.out.println("Successfully navigated to AT0021 (Approval Status page).");
            } catch (TimeoutException e) {
                System.err.println("Failed to verify navigation to AT0021. Title not found: " + xpathAT0021Title);
                // This doesn't stop the test, matching Katalon's empty catch block behavior
            }
        });
    }
}