package ru.korgov.intellij.lspr;


import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.korgov.intellij.lspr.properties.PersistentStateProperties;
import ru.korgov.intellij.lspr.properties.api.XProperties;
import ru.korgov.intellij.lspr.properties.ui.PropertiesWindow;

import javax.swing.JComponent;

/**
 * Author: Kirill Korgov (kirill@korgov.ru))
 * Date: 20.02.13 2:33
 */

public class LazySpringProjectComponent implements Configurable, ProjectComponent {

    private PropertiesWindow propertiesWindow;
    private final XProperties properties;

    public LazySpringProjectComponent(final Project project) {
        this.properties = PersistentStateProperties.getInstance(project);
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "LazySpring";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Override
    public JComponent createComponent() {
        if (propertiesWindow == null) {
            propertiesWindow = new PropertiesWindow();
        }
//        reset();
        return propertiesWindow.getMainPanel();
    }

    @Override
    public boolean isModified() {
        return propertiesWindow.isModified(properties);
    }

    @Override
    public void apply() throws ConfigurationException {
        propertiesWindow.saveCurrentSettings(properties);
    }

    @Override
    public void reset() {
        propertiesWindow.loadCurrentProperties(properties);
    }

    @Override
    public void disposeUIResources() {
        propertiesWindow = null;
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "LazySpringProjectComponent";
    }

    @Override
    public void projectOpened() {
    }

    @Override
    public void projectClosed() {
    }

    @Override
    public void initComponent() {
    }

    @Override
    public void disposeComponent() {
    }
}
