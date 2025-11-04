package com.icuxika;

import com.icuxika.constant.Theme;
import com.icuxika.i18n.ObservableResourceBundleFactory;
import javafx.application.ColorScheme;
import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class AppResource {

    /**
     * 默认语言文件 Base Name
     */
    private static final String LANGUAGE_RESOURCE_NAME = "LanguageResource";

    /**
     * 语言资源工厂
     */
    private static final ObservableResourceBundleFactory LANGUAGE_RESOURCE_FACTORY = new ObservableResourceBundleFactory();

    /**
     * 支持的语言集合，应与语言资源文件同步手动更新
     */
    public static final List<Locale> SUPPORT_LANGUAGE_LIST = Arrays.asList(Locale.SIMPLIFIED_CHINESE, Locale.ENGLISH);

    /**
     * 记录当前所选时区
     */
    private static final ObjectProperty<Locale> currentLocale = new SimpleObjectProperty<>();

    public static ObjectProperty<Locale> currentLocaleProperty() {
        return currentLocale;
    }

    public static void setCurrentLocale(Locale locale) {
        currentLocaleProperty().set(locale);
    }

    /**
     * 更换语言的组件使用此方法初始化自己的值，调用 {@link AppResource#setLanguage(Locale)} 来更新界面语言
     *
     * @return 当前界面语言
     */
    public static Locale getCurrentLocale() {
        return currentLocaleProperty().get();
    }

    /**
     * 更新界面语言
     *
     * @param locale 区域
     */
    public static void setLanguage(Locale locale) {
        setCurrentLocale(locale);
        LANGUAGE_RESOURCE_FACTORY.setResourceBundle(ResourceBundle.getBundle(LANGUAGE_RESOURCE_NAME, locale));
    }

    /**
     * 获取指定标识的字符串绑定
     *
     * @param key 标识
     * @return 对应该标识的字符串属性绑定
     */
    public static StringBinding getLanguageBinding(String key) {
        return LANGUAGE_RESOURCE_FACTORY.getStringBinding(key);
    }

    /**
     * 有此类所在路径决定相对路径基于/com/icuxika
     *
     * @param path 资源文件相对路径
     * @return 资源文件路径
     */
    public static URL load(String path) {
        return AppResource.class.getResource(path);
    }

    /**
     * 从资源文件中读取字符串
     *
     * @param resource 资源文件相对路径
     * @return 资源文件内容字符串
     */
    public static String readStringFromResource(String resource) {
        try (InputStream inputStream = AppResource.class.getResourceAsStream(resource)) {
            if (inputStream != null) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * 应用程序根据此属性判断当前应用主题，{@link Theme#SYSTEM} 不属于可被选择的一项
     */
    private static final ObjectProperty<Theme> theme = new SimpleObjectProperty<>();

    public static ObjectProperty<Theme> themeProperty() {
        return theme;
    }

    public static void setTheme(Theme value) {
        theme.set(value);
    }

    public static Theme getTheme() {
        return theme.get();
    }

    /**
     * 用于主题选择控件，{@link Theme#SYSTEM} 属于可被选择的一项
     */
    private static final ObjectProperty<Theme> availableTheme = new SimpleObjectProperty<>();

    public static ObjectProperty<Theme> availableThemeProperty() {
        return availableTheme;
    }

    public static void setAvailableTheme(Theme value) {
        availableTheme.set(value);
    }

    public static Theme getAvailableTheme() {
        return availableTheme.get();
    }

    static {
        availableTheme.subscribe(theme -> {
            if (theme != null) {
                if (theme == Theme.SYSTEM) {
                    if (Platform.getPreferences().getColorScheme() == ColorScheme.LIGHT) {
                        setTheme(Theme.LIGHT);
                    } else {
                        setTheme(Theme.DARK);
                    }
                } else {
                    setTheme(theme);
                }
            }
        });

        Platform.getPreferences().colorSchemeProperty().subscribe(colorScheme -> {
            if (colorScheme != null) {
                switch (colorScheme) {
                    case ColorScheme.LIGHT -> {
                        if (getAvailableTheme() == Theme.SYSTEM) {
                            setTheme(Theme.LIGHT);
                        }
                    }
                    case ColorScheme.DARK -> {
                        if (getAvailableTheme() == Theme.SYSTEM) {
                            setTheme(Theme.DARK);
                        }
                    }
                }
            }
        });
    }
}
