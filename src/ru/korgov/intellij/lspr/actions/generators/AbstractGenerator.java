package ru.korgov.intellij.lspr.actions.generators;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import ru.korgov.intellij.lspr.impl.BeansFinder;
import ru.korgov.intellij.lspr.impl.DependencyTag;

import java.util.Map;
import java.util.Set;

/**
 * Author: Kirill Korgov (korgov@yandex-team.ru)
 * Date: 19.06.13 3:55
 */
public abstract class AbstractGenerator implements Generator {

    @Override
    public Map<String, Set<DependencyTag>> find(final BeansFinder beansFinder) {
        return ApplicationManager.getApplication().runReadAction(new Computable<Map<String, Set<DependencyTag>>>() {
            @Override
            public Map<String, Set<DependencyTag>> compute() {
                return actualFind(beansFinder);
            }
        });
    }

    protected abstract Map<String, Set<DependencyTag>> actualFind(final BeansFinder beansFinder);
}
