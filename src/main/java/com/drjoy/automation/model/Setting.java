package com.drjoy.automation.model;

import com.drjoy.automation.execution.phase.PhaseSetting;
import lombok.Data;

@Data
public class Setting implements PhaseSetting {
    private String phase;
    private String userName;
    private String password;
    private String sheetName;
    private String targetMonth;
    private String targetUser;
}
