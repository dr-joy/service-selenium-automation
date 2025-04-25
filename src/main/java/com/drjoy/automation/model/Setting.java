package com.drjoy.automation.model;

import lombok.Data;

@Data
public class Setting {
    private String phase;
    private String userName;
    private String password;
    private String sheetName;
    private String targetMonth;
    private String targetUser;
    private String targetUserDepartment;
    private String targetUserJobType;
    private String targetUserWorkForm;
    private String targetUserWorkPattern;
}

