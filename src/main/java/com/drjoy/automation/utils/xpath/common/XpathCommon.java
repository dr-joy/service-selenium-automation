package com.drjoy.automation.utils.xpath.common;

public enum XpathCommon {
    APP_LOAD_CIRCLE("//router-outlet//p[text()='読み込み中...']/ancestor::div[contains(@class, 'loader-show')]"),

    MODAL_CONFIRM_BTN("//app-modal//button[@id='positiveButton']"),
    MODAL_CONFIRM_WITH_JP_TEXT_BTN("//button[@id='positiveButton' and normalize-space(text())='はい']"),

    PAGE_BACK_BTN("//a[@class='page-head-backlink']");

    public final String value;
    XpathCommon(String s) {
        value = s;
    }
}
