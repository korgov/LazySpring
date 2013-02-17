package ru.korgov.util;

/**
 * Author: Kirill Korgov (kirill@korgov.ru))
 * Date: 2/17/13 7:22 PM
 */
public class ObjectUtils {
    private ObjectUtils() {
    }

    public static <T> T avoidNull(final T value, final T defValue) {
        return value == null ? defValue : value;
    }
}
