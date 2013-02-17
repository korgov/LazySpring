package ru.korgov.intellij.lspr.model;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlTag;
import ru.korgov.util.alias.Cf;
import ru.korgov.util.alias.Cu;
import ru.korgov.util.alias.Su;
import ru.korgov.util.collection.Option;

import java.util.Map;
import java.util.Set;

/**
 * Author: Kirill Korgov (kirill@korgov.ru))
 * Date: 2/17/13 2:24 AM
 */
public class DependencyTagDescriptor {

    private static final Set<String> BEAN_ID_ATTRS = Cf.set("name", "id");
    private static final Set<String> ALIAS_ID_ATTRS = Cf.set("alias");

    private final GlobalSearchScope prodScope;
    private final JavaPsiFacade javaPsiFacade;
    private final PsiElementFactory elementFactory;

    private final Map<String, DependencyTagBuilder> builders = Cu.zipMap(
            Cf.pair("bean", new DependencyTagBuilder() {
                @Override
                public Option<DependencyTag> buildFromTag(final XmlTag tag, final Dependency dependency) {
                    if (validBeanId(tag, dependency)) {
                        final String clazzName = tag.getAttributeValue("class");
                        if (clazzName != null) {
                            final PsiClassType tagBeanType = elementFactory.createTypeByFQClassName(clazzName);
                            if (validDependencyClass(tagBeanType, dependency)) {
                                return Option.just(newBeanTag(tag));
                            }
                        }
                    }
                    return Option.nothing();
                }
            }),
            Cf.pair("alias", new DependencyTagBuilder() {

                @Override
                public Option<DependencyTag> buildFromTag(final XmlTag tag, final Dependency dependency) {
                    if (validAliasId(tag, dependency)) {
                        return Option.just(newAliasTag(tag, dependency));
                    }
                    return Option.nothing();
                }
            }),
            Cf.pair("util:list", utilTagBuilder("list-class", "java.util.List")),
            Cf.pair("util:set", utilTagBuilder("set-class", "java.util.Set")),
            Cf.pair("util:map", utilTagBuilder("map-class", "java.util.Map")),
            Cf.pair("list", utilTagBuilder("list-class", "java.util.List")),
            Cf.pair("set", utilTagBuilder("set-class", "java.util.Set")),
            Cf.pair("map", utilTagBuilder("map-class", "java.util.Map"))
    );

    private DependencyTagBuilder utilTagBuilder(final String classAttr, final String defaultClass) {
        return new DependencyTagBuilder() {
            @Override
            public Option<DependencyTag> buildFromTag(final XmlTag tag, final Dependency dependency) {
                if (validBeanId(tag, dependency)) {
                    final String clazzName = Su.avoidEmpty(tag.getAttributeValue(classAttr), defaultClass);
                    final PsiClassType tagBeanType = elementFactory.createTypeByFQClassName(clazzName);
                    if (validDependencyClass(tagBeanType, dependency)) {
                        return Option.just(newBeanTag(tag));
                    }
                }
                return Option.nothing();
            }
        };
    }

    public DependencyTagDescriptor(final GlobalSearchScope prodScope, final JavaPsiFacade javaPsiFacade) {
        this.prodScope = prodScope;
        this.javaPsiFacade = javaPsiFacade;
        this.elementFactory = javaPsiFacade.getElementFactory();
    }

    public boolean isDependencyTag(final XmlTag xmlTag) {
        return builders.containsKey(xmlTag.getName());
    }

    public Option<DependencyTag> buildFromTag(final XmlTag tag, final Dependency dependency) {
        if (tag != null) {
            final DependencyTagBuilder dependencyTagBuilder = builders.get(tag.getName());
            if (dependencyTagBuilder != null) {
                return dependencyTagBuilder.buildFromTag(tag, dependency);
            }
        }
        return Option.nothing();
    }

    private static boolean validDependencyClass(final PsiClassType tagBeanType, final Dependency dependency) {
        final Option<PsiType> psiType = dependency.getPsiType();
        return !psiType.hasValue() || psiType.getValue().isAssignableFrom(tagBeanType);
    }

    private static boolean validBeanId(final XmlTag tag, final Dependency dependency) {
        return validateId(tag, dependency, BEAN_ID_ATTRS);
    }

    private static boolean validAliasId(final XmlTag tag, final Dependency dependency) {
        return validateId(tag, dependency, ALIAS_ID_ATTRS);
    }

    private static boolean validateId(final XmlTag tag, final Dependency dependency, final Set<String> IdAttrs) {
        final String depName = dependency.getName();
        for (final String dependencyIdAttr : IdAttrs) {
            if (depName.equals(tag.getAttributeValue(dependencyIdAttr))) {
                return true;
            }
        }
        return false;
    }

    private interface DependencyTagBuilder {
        Option<DependencyTag> buildFromTag(final XmlTag tag, final Dependency dependency);
    }

    public DependencyTag newBeanTag(final XmlTag tag) {
        return new DependencyTag(tag) {
            @Override
            protected Set<PsiClass> extractClassesFromTag(final XmlTag someTag) {
                final String className = tag.getAttributeValue("class");
                if (className != null) {
                    return Cf.set(javaPsiFacade.findClasses(className, prodScope));
                }
                return Cf.emptyS();
            }
        };
    }

    public DependencyTag newAliasTag(final XmlTag tag, final Dependency dependency) {
        return new DependencyTag(tag) {
            @SuppressWarnings("RefusedBequest")
            @Override
            public Set<PsiClass> extractClasses() {
                return Cf.emptyS();
            }

            @Override
            protected Set<PsiClass> extractClassesFromTag(final XmlTag someTag) {
                return Cf.emptyS();
            }

            @Override
            public Set<Dependency> extractRefs() {
                final Set<Dependency> out = Cf.newSet(super.extractRefs());
                final String aliasBeanName = getTag().getAttributeValue("name");
                if (!Su.isEmpty(aliasBeanName)) {
                    out.add(Dependency.byNameAndOptionType(aliasBeanName, dependency.getPsiType()));
                }
                return out;
            }
        };
    }
}
