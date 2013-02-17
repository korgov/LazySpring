package ru.korgov.util.collection;

import ru.korgov.util.alias.Cu;

import java.util.Iterator;

/**
 * Author: Kirill Korgov (kirill@korgov.ru))
 * Date: 2/17/13 2:40 AM
 */
public class Option<T> implements Iterable<T> {
    private final T value;
    private final boolean hasValue;

    public boolean hasValue() {
        return hasValue;
    }

    public T getValue() {
        return value;
    }

    private Option(final T value, final boolean hasValue) {
        this.value = value;
        this.hasValue = hasValue;
    }

    private Option(final T value) {
        this(value, true);
    }

    @Override
    public Iterator<T> iterator() {
        return hasValue ? Cu.oneTimeIterator(value) : Cu.<T>emptyIterator();
    }

    public static <T> Option<T> just(final T value) {
        return new Option<T>(value);
    }

    public static <T> Option<T> nothing() {
        return new Option<T>(null, false);
    }

    @Override
    public String toString() {
        return "Option[" + (hasValue ? "value=" + value : "nothing") + "]";
    }
}
