package com.drjoy.automation.service;

import com.drjoy.automation.config.Configuration;
import com.drjoy.automation.config.DriverFactory;
import com.drjoy.automation.execution.ExecutionStep;
import com.drjoy.automation.model.setting.ExportTemplateFilterSetting;
import com.drjoy.automation.model.setting.TeireiSetting;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@Log4j2
public class AT0065Service extends AbstractTestSuite {

    /**
     * Test case: AT0065_EditUserDetailCol1AndSave
     * Tìm kiếm user, vào chi tiết, đổi user ở select-col1 và lưu lại.
     */
    @ExecutionStep(value = "editUserDetailCol1AndSave1")
    public static  void editUserDetailCol1AndSave1(TeireiSetting setting) throws InterruptedException {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(Configuration.getBaseUrl() + "at/at0064");

        Thread.sleep(5000);

        // 1. Tìm user cần thao tác
        By searchInputLocator = By.xpath("//*[@id='tab-content32']//input[contains(@class, 'search-input') and @placeholder='ユーザー名を検索']");
        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(searchInputLocator));
        searchInput.clear();
        searchInput.sendKeys(setting.getSearchText1());
        searchInput.sendKeys(Keys.ENTER);

        // 2. Đợi kết quả filter
        Thread.sleep(2000);

        // 3. Click vào nút chi tiết của user
        By detailBtnLocator = By.xpath(String.format(
                "//*[@id='tbl-sheet']//td[1]//span[text()='%s']/../../../..//div[contains(@class, 'btn-detail')]/button",
                setting.getSearchText1()));
        WebElement detailBtn = wait.until(ExpectedConditions.elementToBeClickable(detailBtnLocator));
        detailBtn.click();

        // 4. Tìm ô search user mới ở select-col1 để chọn user chuyển sang
        By innerSearchInputLocator = By.xpath("//div[@class='select-col1']//div[@class='search']/input[@type='text' and contains(@class, 'search-input')]");
        WebElement innerSearchInput = wait.until(ExpectedConditions.elementToBeClickable(innerSearchInputLocator));
        innerSearchInput.clear();
        innerSearchInput.sendKeys(setting.getSearchText2());

        // 5. Đợi gợi ý user xuất hiện
        Thread.sleep(2000);

        // 6. Gửi phím ENTER để hiện list user
        innerSearchInput.sendKeys(Keys.ENTER);

        // 7. Click chọn đúng user
        By userSelectLocator = By.xpath(String.format(
                "//ul[contains(@class, 'select-user')]/virtual-scroll//div[contains(@class, 'select-user-name')]/span[normalize-space(text())='%s']",
                setting.getSearchText2()));
        WebElement userSelect = wait.until(ExpectedConditions.elementToBeClickable(userSelectLocator));
        userSelect.click();
        Thread.sleep(1000);

        // 8. Cuộn xuống vị trí (0, 100)
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollTo(0, 100);");

        // 9. Click nút Lưu
        By saveBtnLocator = By.xpath("//div[contains(@class, 'select-content')]/following-sibling::div/button[normalize-space(text())='保存']");
        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(saveBtnLocator));
        saveBtn.click();
        Thread.sleep(1000);

        // 10. Click xác nhận
        By positiveBtnLocator = By.xpath("//*[@id = 'positiveButton']");
        WebElement positiveBtn = wait.until(ExpectedConditions.elementToBeClickable(positiveBtnLocator));
        positiveBtn.click();
        Thread.sleep(5000);
    }

    /**
     * Test case: AT0065_EditUserDetailCol1AndSave
     * Tìm kiếm user, vào chi tiết, đổi user ở select-col1 và lưu lại.
     */
    @ExecutionStep(value = "editUserDetailCol1AndSave2")
    public static  void editUserDetailCol1AndSave2(TeireiSetting setting) throws InterruptedException {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(Configuration.getBaseUrl() + "at/at0064");

        Thread.sleep(5000);

        // 1. Nhập tên user cần tìm vào ô tìm kiếm
        By searchInputLocator = By.xpath("//*[@id='tab-content32']//input[contains(@class, 'search-input') and @placeholder='ユーザー名を検索']");
        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(searchInputLocator));
        searchInput.clear();
        searchInput.sendKeys(setting.getSearchText1());
        searchInput.sendKeys(Keys.ENTER);

        // 2. Đợi filter kết quả
        Thread.sleep(2000);

        // 3. Click vào nút chi tiết của user
        By detailBtnLocator = By.xpath(String.format(
                "//*[@id='tbl-sheet']//td[1]//span[text()='%s']/../../../..//div[contains(@class, 'btn-detail')]/button",
                setting.getSearchText1()));
        WebElement detailBtn = wait.until(ExpectedConditions.elementToBeClickable(detailBtnLocator));
        detailBtn.click();
        Thread.sleep(1000);

        // 4. Tìm ô search user mới ở select-col1 để chọn user chuyển sang
        By innerSearchInputLocator = By.xpath("//div[@class='select-col3']//div[@class='search']/input[@type='text' and contains(@class, 'search-input')]");
        WebElement innerSearchInput = wait.until(ExpectedConditions.elementToBeClickable(innerSearchInputLocator));
        innerSearchInput.clear();
        innerSearchInput.sendKeys(setting.getSearchText2());

        // 5. Đợi gợi ý user xuất hiện
        Thread.sleep(2000);

        // 6. Gửi phím ENTER để hiện list user
        innerSearchInput.sendKeys(Keys.ENTER);

        // 7. Click chọn đúng user
        By userSelectLocator = By.xpath(String.format(
                "//ul[contains(@class, 'select-user')]/virtual-scroll//div[contains(@class, 'select-user-name')]/span[normalize-space(text())='%s']",
                setting.getSearchText2()));
        WebElement userSelect = wait.until(ExpectedConditions.elementToBeClickable(userSelectLocator));
        userSelect.click();
        Thread.sleep(1000);

        // 8. Cuộn xuống vị trí (0, 100)
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollTo(0, 100);");

        // 9. Click nút Lưu
        By saveBtnLocator = By.xpath("//div[contains(@class, 'select-content')]/following-sibling::div/button[normalize-space(text())='保存']");
        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(saveBtnLocator));
        saveBtn.click();

        // 10. Click xác nhận popup
        By positiveBtnLocator = By.xpath("//*[@id = 'positiveButton']");
        WebElement positiveBtn = wait.until(ExpectedConditions.elementToBeClickable(positiveBtnLocator));
        positiveBtn.click();
        Thread.sleep(5000);
    }

    @Override
    public List<String> getAllTestCase() {
        return List.of("editUserDetailCol1AndSave1", "editUserDetailCol1AndSave2");
    }
}
