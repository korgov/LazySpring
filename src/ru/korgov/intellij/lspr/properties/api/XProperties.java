package ru.korgov.intellij.lspr.properties.api;

import ru.korgov.intellij.lspr.impl.DependencyTag;
import ru.korgov.intellij.lspr.impl.DependencyTagDescriptor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author: Kirill Korgov (kirill@korgov.ru))
 * Date: 20.02.13 2:52
 */
public interface XProperties {

    List<String> getPriorityPaths();

    List<String> getExcludeBeans();

    List<String> getCheckedExcludeBeans();

    ConflictsPolicity getConflictsPolicity();

    Set<SearchScopeEnum> getSearchScope();

    String getBeansHeader();

    String getBeansFooter();

    void setExcludeBeansStatus(boolean status);

    boolean getExcludeBeansStatus();

    void setCustomBeansMappingStatus(boolean status);

    void setOnlyVcsFilesStatus(boolean status);

    boolean getCustomBeansMappingStatus();

    void setCustomBeansMappingFromText(String xmlBeans);

    String getCustomBeansMappingAsText();

    Map<String, DependencyTag> getCustomBeansMappingAsBeans(DependencyTagDescriptor dependencyTagDescriptor);

    Map<String, DependencyTag> getCheckedCustomBeansMappingAsBeans(DependencyTagDescriptor dependencyTagDescriptor);

    void setConflictsPolicity(ConflictsPolicity policity);

    void setHeader(String header);

    void setFooter(String footer);

    void setSearchScope(Collection<SearchScopeEnum> scopes);

    boolean getOnlyVcsFilesStatus();

    String getSavePathSuffix();

    void setSavePathSuffix(String suffix);

    void setExcludeBeans(List<String> beans);

    void setPriorityPaths(List<String> priorityPaths);

    XProperties getDefaultInstance();

}
