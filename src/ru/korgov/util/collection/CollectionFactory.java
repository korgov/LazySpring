package ru.korgov.util.collection;

import ru.korgov.util.alias.Cf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author: Kirill Korgov (kirill@korgov.ru)
 * Date: 22.04.12
 */
public class CollectionFactory {
    protected CollectionFactory() {
    }

    public static <T> List<T> newList(){
        return new ArrayList<T>();
    }

    public static <T> List<T> list(final T... items){
        return Arrays.asList(items);
    }

    public static <T> Set<T> set(final T... items){
        final Set<T> out = Cf.newSet();
        Collections.addAll(out, items);
        return out;
    }

    public static <T> List<T> newList(final Collection<T> c){
        return new ArrayList<T>(c);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> List<T> newList(final Collection c, final Class<T> clazz){
        return new ArrayList<T>(c);
    }

    public static <T> Set<T> newSet(){
        return new HashSet<T>();
    }

    public static <T> Set<T> newLinkedSet(){
        return new LinkedHashSet<T>();
    }

    public static <T> Set<T> newSet(final Collection<T> c){
        return new HashSet<T>(c);
    }

    public static <K, V> Map<K, V> newMap(){
        return new HashMap<K, V>();
    }

    public static <K, V> Map<K, V> newMap(final Map<K, V> map){
        return new HashMap<K, V>(map);
    }

    public static <K, V> LinkedHashMap<K, V> newLinkedMap() {
        return new LinkedHashMap<K, V>();
    }
}
