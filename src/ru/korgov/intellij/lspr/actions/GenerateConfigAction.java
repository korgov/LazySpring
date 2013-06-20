package ru.korgov.intellij.lspr.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.korgov.intellij.lspr.actions.generators.Generator;
import ru.korgov.intellij.lspr.actions.generators.GeneratorsFactory;
import ru.korgov.intellij.lspr.actions.result.XmlConfigSaver;
import ru.korgov.intellij.lspr.impl.BeansFinder;
import ru.korgov.intellij.lspr.impl.DependencyTag;
import ru.korgov.intellij.lspr.properties.PersistentStateProperties;
import ru.korgov.intellij.lspr.properties.api.XProperties;
import ru.korgov.intellij.util.IdeaUtils;

import java.util.Map;
import java.util.Set;

/**
 * Author: Kirill Korgov (kirill@korgov.ru)
 * Date: 30.11.12
 */
public class GenerateConfigAction extends AnAction {

    @Override
    public void actionPerformed(final AnActionEvent e) {
        final Project project = e.getData(PlatformDataKeys.PROJECT);
        final Editor editor = e.getData(PlatformDataKeys.EDITOR);
        final XProperties properties = PersistentStateProperties.getInstance(project);
        if (project != null && editor != null) {
            doGenerateInBackground(project, editor, properties);
        }
    }

    private void doGenerateInBackground(final Project project, final Editor editor, final XProperties properties) {
        new Task.Backgroundable(project, "Resolving dependencies..", true) {
            @Override
            public void run(@NotNull final ProgressIndicator indicator) {
                indicator.setText("Searching beans..");
                indicator.setFraction(0.0);
                final VirtualFile initialFile = getCurrentFile(editor);
                if (initialFile != null) {
                    indicator.setFraction(0.2);
                    final BeansFinder beansFinder = getBeansFinder(indicator, project, initialFile, properties);
                    final Generator generator = GeneratorsFactory.getGenerator(project, initialFile, editor.getCaretModel().getOffset());
                    if (generator != null) {
                        final Map<String, Set<DependencyTag>> requiredBeans = generator.find(beansFinder);
                        indicator.setFraction(0.8);
                        indicator.setText("Saving config file..");
                        XmlConfigSaver.save(project, initialFile, generator.getDefaultFilename(), requiredBeans, properties);
                    }
                }
                indicator.setFraction(1.0);
            }

        }.setCancelText("Cancel task.").queue();
    }

    @Nullable
    private VirtualFile getCurrentFile(final Editor editor) {
        return ApplicationManager.getApplication().runReadAction(new Computable<VirtualFile>() {
            @Override
            public VirtualFile compute() {
                return IdeaUtils.getCurrentFile(editor);
            }
        });
    }

    private BeansFinder getBeansFinder(final ProgressIndicator indicator, final Project project, final VirtualFile initialFile, final XProperties XPropertiesService) {
        return ApplicationManager.getApplication().runReadAction(new Computable<BeansFinder>() {
            @Override
            public BeansFinder compute() {
                return BeansFinder.getInstance(project, initialFile, XPropertiesService, indicator);
            }
        });
    }

    @Override
    public void update(final AnActionEvent e) {
        e.getPresentation().setEnabled(isActionEnabled(e));
    }

    private boolean isActionEnabled(final AnActionEvent e) {
        final Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor != null) {
            final VirtualFile initialFile = getCurrentFile(editor);
            if (initialFile != null) {
                final Project project = editor.getProject();
                if (project != null) {
                    final Generator generator = GeneratorsFactory.getGenerator(project, initialFile, editor.getCaretModel().getOffset());
                    return generator != null;
                }
            }
        }
        return false;
    }
}
