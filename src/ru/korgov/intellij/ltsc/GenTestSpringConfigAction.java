package ru.korgov.intellij.ltsc;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.psi.PsiClass;
import com.intellij.psi.xml.XmlTag;
import ru.korgov.intellij.ltsc.properties.ConflictsPolicity;
import ru.korgov.intellij.ltsc.properties.PropertiesService;
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
            final PsiClass clazz = IdeaUtils.getCurrentClass(editor);
            final BeansFinder beansFinder = BeansFinder.getInstance(project, propertiesService);
            final Map<String, Set<XmlBean>> out = beansFinder.findForClass(clazz);
            createConfig(project, clazz, out, propertiesService);
        }
    }

    private void createConfig(final Project project, final PsiClass clazz, final Map<String, Set<XmlBean>> beanNameToTag, final PropertiesService propertiesService) {
        final ConflictsPolicity conflictsPolicity = propertiesService.getConflictsPolicity();

        final FileSaverDialog dialog = FileChooserFactory.getInstance().createSaveFileDialog(
                new FileSaverDescriptor("Save config to", ""), project);
        final VirtualFile baseDir = project.getBaseDir();
        final VirtualFileWrapper save = dialog.save(baseDir, "test-" + clazz.getName() + ".xml");
        if (save != null) {
            try {
                final Writer writer = new BufferedWriter(new FileWriter(save.getFile()));
                try {
                    writer.write(propertiesService.getBeansHeader() + "\n\n");
                    writeBeans(beanNameToTag, writer, conflictsPolicity);
                    writer.write("\n\n" + propertiesService.getBeansFooter());
                } finally {
                    writer.close();
                }
            } catch (final IOException ignored) {
            }
        }
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
