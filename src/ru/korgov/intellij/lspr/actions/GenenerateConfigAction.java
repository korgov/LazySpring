package ru.korgov.intellij.lspr.actions;

import com.intellij.ide.highlighter.XmlFileType;
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
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.korgov.intellij.lspr.actions.generators.Generator;
import ru.korgov.intellij.lspr.actions.generators.SingleClassGenerator;
import ru.korgov.intellij.lspr.actions.generators.XmlConfigRebuilder;
import ru.korgov.intellij.lspr.impl.BeansFinder;
import ru.korgov.intellij.lspr.impl.DependencyTag;
import ru.korgov.intellij.lspr.properties.PersistentStateProperties;
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
public class GenenerateConfigAction extends AnAction {

    private static final String TAB = "    ";

    @Override
    public void actionPerformed(final AnActionEvent e) {
        final Project project = e.getData(PlatformDataKeys.PROJECT);
        final Editor editor = e.getData(PlatformDataKeys.EDITOR);
        final XProperties properties = PersistentStateProperties.getInstance(project);
        if (project != null && editor != null) {
            new Task.Backgroundable(project, "Resolving dependencies..", true) {
                @Override
                public void run(@NotNull final ProgressIndicator indicator) {
                    indicator.setText("Searching beans..");
                    indicator.setFraction(0.0);
                    final VirtualFile initialFile = getCurrentFile(editor);
                    if (initialFile != null) {
                        indicator.setFraction(0.2);
                        final BeansFinder beansFinder = getBeansFinder(indicator, project, initialFile, properties);
                        indicator.setFraction(0.4);
                        final Generator generator = getGenerator(project, initialFile, editor.getCaretModel().getOffset());
                        if (generator != null) {
                            final Map<String, Set<DependencyTag>> requiredBeans = generator.find(beansFinder);
                            indicator.setFraction(0.8);
                            indicator.setText("Saving config file..");
                            createConfig(project, initialFile, generator.getDefaultFilename(), requiredBeans, properties);
                        }
                    }
                    indicator.setFraction(1.0);
                }
            }.setCancelText("Cancel task.").queue();
        }
    }

    @Nullable
    private Generator getGenerator(final Project project, final @NotNull VirtualFile initialFile, final int offset) {
        return ApplicationManager.getApplication().runReadAction(new Computable<Generator>() {
            @Override
            public Generator compute() {
                final PsiManager psiManager = PsiManager.getInstance(project);
                final PsiFile psiFile = psiManager.findFile(initialFile);
                if (psiFile != null) {
                    if (isXmlFile(initialFile)) {
                        final XmlFile xmlFile = (XmlFile) psiFile;
                        return new XmlConfigRebuilder(xmlFile, project);
                    } else {
                        final PsiClass clazz = IdeaUtils.findClassAt(psiFile, offset);
                        if (clazz != null) {
                            return new SingleClassGenerator(clazz);
                        }
                    }
                }
                return null;
            }
        });
    }

    private boolean isXmlFile(final VirtualFile initialFile) {
        return XmlFileType.INSTANCE.equals(initialFile.getFileType())
                && "xml".equals(initialFile.getExtension());
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

    private void createConfig(final Project project,
                              final VirtualFile file,
                              final String defaultFilename,
                              final Map<String, Set<DependencyTag>> beanNameToTag,
                              final XProperties XPropertiesService) {
        final ConflictsPolicity conflictsPolicity = XPropertiesService.getConflictsPolicity();
        final String beansHeader = XPropertiesService.getBeansHeader();
        final String beansFooter = XPropertiesService.getBeansFooter();
        final String savePathSuffix = XPropertiesService.getSavePathSuffix();
        final Application application = ApplicationManager.getApplication();

        //todo: ask add to VCS after save
        application.invokeLater(new Runnable() {
            @Override
            public void run() {
                final VirtualFileWrapper save = saveDialog(project, file, savePathSuffix, defaultFilename);
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

    private VirtualFileWrapper saveDialog(final Project project, final VirtualFile initialFile, final String savePathSuffix, final String defaultFilename) {
        final FileSaverDialog dialog = FileChooserFactory.getInstance().createSaveFileDialog(
                new FileSaverDescriptor("Save config to", "", "xml"), project);
        final VirtualFile moduleDir = getFileModuleDir(project, initialFile);
        final VirtualFile path = ObjectUtils.avoidNull(addPathSuffix(moduleDir, savePathSuffix), moduleDir);
        return dialog.save(path, defaultFilename);
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

    private VirtualFile getFileModuleDir(final Project project, final VirtualFile file) {
        final Option<Module> classModule = IdeaUtils.getFileModule(project, file);
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
        //todo: fix for class!
        e.getPresentation().setEnabled(isActionEnabled(e));
    }

    private boolean isActionEnabled(final AnActionEvent e) {
        final Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor != null) {
            final VirtualFile initialFile = getCurrentFile(editor);
            if (initialFile != null) {
                final Project project = editor.getProject();
                if (project != null) {
                    final Generator generator = getGenerator(project, initialFile, editor.getCaretModel().getOffset());
                    return generator != null;
                }
            }
        }
        return false;
    }
}
