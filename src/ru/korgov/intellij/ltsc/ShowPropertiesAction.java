package ru.korgov.intellij.ltsc;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import ru.korgov.intellij.ltsc.properties.PropertiesService;
import ru.korgov.intellij.ltsc.properties.PropertiesWindow;

import javax.swing.*;

/**
 * Author: Kirill Korgov (kirill@korgov.ru)
 * Date: 02.12.12
 */
public class ShowPropertiesAction extends AnAction {

    private JFrame createPropsWindow() {
        final JFrame frame = new JFrame("Spring config generation properties");
        final PropertiesService service = PropertiesService.getInstance(PropertiesComponent.getInstance());
        frame.setContentPane(new PropertiesWindow(frame, service).getMainPanel());
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(false);
        return frame;
    }

    @Override
    public void actionPerformed(final AnActionEvent e) {
        final JFrame frame = createPropsWindow();
        frame.setVisible(true);
    }
}
