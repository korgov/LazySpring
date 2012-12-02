package ru.korgov.util;

/**
 * Author: Kirill Korgov (kirill@korgov.ru)
 * Date: 01.12.12
 */
public interface Filter<T> {
    boolean fits(final T x);
}
