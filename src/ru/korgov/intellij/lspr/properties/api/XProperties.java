package ru.korgov.intellij.lspr.properties.api;

import ru.korgov.intellij.lspr.impl.DependencyTag;
import ru.korgov.intellij.lspr.impl.DependencyTagDescriptor;

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

    void setExcludeBeansUsed(boolean status);

    boolean isExcludeBeansUsed();

    void setCustomBeansMappingUsed(boolean status);

    void setOnlyVcsFiles(boolean status);

    boolean isCustomBeansMappingUsed();

    void setCustomBeansMapping(String xmlBeans);

    String getCustomBeansMapping();

    Map<String, DependencyTag> getCustomBeansMappingAsBeans(DependencyTagDescriptor dependencyTagDescriptor);

    Map<String, DependencyTag> getCheckedCustomBeansMappingAsBeans(DependencyTagDescriptor dependencyTagDescriptor);

    void setConflictsPolicity(ConflictsPolicity policity);

    void setBeansHeader(String header);

    void setBeansFooter(String footer);

    void setSearchScope(Set<SearchScopeEnum> scopes);

    boolean isOnlyVcsFiles();

    boolean isOnlyModuleFilesScope();

    void setOnlyModuleFilesScope(boolean onlyModuleFilesScope);

    String getSavePathSuffix();

    void setSavePathSuffix(String suffix);

    void setExcludeBeans(List<String> beans);

    void setPriorityPaths(List<String> priorityPaths);

    XProperties getDefaultInstance();

}
