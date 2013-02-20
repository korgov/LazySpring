package ru.korgov.intellij.lspr.impl;

import com.intellij.psi.PsiType;
import org.jetbrains.annotations.Nullable;

/**
 * Author: Kirill Korgov (kirill@korgov.ru)
 * Date: 01.12.12
 */
@Deprecated
public class BeanDesc {
    private final PsiType psiType;
    private final String name;

    public BeanDesc(final @Nullable PsiType psiType, final String name) {
        this.psiType = psiType;
        this.name = name;
    }

    @Nullable
    public PsiType getPsiType() {
        return psiType;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings({"ControlFlowStatementWithoutBraces", "RedundantIfStatement"})
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final BeanDesc beanDesc = (BeanDesc) o;

        if (name != null ? !name.equals(beanDesc.name) : beanDesc.name != null) return false;
        if (psiType != null ? !psiType.equals(beanDesc.psiType) : beanDesc.psiType != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = psiType != null ? psiType.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BeanDesc{" +
                "psiType=" + psiType +
                ", name='" + name + '\'' +
                '}';
    }
}
