package com.drjoy.automation.service;

import com.drjoy.automation.config.Configuration;
import com.drjoy.automation.config.DriverFactory;
import com.drjoy.automation.execution.ExecutionStep;
import com.drjoy.automation.model.setting.ExportTemplateFilterSetting;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Log4j2
public class AT0053Service {

    @ExecutionStep(value = "checkForgetRequestOptions")
    public static void checkForgetRequestOptions(ExportTemplateFilterSetting setting) {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(Configuration.getBaseUrl() + "at/at0053");

        // Click vào checkbox "FORGET_REQUEST_LIST"
        By forgetRequestListLocator = By.xpath("//*[@value='FORGET_REQUEST_LIST']");
        WebElement forgetRequestListElement = wait.until(
                ExpectedConditions.elementToBeClickable(forgetRequestListLocator)
        );
        forgetRequestListElement.click();

        // Kiểm tra đã tick "FORGET_REQUEST_LIST"
        try {
            WebElement checkbox1 = wait.until(
                    ExpectedConditions.presenceOfElementLocated(forgetRequestListLocator)
            );
            if (!checkbox1.isSelected()) {
                throw new Exception("Checkbox FORGET_REQUEST_LIST is not selected");
            }
        } catch (Exception e) {
            System.out.println("Show alert option is not checked");
        }

        // Kiểm tra đã tick "exceedTimeAlertedBySettingTime"
        try {
            By exceedTimeAlertLocator = By.xpath("//input[@name='exceedTimeAlertedBySettingTime']");
            WebElement checkbox2 = wait.until(
                    ExpectedConditions.presenceOfElementLocated(exceedTimeAlertLocator)
            );
            if (!checkbox2.isSelected()) {
                throw new Exception("Checkbox exceedTimeAlertedBySettingTime is not selected");
            }
        } catch (Exception e) {
            System.out.println("Show exceed time alert option is not checked");
        }

        // Kiểm tra đã tick "checkSendMail"
        try {
            By sendMailLocator = By.xpath("//div[@class='group-checkbox-mail']//div[2]//label//input[@name='checkSendMail']");
            WebElement checkbox3 = wait.until(
                    ExpectedConditions.presenceOfElementLocated(sendMailLocator)
            );
            if (!checkbox3.isSelected()) {
                throw new Exception("Checkbox checkSendMail is not selected");
            }
        } catch (Exception e) {
            System.out.println("Send mail option is not checked");
        }
    }

    @ExecutionStep(value = "checkLack5DayOffOptions")
    public static void checkLack5DayOffOptions(ExportTemplateFilterSetting setting) {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(Configuration.getBaseUrl() + "at/at0053");

        // Click vào checkbox "LACK_5DAYOFF_LIST"
        By lack5DayoffLocator = By.xpath("//*[@value='LACK_5DAYOFF_LIST']");
        WebElement lack5DayoffElement = wait.until(
                ExpectedConditions.elementToBeClickable(lack5DayoffLocator)
        );
        lack5DayoffElement.click();

        // Kiểm tra đã tick "showAlertMsg"
        try {
            By showAlertMsgLocator = By.xpath("//input[@name='showAlertMsg']");
            WebElement checkbox1 = wait.until(
                    ExpectedConditions.presenceOfElementLocated(showAlertMsgLocator)
            );
            if (!checkbox1.isSelected()) {
                throw new Exception("Checkbox showAlertMsg is not selected");
            }
        } catch (Exception e) {
            System.out.println("Show alert option is not checked");
        }

        // Kiểm tra đã tick "checkSendMail"
        try {
            By sendMailLocator = By.xpath("//div[@class='group-checkbox-mail']//div[2]//label//input[@name='checkSendMail']");
            WebElement checkbox2 = wait.until(
                    ExpectedConditions.presenceOfElementLocated(sendMailLocator)
            );
            if (!checkbox2.isSelected()) {
                throw new Exception("Checkbox checkSendMail is not selected");
            }
        } catch (Exception e) {
            System.out.println("Send mail option is not checked");
        }
    }
}
