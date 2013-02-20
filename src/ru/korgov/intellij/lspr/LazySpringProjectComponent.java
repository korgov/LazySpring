package ru.korgov.intellij.lspr;


import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.korgov.intellij.lspr.properties.PersistentStateProperties;
import ru.korgov.intellij.lspr.properties.ui.PropertiesWindow;

import javax.swing.*;

/**
 * Author: Kirill Korgov (kirill@korgov.ru))
 * Date: 20.02.13 2:33
 */
@State(
        name = LazySpringProjectComponent.NAME,
        storages = {
                @Storage(id = "default", file = "$PROJECT_FILE$"),
                @Storage(id = "dir", file = "$PROJECT_CONFIG_DIR$" + "/" + LazySpringProjectComponent.NAME + ".xml", scheme = StorageScheme.DIRECTORY_BASED)
        }
)
public class LazySpringProjectComponent implements Configurable, ProjectComponent, PersistentStateComponent<PersistentStateProperties> {
    public static final String NAME = "LazySpringConfiguration";

    private Icon icon;
    private PropertiesWindow propertiesWindow;
    private PersistentStateProperties state = new PersistentStateProperties();


    public static LazySpringProjectComponent getInstance(final Project project) {
        return ServiceManager.getService(project, LazySpringProjectComponent.class);
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "LazySpring";
    }

    @Override
    public Icon getIcon() {
        if (icon == null) {
            icon = IconLoader.getIcon("/ru/korgov/intellij/lazy.png");
        }
        return icon;
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
        return propertiesWindow.getMainPanel();
    }

    @Override
    public boolean isModified() {
        return propertiesWindow.isModified(state);
    }

    @Override
    public void apply() throws ConfigurationException {
        propertiesWindow.saveCurrentSettings(state);
    }

    @Override
    public void reset() {
        propertiesWindow.loadCurrentProperties(state);
    }

    @Override
    public void disposeUIResources() {
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

    @NotNull
    @Override
    public String getComponentName() {
        return NAME;
    }

    @Override
    public PersistentStateProperties getState() {
        return state;
    }

    @Override
    public void loadState(final PersistentStateProperties serializedState) {
        this.state = serializedState;
    }
}
