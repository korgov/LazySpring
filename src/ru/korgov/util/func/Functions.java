package ru.korgov.util.func;

import ru.korgov.util.alias.Fu;
import ru.korgov.util.collection.Pair;

/**
 * Author: Kirill Korgov (kirill@korgov.ru))
 * Date: 2/17/13 3:40 AM
 */
public class Functions {

    public static <K, V> Fu<Pair<? extends K, ? extends V>, K> firstOfPair() {
        return new Fu<Pair<? extends K, ? extends V>, K>() {
            @Override
            public K apply(final Pair<? extends K, ? extends V> v) {
                return v.getFirst();
            }
        };
    }

    public static <K, V> Fu<Pair<? extends K, ? extends V>, V> secondOfPair() {
        return new Fu<Pair<? extends K, ? extends V>, V>() {
            @Override
            public V apply(final Pair<? extends K, ? extends V> v) {
                return v.getSecond();
            }
        };
    }
}
