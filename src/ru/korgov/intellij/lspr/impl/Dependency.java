package ru.korgov.intellij.lspr.impl;

import com.intellij.psi.PsiType;
import ru.korgov.util.collection.Option;

/**
 * Author: Kirill Korgov (kirill@korgov.ru))
 * Date: 2/17/13 2:39 AM
 */
public class Dependency {
    private final Option<PsiType> psiType;
    private final String name;

    private Dependency(final String name, final PsiType type) {
        this.name = name;
        this.psiType = Option.just(type);
    }

    private Dependency(final String name) {
        this.name = name;
        this.psiType = Option.nothing();
    }

    public Option<PsiType> getPsiType() {
        return psiType;
    }

    public String getName() {
        return name;
    }

    public static Dependency byName(final String name) {
        return new Dependency(name);
    }

    public static Dependency byNameAndType(final String name, final PsiType type) {
        return new Dependency(name, type);
    }

    public static Dependency byNameAndOptionType(final String name, final Option<PsiType> type) {
        if (type.hasValue()) {
            return byNameAndType(name, type.getValue());
        }
        return byName(name);
    }

    @SuppressWarnings({"ControlFlowStatementWithoutBraces", "RedundantIfStatement"})
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Dependency that = (Dependency) o;

        if (!name.equals(that.name)) return false;
        if (!psiType.equals(that.psiType)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = psiType.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Dependency{" +
                "psiType=" + psiType +
                ", name='" + name + '\'' +
                '}';
    }
}
