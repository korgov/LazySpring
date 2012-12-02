package ru.korgov.util.func;

/**
 * Author: Kirill Korgov (kirill@korgov.ru)
 * Date: 01.12.12
 */
public abstract class Function<F, T> {
    public abstract T apply(final F v);

    public <N> Function<F, N> then(final Function<T, N> nextFu) {
        return new Function<F, N>() {
            @Override
            public N apply(final F v) {
                return nextFu.apply(Function.this.apply(v));
            }
        };
    }
}
