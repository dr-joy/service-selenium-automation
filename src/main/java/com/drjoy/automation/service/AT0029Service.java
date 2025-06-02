package com.drjoy.automation.service;

import com.drjoy.automation.config.Configuration;
import com.drjoy.automation.config.DriverFactory;
import com.drjoy.automation.execution.ExecutionStep;
import com.drjoy.automation.model.ExportTemplateFilterSetting;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Log4j2
public class AT0029Service {

    /**
     * Thực hiện quy trình xuất CSV "休暇残数"
     * Giả định đã login, đã chuyển đúng URL.
     */
    @ExecutionStep(value = "exportLeaveBalance")
    public static void exportLeaveBalanceCsv(ExportTemplateFilterSetting setting) throws InterruptedException {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(Configuration.getBaseUrl() + "at/at0029");

        Thread.sleep(5000);

        // Chọn "休暇残数" ở dropdown
        WebElement selectElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@class='type-export-csv']//select")
        ));
        Select select = new Select(selectElement);
        select.selectByVisibleText("休暇残数");

        // Nhấn button 作成
        WebElement btnCreate = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='作成']")
        ));
        btnCreate.click();

        Thread.sleep(3000);

        boolean isCompleted = false;
        By btnDownloadBy = By.xpath("//*[@id='tbl-sheet']//tr[1]//button[normalize-space()='ダウンロード']");
        By btnReloadBy = By.xpath("//button[contains(@class,'btn-repeat')]");

        WebElement btnReload = wait.until(ExpectedConditions.elementToBeClickable(btnReloadBy));

        Thread.sleep(3000);

        // Lấy text trạng thái import, nếu trống thì throw exception
        String txtWaitForImporting = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[@id='tbl-sheet']//tr[1]//td[last()]")
        )).getText();
        if (txtWaitForImporting == null || txtWaitForImporting.trim().isEmpty()) {
            throw new RuntimeException("Importing's failed");
        }

        // Vòng lặp chờ tới khi xuất hiện nút Download
        while (!isCompleted) {
            try {
                wait.withTimeout(java.time.Duration.ofSeconds(1));
                driver.findElement(btnDownloadBy);
                isCompleted = true;
            } catch (NoSuchElementException | TimeoutException e) {
                btnReload.click();
                // Có thể sleep nhẹ để tránh spam click quá nhanh
                try { Thread.sleep(1000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }

//        // Scroll tới nút reload (nếu cần)
//        ((org.openqa.selenium.JavascriptExecutor) driver)
//                .executeScript("arguments[0].scrollIntoView(true);", btnReload);
        Thread.sleep(3000);

        // Click Download
        WebElement btnDownload = wait.until(ExpectedConditions.elementToBeClickable(btnDownloadBy));
        btnDownload.click();
        Thread.sleep(1000);
    }

}
