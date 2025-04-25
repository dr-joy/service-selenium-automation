package com.drjoy.automation.utils.xpath.at;

public enum Screen {
    AT0001("//app-at0001//ul[@role='tablist']/li[1]//div/div[normalize-space(text())='打刻｜出勤簿']", 1),
    AT0007("//app-at0001//ul[@role='tablist']/li[last()]//div//i[contains(@class, 'fa-clock-o')]/ancestor::div[contains(@class, 'dropdown-item')]", 5),
    AT0008("//app-at0001//ul[@role='tablist']/li[last()]//div//i[contains(@class, 'fa fa-calendar ')]/ancestor::div[contains(@class, 'dropdown-item')]", 5),
    AT0022("//app-at0001//ul[@role='tablist']/li[4]//div/div[normalize-space(text())='承認']", 4),
    AT0029("//app-at0001//ul[@role='tablist']/li[4]//div/div[normalize-space(text())='データダウンロード']", 4),
    AT0030("at0030", 5),
    AT0031("at0031", 5),
    AT0033("at0033", 5),
    AT0045("at0045", 5),
    AT0046("at0046", 5),
    AT0055("at0055", 1),
    AT0059("at0059", 4),
    AT0068("at0068", 4);

    // Index of AT menu
    public final int indexInNavBar;
    public final String xpathToScreen;

    Screen(String val, int indexInNavBar) {
        this.xpathToScreen = val;
        this.indexInNavBar = indexInNavBar;
    }
}
