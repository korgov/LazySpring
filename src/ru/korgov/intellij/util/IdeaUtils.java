package ru.korgov.intellij.util;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.korgov.util.alias.Cf;
import ru.korgov.util.collection.Option;

import java.util.List;

/**
 * Author: Kirill Korgov (kirill@korgov.ru)
 * Date: 01.12.12
 */
public class IdeaUtils {

    private IdeaUtils() {
    }

    @Nullable
    public static PsiClass getCurrentClass(final Editor editor) {
        if (editor != null) {
            final Project project = editor.getProject();
            if (project != null) {
                final PsiManager psiManager = PsiManager.getInstance(project);
                final FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
                final VirtualFile vFile = fileDocumentManager.getFile(editor.getDocument());
                return findClassAt(psiManager, vFile, editor.getCaretModel().getOffset());
            }
        }
        return null;
    }

    public static Option<Module> getClassModule(final Project project, final PsiClass clazz) {
        final VirtualFile classFile = clazz.getContainingFile().getVirtualFile();
        if (classFile != null) {
            return getFileModule(project, classFile);
        }
        return Option.nothing();
    }

    public static Option<Module> getFileModule(final Project project, final @NotNull VirtualFile file) {
        for (final Module module : ModuleManager.getInstance(project).getModules()) {
            if (module.getModuleScope().contains(file)) {
                return Option.just(module);
            }
        }
        return Option.nothing();
    }

    @Nullable
    public static PsiClass findClassAt(final PsiManager psiManager, final VirtualFile vFile, final int offset) {
        if (vFile != null) {
            return findClassAt(psiManager.findFile(vFile), offset);
        }
        return null;
    }

    @Nullable
    public static PsiClass findClassAt(final PsiFile psiFile, final int offset) {
        if (psiFile instanceof PsiJavaFile) {
            final PsiJavaFile javaFile = (PsiJavaFile) psiFile;
            final PsiElement element = javaFile.findElementAt(offset);
            return nearestClassParent(element);
        }
        return null;
    }

    @Nullable
    public static PsiClass nearestClassParent(PsiElement element) {
        while (!(element instanceof PsiClass) && element != null) {
            element = element.getParent();
        }
        return element == null ? null : (PsiClass) element;
    }

    public static List<PsiField> extractAnnotatedFields(final PsiClass clazz, final List<String> annotationNames) {
        final List<PsiField> out = Cf.newList();
        if (clazz != null) {
            for (final PsiField field : clazz.getAllFields()) {
                final PsiAnnotation annonation = AnnotationUtil.findAnnotation(field, annotationNames);
                if (annonation != null) {
                    out.add(field);
                }
            }
        }
        return out;
    }
}

