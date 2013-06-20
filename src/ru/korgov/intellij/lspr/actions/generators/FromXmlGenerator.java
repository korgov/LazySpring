package ru.korgov.intellij.lspr.actions.generators;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import ru.korgov.intellij.lspr.impl.BeansFinder;
import ru.korgov.intellij.lspr.impl.Dependency;
import ru.korgov.intellij.lspr.impl.DependencyTag;
import ru.korgov.intellij.lspr.impl.DependencyTagDescriptor;
import ru.korgov.util.alias.Cf;
import ru.korgov.util.alias.Cu;
import ru.korgov.util.collection.Option;

import java.util.Map;
import java.util.Set;

/**
 * Author: Kirill Korgov (korgov@yandex-team.ru)
 * Date: 19.06.13 5:22
 */
public class FromXmlGenerator extends AbstractGenerator {
    private final XmlFile xmlFile;
    private final Project project;

    public FromXmlGenerator(final XmlFile xmlFile, final Project project) {
        this.xmlFile = xmlFile;
        this.project = project;
    }

    @Override
    protected Map<String, Set<DependencyTag>> actualFind(final BeansFinder beansFinder) {
        final DependencyTagDescriptor descriptor = getDepsDescriptor(project);
        final Map<String, Set<DependencyTag>> out = buildSrcDepsMap(descriptor, xmlFile);
        final Set<DependencyTag> srcTags = Cu.union(out.values());
        final Set<PsiClass> classesToResolve = Cf.newSet(DependencyTag.flatMapToClasses(srcTags));
        final Set<Dependency> refsToResolve = Cf.newSet(DependencyTag.flatMapToRefs(srcTags));
        Cu.appendAllToMultiSet(out, beansFinder.findForClasses(classesToResolve, out));
        Cu.appendAllToMultiSet(out, beansFinder.resolveDependencies(refsToResolve, out));
        return out;
    }

    private DependencyTagDescriptor getDepsDescriptor(final Project project) {
        return new DependencyTagDescriptor(
                GlobalSearchScope.allScope(project),
                JavaPsiFacade.getInstance(project),
                XmlElementFactory.getInstance(project)
        );
    }

    private Map<String, Set<DependencyTag>> buildSrcDepsMap(final DependencyTagDescriptor descriptor, final XmlFile xmlFile) {
        final Map<String, Set<DependencyTag>> out = Cf.newLinkedMap();
        final XmlTag rootTag = xmlFile.getRootTag();
        if (rootTag != null) {
            for (final XmlTag subTag : rootTag.getSubTags()) {
                final Option<DependencyTag> dependencyTags = descriptor.buildFromTag(subTag, Option.<Dependency>nothing(), xmlFile);
                for (final DependencyTag dependencyTag : dependencyTags) {
                    final XmlTag xmlTag = dependencyTag.getTag();
                    for (final String id : descriptor.getSomeIdentifier(xmlTag)) {
                        Cu.appendToMultiSet(out, id, dependencyTag);
                    }
                }
            }
        }
        return out;
    }

    @Override
    public String getDefaultFilename() {
        return xmlFile.getName();
    }
}
