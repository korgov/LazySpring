package ru.korgov.intellij.ltsc.properties;

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

    public static final String DEFAULT_EXCLUDE_BEANS = "jdbc\n" +
            "jdbcTemplate\n" +
            "dataSource\n" +
            "transactionTemplate\n" +
            "";

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
}
