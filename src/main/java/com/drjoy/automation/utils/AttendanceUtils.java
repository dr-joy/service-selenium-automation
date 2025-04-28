package com.drjoy.automation.utils;

import com.drjoy.automation.config.Configuration;
import com.drjoy.automation.config.DriverFactory;
import com.drjoy.automation.model.ExportTemplateFilterSetting;
import com.drjoy.automation.repository.ExcelReaderRepository;
import com.drjoy.automation.service.LoginService;
import com.drjoy.automation.utils.xpath.at.Screen;
import com.drjoy.automation.utils.xpath.common.XpathCommon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AttendanceUtils {
    private static final Logger logger = LogManager.getLogger(AttendanceUtils.class);
    private AttendanceUtils() {}

    public static void processPhases(int start, int total, Consumer<ExportTemplateFilterSetting> testLogic) {
        List<ExportTemplateFilterSetting> settings = ExcelReaderRepository.findAllExportFilterSetting();
        String previousUsername = "";

        for (int phaseNumber = start; phaseNumber <= total; phaseNumber++) {
            if (phaseNumber - 1 < settings.size()) {
                ExportTemplateFilterSetting rowData = settings.get(phaseNumber - 1);

                String username = rowData.getUserName();
                String password = rowData.getPassword();

                // In ra thông tin đăng nhập cho phase
                System.out.println("Processing phase: " + rowData);

                // Thực hiện đăng nhập nếu user khác với phase trước
                if (!username.equals(previousUsername) && !username.isEmpty() && !password.isEmpty()) {
                    LoginService.login(username, password);
                    previousUsername = username;
                }

                // Gọi logic kiểm thử
                testLogic.accept(rowData);

                // Thực hiện đăng xuất nếu user của phase tiếp theo khác
                if (phaseNumber < total) {
                    ExportTemplateFilterSetting nextPhase = settings.get(phaseNumber);
                    if (!username.equals(nextPhase.getUserName())) {
                        LoginService.logout();
                        previousUsername = "";
                    }
                }
            }
        }
    }

    public static void navigateToATPage(String pageName) {
        AttendanceUtils.waitForLoadingElement();
        logger.info("Start navigate to page {}", pageName);

        WebUI.sleep(2000);
        waitForLoadingElement();
        WebDriver driver = DriverFactory.getDriver();
        String curURL = driver.getCurrentUrl();
        String targetURL = String.format("%sat/%s", Configuration.getBaseUrl(), pageName);

        if (!targetURL.equals(curURL)) {
            if (curURL.contains("/at/")) {
                WebUI.sleep(1000);
                String accessBtnXpath = String.format(
                    "//app-at0001//ul[@role='tablist']/li[%d]", Screen.valueOf(pageName.toUpperCase()).indexInNavBar
                );
                WebElement accessBtn = WebUI.findWebElementIfVisible(By.xpath(accessBtnXpath));
                WebUI.clickWithScrollTo(accessBtn);
                WebUI.mouseOver(accessBtn);

                WebElement targetPage = WebUI.findWebElementIfVisible(By.xpath(Screen.valueOf(pageName.toUpperCase()).xpathToScreen));
                targetPage.click();
            } else if (curURL.contains("/me/me0090")) {
                String accessAtFuncSideMenu = "//app-side-menu-drjoy//i[@class='fa fa-clock-o']/following-sibling::strong[text()='勤務管理']";
                By byAccessByXpath = By.xpath(accessAtFuncSideMenu);

                WebElement atFuncSideMenu = WebUI.findWebElementIfVisible(byAccessByXpath);
                atFuncSideMenu.click();

                String accessAT0001BtnXpath = accessAtFuncSideMenu + "/ancestor::a[@class='nav-link nav-table']/following-sibling::ul/li[1]";
                By byAccessAt0001Btn = By.xpath(accessAT0001BtnXpath);
                WebUI.click(byAccessAt0001Btn);
            } else {
                driver.navigate().to(targetURL);
                WebUI.sleep(1000);
            }
        }

        waitForLoadingElement();
    }

    public static void selectUserAndMonthOnTimesheetPage(String targetUser, String monthYear) {
        String[] splitMonthYear = DateUtils.splitMonthYear(monthYear);
        String targetYear = splitMonthYear[0];
        String targetMonth = splitMonthYear[1];

        waitForLoadingElement();

        // Kiểm tra current month-year
        WebUI.sleep(1000);
        WebElement monthYearElement = WebUI.findWebElementIfVisible(By.xpath("//*[@id='tab-content1']/app-at0001-summary//div[contains(@class, 'header-block-option')]//h2"));
        String currMonthYearText = convertMonthYearToAT0001TitleFormat(monthYearElement.getText());

        if (monthYear != null && !monthYear.equals(currMonthYearText)) {
            // Click nút chọn tháng
//            WebUI.sleep(2000);
            waitForLoadingOverlayElement();
            WebElement chooseMonthBtn = WebUI.findWebElementIfVisible(By.xpath("//app-at0001-date-picker//span[text()='月選択']/ancestor::button"));
            WebUI.clickAtCoordinates(800, 400);
            chooseMonthBtn.click();

            String baseDatePickerXpath = "//app-at0001-date-picker//div[contains(@class, 'date-picker')]";
            String textYearCenterXpath = baseDatePickerXpath + "//div[contains(@class, 'date-tab-bar')]//div[contains(@class, 'text-center')]//span";

            WebElement yearElement = WebUI.findWebElementIfVisible(By.xpath(textYearCenterXpath));
            String currYear = yearElement.getText();

            // Di chuyển đến năm mục tiêu
            while (Integer.parseInt(targetYear) < Integer.parseInt(currYear)) {
                String backYearBtnXpath = baseDatePickerXpath + "//div[contains(@class, 'date-tab-bar')]//div[contains(@class, 'text-left')]//i";
                WebUI.findWebElementIfVisible(By.xpath(backYearBtnXpath)).click();
                currYear = WebUI.findWebElementIfVisible(By.xpath(textYearCenterXpath)).getText();
            }

            // Chọn tháng
            String targetMonthXpath = baseDatePickerXpath + "//div[contains(@class, 'month text-center')]//button[" + targetMonth + "]";
            WebUI.findWebElementIfVisible(By.xpath(targetMonthXpath)).click();
        }

        // Chọn phòng ban "すべて"
        waitForLoadingOverlayElement();
        String baseDeptSelectionXpath = "//app-history-department-select";
        WebUI.findWebElementIfVisible(By.xpath(baseDeptSelectionXpath)).click();
        WebUI.findWebElementIfVisible(By.xpath(baseDeptSelectionXpath + "//div[contains(@class, 'search-dept')]/input")).sendKeys("すべて");
        WebUI.findWebElementIfVisible(By.xpath(baseDeptSelectionXpath + "//div[contains(@class, 'department-content')]//span[text()='すべて']")).click();
        waitForLoadingElement();

        // Chọn user mục tiêu
        String baseUserSelectionXpath = "//app-at0001-summary//div[contains(@class, 'hoz-user pl-2')]";
        WebUI.findWebElementIfVisible(By.xpath(baseUserSelectionXpath)).click();

        WebUI.findWebElementIfVisible(By.xpath(baseUserSelectionXpath + "//div[contains(@class, 'wrap-popup')]//div[contains(@class, 'search-name')]//input"))
            .sendKeys(targetUser);

        WebUI.findWebElementIfVisible(By.xpath(baseUserSelectionXpath + "//div[contains(@class, 'wrap-popup')]//div[contains(@class, 'search-name')]//button"))
            .click();

        String targetUserBtnXpath = baseUserSelectionXpath +
            "//div[contains(@class, 'wrap-popup')]//div[contains(@class, 'popup-content fs14')]//span[normalize-space(text())='" + targetUser + "']";

        List<WebElement> userBtns = WebUI.findWebElementsIfVisible(By.xpath(targetUserBtnXpath));
        if (!userBtns.isEmpty()) {
            userBtns.get(0).click();
        }

        waitForLoadingElement();
    }

    public static String convertMonthYearToAT0001TitleFormat(String input) {
        Pattern pattern = Pattern.compile("(\\d{4})年(\\d{1,2})月");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String year = matcher.group(1);
            String month = String.format("%02d", Integer.parseInt(matcher.group(2)));
            return year + month;
        }
        return "";
    }

    public static void waitForLoadingOverlayElement() {
        By loadingCircle = By.cssSelector(".loader-overlay");
        WebUI.waitForElementNotPresent(loadingCircle, WebUI.LARGE_TIMEOUT);
    }

    public static void waitForLoadingElement() {
        By loadingCircle = By.xpath(XpathCommon.APP_LOAD_CIRCLE.value);
        WebUI.waitForElementNotPresent(loadingCircle, WebUI.LARGE_TIMEOUT);
    }
}
