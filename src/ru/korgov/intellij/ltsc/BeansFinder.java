package ru.korgov.intellij.ltsc;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
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
import ru.korgov.intellij.ltsc.properties.PropertiesService;
import ru.korgov.intellij.ltsc.properties.SearchScopeEnum;
import ru.korgov.intellij.util.IdeaUtils;
import ru.korgov.util.Filter;
import ru.korgov.util.alias.Cf;
import ru.korgov.util.alias.Cu;
import ru.korgov.util.alias.Fu;
import ru.korgov.util.alias.Su;

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

    private static final String SETTER_PREFIX = "set";

    private static final Fu<PsiField, BeanDesc> FIELD_TO_BEAN_DESC = new Fu<PsiField, BeanDesc>() {
        @Override
        public BeanDesc apply(final PsiField v) {
            return new BeanDesc(v.getType(), v.getName());
        }
    };
    private final GlobalSearchScope prodScope;
    private final PsiSearchHelper helper;
    private final PsiElementFactory elementFactory;
    private final GlobalSearchScope xmlScope;
    private final JavaPsiFacade javaPsiFacede;
    private final PropertiesService propertiesService;
    private final Project project;
    private final Set<String> excludeBeans = Cf.newSet();
    private final Map<String, XmlBean> customBeans = Cf.newMap();
    private final FileStatusManager fileStatusManager;
    private final boolean onlyVcsFiles;

    private BeansFinder(final Project project, final PropertiesService propertiesService) {
        this.project = project;
        fileStatusManager = FileStatusManager.getInstance(project);
        this.propertiesService = propertiesService;
        this.helper = PsiSearchHelper.SERVICE.getInstance(project);
        this.javaPsiFacede = JavaPsiFacade.getInstance(project);
        this.elementFactory = javaPsiFacede.getElementFactory();
        this.prodScope = getSearchScope(project, propertiesService);
        this.xmlScope = GlobalSearchScope.getScopeRestrictedByFileTypes(prodScope, XmlFileType.INSTANCE);
        this.excludeBeans.addAll(propertiesService.getCheckedExcludeBeans());
        this.customBeans.putAll(propertiesService.getCheckedCustomBeansMappingAsBeans());
        this.onlyVcsFiles = propertiesService.getOnlyVcsFilesStatus();
    }

    private GlobalSearchScope getSearchScope(final Project project, final PropertiesService propsService) {
        final Set<SearchScopeEnum> searchScopes = propsService.getSearchScope();
        final ProjectScopeBuilder projectScopeBuilder = ProjectScopeBuilder.getInstance(project);
        GlobalSearchScope scope = projectScopeBuilder.buildAllScope();

        if (!searchScopes.contains(SearchScopeEnum.TEST)) {
            scope = scope.intersectWith(notScope(projectTestScope(project)));
        }

        if (!searchScopes.contains(SearchScopeEnum.PRODUCTION)) {
            scope = scope.intersectWith(notScope(projectProductionScope(project)));
        }

        if (!searchScopes.contains(SearchScopeEnum.LIBRARIES)) {
            scope = scope.intersectWith(notScope(projectScopeBuilder.buildLibrariesScope()));
        }

        return scope;
    }

    public static BeansFinder getInstance(final Project project, final PropertiesService propertiesService) {
        return new BeansFinder(project, propertiesService);
    }

    public Map<String, Set<XmlBean>> findBeans(final List<BeanDesc> beans) {
        final Map<String, Set<XmlBean>> out = Cf.newMap();
        for (final BeanDesc bean : beans) {
            collectForBean(out, bean);
        }
        return out;
    }

    private Map<String, Set<XmlBean>> findForClass(final PsiClass clazz,
                                                   final Map<String, Set<XmlBean>> alreadyFounded,
                                                   final Set<String> processedClassess) {
        System.out.println("find for class: " + clazz.getName());
        final List<BeanDesc> beansToFind = Cu.join(extractAutowiredFields(clazz), extractSetters(clazz));
        if (!beansToFind.isEmpty()) {
            final Map<String, Set<XmlBean>> beans = findBeans(filterBeansToResolve(alreadyFounded, beansToFind));
            appendAll(alreadyFounded, beans);
            final List<PsiClass> psiClasses = extractBeanClasses(beans);
            for (final PsiClass psiClass : psiClasses) {
                final String qualifiedName = psiClass.getQualifiedName();
                if (!processedClassess.contains(qualifiedName)) {
                    appendAll(alreadyFounded, findForClass(psiClass, alreadyFounded, processedClassess));
                    processedClassess.add(qualifiedName);
                }
            }
            return alreadyFounded;
        }
        return Collections.emptyMap();
    }

    private List<BeanDesc> extractSetters(final PsiClass clazz) {
        final PsiMethod[] methods = clazz.getMethods();
        final List<BeanDesc> out = Cf.newList();
        for (final PsiMethod method : methods) {
            final String methodName = method.getName();
            if (methodName.startsWith("set")) {
                final PsiParameter[] parameters = method.getParameterList().getParameters();
                if (parameters.length == 1) {
                    final PsiType type = parameters[0].getType();
                    final String beanName = beanNameFromSetter(methodName);
                    if (beanName != null) {
                        out.add(new BeanDesc(type, beanName));
                    }
                }
            }
        }
        System.out.println("Setters: " + out);
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

    private List<BeanDesc> extractAutowiredFields(final PsiClass clazz) {
        final List<PsiField> fields = IdeaUtils.extractAnnotatedFields(clazz, Cf.list(AT_AUTOWIRED));
        final List<BeanDesc> out = Cu.map(fields, FIELD_TO_BEAN_DESC);
        System.out.println("Autowired: " + out);
        return out;
    }

    private List<PsiClass> extractBeanClasses(final Map<String, Set<XmlBean>> beans) {
        final List<PsiClass> out = Cf.newList();
        for (final XmlBean bean : Cu.join(beans.values())) {
            out.addAll(extractBeanClasses(bean));
        }
        return out;
    }

    private List<PsiClass> extractBeanClasses(final XmlBean bean) {
        final XmlTag tag = bean.getTag();
        return extractBeanClasses(tag);
    }

    private List<PsiClass> extractBeanClasses(final XmlTag tag) {
        final List<PsiClass> out = Cf.newList();
        if ("bean".equals(tag.getName())) {
            final String className = tag.getAttributeValue("class");
            if (className != null) {
                final PsiClass[] classes = javaPsiFacede.findClasses(className, prodScope);
                out.addAll(Cf.list(classes));
            } else {
                System.out.println("Can't find class by name: " + className);
            }
        }
        for (final XmlTag subTag : tag.getSubTags()) {
            out.addAll(extractBeanClasses(subTag));
        }
        return out;
    }

    private List<BeanDesc> filterBeansToResolve(final Map<String, Set<XmlBean>> alreadyFounded, final List<BeanDesc> beansToFind) {
        return Cu.filter(beansToFind, new Filter<BeanDesc>() {
            @Override
            public boolean fits(final BeanDesc bean) {
                final String beanName = bean.getName();
                return !alreadyFounded.containsKey(beanName);
            }
        });
    }

    private void collectForBean(final Map<String, Set<XmlBean>> out, final BeanDesc bean) {
        final String beanId = bean.getName();
        if (!out.containsKey(beanId) && !excludeBeans.contains(beanId)) {
            System.out.println("for bean: " + beanId);
            if (customBeans.containsKey(beanId)) {
                append(out, beanId, customBeans.get(beanId));
                return;
            }
            final PsiType beanClass = bean.getPsiType();
            final Map<String, Set<XmlBean>> foundedForBean = Cf.newMap();
            helper.processUsagesInNonJavaFiles(beanId, getProcessor(foundedForBean, beanId, beanClass), xmlScope);

            appendAll(out, foundedForBean);
            final List<XmlTag> foundedTags = Cu.map(Cu.join(foundedForBean.values()), XmlBean.TO_TAG);
            final Set<BeanDesc> refs = Cf.newSet(Cu.join(extractRefs(foundedTags), extractAliasRefs(foundedTags, bean)));
            System.out.println("refs: " + refs);
            for (final BeanDesc ref : refs) {
                collectForBean(out, ref);
            }

        }
    }

    private List<BeanDesc> extractAliasRefs(final List<XmlTag> foundedTags, final BeanDesc bean) {
        final List<BeanDesc> out = Cf.newList();
        for (final XmlTag foundedTag : foundedTags) {
            if ("alias".equals(foundedTag.getName())) {
                final String aliasBeanName = foundedTag.getAttributeValue("name");
                if (!Su.isEmpty(aliasBeanName)) {
                    out.add(new BeanDesc(bean.getPsiType(), aliasBeanName));
                }
            }
        }
        return out;
    }

    private List<BeanDesc> extractRefs(final List<XmlTag> beans) {
        final List<BeanDesc> out = Cf.newList();
        for (final XmlTag bean : beans) {
            out.addAll(extractRefs(bean));
        }
        return out;
    }

    private List<BeanDesc> extractRefs(final XmlTag bean) {
        final List<BeanDesc> out = Cf.newList();
        for (final XmlTag subTag : bean.getSubTags()) {
            final String subTagName = subTag.getName();
            addIfNotNull(out, subTag.getAttributeValue("ref".equals(subTagName) ? "bean" : "ref"));
            out.addAll(extractRefs(subTag));
        }
        return out;
    }

    private void addIfNotNull(final List<BeanDesc> out, final String refBean) {
        if (refBean != null) {
            out.add(new BeanDesc(null, refBean));
        }
    }

    private void appendAll(final Map<String, Set<XmlBean>> out, final Map<String, Set<XmlBean>> foundedForBean) {
        for (final Map.Entry<String, Set<XmlBean>> kvs : foundedForBean.entrySet()) {
            appendAll(out, kvs.getKey(), kvs.getValue());
        }
    }

    private void appendAll(final Map<String, Set<XmlBean>> out, final String key, final Set<XmlBean> values) {
        for (final XmlBean v : values) {
            append(out, key, v);
        }
    }

    private PsiNonJavaFileReferenceProcessor getProcessor(final Map<String, Set<XmlBean>> out, final String beanId, final @Nullable PsiType beanClass) {
        return new PsiNonJavaFileReferenceProcessor() {
            @Override
            public boolean process(final PsiFile fileWithBean, final int startOffset, final int endOffset) {
                final String fileName = fileWithBean.getName();
                if (XmlFileType.INSTANCE.equals(fileWithBean.getFileType())) {
                    final VirtualFile virtualFile = fileWithBean.getVirtualFile();
                    if (virtualFile != null
                            && "xml".equals(virtualFile.getExtension()) && isUnderVcsCheck(virtualFile)) {

                        System.out.println("found word: " + beanId + " in file: " + fileName);

                        final XmlFile xmlFile = (XmlFile) fileWithBean;
                        final XmlTag beanTag = getBeanAt(xmlFile, startOffset);
                        if (beanTag != null && isOurBean(beanTag, beanId, beanClass)) {
                            append(out, beanId, XmlBean.from(beanTag, xmlFile));
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
    private XmlTag getBeanAt(final XmlFile xmlFile, final int startOffset) {
        PsiElement maybeBean = xmlFile.findElementAt(startOffset);
        if (maybeBean != null) {
            final XmlTag rootTag = xmlFile.getRootTag();
            while (true) {
                if (isBeanXml(maybeBean)) {
                    return (XmlTag) maybeBean;
                } else {
                    final PsiElement parent = maybeBean.getParent();
                    if (parent != null && !parent.equals(rootTag)) {
                        maybeBean = parent;
                    } else {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    private boolean isBeanXml(final PsiElement elem) {
        if (elem instanceof XmlTag) {
            final XmlTag beanTag = (XmlTag) elem;
            final String tagName = beanTag.getName();
            return "bean".equals(tagName) || "alias".equals(tagName);
        }
        return false;
    }

    private void append(final Map<String, Set<XmlBean>> m, final String k, final XmlBean v) {
        Set<XmlBean> values = m.get(k);
        if (values == null) {
            values = Cf.newSet();
            m.put(k, values);
        }
        values.add(v);
    }

    private boolean isOurBean(final XmlTag tag, final String beanId, final @Nullable PsiType beanClassType) {
        final String tagName = tag.getName();
        if ("bean".equals(tagName)) {
            if (beanId.equals(tag.getAttributeValue("id")) || beanId.equals(tag.getAttributeValue("name"))) {
                final String clazzName = tag.getAttributeValue("class");
                if (!(clazzName == null)) {
                    final PsiClassType tagBeanType = elementFactory.createTypeByFQClassName(clazzName);
                    return beanClassType == null || beanClassType.isAssignableFrom(tagBeanType);
                }
            }
        } else if ("alias".equals(tagName)) {
            System.out.println("found alias: " + tag.getText());
            return beanId.equals(tag.getAttributeValue("alias"));
        }
        return false;
    }

    public Map<String, Set<XmlBean>> findForClass(final PsiClass clazz) {
        return findForClass(clazz, Cf.<String, Set<XmlBean>>newMap(), Cf.<String>newSet());
    }
}
