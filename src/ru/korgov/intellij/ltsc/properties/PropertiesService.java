package ru.korgov.intellij.ltsc.properties;

import com.intellij.ide.util.PropertiesComponent;
import ru.korgov.util.alias.Cf;
import ru.korgov.util.alias.Cu;
import ru.korgov.util.alias.Fu;
import ru.korgov.util.alias.Su;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Author: Kirill Korgov (kirill@korgov.ru)
 * Date: 02.12.12
 */
public class PropertiesService {

    private static final Fu<String, String> TRIM = new Fu<String, String>() {
        @Override
        public String apply(final String v) {
            return v.trim();
        }
    };
    private PropertiesComponent propertiesComponent;

    private PropertiesService(final PropertiesComponent propertiesComponent) {
        this.propertiesComponent = propertiesComponent;
    }


    public static PropertiesService getInstance(final PropertiesComponent propertiesComponent) {
        return new PropertiesService(propertiesComponent);
    }

    public List<String> getExcludeBeans() {
        final String excludeBeansLines = propertiesComponent.getValue(Constants.PROP_EXCLUDE_BEANS, Constants.DEFAULT_EXCLUDE_BEANS);
        return Cf.list(excludeBeansLines.split("\\n"));
    }

    public ConflictsPolicity getConflictsPolicity() {
        final String policityName = propertiesComponent.getValue(Constants.PROP_CONFLICTS_POLICITY, ConflictsPolicity.AUTO_ONE.name());
        return ConflictsPolicity.valueOf(policityName);
    }

    public Set<SearchScopeEnum> getSearchScope() {
        final Set<SearchScopeEnum> out = Cf.newSet();
        for (final SearchScopeEnum scopeEnum : SearchScopeEnum.values()) {
            final String scopeStatus = propertiesComponent.getValue(Constants.PROP_SCOPE_PREFIX + scopeEnum.name(), Constants.PROP_SCOPE_FALSE);
            if (Constants.PROP_SCOPE_TRUE.equals(scopeStatus)) {
                out.add(scopeEnum);
            }
        }
        return out.isEmpty() ? Cf.set(SearchScopeEnum.LIBRARIES, SearchScopeEnum.PRODUCTION) : out;
    }

    public String getBeansHeader() {
        return propertiesComponent.getValue(Constants.PROP_BEANS_HEADER, Constants.DEFAULT_HEADER);
    }

    public String getBeansFooter() {
        return propertiesComponent.getValue(Constants.PROP_BEANS_FOOTER, Constants.DEFAULT_FOOTER);
    }


    public void setExcludeBeans(final List<String> beans) {
        propertiesComponent.setValue(Constants.PROP_EXCLUDE_BEANS, Su.join(Cu.map(beans, TRIM), "\n"));
    }

    public void setConflictsPolicity(final ConflictsPolicity policity) {
        propertiesComponent.setValue(Constants.PROP_CONFLICTS_POLICITY, policity.name());
    }

    public void setHeader(final String header){
        propertiesComponent.setValue(Constants.PROP_BEANS_HEADER, header);
    }

    public void setFooter(final String footer){
        propertiesComponent.setValue(Constants.PROP_BEANS_FOOTER, footer);
    }

    public void setSearchScope(final Collection<SearchScopeEnum> scopes) {
        final Set<SearchScopeEnum> scopesSet = Cf.newSet(scopes);
        for (final SearchScopeEnum someScope : SearchScopeEnum.values()) {
            final String someScopeValue = scopesSet.contains(someScope) ? Constants.PROP_SCOPE_TRUE : Constants.PROP_SCOPE_FALSE;
            propertiesComponent.setValue(Constants.PROP_SCOPE_PREFIX + someScope.name(), someScopeValue);
        }
    }
}
