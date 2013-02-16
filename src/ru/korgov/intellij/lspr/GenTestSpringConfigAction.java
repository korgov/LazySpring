package ru.korgov.intellij.lspr;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.psi.PsiClass;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import ru.korgov.intellij.lspr.properties.ConflictsPolicity;
import ru.korgov.intellij.lspr.properties.PropertiesService;
import ru.korgov.intellij.util.IdeaUtils;
import ru.korgov.util.alias.Cu;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

/**
 * Author: Kirill Korgov (kirill@korgov.ru)
 * Date: 30.11.12
 */
public class GenTestSpringConfigAction extends AnAction {

    private static final String TAB = "    ";

    @Override
    public void actionPerformed(final AnActionEvent e) {
        final Project project = e.getData(PlatformDataKeys.PROJECT);
        final Editor editor = e.getData(PlatformDataKeys.EDITOR);
        final PropertiesService propertiesService = PropertiesService.getInstance(PropertiesComponent.getInstance(), project);
        if (project != null && editor != null) {
            new Task.Backgroundable(project, "Resolving dependencies..", true) {
                @Override
                public void run(@NotNull final ProgressIndicator indicator) {
                    indicator.setText("Searching beans..");
                    indicator.setFraction(0.0);
                    final PsiClass clazz = getCurrentClass(editor);
                    indicator.setFraction(0.2);
                    final BeansFinder beansFinder = getBeansFinder(indicator, project, propertiesService);
                    indicator.setFraction(0.4);
                    final Map<String, Set<XmlBean>> requiredBeans = findForClass(clazz, beansFinder);
                    indicator.setFraction(0.8);
                    indicator.setText("Saving config file..");
                    createConfig(project, clazz, requiredBeans, propertiesService);
                    indicator.setFraction(1.0);
                }
            }.setCancelText("Cancel task.").queue();
        }
    }

    private Map<String, Set<XmlBean>> findForClass(final PsiClass clazz, final BeansFinder beansFinder) {
        return ApplicationManager.getApplication().runReadAction(new Computable<Map<String, Set<XmlBean>>>() {
            @Override
            public Map<String, Set<XmlBean>> compute() {
                return beansFinder.findForClass(clazz);
            }
        });
    }

    private PsiClass getCurrentClass(final Editor editor) {
        return ApplicationManager.getApplication().runReadAction(new Computable<PsiClass>() {
            @Override
            public PsiClass compute() {
                return IdeaUtils.getCurrentClass(editor);
            }
        });
    }

    private BeansFinder getBeansFinder(final ProgressIndicator indicator, final Project project, final PropertiesService propertiesService) {
        return ApplicationManager.getApplication().runReadAction(new Computable<BeansFinder>() {
            @Override
            public BeansFinder compute() {
                return BeansFinder.getInstance(project, propertiesService, indicator);
            }
        });
    }

    private void createConfig(final Project project, final PsiClass clazz, final Map<String, Set<XmlBean>> beanNameToTag, final PropertiesService propertiesService) {
        final ConflictsPolicity conflictsPolicity = propertiesService.getConflictsPolicity();
        final String beansHeader = propertiesService.getBeansHeader();
        final String beansFooter = propertiesService.getBeansFooter();
        final Application application = ApplicationManager.getApplication();
        application.invokeLater(new Runnable() {
            @Override
            public void run() {
                final VirtualFileWrapper save = saveDialog(project, clazz);
                if (save != null) {
                    doWrite(save);
                }
            }

            private void doWrite(final VirtualFileWrapper save) {
                application.runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final Writer writer = new BufferedWriter(new FileWriter(save.getFile()));
                            try {
                                writer.write(beansHeader + "\n\n");
                                writeBeans(beanNameToTag, writer, conflictsPolicity);
                                writer.write("\n\n" + beansFooter);
                            } finally {
                                writer.close();
                            }
                        } catch (final IOException ignored) {
                        }
                    }
                });
            }
        });

    }

    private VirtualFileWrapper saveDialog(final Project project, final PsiClass clazz) {
        final FileSaverDialog dialog = FileChooserFactory.getInstance().createSaveFileDialog(
                new FileSaverDescriptor("Save config to", "", "xml"), project);
        return dialog.save(project.getBaseDir(), "test-" + clazz.getName() + ".xml");
    }


    private void writeBeans(final Map<String, Set<XmlBean>> beanNameToTag, final Writer writer, final ConflictsPolicity conflictsPolicity) throws IOException {
        final boolean writeAll = ConflictsPolicity.AUTO_ALL.equals(conflictsPolicity);
        for (final Set<XmlBean> xmlTags : beanNameToTag.values()) {
            if (writeAll) {
                for (final XmlTag xmlTag : Cu.map(xmlTags, XmlBean.TO_TAG)) {
                    writer.write(TAB + xmlTag.getText() + "\n\n");
                }
            } else {
                final XmlBean xmlTag = Cu.firstOrNull(xmlTags);
                if (xmlTag != null) {
                    writer.write(TAB + xmlTag.getText() + "\n\n");
                }
            }
        }
    }

    @Override
    public void update(final AnActionEvent e) {
        final Editor editor = e.getData(PlatformDataKeys.EDITOR);
        e.getPresentation().setEnabled(IdeaUtils.getCurrentClass(editor) != null);
    }
}
