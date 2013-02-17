package ru.korgov.intellij.lspr.model;

import com.intellij.psi.PsiClass;
import com.intellij.psi.xml.XmlTag;
import ru.korgov.util.alias.Cf;
import ru.korgov.util.alias.Cu;
import ru.korgov.util.alias.Fu;

import java.util.List;
import java.util.Set;

/**
 * Author: Kirill Korgov (kirill@korgov.ru))
 * Date: 2/17/13 1:41 AM
 */
public abstract class DependencyTag {

    private static final List<String> DEFAULT_REF_ATTRS = Cf.list("ref", "bean", "parent", "value-ref", "key-ref");

    private final XmlTag tag;
    private final String text;

    private DependencyTag(final XmlTag tag, final String text) {
        this.tag = tag;
        this.text = text;
    }

    protected DependencyTag(final XmlTag tag) {
        this(tag, collapsedTagText(tag));
    }

    private static String collapsedTagText(final XmlTag tag) {
        tag.collapseIfEmpty();
        return tag.getText().trim();
    }

    public XmlTag getTag() {
        return tag;
    }

    public String getText() {
        return text;
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
