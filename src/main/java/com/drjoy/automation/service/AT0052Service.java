package com.drjoy.automation.service;

import com.drjoy.automation.config.Configuration;
import com.drjoy.automation.config.DriverFactory;
import com.drjoy.automation.execution.ExecutionStep;
import com.drjoy.automation.model.ExportTemplateFilterSetting;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Log4j2
public class AT0052Service {

    @ExecutionStep(value = "executeAT0052Flow")
    public static void executeAT0052Flow(ExportTemplateFilterSetting setting) {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(Configuration.getBaseUrl() + "at/at0052");

        try {
            // Đợi page load (10s)
            Thread.sleep(5000);

            // Chọn "有給取得管理" trong dropdown label '種別'
            WebElement typeSelectElem = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//p[normalize-space()='種別']/following-sibling::select")));
            Select typeSelect = new Select(typeSelectElem);
            typeSelect.selectByVisibleText("有給取得管理");

            Thread.sleep(5000);

            // Click dropdown '所属を選択' và chọn 'すべて'
            WebElement belongDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//label[normalize-space()='所属を選択']/following-sibling::div")));
            belongDropdown.click();
            WebElement allOption = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//label[normalize-space()='所属を選択']/following-sibling::div//span[@class='text-ellipsis' and normalize-space()='すべて']")));
            allOption.click();
            // Đóng lại dropdown
            belongDropdown.click();

            // Chọn "すべて" trong dropdown '職業を選択'
            WebElement occupationSelectElem = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//p[normalize-space()='職業を選択']/following-sibling::select")));
            Select occupationSelect = new Select(occupationSelectElem);
            occupationSelect.selectByVisibleText("すべて");

            // Chọn "2022" trong dropdown '有給取得義務期日の属する年を選択'
            WebElement yearSelectElem = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//p[normalize-space()='有給取得義務期日の属する年を選択']/following-sibling::select")));
            Select yearSelect = new Select(yearSelectElem);
            yearSelect.selectByValue(setting.getAt0052Year());

            // Chọn "7" trong dropdown '月度を選択' (nếu có)
            try {
                WebElement monthSelectElem = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//p[normalize-space()='月度を選択']/following-sibling::select")));
                Select monthSelect = new Select(monthSelectElem);
                monthSelect.selectByValue(setting.getAt0052Number());
            } catch (Exception ignore) {
                // Nếu không có dropdown này thì bỏ qua
            }

            // Gõ tìm kiếm và nhấn ENTER
            WebElement txtSearch = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[@class='search']/input")));
            txtSearch.clear();
            txtSearch.sendKeys("");
            txtSearch.sendKeys(Keys.ENTER);

            Thread.sleep(5000);

            // Click button "csv" và xác nhận nếu xuất hiện popup
            try {
                WebElement csvBtn = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[normalize-space()='csv']")));
                csvBtn.click();

                WebElement confirmBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//*[@id = 'positiveButton']")));
                confirmBtn.click();
            } catch (Exception ignore) {
                // Nếu không có popup hoặc nút csv, bỏ qua
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Thực hiện toàn bộ quy trình kiểm thử quản lý thời gian đơn vị của 有給取得
     * Giả định: Đã login và đã ở đúng trang '/at/at0052'
     */
    @ExecutionStep(value = "runHourlyPaidLeaveManagement")
    public static void runHourlyPaidLeaveManagement(ExportTemplateFilterSetting setting) {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(Configuration.getBaseUrl() + "at/at0052");

        try {
            // Đợi 3s
            Thread.sleep(5000);

            // Chọn "時間単位の有給取得時間数管理" ở dropdown 種別
            WebElement typeDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//p[normalize-space()='種別']/following-sibling::select")));
            Select selectType = new Select(typeDropdown);
            selectType.selectByVisibleText("時間単位の有給取得時間数管理");

            // Chọn '所属を選択' → すべて
            WebElement belongDiv = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//label[normalize-space()='所属を選択']/following-sibling::div")));
            belongDiv.click();
            WebElement allBelong = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//label[normalize-space()='所属を選択']/following-sibling::div//span[@class='text-ellipsis' and normalize-space()='すべて']")));
            allBelong.click();
            belongDiv.click(); // Đóng lại dropdown

            // Chọn "すべて" trong dropdown 職業を選択
            WebElement occupationDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//p[normalize-space()='職業を選択']/following-sibling::select")));
            Select selectOccupation = new Select(occupationDropdown);
            selectOccupation.selectByVisibleText("すべて");

            // Chọn năm "2022" ở dropdown 年を選択
            WebElement yearDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//p[normalize-space()='年を選択']/following-sibling::select")));
           Select selectYear = new Select(yearDropdown);
            selectYear.selectByValue(setting.getAt0052Year());

            // Nếu có dropdown '月度を選択', chọn "7"
            try {
                WebElement monthDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//p[normalize-space()='月度を選択']/following-sibling::select")));
                Select selectMonth = new Select(monthDropdown);
                selectMonth.selectByValue(setting.getAt0052Number());
            } catch (Exception ignore) {
                // Không có cũng không sao
            }

            // Search
            WebElement txtSearch = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[@class='search']/input")));
            txtSearch.clear();
            txtSearch.sendKeys("");
            txtSearch.sendKeys(Keys.ENTER);

            // Click export csv và xác nhận popup nếu có
            try {
                WebElement csvBtn = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[normalize-space()='csv']")));
                csvBtn.click();

                WebElement confirmBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//*[@id = 'positiveButton']")));
                confirmBtn.click();
            } catch (Exception ignore) {
                // Có thể popup không xuất hiện, bỏ qua
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
