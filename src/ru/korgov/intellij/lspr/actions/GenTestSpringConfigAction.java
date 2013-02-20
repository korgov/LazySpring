package ru.korgov.intellij.lspr.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.psi.PsiClass;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.korgov.intellij.lspr.impl.BeansFinder;
import ru.korgov.intellij.lspr.LazySpringProjectComponent;
import ru.korgov.intellij.lspr.impl.DependencyTag;
import ru.korgov.intellij.lspr.properties.api.ConflictsPolicity;
import ru.korgov.intellij.lspr.properties.api.XProperties;
import ru.korgov.intellij.util.IdeaUtils;
import ru.korgov.util.ObjectUtils;
import ru.korgov.util.alias.Cu;
import ru.korgov.util.collection.Option;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
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
        final XProperties XPropertiesService = LazySpringProjectComponent.getInstance(project).getState();
//        final XProperties XPropertiesService = SimpleProperties.getInstance(PropertiesComponent.getInstance(project), project);
        if (project != null && editor != null) {
            new Task.Backgroundable(project, "Resolving dependencies..", true) {
                @Override
                public void run(@NotNull final ProgressIndicator indicator) {
                    indicator.setText("Searching beans..");
                    indicator.setFraction(0.0);
                    final PsiClass clazz = getCurrentClass(editor);

                    indicator.setFraction(0.2);
                    final BeansFinder beansFinder = getBeansFinder(indicator, project, XPropertiesService);
                    indicator.setFraction(0.4);
                    final Map<String, Set<DependencyTag>> requiredBeans = findForClass(clazz, beansFinder);
                    indicator.setFraction(0.8);
                    indicator.setText("Saving config file..");
                    createConfig(project, clazz, requiredBeans, XPropertiesService);
                    indicator.setFraction(1.0);
                }
            }.setCancelText("Cancel task.").queue();
        }
    }

    private Map<String, Set<DependencyTag>> findForClass(final PsiClass clazz, final BeansFinder beansFinder) {
        return ApplicationManager.getApplication().runReadAction(new Computable<Map<String, Set<DependencyTag>>>() {
            @Override
            public Map<String, Set<DependencyTag>> compute() {
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

    private BeansFinder getBeansFinder(final ProgressIndicator indicator, final Project project, final XProperties XPropertiesService) {
        return ApplicationManager.getApplication().runReadAction(new Computable<BeansFinder>() {
            @Override
            public BeansFinder compute() {
                return BeansFinder.getInstance(project, XPropertiesService, indicator);
            }
        });
    }

    private void createConfig(final Project project, final PsiClass clazz, final Map<String, Set<DependencyTag>> beanNameToTag, final XProperties XPropertiesService) {
        final ConflictsPolicity conflictsPolicity = XPropertiesService.getConflictsPolicity();
        final String beansHeader = XPropertiesService.getBeansHeader();
        final String beansFooter = XPropertiesService.getBeansFooter();
        final String savePathSuffix = XPropertiesService.getSavePathSuffix();
        final Application application = ApplicationManager.getApplication();

        //todo: ask add to VCS after save
        application.invokeLater(new Runnable() {
            @Override
            public void run() {
                final VirtualFileWrapper save = saveDialog(project, clazz, savePathSuffix);
                if (save != null) {
                    doWrite(save);
                }
            }

            private void doWrite(final VirtualFileWrapper save) {
                application.runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            writeVirtFile();
                        } catch (final IOException ignored) {
                        }
                    }

                    private void writeVirtFile() throws IOException {
                        final VirtualFile virtualFile = save.getVirtualFile(true);
                        if (virtualFile != null) {
                            virtualFile.setBinaryContent(buildFileBody().getBytes(Charset.forName("UTF-8")));
                        }
                    }

                    private String buildFileBody() {
                        final StringBuilder sb = new StringBuilder(256);
                        sb.append(beansHeader).append("\n\n");
                        appendBeans(beanNameToTag, sb, conflictsPolicity);
                        sb.append("\n\n").append(beansFooter);
                        return sb.toString();
                    }
                });
            }
        });

    }

    private VirtualFileWrapper saveDialog(final Project project, final PsiClass clazz, final String savePathSuffix) {
        final FileSaverDialog dialog = FileChooserFactory.getInstance().createSaveFileDialog(
                new FileSaverDescriptor("Save config to", "", "xml"), project);
        final VirtualFile moduleDir = getClassModuleDir(project, clazz);
        final VirtualFile path = ObjectUtils.avoidNull(addPathSuffix(moduleDir, savePathSuffix), moduleDir);
        return dialog.save(path, "test-" + clazz.getName() + ".xml");
    }

    @Nullable
    private VirtualFile addPathSuffix(final VirtualFile moduleDir, final String suffix) {
        final File ioFile = new File(addPathSuffix(moduleDir.getPath(), suffix, File.separator));
        return new VirtualFileWrapper(ioFile).getVirtualFile();
    }

    private String addPathSuffix(final String path, final String suffix, final String sep) {
        return stripEndSep(path, sep) + sep + stripStartSep(suffix, sep);
    }

    private String stripEndSep(final String path, final String sep) {
        return path.endsWith(sep) ? path.substring(0, path.length() - sep.length()) : path;
    }

    private String stripStartSep(final String path, final String sep) {
        return path.startsWith(sep) ? path.substring(sep.length()) : path;
    }

    private VirtualFile getClassModuleDir(final Project project, final PsiClass clazz) {
        final Option<Module> classModule = IdeaUtils.getClassModule(project, clazz);
        if (classModule.hasValue()) {
            final VirtualFile moduleFile = classModule.getValue().getModuleFile();
            if (moduleFile != null) {
                return moduleFile.getParent();
            }
        }
        return project.getBaseDir();
    }


    private void appendBeans(final Map<String, Set<DependencyTag>> beanNameToTag, final StringBuilder sb, final ConflictsPolicity conflictsPolicity) {
        for (final Set<DependencyTag> tags : beanNameToTag.values()) {
            for (final DependencyTag dependencyTag : getTagsToWrite(conflictsPolicity, tags)) {
                appendTag(sb, dependencyTag.getTag());
            }
        }
    }

    private Iterable<DependencyTag> getTagsToWrite(final ConflictsPolicity conflictsPolicity, final Set<DependencyTag> tags) {
        return conflictsPolicity == ConflictsPolicity.AUTO_ALL ? tags : Cu.firstOrNothing(tags);
    }

    private void appendTag(final StringBuilder sb, final XmlTag xmlTag) {
        sb.append(TAB).append(xmlTag.getText()).append("\n\n");
    }

    @Override
    public void update(final AnActionEvent e) {
        final Editor editor = e.getData(PlatformDataKeys.EDITOR);
        e.getPresentation().setEnabled(IdeaUtils.getCurrentClass(editor) != null);
    }
}
