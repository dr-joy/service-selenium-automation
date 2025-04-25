package com.drjoy.automation.model;

import lombok.Data;

@Data
public class Request {
    private String phase;
    private String dateIndex;
    private String startTime;
    private String endTime;
    private String targetDate;
    private String reasonCategory;
    private String reasonType;
    private String reasonName;
    private String requestTimeUnit;
    private String periodType;
    private String holidayWorkDate;
    private String vacationTakenDate;
}
