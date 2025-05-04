package com.drjoy.automation.model;

import lombok.Data;

@Data
public class ExportTemplateFilterSetting extends Setting {
    private String targetUserDepartment;
    private String targetUserJobType;
    private String targetUserWorkForm;
    private String targetUserWorkPattern;

    // Use for export template DAY
    private String startDate;
    private String endDate;

    private String templateOp1;
    private String templateOp2;
    private String templateOp3;
}
