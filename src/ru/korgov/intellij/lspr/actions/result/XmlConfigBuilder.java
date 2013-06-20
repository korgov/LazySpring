package ru.korgov.intellij.lspr.actions.result;

import com.intellij.psi.xml.XmlTag;
import ru.korgov.intellij.lspr.impl.DependencyTag;
import ru.korgov.intellij.lspr.properties.api.ConflictsPolicity;
import ru.korgov.intellij.lspr.properties.api.XProperties;
import ru.korgov.util.alias.Cu;

import java.util.Map;
import java.util.Set;

/**
 * Author: Kirill Korgov (korgov@yandex-team.ru)
 * Date: 21.06.13 0:38
 */
public class XmlConfigBuilder {
    private static final String TAB = "    ";

    public static String buildFileBody(final Map<String, Set<DependencyTag>> beanNameToTag, final XProperties properties) {
        final StringBuilder sb = new StringBuilder(256);
        sb.append(properties.getBeansHeader()).append("\n\n");
        appendBeans(beanNameToTag, sb, properties.getConflictsPolicity());
        sb.append("\n\n").append(properties.getBeansFooter());
        return sb.toString();
    }

    private static void appendBeans(final Map<String, Set<DependencyTag>> beanNameToTag, final StringBuilder sb, final ConflictsPolicity conflictsPolicity) {
        for (final Set<DependencyTag> tags : beanNameToTag.values()) {
            for (final DependencyTag dependencyTag : getTagsToWrite(conflictsPolicity, tags)) {
                appendTag(sb, dependencyTag.getTag());
            }
        }
    }

    private static Iterable<DependencyTag> getTagsToWrite(final ConflictsPolicity conflictsPolicity, final Set<DependencyTag> tags) {
        return conflictsPolicity == ConflictsPolicity.AUTO_ALL ? tags : Cu.firstOrNothing(tags);
    }

    private static void appendTag(final StringBuilder sb, final XmlTag xmlTag) {
        sb.append(TAB).append(xmlTag.getText()).append("\n\n");
    }

    private XmlConfigBuilder(){
    }
}
