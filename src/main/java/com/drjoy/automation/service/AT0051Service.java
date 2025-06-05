package com.drjoy.automation.service;

import com.drjoy.automation.config.Configuration;
import com.drjoy.automation.config.DriverFactory;
import com.drjoy.automation.execution.ExecutionStep;
import com.drjoy.automation.model.setting.TeireiSetting;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@Log4j2
public class AT0051Service extends AbstractTestSuite {

    /**
     * Thực hiện tìm kiếm user, click "休暇履歴" và lọc dữ liệu với filterName.
     * @throws InterruptedException Nếu delay bị gián đoạn
     */
    @ExecutionStep(value = "viewLeaveHistory")
    public static void viewLeaveHistory(TeireiSetting setting) throws InterruptedException {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(Configuration.getBaseUrl() + "at/at0049");
        // 2. Đợi dữ liệu load (10s)
        Thread.sleep(10000);

        // 1. Tìm ô input và nhập username
        WebElement searchInput = driver.findElement(By.xpath("//app-at0049//input[contains(@class, 'search-input')]"));
        searchInput.clear();
        searchInput.sendKeys(setting.getSearchText1());
        searchInput.sendKeys(Keys.ENTER);

        // 2. Đợi dữ liệu load (10s)
        Thread.sleep(5000);

        // 3. Click nút 休暇履歴 ứng với user
        WebElement leaveHistoryBtn = driver.findElement(By.xpath(
                String.format("//*[@id='tbl-sheet']//td[1]//span[normalize-space()='%s']/../../../../td[last()]//button[normalize-space()='休暇履歴']", setting.getSearchText1())
        ));
        leaveHistoryBtn.click();

        // 4. Đợi trang lịch sử load (10s)
        Thread.sleep(5000);

        // 5. Click filter theo tên (ví dụ 年休, 代休...)
        WebElement filterBtn = driver.findElement(By.xpath("//*[@id='table-header']//div[contains(@class,'table-title-text') and normalize-space()='休暇名']/../.."));
        filterBtn.click();
    }

    /**
     * Tìm kiếm username, mở lịch sử nghỉ phép, edit phân bổ nghỉ phép ở dòng thứ rowIndex với giá trị tăng giảm.
     * @throws InterruptedException Nếu bị gián đoạn khi delay
     */
    @ExecutionStep(value = "editDayOffAllocation")
    public static void editDayOffAllocation(TeireiSetting setting) throws InterruptedException {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(Configuration.getBaseUrl() + "at/at0049");

        // 2. Đợi dữ liệu load (10s)
        Thread.sleep(10000);

        // 1. Tìm ô input search và nhập username
        WebElement searchInput = driver.findElement(By.xpath("//app-at0049//input[contains(@class, 'search-input')]"));
        searchInput.clear();
        searchInput.sendKeys(setting.getSearchText1());
        searchInput.sendKeys(Keys.ENTER);

        // 2. Đợi dữ liệu load (10s)
        Thread.sleep(5000);

        // 3. Click nút 休暇履歴 ứng với user
        WebElement leaveHistoryBtn = driver.findElement(By.xpath(
                String.format("//*[@id='tbl-sheet']//td[1]//span[normalize-space()='%s']/../../../../td[last()]//button[normalize-space()='休暇履歴']", setting.getSearchText1())
        ));
        leaveHistoryBtn.click();

        // 4. Đợi trang lịch sử load (10s)
        Thread.sleep(5000);

        // 5. Click icon chỉnh sửa phân bổ nghỉ phép ở dòng rowIndex
        WebElement editBtn = driver.findElement(By.xpath(
                String.format("//*[@id='table-content']//tr[%s]//i[contains(@class,'btn-edit-day-off-allocation')]", setting.getAt0051OrderUser())
        ));
        editBtn.click();

        // 6. Click vào dropdown "増減"
        WebElement dropdown = driver.findElement(By.xpath("//div[@class='modal-day-off-body-label' and normalize-space()='増減']/following-sibling::div"));
        dropdown.click();

        // 7. Chọn giá trị tăng/giảm, ví dụ "+4.00"
        WebElement valueOption = driver.findElement(By.xpath(
                String.format("//div[@class='dd-custom-container']//span[normalize-space()='%s']/..", setting.getAt0051Number())
        ));
        valueOption.click();

        // 8. Click nút lưu (submit)
        WebElement submitBtn = driver.findElement(By.xpath("//button[contains(@class,'modal-day-off-btn-submit')]"));
        submitBtn.click();
    }

    @ExecutionStep(value = "confirmFirstLeaveHistoryRow")
    public static void confirmFirstLeaveHistoryRow(TeireiSetting setting) throws InterruptedException {
        WebDriver driver = DriverFactory.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(Configuration.getBaseUrl() + "at/at0049");

        // 2. Đợi dữ liệu load (10s)
        Thread.sleep(10000);

        // 1. Nhập username vào ô search
        WebElement searchInput = driver.findElement(By.xpath("//app-at0049//input[contains(@class, 'search-input')]"));
        searchInput.clear();
        searchInput.sendKeys(setting.getSearchText1());
        searchInput.sendKeys(Keys.ENTER);

        // 2. Đợi dữ liệu load (10s)
        Thread.sleep(5000);

        // 3. Click nút "休暇履歴" ứng với user
        WebElement leaveHistoryBtn = driver.findElement(By.xpath(
                String.format("//*[@id='tbl-sheet']//td[1]//span[normalize-space()='%s']/../../../../td[last()]//button[normalize-space()='休暇履歴']", setting.getSearchText1())
        ));
        leaveHistoryBtn.click();

        // 4. Click nút thao tác ở dòng đầu tiên của table content
        WebElement actionBtn = driver.findElement(By.xpath(
                "//div[@id='table-content']//tr[1]//td[last()]//button"
        ));
        actionBtn.click();

        // 5. Click nút xác nhận (confirm)
        WebElement confirmBtn = driver.findElement(By.xpath("//*[@id = 'positiveButton']"));
        confirmBtn.click();
    }

    @Override
    public List<String> getAllTestCase() {
        return List.of("viewLeaveHistory", "editDayOffAllocation", "confirmFirstLeaveHistoryRow");
    }
}
