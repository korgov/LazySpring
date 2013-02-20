package ru.korgov.intellij.lspr.properties.api;

import ru.korgov.util.alias.Cf;

import java.util.Collections;
import java.util.List;

/**
 * Author: Kirill Korgov (kirill@korgov.ru)
 * Date: 02.12.12
 */
public class Constants {
    public static final String DEFAULT_HEADER =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" +
                    "       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "       xmlns:context=\"http://www.springframework.org/schema/context\"\n" +
                    "       xmlns:util=\"http://www.springframework.org/schema/util\"\n" +
                    "       xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.1.xsd http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd\"\n" +
                    "       default-autowire=\"byName\" default-lazy-init=\"true\">\n" +
                    "\n" +
                    "    <context:annotation-config/>";

    public static final String DEFAULT_FOOTER = "</beans>";

    private static final List<String> DEFAULT_EXCLUDE_BEANS = Cf.list(
            "jdbc",
            "jdbcTemplate",
            "dataSource",
            "transactionTemplate"
    );

    public static final String DEFAULT_SAVE_PATH_SUFFIX = "src/test";


    public static final String PROP_EXCLUDE_BEANS = "exclude-beans";
    public static final String PROP_EXCLUDE_BEANS_STATUS = "exclude-beans-status";
    public static final String PROP_CONFLICTS_POLICITY = "conflicts-policity";
    public static final String PROP_SCOPE_PREFIX = "scope-";
    public static final String PROP_BEANS_HEADER = "beans-header";
    public static final String PROP_BEANS_FOOTER = "beans-footer";
    public static final String PROP_FALSE = "false";
    public static final String PROP_TRUE = "true";
    public static final String PROP_CUSTOM_BEANS_MAPPING = "custom-beans-mapping";
    public static final String PROP_CUSTOM_BEANS_MAPPING_STATUS = "custom-beans-mapping-status";
    public static final String PROP_ONLY_VCF_FILES_STATUS = "only-vcs-files-status";
    public static final String PROP_PRIORITY_PATHS = "priority-paths";
    public static final String PROP_SAVE_PATH_SUFFIX = "save-path-suffix";

    private Constants() {
    }

    public static List<String> getDefaultExcludeBeans() {
        return Collections.unmodifiableList(DEFAULT_EXCLUDE_BEANS);
    }
}
