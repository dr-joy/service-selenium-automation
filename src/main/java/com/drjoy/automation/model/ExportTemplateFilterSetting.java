package com.drjoy.automation.model;

import lombok.Data;

import java.util.Date;

@Data
public class ExportTemplateFilterSetting extends Setting {
    // Use for export template DAY
    private String startDate;
    private String endDate;

    private String templateOp1;
    private String templateOp2;
    private String templateOp3;
}
