package ru.korgov.intellij.lspr.properties.api;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.xml.XmlTag;
import ru.korgov.intellij.lspr.impl.DependencyTag;
import ru.korgov.intellij.lspr.impl.DependencyTagDescriptor;
import ru.korgov.util.alias.Cf;
import ru.korgov.util.alias.Cu;
import ru.korgov.util.alias.Fu;
import ru.korgov.util.alias.Su;
import ru.korgov.util.func.Function;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Author: Kirill Korgov (kirill@korgov.ru))
 * Date: 20.02.13 4:08
 */
public abstract class AbstractXProperties implements XProperties {

    @Override
    public List<String> getCheckedExcludeBeans() {
        if (isExcludeBeansUsed()) {
            return getExcludeBeans();
        }
        return Collections.emptyList();
    }

    @Override
    public Map<String, DependencyTag> getCustomBeansMappingAsBeans(final DependencyTagDescriptor dependencyTagDescriptor) {
        return convertMappingToBeans(getCustomBeansMapping(), dependencyTagDescriptor);
    }

    @Override
    public Map<String, DependencyTag> getCheckedCustomBeansMappingAsBeans(final DependencyTagDescriptor dependencyTagDescriptor) {
        if (isCustomBeansMappingUsed()) {
            return getCustomBeansMappingAsBeans(dependencyTagDescriptor);
        }
        return Collections.emptyMap();
    }

    private Map<String, DependencyTag> convertMappingToBeans(final String xmlBeans, final DependencyTagDescriptor dependencyTagDescriptor) {
        final String trimmed = xmlBeans.trim();
        if (!Su.isEmpty(trimmed)) {
            try {
                final XmlTag rootTag = createTagFromText(trimmed, dependencyTagDescriptor.getXmlElementFactory());
                return Cu.mapFromIterable(tagToIdFu(dependencyTagDescriptor), toDependencyTagFu(dependencyTagDescriptor), Cf.list(rootTag.findSubTags("bean")));
            } catch (final Exception ignored) {
            }
        }
        return Collections.emptyMap();
    }

    private Function<XmlTag, String> tagToIdFu(final DependencyTagDescriptor dependencyTagDescriptor) {
        return new Fu<XmlTag, String>() {
            @Override
            public String apply(final XmlTag xmlTag) {
                return dependencyTagDescriptor.getSomeIdentifier(xmlTag).getValue(null);
            }
        };
    }

    private Function<XmlTag, DependencyTag> toDependencyTagFu(final DependencyTagDescriptor dependencyTagDescriptor) {
        return new Fu<XmlTag, DependencyTag>() {
            @Override
            public DependencyTag apply(final XmlTag tag) {
                return dependencyTagDescriptor.newBeanTag(tag);
            }
        };
    }

    private XmlTag createTagFromText(final String trimmed, final XmlElementFactory xmlElementFactory) {
        return ApplicationManager.getApplication().runReadAction(new Computable<XmlTag>() {
            @Override
            public XmlTag compute() {
                return xmlElementFactory.createTagFromText("<beans>" + trimmed + "</beans>");
            }
        });
    }

}
