package ru.korgov.intellij.lspr.actions.result;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsConfiguration;
import com.intellij.openapi.vcs.checkin.CheckinEnvironment;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import org.jetbrains.annotations.Nullable;
import ru.korgov.intellij.lspr.impl.DependencyTag;
import ru.korgov.intellij.lspr.properties.api.XProperties;
import ru.korgov.intellij.util.IdeaUtils;
import ru.korgov.util.ObjectUtils;
import ru.korgov.util.alias.Cf;
import ru.korgov.util.alias.Cu;
import ru.korgov.util.collection.Option;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Author: Kirill Korgov (korgov@yandex-team.ru)
 * Date: 21.06.13 1:01
 */
public class XmlConfigSaver {

    private static final Set<FileStatus> VERSIONED_STATUSES = Cf.set(FileStatus.ADDED, FileStatus.MODIFIED);

    private XmlConfigSaver() {
    }

    public static void save(final Project project,
                            final VirtualFile file,
                            final String defaultFilename,
                            final Map<String, Set<DependencyTag>> beanNameToTag,
                            final XProperties properties) {

        final String savePathSuffix = properties.getSavePathSuffix();
        final Application application = ApplicationManager.getApplication();

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
                            virtualFile.setBinaryContent(XmlConfigBuilder.buildFileBody(beanNameToTag, properties).getBytes(Charset.forName("UTF-8")));
                            checkInVcs(virtualFile);
                        }
                    }

                    private void checkInVcs(final VirtualFile virtualFile) {
                        final ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(project);
                        final AbstractVcs vcs = vcsManager.getVcsFor(virtualFile);
                        final FileStatus fileStatus = FileStatusManager.getInstance(project).getStatus(virtualFile);
                        if (vcs != null && !VERSIONED_STATUSES.contains(fileStatus)) {
                            final CheckinEnvironment checkinEnvironment = vcs.getCheckinEnvironment();
                            if (checkinEnvironment != null) {
                                final Collection<VirtualFile> filesToProcess = selectFilesToProcess(virtualFile, vcsManager, vcs);
                                if (!Cu.isEmpty(filesToProcess)) {
                                    checkinEnvironment.scheduleUnversionedFilesForAddition(Cf.newList(filesToProcess));
                                }
                            }
                        }
                    }

                    private Collection<VirtualFile> selectFilesToProcess(final VirtualFile virtualFile, final ProjectLevelVcsManager vcsManager, final AbstractVcs vcs) {
                        final AbstractVcsHelper helper = AbstractVcsHelper.getInstance(project);
                        final String vcsName = vcs.getName();
                        return helper.selectFilesToProcess(
                                Cf.list(virtualFile),
                                getVcsConfirmTitle(vcsName, true), null,
                                getVcsConfirmTitle(vcsName, false), getSinglePromptTemplate(vcsName),
                                vcsManager.getStandardConfirmation(VcsConfiguration.StandardConfirmation.ADD, vcs)
                        );
                    }
                });
            }
        });

    }

    private static String getSinglePromptTemplate(final String vcsName) {
        return "Do you want to add the following file to " + vcsName + "?\n{0}\n\nIf you say No, you can still add it later manually.";
    }

    private static String getVcsConfirmTitle(final String vcsName, final boolean multiple) {
        return "Add file" + (multiple ? "s" : "") + " to " + vcsName + "?";
    }

    private static VirtualFileWrapper saveDialog(final Project project, final VirtualFile initialFile, final String savePathSuffix, final String defaultFilename) {
        final FileSaverDialog dialog = FileChooserFactory.getInstance().createSaveFileDialog(
                new FileSaverDescriptor("Save config to", "", "xml"), project);
        final VirtualFile moduleDir = getFileModuleDir(project, initialFile);
        final VirtualFile path = ObjectUtils.avoidNull(addPathSuffix(moduleDir, savePathSuffix), moduleDir);
        return dialog.save(path, defaultFilename);
    }

    @Nullable
    private static VirtualFile addPathSuffix(final VirtualFile moduleDir, final String suffix) {
        final File ioFile = new File(addPathSuffix(moduleDir.getPath(), suffix, File.separator));
        return new VirtualFileWrapper(ioFile).getVirtualFile();
    }

    private static String addPathSuffix(final String path, final String suffix, final String sep) {
        return stripEndSep(path, sep) + sep + stripStartSep(suffix, sep);
    }

    private static String stripEndSep(final String path, final String sep) {
        return path.endsWith(sep) ? path.substring(0, path.length() - sep.length()) : path;
    }

    private static String stripStartSep(final String path, final String sep) {
        return path.startsWith(sep) ? path.substring(sep.length()) : path;
    }

    private static VirtualFile getFileModuleDir(final Project project, final VirtualFile file) {
        final Option<Module> classModule = IdeaUtils.getFileModule(project, file);
        if (classModule.hasValue()) {
            final VirtualFile moduleFile = classModule.getValue().getModuleFile();
            if (moduleFile != null) {
                return moduleFile.getParent();
            }
        }
        return project.getBaseDir();
    }
}
