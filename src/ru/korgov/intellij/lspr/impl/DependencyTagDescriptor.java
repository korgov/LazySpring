package ru.korgov.intellij.lspr.impl;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiType;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import ru.korgov.util.ObjectUtils;
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

    private static final Map<String, Set<String>> IDENTIFIERS = Cu.zipMap(
            Cf.pair("bean", BEAN_ID_ATTRS),
            Cf.pair("alias", ALIAS_ID_ATTRS)
    );

    private final GlobalSearchScope prodScope;
    private final JavaPsiFacade javaPsiFacade;
    private final XmlElementFactory xmlElementFactory;
    private final PsiElementFactory elementFactory;

    private final Map<String, DependencyTagBuilder> builders = Cu.zipMap(
            Cf.pair("bean", new DependencyTagBuilder() {
                @Override
                public Option<DependencyTag> buildFromTag(final XmlTag tag, final Option<Dependency> dependency, final XmlFile xmlFile) {
                    if (!dependency.hasValue() || validIdAndTag(tag, dependency.getValue())) {
                        return Option.just(newBeanTag(tag, Option.just(xmlFile)));
                    }
                    return Option.nothing();
                }
            }),
            Cf.pair("alias", new DependencyTagBuilder() {

                @Override
                public Option<DependencyTag> buildFromTag(final XmlTag tag, final Option<Dependency> dependency, final XmlFile xmlFile) {
                    if (!dependency.hasValue() || validAliasId(tag, dependency.getValue())) {
                        return Option.just(newAliasTag(tag, dependency, Option.just(xmlFile)));
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

    private boolean validIdAndTag(final XmlTag tag, final Dependency dependencyValue) {
        return validBeanId(tag, dependencyValue) && validBeanTag(tag, dependencyValue);
    }

    private boolean validBeanTag(final XmlTag tag, final Dependency dependency) {
        final String clazzName = tag.getAttributeValue("class");
        if (clazzName != null) {
            final PsiClassType classValue = elementFactory.createTypeByFQClassName(clazzName);
            return validDependencyClass(classValue, dependency);
        }
        return !Su.isEmpty(tag.getAttributeValue("parent"))
          || !Su.isEmpty(tag.getAttributeValue("factory-bean"));
    }

    private DependencyTagBuilder utilTagBuilder(final String classAttr, final String defaultClass) {
        return new DependencyTagBuilder() {
            @Override
            public Option<DependencyTag> buildFromTag(final XmlTag tag, final Option<Dependency> dependency, final XmlFile xmlFile) {
                final boolean notCheckedDep = !dependency.hasValue();
                final Dependency dependencyValue = dependency.getValue();
                final String clazzName = Su.avoidEmpty(tag.getAttributeValue(classAttr), defaultClass);
                final PsiClassType tagBeanType = elementFactory.createTypeByFQClassName(clazzName);
                if (notCheckedDep 
                        || validIdAndClass(tag, dependencyValue, tagBeanType)) {
                    return Option.just(newBeanTag(tag, Option.just(xmlFile)));
                }
                return Option.nothing();
            }
        };
    }

    private boolean validIdAndClass(final XmlTag tag, final Dependency dependencyValue, final PsiClassType tagBeanType) {
        return validBeanId(tag, dependencyValue) && validDependencyClass(tagBeanType, dependencyValue);
    }

    public DependencyTagDescriptor(final GlobalSearchScope prodScope, final JavaPsiFacade javaPsiFacade, final XmlElementFactory xmlElementFactory) {
        this.prodScope = prodScope;
        this.javaPsiFacade = javaPsiFacade;
        this.xmlElementFactory = xmlElementFactory;
        this.elementFactory = javaPsiFacade.getElementFactory();
    }

    public XmlElementFactory getXmlElementFactory() {
        return xmlElementFactory;
    }

    public boolean isDependencyTag(final XmlTag xmlTag) {
        return builders.containsKey(xmlTag.getName());
    }

    public Option<DependencyTag> buildFromTag(final XmlTag tag, final Option<Dependency> dependency, final XmlFile xmlFile) {
        if (tag != null) {
            final DependencyTagBuilder dependencyTagBuilder = builders.get(tag.getName());
            if (dependencyTagBuilder != null) {
                try {
                    return dependencyTagBuilder.buildFromTag(tag, dependency, xmlFile);
                } catch (Exception ignored) {
                    //todo: log this exception
                }
            }
        }
        return Option.nothing();
    }

    private boolean validDependencyClass(final PsiClassType tagBeanType, final Dependency dependency) {
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

    @NotNull
    public Option<String> getSomeIdentifier(final XmlTag xmlTag) {
        final String tagName = xmlTag.getName();
        for (final String idAttr : ObjectUtils.avoidNull(IDENTIFIERS.get(tagName), BEAN_ID_ATTRS)) {
            final String idOrNull = xmlTag.getAttributeValue(idAttr);
            if (idOrNull != null) {
                return Option.just(idOrNull);
            }
        }
        return Option.nothing();
    }

    private interface DependencyTagBuilder {
        Option<DependencyTag> buildFromTag(final XmlTag tag, final Option<Dependency> dependency, final XmlFile xmlFile);
    }

    public DependencyTag newBeanTag(final XmlTag tag) {
        return newBeanTag(tag, Option.<XmlFile>nothing());
    }

    public DependencyTag newBeanTag(final XmlTag tag, final Option<XmlFile> xmlFile) {
        return new DependencyTag(lazyBeanTag(tag), xmlFile) {
            @Override
            protected Set<PsiClass> extractClassesFromTag(final XmlTag someTag) {
                final String className = someTag.getAttributeValue("class");
                if (className != null) {
//                    System.out.println("trying extract class: " + className);
                    return Cf.set(javaPsiFacade.findClasses(className, prodScope));
//                    System.out.println("extracted for: " + className + " :: " + result);
                }
                return Cf.emptyS();
            }
        };
    }

    private XmlTag lazyBeanTag(final XmlTag tag) {
        if ("false".equals(tag.getAttributeValue("lazy-init"))) {
            final XmlTag copy = (XmlTag) tag.copy();
            copy.setAttribute("lazy-init", "true");
            return copy;
        }
        return tag;
    }

    public DependencyTag newAliasTag(final XmlTag tag, final Option<Dependency> dependency, final Option<XmlFile> xmlFile) {
        return new DependencyTag(tag, xmlFile) {
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
                    final Option<PsiType> psiType = dependency.hasValue() ? dependency.getValue().getPsiType() : Option.<PsiType>nothing();
                    out.add(Dependency.byNameAndOptionType(aliasBeanName, psiType));
                }
                return out;
            }
        };
    }
}
