package ru.korgov.intellij.lspr.actions.generators;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.korgov.intellij.util.IdeaUtils;

/**
 * Author: Kirill Korgov (korgov@yandex-team.ru)
 * Date: 21.06.13 0:58
 */
public class GeneratorsFactory {
    private GeneratorsFactory(){
    }

    @Nullable
    public static Generator getGenerator(final Project project, final @NotNull VirtualFile initialFile, final int offset) {
        return ApplicationManager.getApplication().runReadAction(new Computable<Generator>() {
            @Override
            public Generator compute() {
                final PsiManager psiManager = PsiManager.getInstance(project);
                final PsiFile psiFile = psiManager.findFile(initialFile);
                if (psiFile != null) {
                    if (isXmlFile(initialFile)) {
                        final XmlFile xmlFile = (XmlFile) psiFile;
                        return new FromXmlGenerator(xmlFile, project);
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

    private static boolean isXmlFile(final VirtualFile initialFile) {
        return XmlFileType.INSTANCE.equals(initialFile.getFileType())
                && "xml".equals(initialFile.getExtension());
    }
}
