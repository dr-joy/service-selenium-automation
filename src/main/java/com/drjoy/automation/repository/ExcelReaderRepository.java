package com.drjoy.automation.repository;

import com.drjoy.automation.config.Configuration;
import com.drjoy.automation.model.CheckingLog;
import com.drjoy.automation.model.ExportTemplateFilterSetting;
import com.drjoy.automation.model.Request;
import com.drjoy.automation.utils.ExcelUtils;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

public class ExcelReaderRepository {

    private ExcelReaderRepository() {}

    public static List<ExportTemplateFilterSetting> findAllExportFilterSetting() {
        List<String[]> data = ExcelUtils.readDataFromFilePath(String.format("%s/Setting.xlsx", Configuration.getDataBasePath()));
        List<ExportTemplateFilterSetting> result = Lists.newArrayList();

        for (String[] row : data) {
            ExportTemplateFilterSetting setting = new ExportTemplateFilterSetting();
            setting.setPhase(row[0]);
            setting.setUserName(row[1]);
            setting.setPassword(row[2]);
            setting.setSheetName(row[3]);
            setting.setTargetMonth(row[4]);
            setting.setTargetUser(row[5]);
            setting.setTargetUserDepartment(row[6]);
            setting.setTargetUserJobType(row[7]);
            setting.setTargetUserWorkForm(row[8]);
            setting.setTargetUserWorkPattern(row[9]);
            setting.setStartDate(row[10]);
            setting.setEndDate(row[11]);
            setting.setTemplateOp1(row[12]);
            setting.setTemplateOp2(row[13]);
            setting.setTemplateOp3(row[14]);

            result.add(setting);
        }

        return result;
    }

    public static List<CheckingLog> findAllCheckingLog() {
        List<String[]> data = ExcelUtils.readDataFromFilePath(String.format("%s/CheckingLog.xlsx", Configuration.getDataBasePath()));
        List<CheckingLog> result = Lists.newArrayList();

        for (String[] row : data) {
            CheckingLog setting = new CheckingLog();
            setting.setPhase(row[0]);
            setting.setDateIndex(row[1]);
            setting.setLogTime(row[2]);
            setting.setReason(row[3]);

            result.add(setting);
        }

        return result;
    }

    public static List<Request> findAllRequest(String sheet) {
        List<String[]> data = ExcelUtils.readDataFromFilePath(String.format("%s/Request.xlsx", Configuration.getDataBasePath()), sheet);
        List<Request> result = new ArrayList<>();

        for (String[] row : data) {
            Request setting = new Request();
            setting.setPhase(row[0]);
            setting.setDateIndex(row[1]);
            setting.setStartTime(row[2]);
            setting.setEndTime(row[3]);
            setting.setTargetDate(row[4]);
            setting.setReasonCategory(row[5]);
            setting.setReasonType(row[6]);
            setting.setReasonName(row[7]);
            setting.setRequestTimeUnit(row[8]);
            setting.setPeriodType(row[9]);
            setting.setHolidayWorkDate(row[10]);
            setting.setVacationTakenDate(row[11]);

            result.add(setting);
        }

        return result;
    }
}
