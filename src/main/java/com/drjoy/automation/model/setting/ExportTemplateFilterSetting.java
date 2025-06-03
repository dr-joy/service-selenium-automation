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

}
