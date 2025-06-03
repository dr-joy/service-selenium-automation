package com.drjoy.automation.model.setting;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TeireiSetting extends Setting{
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

    String searchText1;
    String searchText2;
    String at0051OrderUser;
    String at0051Number;
    String at0052Year;
    String at0052Number;
}
