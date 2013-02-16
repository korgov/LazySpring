package ru.korgov.util;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.korgov.util.collection.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Author: Kirill Korgov (kirill@korgov.ru)
 * Date: 12/18/11
 */
public class JSONUtils {
    private JSONUtils() {
    }

    public static List<String> asStringList(final JSONArray jsonArray) {
        if (jsonArray != null) {
            final int length = jsonArray.length();
            final List<String> out = new ArrayList<String>(length);
            for (int j = 0; j < length; j++) {
                CollectionUtils.addIfNotNull(out, jsonArray.optString(j));
            }
            return out;
        }
        return Collections.emptyList();
    }

    public static List<JSONObject> asJSONObjectList(final JSONArray jsonArray) {
        if (jsonArray != null) {
            final int length = jsonArray.length();
            final List<JSONObject> out = new ArrayList<JSONObject>(length);
            for (int j = 0; j < length; j++) {
                CollectionUtils.addIfNotNull(out, jsonArray.optJSONObject(j));
            }
            return out;
        }
        return Collections.emptyList();
    }

    public static List<JSONArray> asJSONArrayList(final JSONArray jsonArray) {
        if (jsonArray != null) {
            final int length = jsonArray.length();
            final List<JSONArray> out = new ArrayList<JSONArray>(length);
            for (int j = 0; j < length; j++) {
                CollectionUtils.addIfNotNull(out, jsonArray.optJSONArray(j));
            }
            return out;
        }
        return Collections.emptyList();
    }

    public static Map<String, String> asMap(final JSONObject jsonObject) {
        if (jsonObject == null) {
            return Collections.emptyMap();
        }

        final List<String> keys = getKeys(jsonObject);
        final Map<String, String> out = new HashMap<String, String>(keys.size());
        for (final String key : keys) {
            final String value = jsonObject.optString(key, null);
            CollectionUtils.addIfNotNull(out, key, value);
        }
        return out;
    }

    public static Map<Class<?>, List<String>> getTypedKeys(final JSONObject jsonObject) {
        if (jsonObject == null) {
            return Collections.emptyMap();
        }
        final Map<Class<?>, List<String>> out = new HashMap<Class<?>, List<String>>();
        for (final String key : getKeys(jsonObject)) {
            final Object value = getSafetyObject(jsonObject, key);
            if (value != null) {
                if (value instanceof JSONObject) {
                    CollectionUtils.appendToMultiMap(out, JSONObject.class, key);
                } else if (value instanceof JSONArray) {
                    CollectionUtils.appendToMultiMap(out, JSONArray.class, key);
                } else {
                    CollectionUtils.appendToMultiMap(out, String.class, key);
                }
            }
        }
        return out;
    }

    @Nullable
    private static Object getSafetyObject(final JSONObject jsonObject, final String key) {
        try {
            return jsonObject.get(key);
        } catch (JSONException ignored) {
            return null;
        }
    }

    public static List<String> getKeys(final JSONObject jsonObject) {
        final List<String> out = new ArrayList<String>();
        final Iterator<?> it = jsonObject.keys();
        while (it.hasNext()) {
            out.add(it.next().toString());
        }
        return out;
    }
}
