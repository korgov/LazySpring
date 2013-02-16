package ru.korgov.util.func;

import ru.korgov.util.alias.Cu;

import java.util.List;

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

    public List<T> map(final Iterable<F> iterable){
        return Cu.map(iterable, this);
    }
}
