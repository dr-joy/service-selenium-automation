package com.drjoy.automation.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExportTemplateFilterSetting extends Setting {
    String targetUserDepartment;
    String targetUserJobType;
    String targetUserWorkForm;
    String targetUserWorkPattern;

    // Use for export template DAY
    String startDate;
    String endDate;

    String templateOp1;
    String templateOp2;
    String templateOp3;

    // New fields based on your request
    String yearStart;
    String monthStart;
    String dateStart;
    String yearEnd;
    String monthEnd;
    String dateEnd;
    String requestType;
    String requestStatus;

    // New fields for AT0024B based on Katalon script
    String unitTime; // Corresponds to 'unitTime' variable
    String year;     // Corresponds to 'year' variable for single date
    String month;    // Corresponds to 'month' variable for single date
    String date;     // Corresponds to 'date' variable for single date
    String requestDescription; // Corresponds to 'requestDescription' variable

    // TODO NAM
    String searchText1;
    String searchText2;
    String at0051OrderUser;
    String at0051Number;
    String at0052Year;
    String at0052Number;

}
