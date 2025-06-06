package com.drjoy.automation.utils;

import com.drjoy.automation.config.Configuration;
import com.drjoy.automation.config.DriverFactory;
import com.drjoy.automation.execution.ExecutionHelper;
import com.drjoy.automation.logging.TaskLoggerManager;
import com.drjoy.automation.model.setting.ExportTemplateFilterSetting;
import com.drjoy.automation.model.JobType;
import com.drjoy.automation.repository.ExcelReaderRepository;
import com.drjoy.automation.utils.xpath.at.Screen;
import com.drjoy.automation.utils.xpath.common.XpathCommon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.drjoy.automation.utils.WebUI.*;
import static java.lang.String.format;

public class AttendanceUtils {
    private static final Logger logger = LogManager.getLogger(AttendanceUtils.class);
    private AttendanceUtils() {}

    public static void navigateToATPage(String pageName) {
        waitForLoadingElement();
        logger.info("Start navigate to page {}", pageName);

        WebUI.sleep(2000);
        waitForLoadingElement();
        WebDriver driver = DriverFactory.getDriver();
        String curURL = driver.getCurrentUrl();
        String targetURL = format("%sat/%s", Configuration.getBaseUrl(), pageName);

        ExecutionHelper.runStepWithLogging(format("Navigate to %s with %s", pageName, targetURL), () -> {
            if (!targetURL.equals(curURL)) {
                WebUI.scrollToTop();

                if (curURL.contains("/at/")) {
                    WebUI.sleep(1000);
                    String accessBtnXpath = format(
                        "//app-at0001//ul[@role='tablist']/li[%d]", Screen.valueOf(pageName.toUpperCase()).indexInNavBar
                    );
                    WebElement accessBtn = WebUI.findWebElementIfVisible(By.xpath(accessBtnXpath));
                    WebUI.mouseOver(accessBtn, 1000);

                    By targetPageBy = By.xpath(Screen.valueOf(pageName.toUpperCase()).xpathToScreen);
                    WebUI.clickByJS(WebUI.findWebElementIfVisible(targetPageBy));

                    waitForLoadingElement();
                    waitForLoadingOverlayElement();
                } else if (curURL.contains("/me/me0090")) {
                    String accessAtFuncSideMenu = "//app-side-menu-drjoy//i[@class='fa fa-clock-o']/following-sibling::strong[text()='勤務管理']";
                    By byAccessByXpath = By.xpath(accessAtFuncSideMenu);

                    WebElement atFuncSideMenu = WebUI.findWebElementIfVisible(byAccessByXpath);
                    atFuncSideMenu.click();

                    String accessAT0001BtnXpath = accessAtFuncSideMenu + "/ancestor::a[@class='nav-link nav-table']/following-sibling::ul/li[1]";
                    By byAccessAt0001Btn = By.xpath(accessAT0001BtnXpath);
                    WebUI.click(byAccessAt0001Btn);
                    waitForLoadingElement();

                    waitForLoadingOverlayElement();

                    if (!pageName.equals("at0001")) {
                        WebUI.sleep(1000);
                        waitForLoadingElement();
                        String accessBtnXpath = format(
                            "//app-at0001//ul[@role='tablist']/li[%d]", Screen.valueOf(pageName.toUpperCase()).indexInNavBar
                        );
                        WebElement accessBtn = WebUI.findWebElementIfVisible(By.xpath(accessBtnXpath));
                        WebUI.mouseOver(accessBtn, 500);

                        waitForLoadingElement();
                        WebElement targetPage = WebUI.findWebElementIfVisible(By.xpath(Screen.valueOf(pageName.toUpperCase()).xpathToScreen));
                        WebUI.clickByJS(targetPage);

                        waitForLoadingElement();
                        waitForLoadingOverlayElement();
                    }
                } else {
                    driver.navigate().to(targetURL);
                    WebUI.sleep(1000);
                }
            }
        });

        waitForLoadingElement();

        TaskLoggerManager.info("Loading page ....");
        waitForConditionSucceed(driver.getCurrentUrl().equals(targetURL));

        waitForLoadingElement();
        WebUI.click(By.xpath("//app-at0001"));
    }

    public static void selectUserAndMonthOnTimesheetPage(ExportTemplateFilterSetting setting) {
        String[] splitMonthYear = DateUtils.splitMonthYear(setting.getTargetMonth());
        String targetYear = splitMonthYear[0];
        String targetMonth = splitMonthYear[1];

        waitForLoadingElement();

        // Kiểm tra current month-year
        WebUI.sleep(500);

        By monthYearBy = By.xpath("//*[@id='tab-content1']/app-at0001-summary//div[contains(@class, 'header-block-option')]//h2");
        waitForConditionSucceed(WebUI.isElementPresent(monthYearBy, 500L));

        WebElement monthYearElement = WebUI.findWebElementIfVisible(monthYearBy);
        String currMonthYearText = convertMonthYearToAT0001TitleFormat(monthYearElement.getText());

        ExecutionHelper.runStepWithLoggingByPhase(setting, format("Select Month: month: %s", setting.getTargetMonth()), () -> {
            if (setting.getTargetMonth() != null && !setting.getTargetMonth().equals(currMonthYearText)) {
                // Click nút chọn tháng
                waitForLoadingOverlayElement();
                WebUI.sleep(200);
                WebElement chooseMonthBtn = WebUI.findWebElementIfVisible(By.xpath("//app-at0001-date-picker//span[text()='月選択']/ancestor::button"));
                WebUI.clickAtCoordinates(800, 400);
                WebUI.sleep(200);
                WebUI.clickByJS(chooseMonthBtn);

                String baseDatePickerXpath = "//app-at0001-date-picker//div[contains(@class, 'date-picker')]";
                String textYearCenterXpath = baseDatePickerXpath + "//div[contains(@class, 'date-tab-bar')]//div[contains(@class, 'text-center')]//span";

                WebElement yearElement = WebUI.findWebElementIfVisible(By.xpath(textYearCenterXpath));
                String currYear = yearElement.getText();

                // Di chuyển đến năm mục tiêu
                while (Integer.parseInt(targetYear) < Integer.parseInt(currYear)) {
                    String backYearBtnXpath = baseDatePickerXpath + "//div[contains(@class, 'date-tab-bar')]//div[contains(@class, 'text-left')]//i";
                    WebUI.clickByJS(WebUI.findWebElementIfVisible(By.xpath(backYearBtnXpath)));
                    currYear = WebUI.findWebElementIfVisible(By.xpath(textYearCenterXpath)).getText();
                }

                // Chọn tháng
                WebUI.sleep(300);
                String targetMonthXpath = baseDatePickerXpath + "//div[contains(@class, 'month text-center')]//button[" + targetMonth + "]";
                WebUI.clickByJS(WebUI.findWebElementIfVisible(By.xpath(targetMonthXpath)));
                WebUI.sleep(200);
            }
        });

        // Chọn phòng ban "すべて"
        ExecutionHelper.runStepWithLoggingByPhase(setting, "Select department", () -> {
            WebUI.sleep(200);
            waitForLoadingOverlayElement();
            WebUI.sleep(400);
            String baseDeptSelectionXpath = "//app-history-department-select";
            By deptSelectionBy = By.xpath(baseDeptSelectionXpath);
            WebUI.findWebElementIfVisible(deptSelectionBy).click();

            WebUI.sleep(500);
            WebUI.findWebElementIfVisible(By.xpath(baseDeptSelectionXpath + "//div[contains(@class, 'search-dept')]/input")).sendKeys("すべて");
            WebElement allDeptBtn = WebUI.findWebElementIfVisible(By.xpath(baseDeptSelectionXpath + "//div[contains(@class, 'department-content')]//span[text()='すべて']"));

            WebUI.clickByJS(allDeptBtn);
            waitForLoadingElement();
        });

        // Chọn user mục tiêu
        ExecutionHelper.runStepWithLoggingByPhase(setting, format("Select user: targetUser: %s", setting.getTargetUser()), () -> {
            String baseUserSelectionXpath = "//app-at0001-summary//div[contains(@class, 'hoz-user pl-2')]";
            WebUI.findWebElementIfVisible(By.xpath(baseUserSelectionXpath)).click();

            WebUI.findWebElementIfVisible(By.xpath(baseUserSelectionXpath + "//div[contains(@class, 'wrap-popup')]//div[contains(@class, 'search-name')]//input"))
                .sendKeys(setting.getTargetUser());

            WebUI.sleep(200);
            WebElement searchUserBtn = WebUI.findWebElementIfVisible(
                By.xpath(baseUserSelectionXpath + "//div[contains(@class, 'wrap-popup')]//div[contains(@class, 'search-name')]//button")
            );
            WebUI.clickByJS(searchUserBtn);
            WebUI.sleep(500);

            String targetUserBtnXpath = baseUserSelectionXpath +
                "//div[contains(@class, 'wrap-popup')]//div[contains(@class, 'popup-content fs14')]//span[normalize-space(text())='" + setting.getTargetUser() + "']";

            int findSearchBtnCounter = 0;
            while (!WebUI.isElementPresent(By.xpath(targetUserBtnXpath), 1)) {
                if (findSearchBtnCounter >= 5) break;

                WebUI.clickByJS(searchUserBtn);
                WebUI.sleep(500);

                findSearchBtnCounter++;
            }

            List<WebElement> userBtns = WebUI.findWebElementsIfVisible(By.xpath(targetUserBtnXpath));
            WebUI.sleep(200);
            if (!userBtns.isEmpty()) {
                WebElement targetBtn = userBtns.get(0);
                WebUI.clickByJS(targetBtn);
            }

            waitForLoadingElement();
        });
    }

    public static String convertMonthYearToAT0001TitleFormat(String input) {
        Pattern pattern = Pattern.compile("(\\d{4})年(\\d{1,2})月");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String year = matcher.group(1);
            String month = format("%02d", Integer.parseInt(matcher.group(2)));
            return year + month;
        }
        return "";
    }

    public static void clickAndConfirm(By targetButton, int timeoutSeconds) {
        WebDriverWait wait = new WebDriverWait(DriverFactory.getDriver(), Duration.ofSeconds(timeoutSeconds));

        if (WebUI.isElementNotPresent(targetButton, 500L)) return;
        // Wait và click target button
        wait.until(ExpectedConditions.elementToBeClickable(targetButton));
        DriverFactory.getDriver().findElement(targetButton).click();

        WebUI.sleep(500);
        // Wait và click confirm button
        By confirmButton = By.xpath(XpathCommon.MODAL_CONFIRM_BTN.value); // cần chỉnh lại xpath cho đúng với 'button_confirm' thực tế
        wait.until(ExpectedConditions.elementToBeClickable(confirmButton));
        DriverFactory.getDriver().findElement(confirmButton).click();

        // Wait for loading element disappear (hoặc attribute = false)
        waitForLoadingElement();
    }

    public static String getJobTypeName(String jobType) {
        List<JobType> jobTypeList = ExcelReaderRepository.findAllJobTypes("Example");
        Map<String, String> jobTypeMapping = jobTypeList.stream()
                .collect(Collectors.toMap(JobType::getJobType, JobType::getJobName));
        return jobTypeMapping.getOrDefault(jobType, "");
    }

    public static String getWorkFormName(String workForm) {
        return switch (workForm) {
            case "WF_FULL_TIME" -> "常勤";
            case "WF_FULL_TIME_DISPATCH" -> "常勤（派遣）";
            case "WF_PART_TIME" -> "非常勤（パート）";
            case "WF_PART_TIME_JOB" -> "非常勤（アルバイト）";
            case "WF_OTHER" -> "その他";
            case "NONE" -> "未選択";
            case "WF_PART_TIME_GENERAL" -> "非常勤";
            default -> "すべて";
        };
    }

    public static String getWorkPatternName(String workPattern) {
        return switch (workPattern) {
            case "OFFICE" -> "病院の設定と同じ勤務時間";
            case "INDIVIDUAL" -> "個別に設定した勤務時間";
            case "DISCRETIONARY" -> "裁量労働制";
            case "VARIABLE" -> "変形労働制";
            default -> "すべて";
        };
    }
}
