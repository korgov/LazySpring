package ru.korgov.intellij.lspr.impl;

import com.intellij.psi.PsiClass;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import ru.korgov.util.alias.Cf;
import ru.korgov.util.alias.Cu;
import ru.korgov.util.alias.Fu;
import ru.korgov.util.collection.Option;

import java.util.List;
import java.util.Set;

/**
 * Author: Kirill Korgov (kirill@korgov.ru))
 * Date: 2/17/13 1:41 AM
 */
public abstract class DependencyTag {

    private static final List<String> DEFAULT_REF_ATTRS = Cf.list(
            "ref", "bean", "parent", "factory-bean", "depends-on",
            "value-ref", "key-ref"
    );

    private final XmlTag tag;
    private final String text;
    private final Option<XmlFile> xmlFile;

    private DependencyTag(final XmlTag tag, final String text, final Option<XmlFile> xmlFile) {
        this.tag = tag;
        this.text = text;
        this.xmlFile = xmlFile;
    }

    protected DependencyTag(final XmlTag tag, final Option<XmlFile> xmlFile) {
        this(tag, tag.getText().trim(), xmlFile);
    }

    public XmlTag getTag() {
        return tag;
    }

    public String getText() {
        return text;
    }

    public Option<XmlFile> getXmlFile() {
        return xmlFile;
    }

    public Set<PsiClass> extractClasses() {
        return extractClasses(tag);
    }

    private Set<PsiClass> extractClasses(final XmlTag someTag) {
        final Set<PsiClass> out = Cf.newSet(extractClassesFromTag(someTag));
        for (final XmlTag subTag : someTag.getSubTags()) {
            out.addAll(extractClasses(subTag));
        }
        return out;
    }

    protected abstract Set<PsiClass> extractClassesFromTag(final XmlTag someTag);

    public Set<Dependency> extractRefs() {
        return extractRefs(tag);
    }

    private Set<Dependency> extractRefs(final XmlTag someTag) {
        final Set<Dependency> out = Cf.newSet(extractRefsFromTag(someTag));
        for (final XmlTag subTag : someTag.getSubTags()) {
            out.addAll(extractRefs(subTag));
        }
        return out;
    }

    protected Set<Dependency> extractRefsFromTag(final XmlTag someTag) {
        final Set<Dependency> out = Cf.newSet();
        for (final String refAttr : DEFAULT_REF_ATTRS) {
            final String refBean = someTag.getAttributeValue(refAttr);
            if (refBean != null) {
                out.add(Dependency.byName(refBean));
            }
        }
        return out;
    }

    public static final Fu<DependencyTag, XmlTag> TO_TAG = new Fu<DependencyTag, XmlTag>() {
        @Override
        public XmlTag apply(final DependencyTag v) {
            return v.getTag();
        }
    };

    public static final Fu<DependencyTag, Set<Dependency>> TO_REFS = new Fu<DependencyTag, Set<Dependency>>() {
        @Override
        public Set<Dependency> apply(final DependencyTag v) {
            return v.extractRefs();
        }
    };

    public static final Fu<DependencyTag, Set<PsiClass>> TO_CLASSES = new Fu<DependencyTag, Set<PsiClass>>() {
        @Override
        public Set<PsiClass> apply(final DependencyTag v) {
            return v.extractClasses();
        }
    };

    public static List<PsiClass> flatMapToClasses(final Iterable<DependencyTag> tags) {
        return Cu.join(TO_CLASSES.map(tags));
    }

    public static List<Dependency> flatMapToRefs(final Iterable<DependencyTag> tags) {
        return Cu.join(TO_REFS.map(tags));
    }

    @SuppressWarnings({"ControlFlowStatementWithoutBraces", "RedundantIfStatement"})
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final DependencyTag that = (DependencyTag) o;

        if (text != null ? !text.equals(that.text) : that.text != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return text != null ? text.hashCode() : 0;
    }
}
