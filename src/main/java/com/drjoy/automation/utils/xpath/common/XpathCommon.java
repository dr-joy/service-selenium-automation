package com.drjoy.automation.utils.xpath.common;

public enum XpathCommon {
    APP_LOAD_CIRCLE("//router-outlet//p[text()='読み込み中...']/ancestor::div[contains(@class, 'loader-show')]");

    public final String value;
    XpathCommon(String s) {
        value = s;
    }
}
