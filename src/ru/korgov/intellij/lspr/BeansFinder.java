package ru.korgov.intellij.lspr;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScopeBuilder;
import com.intellij.psi.search.PsiNonJavaFileReferenceProcessor;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.Nullable;
import ru.korgov.intellij.lspr.model.Dependency;
import ru.korgov.intellij.lspr.model.DependencyTag;
import ru.korgov.intellij.lspr.model.DependencyTagDescriptor;
import ru.korgov.intellij.lspr.properties.PropertiesService;
import ru.korgov.intellij.lspr.properties.SearchScopeEnum;
import ru.korgov.intellij.util.IdeaUtils;
import ru.korgov.util.Filter;
import ru.korgov.util.alias.Cf;
import ru.korgov.util.alias.Cu;
import ru.korgov.util.alias.Fu;
import ru.korgov.util.alias.Su;
import ru.korgov.util.collection.Option;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.intellij.psi.search.GlobalSearchScope.notScope;
import static com.intellij.psi.search.GlobalSearchScopes.projectProductionScope;
import static com.intellij.psi.search.GlobalSearchScopes.projectTestScope;

/**
 * Author: Kirill Korgov (kirill@korgov.ru)
 * Date: 01.12.12
 */
public class BeansFinder {
    private static final String AT_AUTOWIRED = "org.springframework.beans.factory.annotation.Autowired";
    private static final String AT_REQUIRED = "org.springframework.beans.factory.annotation.Required";

    private static final String SETTER_PREFIX = "set";
    private static final List<String> REF_ATTRS = Cf.list("ref", "bean", "parent");

    private static final Fu<PsiField, Dependency> FIELD_TO_BEAN_DESC = new Fu<PsiField, Dependency>() {
        @Override
        public Dependency apply(final PsiField v) {
            return Dependency.byNameAndType(v.getName(), v.getType());
        }
    };
    private final GlobalSearchScope prodScope;
    private final PsiSearchHelper helper;
    private final DependencyTagDescriptor dependencyTagDescriptor;
    private final GlobalSearchScope xmlScope;
    private final JavaPsiFacade javaPsiFacede;
    //    private final Project project;
//    private final PropertiesService propertiesService;
    private final Set<String> excludeBeans = Cf.newSet();
    private final Map<String, DependencyTag> customBeans = Cf.newMap();
    private final FileStatusManager fileStatusManager;
    private final boolean onlyVcsFiles;
    private final ProgressIndicator indicator;

    private BeansFinder(final Project project, final PropertiesService propertiesService, final ProgressIndicator indicator) {
        this.indicator = indicator;
//        this.project = project;
//        this.propertiesService = propertiesService;
        fileStatusManager = FileStatusManager.getInstance(project);
        this.helper = PsiSearchHelper.SERVICE.getInstance(project);
        this.javaPsiFacede = JavaPsiFacade.getInstance(project);
        this.prodScope = getSearchScope(project, propertiesService);
        this.xmlScope = GlobalSearchScope.getScopeRestrictedByFileTypes(prodScope, XmlFileType.INSTANCE);
        this.excludeBeans.addAll(propertiesService.getCheckedExcludeBeans());
        this.onlyVcsFiles = propertiesService.getOnlyVcsFilesStatus();
        this.dependencyTagDescriptor = new DependencyTagDescriptor(prodScope, javaPsiFacede);
        this.customBeans.putAll(propertiesService.getCheckedCustomBeansMappingAsBeans(dependencyTagDescriptor));
    }

    private GlobalSearchScope getSearchScope(final Project prj, final PropertiesService propsService) {
        final Set<SearchScopeEnum> searchScopes = propsService.getSearchScope();
        final ProjectScopeBuilder projectScopeBuilder = ProjectScopeBuilder.getInstance(prj);
        GlobalSearchScope scope = projectScopeBuilder.buildAllScope();

        if (!searchScopes.contains(SearchScopeEnum.TEST)) {
            scope = scope.intersectWith(notScope(projectTestScope(prj)));
        }

        if (!searchScopes.contains(SearchScopeEnum.PRODUCTION)) {
            scope = scope.intersectWith(notScope(projectProductionScope(prj)));
        }

        if (!searchScopes.contains(SearchScopeEnum.LIBRARIES)) {
            scope = scope.intersectWith(notScope(projectScopeBuilder.buildLibrariesScope()));
        }

        return scope;
    }

    public static BeansFinder getInstance(final Project project, final PropertiesService propertiesService, final ProgressIndicator indicator) {
        return new BeansFinder(project, propertiesService, indicator);
    }

    public Map<String, Set<DependencyTag>> resolveDependencies(final List<Dependency> dependencies) {
        final Map<String, Set<DependencyTag>> out = Cf.newMap();
        for (final Dependency dependency : dependencies) {
            collectForDependency(out, dependency);
        }
        return out;
    }

    private Map<String, Set<DependencyTag>> findForClass(final PsiClass clazz,
                                                         final Map<String, Set<DependencyTag>> alreadyResolved,
                                                         final Set<String> processedClassess) {
//        System.out.println("find for class: " + clazz.getName());
        final List<Dependency> classDependencies = extractClassDependencies(clazz);
        if (!classDependencies.isEmpty()) {
            final List<Dependency> filteredDependencies = filterUnresolvedDependencies(alreadyResolved, classDependencies);
            final Map<String, Set<DependencyTag>> depTags = resolveDependencies(filteredDependencies);
            Cu.appendAllToMultiSet(alreadyResolved, depTags);
            for (final PsiClass psiClass : extractClasses(depTags)) {
                final String qualifiedName = psiClass.getQualifiedName();
                if (!processedClassess.contains(qualifiedName)) {
                    Cu.appendAllToMultiSet(alreadyResolved, findForClass(psiClass, alreadyResolved, processedClassess));
                    processedClassess.add(qualifiedName);
                }
            }
            return alreadyResolved;
        }
        return Collections.emptyMap();
    }

    private List<Dependency> extractClassDependencies(final PsiClass clazz) {
        return Cu.join(extractFromAutowiredFields(clazz), extractFromSetters(clazz));
    }

    private List<Dependency> extractFromSetters(final PsiClass clazz) {
        final PsiMethod[] methods = clazz.getAllMethods();
        final List<Dependency> out = Cf.newList();
        for (final PsiMethod method : methods) {
            final String methodName = method.getName();
            if (methodName.startsWith("set")) {
                final PsiParameter[] parameters = method.getParameterList().getParameters();
                if (parameters.length == 1) {
                    final PsiType type = parameters[0].getType();
                    final String beanName = beanNameFromSetter(methodName);
                    if (beanName != null) {
                        out.add(Dependency.byNameAndType(beanName, type));
                    }
                }
            }
        }
//        System.out.println("Setters: " + out);
        return out;
    }

    @Nullable
    private String beanNameFromSetter(final String methodName) {
        if (methodName.startsWith(SETTER_PREFIX)) {
            final String capName = methodName.substring(SETTER_PREFIX.length());
            if (!Su.isEmpty(capName)) {
                return capName.substring(0, 1).toLowerCase() + capName.substring(1);
            }
        }
        return null;
    }

    private List<Dependency> extractFromAutowiredFields(final PsiClass clazz) {
        final List<PsiField> fields = IdeaUtils.extractAnnotatedFields(clazz, Cf.list(AT_AUTOWIRED));
        return Cu.map(fields, FIELD_TO_BEAN_DESC);
        //        System.out.println("Autowired: " + out);
    }

    private Set<PsiClass> extractClasses(final Map<String, Set<DependencyTag>> depTags) {
        return Cf.newSet(DependencyTag.flatMapToClasses(Cu.join(depTags.values())));
    }

    private List<Dependency> filterUnresolvedDependencies(final Map<String, Set<DependencyTag>> alreadyFounded, final List<Dependency> beansToFind) {
        return Cu.filter(beansToFind, new Filter<Dependency>() {
            @Override
            public boolean fits(final Dependency bean) {
                final String beanName = bean.getName();
                return !alreadyFounded.containsKey(beanName);
            }
        });
    }

    private void collectForDependency(final Map<String, Set<DependencyTag>> out, final Dependency dependency) {
        final String beanId = dependency.getName();
        if (!out.containsKey(beanId) && !excludeBeans.contains(beanId)) {
//            System.out.println("for bean: " + beanId);

            final Map<String, Set<DependencyTag>> foundedForBean = resolveDependencyWithCustom(dependency);
            Cu.appendAllToMultiSet(out, foundedForBean);

            final Set<Dependency> refs = extractRefs(Cu.join(foundedForBean.values()));
//            System.out.println("refs: " + refs);
            for (final Dependency ref : refs) {
                collectForDependency(out, ref);
            }
        }
    }

    private Map<String, Set<DependencyTag>> resolveDependencyWithCustom(final Dependency dependency) {
        final String dependencyName = dependency.getName();
        return customBeans.containsKey(dependencyName)
                ? Collections.singletonMap(dependencyName, Cf.set(customBeans.get(dependencyName)))
                : resolveDependency(dependency);
    }

    private Map<String, Set<DependencyTag>> resolveDependency(final Dependency dependency) {
        final Map<String, Set<DependencyTag>> out = Cf.newMap();
        helper.processUsagesInNonJavaFiles(dependency.getName(), getXmlProcessor(out, dependency), xmlScope);
        return out;
    }

    private Set<Dependency> extractRefs(final Iterable<DependencyTag> dependencyTags) {
        return Cf.newSet(DependencyTag.flatMapToRefs(dependencyTags));
    }

    private PsiNonJavaFileReferenceProcessor getXmlProcessor(final Map<String, Set<DependencyTag>> out, final Dependency dependency) {
        return new PsiNonJavaFileReferenceProcessor() {
            @Override
            public boolean process(final PsiFile fileWithBean, final int startOffset, final int endOffset) {
                if (XmlFileType.INSTANCE.equals(fileWithBean.getFileType())) {
                    final VirtualFile virtualFile = fileWithBean.getVirtualFile();
                    if (virtualFile != null
                            && "xml".equals(virtualFile.getExtension()) && isUnderVcsCheck(virtualFile)) {
//                        System.out.println("found word: " + beanId + " in file: " + fileWithBean.getName());
                        final String beanName = dependency.getName();
                        indicator.setText2(beanName + " found in " + virtualFile.getName());

                        final XmlFile xmlFile = (XmlFile) fileWithBean;
                        final XmlTag beanTag = getXmlTagAt(xmlFile, startOffset);
                        final Option<DependencyTag> dependencyTag = dependencyTagDescriptor.buildFromTag(beanTag, dependency);
                        if (dependencyTag.hasValue()) {
                            Cu.appendToMultiSet(out, beanName, dependencyTag.getValue());
                        }
                    }
                }
                return true;
            }
        };
    }

    private boolean isUnderVcsCheck(final VirtualFile virtualFile) {
        if (onlyVcsFiles) {
            final FileStatus status = fileStatusManager.getStatus(virtualFile);
            return !FileStatus.UNKNOWN.equals(status) && !FileStatus.IGNORED.equals(status);
        }
        return true;
    }

    @Nullable
    private XmlTag getXmlTagAt(final XmlFile xmlFile, final int startOffset) {
        PsiElement element = xmlFile.findElementAt(startOffset);
        if (element != null) {
            final XmlTag rootTag = xmlFile.getRootTag();
            while (true) {
                if (isDependencyTag(element)) {
                    return (XmlTag) element;
                } else {
                    final PsiElement parent = element.getParent();
                    if (parent != null && !parent.equals(rootTag)) {
                        element = parent;
                    } else {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    private boolean isDependencyTag(final PsiElement elem) {
        return elem instanceof XmlTag && dependencyTagDescriptor.isDependencyTag((XmlTag) elem);
    }

    public Map<String, Set<DependencyTag>> findForClass(final PsiClass clazz) {
        return findForClass(clazz, Cf.<String, Set<DependencyTag>>newMap(), Cf.<String>newSet());
    }
}
