package ru.korgov.intellij.lspr.actions.generators;

import com.intellij.psi.PsiClass;
import ru.korgov.intellij.lspr.impl.BeansFinder;
import ru.korgov.intellij.lspr.impl.DependencyTag;

import java.util.Map;
import java.util.Set;

/**
 * Author: Kirill Korgov (korgov@yandex-team.ru)
 * Date: 19.06.13 3:54
 */
public class SingleClassGenerator extends AbstractGenerator {
    private PsiClass clazz;

    public SingleClassGenerator(final PsiClass clazz) {
        this.clazz = clazz;
    }

    @Override
    public Map<String, Set<DependencyTag>> actualFind(final BeansFinder beansFinder) {
        return beansFinder.findForClass(clazz);
    }

    @Override
    public String getDefaultFilename() {
        return "test-" + clazz.getName() + ".xml";
    }
}
