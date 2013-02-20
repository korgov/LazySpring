package ru.korgov.intellij.lspr.impl;

import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.Nullable;
import ru.korgov.util.alias.Fu;

/**
 * Author: Kirill Korgov (kirill@korgov.ru)
 * Date: 02.12.12
 */
@Deprecated
public class XmlBean {
    private final XmlTag tag;
    private final String text;
    private final XmlFile file;

    private XmlBean(final XmlTag tag, final String text, final XmlFile file) {
        this.tag = tag;
        this.text = text;
        this.file = file;
    }

    public XmlTag getTag() {
        return tag;
    }

    public String getText() {
        return text;
    }

    public XmlFile getFile() {
        return file;
    }

    @SuppressWarnings({"ControlFlowStatementWithoutBraces", "RedundantIfStatement"})
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final XmlBean xmlBean = (XmlBean) o;

        if (text != null ? !text.equals(xmlBean.text) : xmlBean.text != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return text != null ? text.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "XmlBean{" +
                "tag=" + tag +
                ", text='" + text + '\'' +
                '}';
    }

    public static XmlBean from(final XmlTag tag, @Nullable final XmlFile file) {
        tag.collapseIfEmpty();
        return new XmlBean(tag, tag.getText().trim(), file);
    }

    public static XmlBean from(final XmlTag tag) {
        return from(tag, null);
    }

    public static final Fu<XmlBean, XmlTag> TO_TAG = new Fu<XmlBean, XmlTag>() {
        @Override
        public XmlTag apply(final XmlBean v) {
            return v.getTag();
        }
    };
}
