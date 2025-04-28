package com.drjoy.automation.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class DateUtils {
    public static String[] splitMonthYear(String monthYear) {
        if (monthYear != null && monthYear.length() == 6) {
            return new String[] {
                monthYear.substring(0, 4),  // Year
                monthYear.substring(4, 6)   // Month
            };
        }
        return new String[] {"", ""};
    }

    public static void chooseDatePicker(WebElement openButton, String year, String month, String date) {
        openButton.click();

        WebElement selectYear = WebUI.findWebElementIfVisible(By.xpath("//ngb-datepicker//ngb-datepicker-navigation/ngb-datepicker-navigation-select/select[2]"));
        Select yearDropdown = new Select(selectYear);
        yearDropdown.selectByValue(year);

        WebElement selectMonth = WebUI.findWebElementIfVisible(By.xpath("//ngb-datepicker//ngb-datepicker-navigation/ngb-datepicker-navigation-select/select[1]"));
        Select monthDropdown = new Select(selectMonth);
        monthDropdown.selectByValue(month);

        String queryXpathChooseDate = String.format(
            "//ngb-datepicker//ngb-datepicker-month-view//div[@class='ngb-dp-day']/div[contains(@class, 'btn-light') and normalize-space(text())='%s' and not(contains(@class, 'outside'))]",
            date
        );
        WebElement btnDate = WebUI.findWebElementIfVisible(By.xpath(queryXpathChooseDate));
        btnDate.click();
    }

}
