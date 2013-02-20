package ru.korgov.intellij.lspr.properties;

import com.intellij.ide.util.PropertiesComponent;
import ru.korgov.intellij.lspr.properties.api.AbstractXProperties;
import ru.korgov.intellij.lspr.properties.api.ConflictsPolicity;
import ru.korgov.intellij.lspr.properties.api.Constants;
import ru.korgov.intellij.lspr.properties.api.SearchScopeEnum;
import ru.korgov.intellij.lspr.properties.api.XProperties;
import ru.korgov.util.alias.Cf;
import ru.korgov.util.alias.Cu;
import ru.korgov.util.alias.Fu;
import ru.korgov.util.alias.Su;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Author: Kirill Korgov (kirill@korgov.ru)
 * Date: 02.12.12
 */
@Deprecated
public class SimpleProperties extends AbstractXProperties {

    private static final Fu<String, String> TRIM = new Fu<String, String>() {
        @Override
        public String apply(final String v) {
            return v.trim();
        }
    };

    private final PropertiesComponent propertiesComponent;

    private SimpleProperties(final PropertiesComponent propertiesComponent) {
        this.propertiesComponent = propertiesComponent;
    }

    public static XProperties getInstance(final PropertiesComponent propertiesComponent) {
        return new SimpleProperties(propertiesComponent);
    }

    @Override
    public List<String> getPriorityPaths() {
        final String priorityPathsLines = propertiesComponent.getValue(Constants.PROP_PRIORITY_PATHS, "");
        return Cf.list(priorityPathsLines.split("\\n"));
    }

    @Override
    public List<String> getExcludeBeans() {
        final String excludeBeansLines = propertiesComponent.getValue(Constants.PROP_EXCLUDE_BEANS, Su.join(Constants.getDefaultExcludeBeans(), "\n"));
        return Cf.list(excludeBeansLines.split("\\n"));
    }

    @Override
    public ConflictsPolicity getConflictsPolicity() {
        final String policityName = propertiesComponent.getValue(Constants.PROP_CONFLICTS_POLICITY, ConflictsPolicity.AUTO_ALL.name());
        return ConflictsPolicity.valueOf(policityName);
    }

    @Override
    public Set<SearchScopeEnum> getSearchScope() {
        final Set<SearchScopeEnum> out = Cf.newSet();
        for (final SearchScopeEnum scopeEnum : SearchScopeEnum.values()) {
            final String scopeStatus = propertiesComponent.getValue(Constants.PROP_SCOPE_PREFIX + scopeEnum.name(), Constants.PROP_FALSE);
            if (Constants.PROP_TRUE.equals(scopeStatus)) {
                out.add(scopeEnum);
            }
        }
        return out.isEmpty() ? Cf.set(SearchScopeEnum.LIBRARIES, SearchScopeEnum.PRODUCTION) : out;
    }

    @Override
    public String getBeansHeader() {
        return propertiesComponent.getValue(Constants.PROP_BEANS_HEADER, Constants.DEFAULT_HEADER);
    }

    @Override
    public String getBeansFooter() {
        return propertiesComponent.getValue(Constants.PROP_BEANS_FOOTER, Constants.DEFAULT_FOOTER);
    }


    @Override
    public void setExcludeBeans(final List<String> beans) {
        propertiesComponent.setValue(Constants.PROP_EXCLUDE_BEANS, Su.join(Cu.map(beans, TRIM), "\n"));
    }

    @Override
    public void setExcludeBeansStatus(final boolean status) {
        propertiesComponent.setValue(Constants.PROP_EXCLUDE_BEANS_STATUS, status ? Constants.PROP_TRUE : Constants.PROP_FALSE);
    }

    @Override
    public boolean getExcludeBeansStatus() {
        final String status = propertiesComponent.getValue(Constants.PROP_EXCLUDE_BEANS_STATUS, Constants.PROP_TRUE);
        return Constants.PROP_TRUE.equals(status);
    }

    @Override
    public void setCustomBeansMappingStatus(final boolean status) {
        propertiesComponent.setValue(Constants.PROP_CUSTOM_BEANS_MAPPING_STATUS, status ? Constants.PROP_TRUE : Constants.PROP_FALSE);
    }

    @Override
    public void setOnlyVcsFilesStatus(final boolean status) {
        propertiesComponent.setValue(Constants.PROP_ONLY_VCF_FILES_STATUS, status ? Constants.PROP_TRUE : Constants.PROP_FALSE);
    }

    @Override
    public boolean getCustomBeansMappingStatus() {
        final String status = propertiesComponent.getValue(Constants.PROP_CUSTOM_BEANS_MAPPING_STATUS, Constants.PROP_TRUE);
        return Constants.PROP_TRUE.equals(status);
    }

    @Override
    public void setCustomBeansMappingFromText(final String xmlBeans) {
        propertiesComponent.setValue(Constants.PROP_CUSTOM_BEANS_MAPPING, xmlBeans);
    }

    @Override
    public String getCustomBeansMappingAsText() {
        return propertiesComponent.getValue(Constants.PROP_CUSTOM_BEANS_MAPPING, "");
    }

    @Override
    public void setConflictsPolicity(final ConflictsPolicity policity) {
        propertiesComponent.setValue(Constants.PROP_CONFLICTS_POLICITY, policity.name());
    }

    @Override
    public void setHeader(final String header) {
        propertiesComponent.setValue(Constants.PROP_BEANS_HEADER, header);
    }

    @Override
    public void setFooter(final String footer) {
        propertiesComponent.setValue(Constants.PROP_BEANS_FOOTER, footer);
    }

    @Override
    public void setSearchScope(final Collection<SearchScopeEnum> scopes) {
        final Set<SearchScopeEnum> scopesSet = Cf.newSet(scopes);
        for (final SearchScopeEnum someScope : SearchScopeEnum.values()) {
            final String someScopeValue = scopesSet.contains(someScope) ? Constants.PROP_TRUE : Constants.PROP_FALSE;
            propertiesComponent.setValue(Constants.PROP_SCOPE_PREFIX + someScope.name(), someScopeValue);
        }
    }

    @Override
    public boolean getOnlyVcsFilesStatus() {
        final String status = propertiesComponent.getValue(Constants.PROP_ONLY_VCF_FILES_STATUS, Constants.PROP_TRUE);
        return Constants.PROP_TRUE.equals(status);
    }

    @Override
    public void setPriorityPaths(final List<String> priorityPaths) {
        propertiesComponent.setValue(Constants.PROP_PRIORITY_PATHS, Su.join(Cu.map(priorityPaths, TRIM), "\n"));
    }

    @Override
    public XProperties getDefaultInstance() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSavePathSuffix() {
        return propertiesComponent.getValue(Constants.PROP_SAVE_PATH_SUFFIX, Constants.DEFAULT_SAVE_PATH_SUFFIX);
    }

    @Override
    public void setSavePathSuffix(final String suffix) {
        propertiesComponent.setValue(Constants.PROP_SAVE_PATH_SUFFIX, suffix);
    }

}
