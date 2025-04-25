package com.drjoy.automation.repository;

import com.drjoy.automation.config.Configuration;
import com.drjoy.automation.model.ExportTemplateFilterSetting;
import com.drjoy.automation.utils.ExcelUtils;
import com.google.common.collect.Lists;

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

}
