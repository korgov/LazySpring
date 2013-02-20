package ru.korgov.intellij.lspr.properties.ui;

import org.apache.batik.util.gui.xmleditor.XMLEditorKit;
import org.jetbrains.annotations.Nullable;
import ru.korgov.intellij.lspr.properties.api.ConflictsPolicity;
import ru.korgov.intellij.lspr.properties.api.Constants;
import ru.korgov.intellij.lspr.properties.api.SearchScopeEnum;
import ru.korgov.intellij.lspr.properties.api.XProperties;
import ru.korgov.util.alias.Cf;
import ru.korgov.util.alias.Su;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

/**
 * Author: Kirill Korgov (kirill@korgov.ru)
 * Date: 02.12.12
 */
public class PropertiesWindow {
    private JEditorPane beansFooterPane;
    private JEditorPane beansHeaderPane;
    private JButton setDefaultHeadersButton;
    private JCheckBox productionScope;
    private JCheckBox testScope;
    private JCheckBox librariesScope;
    private JCheckBox excludeBeansCheckbox;
    private JEditorPane excludeBeansTextArea;
    private JRadioButton conflictsAutoOneRadBut;
    private JRadioButton conflictsAutoAllRadBut;
    private JRadioButton conflictsManualSelectRadBut;
    private JPanel mainPanel;
    private JEditorPane customBeansMappingPane;
    private JCheckBox customBeansMappingCheckbox;
    private JCheckBox onlyVCSFilesCheckbox;
    private JEditorPane priorityFilePathsTextArea;

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public PropertiesWindow() {

        setFormatters();

        setDefaultHeadersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                beansHeaderPane.setText(Constants.DEFAULT_HEADER);
                beansFooterPane.setText(Constants.DEFAULT_FOOTER);
            }
        });
    }

    private void setFormatters() {
        final XMLEditorKit xmlKit = new XMLEditorKit();
        customBeansMappingPane.setEditorKit(xmlKit);
        beansFooterPane.setEditorKit(xmlKit);
        beansHeaderPane.setEditorKit(xmlKit);
    }

    public void loadCurrentProperties(final XProperties properties) {
        if (properties != null) {
            beansHeaderPane.setText(properties.getBeansHeader());
            beansFooterPane.setText(properties.getBeansFooter());
            selectScopes(properties);
            excludeBeansTextArea.setText(Su.join(properties.getExcludeBeans(), "\n"));
            priorityFilePathsTextArea.setText(Su.join(properties.getPriorityPaths(), "\n"));
            selectConflictPolicity(properties);
            customBeansMappingCheckbox.setSelected(properties.isCustomBeansMappingUsed());
            excludeBeansCheckbox.setSelected(properties.isExcludeBeansUsed());
            onlyVCSFilesCheckbox.setSelected(properties.isOnlyVcsFiles());
            customBeansMappingPane.setText(properties.getCustomBeansMapping());
        }
    }

    private void selectConflictPolicity(final XProperties properties) {
        final ConflictsPolicity policity = properties.getConflictsPolicity();
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

    private void selectScopes(final XProperties properties) {
        final Set<SearchScopeEnum> scopes = properties.getSearchScope();
        productionScope.setSelected(scopes.contains(SearchScopeEnum.PRODUCTION));
        librariesScope.setSelected(scopes.contains(SearchScopeEnum.LIBRARIES));
        testScope.setSelected(scopes.contains(SearchScopeEnum.TEST));
    }

    public void saveCurrentSettings(final XProperties service) {
        service.setBeansHeader(beansHeaderPane.getText());
        service.setBeansFooter(beansFooterPane.getText());
        service.setExcludeBeans(Cf.list(excludeBeansTextArea.getText().split("\\s")));
        service.setPriorityPaths(Cf.list(priorityFilePathsTextArea.getText().split("\\s")));
        service.setSearchScope(getSelectedScopes());
        setConflictsPolicityIfExists(service);
        service.setCustomBeansMappingUsed(customBeansMappingCheckbox.isSelected());
        service.setExcludeBeansUsed(excludeBeansCheckbox.isSelected());
        service.setOnlyVcsFiles(onlyVCSFilesCheckbox.isSelected());
        service.setCustomBeansMapping(customBeansMappingPane.getText());
    }

    private void setConflictsPolicityIfExists(final XProperties service) {
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

    private Set<SearchScopeEnum> getSelectedScopes() {
        final Set<SearchScopeEnum> out = Cf.newSet();
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


    public boolean isModified(final XProperties state) {
        final XProperties currentState = state.getDefaultInstance();
        saveCurrentSettings(currentState);
        return !state.equals(currentState);
    }
}
