package ru.korgov.intellij.ltsc.properties;

import org.apache.batik.util.gui.xmleditor.XMLEditorKit;
import org.jetbrains.annotations.Nullable;
import ru.korgov.util.alias.Cf;
import ru.korgov.util.alias.Su;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Set;

/**
 * Author: Kirill Korgov (kirill@korgov.ru)
 * Date: 02.12.12
 */
public class PropertiesWindow {
    private JEditorPane beansFooterPane;
    private JEditorPane beansHeaderPane;
    private JButton setDefaultHeadersButton;
    private JButton okButton;
    private JButton cancelButton;
    private JCheckBox productionScope;
    private JCheckBox testScope;
    private JCheckBox librariesScope;
    private JCheckBox excludeBeansCheckbox;
    private JTextArea excludeBeansTextArea;
    private JRadioButton conflictsAutoOneRadBut;
    private JRadioButton conflictsAutoAllRadBut;
    private JRadioButton conflictsManualSelectRadBut;
    private JButton applyButton;
    private JPanel mainPanel;
    private JEditorPane customBeansMappingPane;
    private JCheckBox customBeansMappingCheckbox;
    private JCheckBox onlyVCSFilesCheckbox;

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public PropertiesWindow(final JFrame frame, final PropertiesService service) {

        setFormatters();

        loadCurrentProperties(service);

        setDefaultHeadersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                beansHeaderPane.setText(Constants.DEFAULT_HEADER);
                beansFooterPane.setText(Constants.DEFAULT_FOOTER);
            }
        });
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                saveCurrentSettings(service);
            }
        });
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                saveCurrentSettings(service);
                frame.dispose();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                frame.dispose();
            }
        });
    }

    private void setFormatters() {
        final XMLEditorKit xmlKit = new XMLEditorKit();
        customBeansMappingPane.setEditorKit(xmlKit);
        beansFooterPane.setEditorKit(xmlKit);
        beansHeaderPane.setEditorKit(xmlKit);
    }

    private void loadCurrentProperties(final PropertiesService service) {
        beansHeaderPane.setText(service.getBeansHeader());
        beansFooterPane.setText(service.getBeansFooter());
        selectScopes(service);
        excludeBeansTextArea.setText(Su.join(service.getExcludeBeans(), "\n"));
        selectConflictPolicity(service);
        customBeansMappingCheckbox.setSelected(service.getCustomBeansMappingStatus());
        excludeBeansCheckbox.setSelected(service.getExcludeBeansStatus());
        onlyVCSFilesCheckbox.setSelected(service.getOnlyVcsFilesStatus());
        customBeansMappingPane.setText(service.geCustomBeansMappingAsText());
    }

    private void selectConflictPolicity(final PropertiesService service) {
        final ConflictsPolicity policity = service.getConflictsPolicity();
        switch (policity) {
            case AUTO_ALL:
                conflictsAutoAllRadBut.setSelected(true);
                break;
            case AUTO_ONE:
                conflictsAutoOneRadBut.setSelected(true);
                break;
            case MANUAL_SELECT:
                conflictsManualSelectRadBut.setSelected(true);
                break;
        }
    }

    private void selectScopes(final PropertiesService service) {
        final Set<SearchScopeEnum> scopes = service.getSearchScope();
        productionScope.setSelected(scopes.contains(SearchScopeEnum.PRODUCTION));
        librariesScope.setSelected(scopes.contains(SearchScopeEnum.LIBRARIES));
        testScope.setSelected(scopes.contains(SearchScopeEnum.TEST));
    }

    private void saveCurrentSettings(final PropertiesService service) {
        service.setHeader(beansHeaderPane.getText());
        service.setFooter(beansFooterPane.getText());
        service.setExcludeBeans(Cf.list(excludeBeansTextArea.getText().split("\\s")));
        service.setSearchScope(getSelectedScopes());
        setConflictsPolicityIfExists(service);
        service.setCustomBeansMappingStatus(customBeansMappingCheckbox.isSelected());
        service.setExcludeBeansStatus(excludeBeansCheckbox.isSelected());
        service.setOnlyVcsFilesStatus(onlyVCSFilesCheckbox.isSelected());
        service.setCustomBeansMappingFromText(customBeansMappingPane.getText());
    }

    private void setConflictsPolicityIfExists(final PropertiesService service) {
        final ConflictsPolicity selectedPolicity = getSelectedPolicity();
        if (selectedPolicity != null) {
            service.setConflictsPolicity(selectedPolicity);
        }
    }

    @Nullable
    private ConflictsPolicity getSelectedPolicity() {
        if (conflictsAutoAllRadBut.isSelected()) {
            return ConflictsPolicity.AUTO_ALL;
        }

        if (conflictsAutoOneRadBut.isSelected()) {
            return ConflictsPolicity.AUTO_ONE;
        }

        if (conflictsManualSelectRadBut.isSelected()) {
            return ConflictsPolicity.MANUAL_SELECT;
        }
        return null;
    }

    private List<SearchScopeEnum> getSelectedScopes() {
        final List<SearchScopeEnum> out = Cf.newList();
        if (productionScope.isSelected()) {
            out.add(SearchScopeEnum.PRODUCTION);
        }

        if (testScope.isSelected()) {
            out.add(SearchScopeEnum.TEST);
        }

        if (librariesScope.isSelected()) {
            out.add(SearchScopeEnum.LIBRARIES);
        }

        return out;
    }


}
