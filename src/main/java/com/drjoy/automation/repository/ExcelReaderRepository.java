package com.drjoy.automation.repository;

import com.drjoy.automation.config.Configuration;
import com.drjoy.automation.model.CheckingLog;
import com.drjoy.automation.model.DownloadTemplate;
import com.drjoy.automation.model.ExportTemplateFilterSetting;
import com.drjoy.automation.model.JobType;
import com.drjoy.automation.model.Request;
import com.drjoy.automation.model.WorkSchedule;
import com.drjoy.automation.utils.ExcelUtils;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExcelReaderRepository {

    private ExcelReaderRepository() {}

    private static String getAbsolutePath(String fileName) {
        String resourcePath = String.format("%s/%s", Configuration.getDataBasePath(), fileName);
        File tmpFile = new File(resourcePath);
        return tmpFile.getAbsolutePath();
    }

    public static List<ExportTemplateFilterSetting> findAllExportFilterSetting() {
        List<ExportTemplateFilterSetting> result = Lists.newArrayList();

        List<String[]> data = ExcelUtils.readDataFromFilePath(getAbsolutePath("Setting.xlsx"));
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
//            setting.setSearchText1(row[15]);
//            setting.setSearchText2(row[16]);
//            setting.setAt0051OrderUser(row[17]);
//            setting.setAt0051Number(row[18]);
//            setting.setAt0052Year(row[19]);
//            setting.setAt0052Number(row[20]);

            result.add(setting);
        }

        return result;
    }

    public static List<CheckingLog> findAllCheckingLog() {
        List<CheckingLog> result = Lists.newArrayList();

        List<String[]> data = ExcelUtils.readDataFromFilePath(getAbsolutePath("CheckingLog.xlsx"));

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
        List<String[]> data = ExcelUtils.readDataFromFilePath(getAbsolutePath(("Request.xlsx")), sheet);
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

    public static List<WorkSchedule> findAllWorkSchedule(String sheet) {
        List<String[]> data = ExcelUtils.readDataFromFilePath(getAbsolutePath(("WorkSchedule.xlsx")), sheet);
        List<WorkSchedule> result = new ArrayList<>();

        for (String[] row : data) {
            WorkSchedule setting = new WorkSchedule();
            setting.setPhase(row[0]);
            setting.setDayIndex(row[1]);
            setting.setPresetName(row[2]);
            setting.setDayType(row[3]);
            setting.setWorkingTimeType(row[4]);
            setting.setStartTime(row[5]);
            setting.setEndTime(row[6]);
            setting.setStartBreakTime(row[7]);
            setting.setEndBreakTime(row[8]);

            result.add(setting);
        }

        return result;
    }

    public static List<DownloadTemplate> findAllDownloadTemplate(String sheet) {
        List<String[]> data = ExcelUtils.readDataFromFilePath(String.format("%s/DownloadTemplate.xlsx", Configuration.getDataBasePath()), sheet);
        List<DownloadTemplate> result = new ArrayList<>();

        for (String[] row : data) {
            DownloadTemplate setting = new DownloadTemplate();
            setting.setPhase(row[0]);
            setting.setMode(row[1]);
            setting.setOption(row[2]);
            setting.setTitle(row[3]);

            result.add(setting);
        }

        return result;
    }

    public static List<JobType> findAllJobTypes(String sheet) {
        List<String[]> data = ExcelUtils.readDataFromFilePath(String.format("%s/JobTypes.xlsx",
                Configuration.getDataBasePath()), sheet);
        List<JobType> result = new ArrayList<>();

        for (String[] row : data) {
            JobType setting = new JobType();
            setting.setJobType(row[0]);
            setting.setJobName(row[1]);

            result.add(setting);
        }

        return result;
    }
}
