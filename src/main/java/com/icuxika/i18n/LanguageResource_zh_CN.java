package com.icuxika.i18n;

import java.util.ListResourceBundle;

/**
 * 中文环境下，若设置了 Locale.SIMPLIFIED_CHINESE，则判断此文件是否存在，
 * 不存在的话，默认文件 LanguageResource 的配置生效，
 * 存在的话，则当前文件生效
 */
public class LanguageResource_zh_CN extends ListResourceBundle {
    @Override
    protected Object[][] getContents() {
        return new Object[][]{
                {"title", "标题"},
        };
    }
}
