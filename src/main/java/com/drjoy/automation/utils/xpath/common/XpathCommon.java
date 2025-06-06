package com.drjoy.automation.utils.xpath.common;

public enum XpathCommon {
    APP_ROUTER_OUTLET_LOADER_SHOW_CIRCLE("//router-outlet//p[text()='読み込み中...']/ancestor::div[contains(@class, 'loader-show')]"),
    APP_ROOT_LOADER_CIRCLE("//app-root/div[contains(@class, 'loader-wrap loader-show loader-white-page')]//div[@class='loader']"),
    APP_ROUTER_OUTLET_LOADER_CIRCLE("//router-outlet//div[@class='loader']"),

    MODAL_CONFIRM_BTN("//app-modal//button[@id='positiveButton']"),
    MODAL_CONFIRM_WITH_JP_TEXT_BTN("//button[@id='positiveButton' and normalize-space(text())='はい']"),

    PAGE_BACK_BTN("//a[@class='page-head-backlink']"),

    ERROR_PAGE("//img[@src='/assets/img/error.png']"),
    NOT_FOUND_PAGE("//img[@src='/assets/img/not-found.png']");

    public final String value;
    XpathCommon(String s) {
        value = s;
    }
}
