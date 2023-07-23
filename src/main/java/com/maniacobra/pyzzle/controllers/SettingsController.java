package com.maniacobra.pyzzle.controllers;

import com.maniacobra.pyzzle.properties.AppSettings;
import com.maniacobra.pyzzle.properties.FilePaths;
import com.maniacobra.pyzzle.resources.CodeRunner;
import com.maniacobra.pyzzle.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.util.Arrays;

public class SettingsController {

    @FXML
    private Button buttonReturn;
    @FXML
    private CheckBox cbAutoSave;
    @FXML
    private CheckBox cbDragAndDrop;
    @FXML
    private RadioButton radioAutoArgs;
    @FXML
    private RadioButton radioCustomArgs;
    @FXML
    private TextField textfArgs;
    @FXML
    private Label labelArgs;

    private Pane returnPane = null;
    private BorderPane mainPane = null;

    @FXML
    public void quitSettings() {

        updateSettings();
        CodeRunner.getInstance().pythonTest();

        if (mainPane == null)
            return;
        mainPane.setCenter(returnPane);
        System.gc();
    }

    public void init(Pane returnPane, BorderPane mainPane, String returnText) {

        this.returnPane = returnPane;
        this.mainPane = mainPane;

        buttonReturn.setText(returnText);
        updateUI();

        if (FilePaths.getInstance().getPythonExePath() == null)
            radioAutoArgs.setText("Trouver Python automatiquement sur le système");
    }

    @FXML
    public void updateSettings() {

        AppSettings settings = AppSettings.getInstance();
        settings.autoSave = cbAutoSave.isSelected();
        settings.dragAndDrop = cbDragAndDrop.isSelected();
        settings.autoArgs = radioAutoArgs.isSelected();
        settings.terminalArgs.clear();
        settings.terminalArgs.addAll(Arrays.asList(textfArgs.getText().split(" ")));
        CodeRunner.getInstance().resetCommand();
        settings.save();

        updateUI();
    }

    @FXML
    public void checkPyzzle() {
        updateSettings();
        if (CodeRunner.getInstance().pythonTest()) {
            Utils.systemAlert(Alert.AlertType.INFORMATION, "Pyzzle fonctionne !", "L'exécution de Python par Pyzzle fonctionne correctement.\n" +
                    "Arguments utilisés :\n\n" + String.join(" ", CodeRunner.getInstance().getCommand()));
        }
    }

    private void updateUI() {

        AppSettings settings = AppSettings.getInstance();
        cbAutoSave.setSelected(settings.autoSave);
        cbDragAndDrop.setSelected(settings.dragAndDrop);

        radioAutoArgs.setSelected(settings.autoArgs);
        radioCustomArgs.setSelected(!settings.autoArgs);
        textfArgs.setDisable(settings.autoArgs);
        labelArgs.setDisable(settings.autoArgs);

        textfArgs.setText(String.join(" ", settings.terminalArgs));
    }
}
