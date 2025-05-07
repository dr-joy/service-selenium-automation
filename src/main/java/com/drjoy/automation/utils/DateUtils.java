package com.drjoy.automation.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static String convertToYYYYMM(String input) {
        Pattern pattern = Pattern.compile("(\\d{4})年(\\d{1,2})月");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String year = matcher.group(1);
            String month = String.format("%02d", Integer.parseInt(matcher.group(2)));
            return year + month;
        }
        return "";
    }

    public static String convertToYearMonthAT0029(String yearMonth) {
        String[] splitedMonthYear = splitMonthYear(yearMonth);
        String yearMonthOut = "%s, %s度";
        String monthEn = switch (splitedMonthYear[1]) {
            case "01" -> "Jan";
            case "02" -> "Feb";
            case "03" -> "Mar";
            case "04" -> "Apr";
            case "05" -> "May";
            case "06" -> "Jun";
            case "07" -> "Jul";
            case "08" -> "Aug";
            case "09" -> "Sep";
            case "10" -> "Oct";
            case "11" -> "Nov";
            case "12" -> "Dec";
            default -> "";
        };
        return String.format(yearMonthOut, monthEn, splitedMonthYear[0]);
    }
}
