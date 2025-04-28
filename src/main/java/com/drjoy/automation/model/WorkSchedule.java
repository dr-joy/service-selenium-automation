package com.drjoy.automation.model;
import lombok.Data;

@Data
public class WorkSchedule {
    private String phase;
    private String dayIndex;
    private String presetName;
    private String dayType;
    private String workingTimeType;
    private String startTime;
    private String endTime;
    private String startBreakTime;
    private String endBreakTime;
}
