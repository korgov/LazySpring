package ru.korgov.intellij.lspr.properties;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.xml.XmlTag;
import ru.korgov.intellij.lspr.model.DependencyTag;
import ru.korgov.intellij.lspr.model.DependencyTagDescriptor;
import ru.korgov.util.alias.Cf;
import ru.korgov.util.alias.Cu;
import ru.korgov.util.alias.Fu;
import ru.korgov.util.alias.Su;
import ru.korgov.util.func.Function;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author: Kirill Korgov (kirill@korgov.ru)
 * Date: 02.12.12
 */
public class PropertiesService {

    private static final Fu<String, String> TRIM = new Fu<String, String>() {
        @Override
        public String apply(final String v) {
            return v.trim();
        }
    };
    private static final Fu<XmlTag, String> TAG_TO_ID_OR_NAME = new Fu<XmlTag, String>() {
        @Override
        public String apply(final XmlTag tag) {
            final String beanId = tag.getAttributeValue("id");
            if (!Su.isEmpty(beanId)) {
                return beanId;
            }
            return tag.getAttributeValue("name");
        }
    };
    private final PropertiesComponent propertiesComponent;
    private final XmlElementFactory xmlElementFactory;


    private PropertiesService(final PropertiesComponent propertiesComponent, final Project project) {
        this.propertiesComponent = propertiesComponent;
        this.xmlElementFactory = XmlElementFactory.getInstance(project);
    }

    public static PropertiesService getInstance(final PropertiesComponent propertiesComponent, final Project project) {
        return new PropertiesService(propertiesComponent, project);
    }

    public Set<String> getPriorityPaths() {
        final String priorityPathsLines = propertiesComponent.getValue(Constants.PROP_PRIORITY_PATHS, "");
        return Cf.linkedSet(priorityPathsLines.split("\\n"));
    }

    public List<String> getExcludeBeans() {
        final String excludeBeansLines = propertiesComponent.getValue(Constants.PROP_EXCLUDE_BEANS, Constants.DEFAULT_EXCLUDE_BEANS);
        return Cf.list(excludeBeansLines.split("\\n"));
    }

    public List<String> getCheckedExcludeBeans() {
        if (getExcludeBeansStatus()) {
            return getExcludeBeans();
        }
        return Collections.emptyList();
    }

    public ConflictsPolicity getConflictsPolicity() {
        final String policityName = propertiesComponent.getValue(Constants.PROP_CONFLICTS_POLICITY, ConflictsPolicity.AUTO_ALL.name());
        return ConflictsPolicity.valueOf(policityName);
    }

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

    public String getBeansHeader() {
        return propertiesComponent.getValue(Constants.PROP_BEANS_HEADER, Constants.DEFAULT_HEADER);
    }

    public String getBeansFooter() {
        return propertiesComponent.getValue(Constants.PROP_BEANS_FOOTER, Constants.DEFAULT_FOOTER);
    }


    public void setExcludeBeans(final List<String> beans) {
        propertiesComponent.setValue(Constants.PROP_EXCLUDE_BEANS, Su.join(Cu.map(beans, TRIM), "\n"));
    }

    public void setExcludeBeansStatus(final boolean status) {
        propertiesComponent.setValue(Constants.PROP_EXCLUDE_BEANS_STATUS, status ? Constants.PROP_TRUE : Constants.PROP_FALSE);
    }

    public boolean getExcludeBeansStatus() {
        final String status = propertiesComponent.getValue(Constants.PROP_EXCLUDE_BEANS_STATUS, Constants.PROP_TRUE);
        return Constants.PROP_TRUE.equals(status);
    }

    public void setCustomBeansMappingStatus(final boolean status) {
        propertiesComponent.setValue(Constants.PROP_CUSTOM_BEANS_MAPPING_STATUS, status ? Constants.PROP_TRUE : Constants.PROP_FALSE);
    }

    public void setOnlyVcsFilesStatus(final boolean status) {
        propertiesComponent.setValue(Constants.PROP_ONLY_VCF_FILES_STATUS, status ? Constants.PROP_TRUE : Constants.PROP_FALSE);
    }

    public boolean getCustomBeansMappingStatus() {
        final String status = propertiesComponent.getValue(Constants.PROP_CUSTOM_BEANS_MAPPING_STATUS, Constants.PROP_TRUE);
        return Constants.PROP_TRUE.equals(status);
    }

    public void setCustomBeansMappingFromText(final String xmlBeans) {
        propertiesComponent.setValue(Constants.PROP_CUSTOM_BEANS_MAPPING, xmlBeans);
    }

    public String geCustomBeansMappingAsText() {
        return propertiesComponent.getValue(Constants.PROP_CUSTOM_BEANS_MAPPING, "");
    }

    public Map<String, DependencyTag> getCustomBeansMappingAsBeans(final DependencyTagDescriptor dependencyTagDescriptor) {
        return convertMappingToBeans(geCustomBeansMappingAsText(), dependencyTagDescriptor);
    }

    public Map<String, DependencyTag> getCheckedCustomBeansMappingAsBeans(final DependencyTagDescriptor dependencyTagDescriptor) {
        if (getCustomBeansMappingStatus()) {
            return getCustomBeansMappingAsBeans(dependencyTagDescriptor);
        }
        return Collections.emptyMap();
    }

    private Map<String, DependencyTag> convertMappingToBeans(final String xmlBeans, final DependencyTagDescriptor dependencyTagDescriptor) {
        final String trimmed = xmlBeans.trim();
        if (!Su.isEmpty(trimmed)) {
            try {
                final XmlTag rootTag = createTagFromText(trimmed);
                return Cu.mapFromIterable(TAG_TO_ID_OR_NAME, toDependencyTagFu(dependencyTagDescriptor), Cf.list(rootTag.findSubTags("bean")));
            } catch (final Exception ignored) {
            }
        }
        return Collections.emptyMap();
    }

    private Function<XmlTag, DependencyTag> toDependencyTagFu(final DependencyTagDescriptor dependencyTagDescriptor) {
        return new Fu<XmlTag, DependencyTag>() {
            @Override
            public DependencyTag apply(final XmlTag tag) {
                return dependencyTagDescriptor.newBeanTag(tag);
            }
        };
    }

    private XmlTag createTagFromText(final String trimmed) {
        return ApplicationManager.getApplication().runReadAction(new Computable<XmlTag>() {
            @Override
            public XmlTag compute() {
                return xmlElementFactory.createTagFromText("<beans>" + trimmed + "</beans>");
            }
        });
    }

    public void setConflictsPolicity(final ConflictsPolicity policity) {
        propertiesComponent.setValue(Constants.PROP_CONFLICTS_POLICITY, policity.name());
    }

    public void setHeader(final String header) {
        propertiesComponent.setValue(Constants.PROP_BEANS_HEADER, header);
    }

    public void setFooter(final String footer) {
        propertiesComponent.setValue(Constants.PROP_BEANS_FOOTER, footer);
    }

    public void setSearchScope(final Collection<SearchScopeEnum> scopes) {
        final Set<SearchScopeEnum> scopesSet = Cf.newSet(scopes);
        for (final SearchScopeEnum someScope : SearchScopeEnum.values()) {
            final String someScopeValue = scopesSet.contains(someScope) ? Constants.PROP_TRUE : Constants.PROP_FALSE;
            propertiesComponent.setValue(Constants.PROP_SCOPE_PREFIX + someScope.name(), someScopeValue);
        }
    }

    public boolean getOnlyVcsFilesStatus() {
        final String status = propertiesComponent.getValue(Constants.PROP_ONLY_VCF_FILES_STATUS, Constants.PROP_TRUE);
        return Constants.PROP_TRUE.equals(status);
    }

    public void setPriorityPaths(final Collection<String> priorityPaths) {
        propertiesComponent.setValue(Constants.PROP_PRIORITY_PATHS, Su.join(Cu.map(priorityPaths, TRIM), "\n"));
    }
}
