package ru.korgov.intellij.lspr.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import ru.korgov.intellij.lspr.properties.ui.PropertiesWindow;

import javax.swing.*;

/**
 * Author: Kirill Korgov (kirill@korgov.ru)
 * Date: 02.12.12
 */
@Deprecated
public class ShowPropertiesAction extends AnAction {

    private JFrame createPropsWindow(final Project project) {
        final JFrame frame = new JFrame("Spring config generation properties");
        frame.setContentPane(new PropertiesWindow().getMainPanel());
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(false);
        return frame;
    }

    @Override
    public void actionPerformed(final AnActionEvent e) {
        final Project project = e.getData(PlatformDataKeys.PROJECT);
        final JFrame frame = createPropsWindow(project);
        frame.setVisible(true);
    }
}
