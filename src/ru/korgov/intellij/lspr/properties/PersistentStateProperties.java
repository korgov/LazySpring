package ru.korgov.intellij.lspr.properties;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;
import ru.korgov.intellij.lspr.properties.api.AbstractXProperties;
import ru.korgov.intellij.lspr.properties.api.ConflictsPolicity;
import ru.korgov.intellij.lspr.properties.api.Constants;
import ru.korgov.intellij.lspr.properties.api.SearchScopeEnum;
import ru.korgov.intellij.lspr.properties.api.XProperties;
import ru.korgov.util.alias.Cf;

import java.util.List;
import java.util.Set;

/**
 * Author: Kirill Korgov (kirill@korgov.ru))
 * Date: 20.02.13 3:48
 */
@State(
        name = PersistentStateProperties.NAME,
        storages = {
                @Storage(id = "default", file = "$PROJECT_FILE$"),
                @Storage(id = "dir", file = "$PROJECT_CONFIG_DIR$" + "/" + PersistentStateProperties.NAME + ".xml", scheme = StorageScheme.DIRECTORY_BASED)
        }
)
public class PersistentStateProperties extends AbstractXProperties implements PersistentStateComponent<PersistentStateProperties> {
    public static final String NAME = "LazySpringConfiguration";

    public static PersistentStateProperties getInstance(final Project project) {
        return ServiceManager.getService(project, PersistentStateProperties.class);
    }

    private List<String> excludeBeans = Constants.getDefaultExcludeBeans();
    private boolean excludeBeansUsed = true;
    private ConflictsPolicity conflictsPolicity = ConflictsPolicity.AUTO_ALL;
    private Set<SearchScopeEnum> searchScope = Cf.set(SearchScopeEnum.PRODUCTION, SearchScopeEnum.LIBRARIES);
    private String beansHeader = Constants.DEFAULT_HEADER;
    private String beansFooter = Constants.DEFAULT_FOOTER;
    private String customBeansMapping = "";
    private boolean customBeansMappingUsed = true;
    private boolean onlyVcsFiles = false;
    private String savePathSuffix = Constants.DEFAULT_SAVE_PATH_SUFFIX;
    private List<String> priorityPaths = Cf.newList();

    @Override
    public List<String> getPriorityPaths() {
        return Cf.newList(priorityPaths);
    }

    @Override
    public List<String> getExcludeBeans() {
        return Cf.newList(excludeBeans);
    }

    @Override
    public ConflictsPolicity getConflictsPolicity() {
        return conflictsPolicity;
    }

    @Override
    public Set<SearchScopeEnum> getSearchScope() {
        return Cf.newSet(searchScope);
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
    public void setExcludeBeansUsed(final boolean status) {
        excludeBeansUsed = status;
    }

    @Override
    public boolean isExcludeBeansUsed() {
        return excludeBeansUsed;
    }

    @Override
    public void setCustomBeansMappingUsed(final boolean status) {
        customBeansMappingUsed = status;
    }

    @Override
    public void setOnlyVcsFiles(final boolean status) {
        onlyVcsFiles = status;
    }

    @Override
    public boolean isCustomBeansMappingUsed() {
        return customBeansMappingUsed;
    }

    @Override
    public void setCustomBeansMapping(final String xmlBeans) {
        customBeansMapping = xmlBeans;
    }

    @Override
    public String getCustomBeansMapping() {
        return customBeansMapping;
    }

    @Override
    public void setConflictsPolicity(final ConflictsPolicity policity) {
        conflictsPolicity = policity;
    }

    @Override
    public void setBeansHeader(final String header) {
        beansHeader = header;
    }

    @Override
    public void setBeansFooter(final String footer) {
        beansFooter = footer;
    }

    @Override
    public void setSearchScope(final Set<SearchScopeEnum> scopes) {
        searchScope = Cf.newSet(scopes);
    }

    @Override
    public boolean isOnlyVcsFiles() {
        return onlyVcsFiles;
    }

    @Override
    public void setPriorityPaths(final List<String> priorityPaths) {
        this.priorityPaths = Cf.newList(priorityPaths);
    }

    @Override
    public String getSavePathSuffix() {
        return savePathSuffix;
    }

    @Override
    public void setSavePathSuffix(final String suffix) {
        savePathSuffix = suffix;
    }

    @Transient
    @Override
    public XProperties getDefaultInstance() {
        return new PersistentStateProperties();
    }

    @Transient
    @SuppressWarnings({"NonFinalFieldReferenceInEquals", "ControlFlowStatementWithoutBraces", "OverlyComplexMethod", "RedundantIfStatement"})
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final PersistentStateProperties that = (PersistentStateProperties) o;

        if (customBeansMappingUsed != that.customBeansMappingUsed) return false;
        if (excludeBeansUsed != that.excludeBeansUsed) return false;
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

    @Transient
    @SuppressWarnings({"OverlyComplexMethod", "NonFinalFieldReferencedInHashCode"})
    @Override
    public int hashCode() {
        int result = excludeBeans != null ? excludeBeans.hashCode() : 0;
        result = 31 * result + (excludeBeansUsed ? 1 : 0);
        result = 31 * result + (conflictsPolicity != null ? conflictsPolicity.hashCode() : 0);
        result = 31 * result + (searchScope != null ? searchScope.hashCode() : 0);
        result = 31 * result + (beansHeader != null ? beansHeader.hashCode() : 0);
        result = 31 * result + (beansFooter != null ? beansFooter.hashCode() : 0);
        result = 31 * result + (customBeansMapping != null ? customBeansMapping.hashCode() : 0);
        result = 31 * result + (customBeansMappingUsed ? 1 : 0);
        result = 31 * result + (onlyVcsFiles ? 1 : 0);
        result = 31 * result + (savePathSuffix != null ? savePathSuffix.hashCode() : 0);
        result = 31 * result + (priorityPaths != null ? priorityPaths.hashCode() : 0);
        return result;
    }

    @Override
    public PersistentStateProperties getState() {
        return this;
    }

    @Override
    public void loadState(final PersistentStateProperties state) {
        XmlSerializerUtil.copyBean(state, this);
    }

}
