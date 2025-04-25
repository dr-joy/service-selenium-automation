package com.drjoy.automation;

import com.drjoy.automation.config.DriverFactory;
import com.drjoy.automation.model.ExportTemplateFilterSetting;
import com.drjoy.automation.repository.ExcelReaderRepository;


import java.util.List;

public class Main {
    public static void main(String[] args) {

        try {

            List<ExportTemplateFilterSetting> settings = ExcelReaderRepository.findAllExportFilterSetting();

            settings.forEach(s -> {
                System.out.println(s.toString());
            });
        }finally {
            DriverFactory.stopDriver();
        }
    }
}