package ru.korgov.intellij.lspr.actions.generators;

import ru.korgov.intellij.lspr.impl.BeansFinder;
import ru.korgov.intellij.lspr.impl.DependencyTag;

import java.util.Map;
import java.util.Set;

/**
 * Author: Kirill Korgov (korgov@yandex-team.ru)
 * Date: 19.06.13 3:53
 */
public interface Generator {
    Map<String, Set<DependencyTag>> find(final BeansFinder beansFinder);

    String getDefaultFilename();
}