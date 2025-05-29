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
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Log4j2
public class AT0064Service {

    /**
     * Test case: AT0064_SearchAndClickUserDetail
     * Tìm kiếm user và click vào nút chi tiết ở màn hình AT0064.
     */
    @ExecutionStep(value = "searchAndClickUserDetail")
    public static void searchAndClickUserDetail(ExportTemplateFilterSetting setting) throws InterruptedException {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(Configuration.getBaseUrl() + "at/at0064");

        Thread.sleep(5000);

        // Nhập tên user cần tìm vào ô tìm kiếm
        By searchInputLocator = By.xpath("//*[@id='tab-content32']//input[contains(@class, 'search-input') and @placeholder='ユーザー名を検索']");
        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(searchInputLocator));
        searchInput.clear();
        searchInput.sendKeys(setting.getSearchText1());

        // Gửi phím ENTER
        searchInput.sendKeys(Keys.ENTER);

        // Delay 2s cho hệ thống filter kết quả
        Thread.sleep(2000);

        // Click vào nút chi tiết của user
        By detailBtnLocator = By.xpath(String.format(
                "//*[@id='tbl-sheet']//td[1]//span[text()='%s']/../../../..//div[contains(@class, 'btn-detail')]/button",
                setting.getSearchText1()));
        WebElement detailBtn = wait.until(ExpectedConditions.elementToBeClickable(detailBtnLocator));
        detailBtn.click();
    }

}
