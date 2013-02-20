package ru.korgov.intellij.lspr.properties;

import ru.korgov.intellij.lspr.properties.api.AbstractXProperties;
import ru.korgov.intellij.lspr.properties.api.ConflictsPolicity;
import ru.korgov.intellij.lspr.properties.api.Constants;
import ru.korgov.intellij.lspr.properties.api.SearchScopeEnum;
import ru.korgov.intellij.lspr.properties.api.XProperties;
import ru.korgov.util.alias.Cf;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Author: Kirill Korgov (kirill@korgov.ru))
 * Date: 20.02.13 3:48
 */
public class PersistentStateProperties extends AbstractXProperties {

    private List<String> excludeBeans = Constants.getDefaultExcludeBeans();
    private boolean excludeBeansStatus = true;
    private ConflictsPolicity conflictsPolicity = ConflictsPolicity.AUTO_ALL;
    private Set<SearchScopeEnum> searchScope = Cf.set(SearchScopeEnum.PRODUCTION, SearchScopeEnum.LIBRARIES);
    private String beansHeader = Constants.DEFAULT_HEADER;
    private String beansFooter = Constants.DEFAULT_FOOTER;
    private String customBeansMapping = "";
    private boolean customBeansMappingStatus = true;
    private boolean onlyVcsFiles = false;
    private String savePathSuffix = Constants.DEFAULT_SAVE_PATH_SUFFIX;
    private List<String> priorityPaths = Cf.newList();


    public boolean isExcludeBeansStatus() {
        return excludeBeansStatus;
    }

    public String getCustomBeansMapping() {
        return customBeansMapping;
    }

    public boolean isCustomBeansMappingStatus() {
        return customBeansMappingStatus;
    }

    public boolean isOnlyVcsFiles() {
        return onlyVcsFiles;
    }

    public void setSearchScope(final Set<SearchScopeEnum> searchScope) {
        this.searchScope = searchScope;
    }

    public void setBeansHeader(final String beansHeader) {
        this.beansHeader = beansHeader;
    }

    public void setBeansFooter(final String beansFooter) {
        this.beansFooter = beansFooter;
    }

    public void setCustomBeansMapping(final String customBeansMapping) {
        this.customBeansMapping = customBeansMapping;
    }

    public void setOnlyVcsFiles(final boolean onlyVcsFiles) {
        this.onlyVcsFiles = onlyVcsFiles;
    }

    @Override
    public List<String> getPriorityPaths() {
        return Collections.unmodifiableList(priorityPaths);
    }

    @Override
    public List<String> getExcludeBeans() {
        return Collections.unmodifiableList(excludeBeans);
    }

    @Override
    public ConflictsPolicity getConflictsPolicity() {
        return conflictsPolicity;
    }

    @Override
    public Set<SearchScopeEnum> getSearchScope() {
        return Collections.unmodifiableSet(searchScope);
    }

    @Override
    public String getBeansHeader() {
        return beansHeader;
    }

    @Override
    public String getBeansFooter() {
        return beansFooter;
    }

    @Override
    public void setExcludeBeans(final List<String> beans) {
        excludeBeans = Cf.newList(beans);
    }

    @Override
    public void setExcludeBeansStatus(final boolean status) {
        excludeBeansStatus = status;
    }

    @Override
    public boolean getExcludeBeansStatus() {
        return excludeBeansStatus;
    }

    @Override
    public void setCustomBeansMappingStatus(final boolean status) {
        customBeansMappingStatus = status;
    }

    @Override
    public void setOnlyVcsFilesStatus(final boolean status) {
        onlyVcsFiles = status;
    }

    @Override
    public boolean getCustomBeansMappingStatus() {
        return customBeansMappingStatus;
    }

    @Override
    public void setCustomBeansMappingFromText(final String xmlBeans) {
        customBeansMapping = xmlBeans;
    }

    @Override
    public String getCustomBeansMappingAsText() {
        return customBeansMapping;
    }

    @Override
    public void setConflictsPolicity(final ConflictsPolicity policity) {
        conflictsPolicity = policity;
    }

    @Override
    public void setHeader(final String header) {
        beansHeader = header;
    }

    @Override
    public void setFooter(final String footer) {
        beansFooter = footer;
    }

    @Override
    public void setSearchScope(final Collection<SearchScopeEnum> scopes) {
        searchScope = Cf.newSet(scopes);
    }

    @Override
    public boolean getOnlyVcsFilesStatus() {
        return onlyVcsFiles;
    }

    @Override
    public void setPriorityPaths(final List<String> priorityPaths) {
        this.priorityPaths = Cf.newList(priorityPaths);
    }

    @Override
    public XProperties getDefaultInstance() {
        return new PersistentStateProperties();
    }

    @Override
    public String getSavePathSuffix() {
        return savePathSuffix;
    }

    @Override
    public void setSavePathSuffix(final String suffix) {
        savePathSuffix = suffix;
    }

    @SuppressWarnings({"NonFinalFieldReferenceInEquals", "ControlFlowStatementWithoutBraces", "OverlyComplexMethod", "RedundantIfStatement"})
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final PersistentStateProperties that = (PersistentStateProperties) o;

        if (customBeansMappingStatus != that.customBeansMappingStatus) return false;
        if (excludeBeansStatus != that.excludeBeansStatus) return false;
        if (onlyVcsFiles != that.onlyVcsFiles) return false;
        if (beansFooter != null ? !beansFooter.equals(that.beansFooter) : that.beansFooter != null) return false;
        if (beansHeader != null ? !beansHeader.equals(that.beansHeader) : that.beansHeader != null) return false;
        if (conflictsPolicity != that.conflictsPolicity) return false;
        if (customBeansMapping != null ? !customBeansMapping.equals(that.customBeansMapping) : that.customBeansMapping != null)
            return false;
        if (excludeBeans != null ? !excludeBeans.equals(that.excludeBeans) : that.excludeBeans != null) return false;
        if (priorityPaths != null ? !priorityPaths.equals(that.priorityPaths) : that.priorityPaths != null)
            return false;
        if (savePathSuffix != null ? !savePathSuffix.equals(that.savePathSuffix) : that.savePathSuffix != null)
            return false;
        if (searchScope != null ? !searchScope.equals(that.searchScope) : that.searchScope != null) return false;

        return true;
    }

    @SuppressWarnings({"OverlyComplexMethod", "NonFinalFieldReferencedInHashCode"})
    @Override
    public int hashCode() {
        int result = excludeBeans != null ? excludeBeans.hashCode() : 0;
        result = 31 * result + (excludeBeansStatus ? 1 : 0);
        result = 31 * result + (conflictsPolicity != null ? conflictsPolicity.hashCode() : 0);
        result = 31 * result + (searchScope != null ? searchScope.hashCode() : 0);
        result = 31 * result + (beansHeader != null ? beansHeader.hashCode() : 0);
        result = 31 * result + (beansFooter != null ? beansFooter.hashCode() : 0);
        result = 31 * result + (customBeansMapping != null ? customBeansMapping.hashCode() : 0);
        result = 31 * result + (customBeansMappingStatus ? 1 : 0);
        result = 31 * result + (onlyVcsFiles ? 1 : 0);
        result = 31 * result + (savePathSuffix != null ? savePathSuffix.hashCode() : 0);
        result = 31 * result + (priorityPaths != null ? priorityPaths.hashCode() : 0);
        return result;
    }
}
