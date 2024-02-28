package com.maniacobra.pyzzle.controllers;

import com.maniacobra.pyzzle.models.ExerciseManager;
import com.maniacobra.pyzzle.properties.AppSettings;
import com.maniacobra.pyzzle.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class IntroController {

    @FXML
    Label labelRestriction;
    @FXML
    Label labelPackName;
    @FXML
    Label labelAuthor;
    @FXML
    TextField textfName;
    @FXML
    Label labelBrowse;
    @FXML
    TextField textfSavePath;
    @FXML
    Button buttonBrowse;
    @FXML
    Label labelExam;
    @FXML
    Button buttonStart;
    @FXML
    Label labelAttempts;

    private ExerciseManager manager;
    private boolean forceInfos = false;

    public void init(ExerciseManager manager, String packName, String author, boolean forceInfos, boolean examMode, int attemptsCount) {
        this.manager = manager;
        this.forceInfos = forceInfos;

        String userName = AppSettings.getInstance().userName;
        textfName.setText(userName);
        manager.setUserName(userName);
        if (!forceInfos)
            labelRestriction.setVisible(false);
        if (userName.isEmpty()) {
            labelBrowse.setDisable(forceInfos);
            buttonBrowse.setDisable(forceInfos);
        }
        buttonStart.setDisable(forceInfos);
        labelExam.setVisible(examMode);

        labelPackName.setVisible(!packName.isEmpty());
        labelPackName.setText(packName);
        labelAuthor.setVisible(!author.isEmpty());
        labelAuthor.setText("Pack d'exercices créé par " + author);
        labelAttempts.setText("Ouverture n°" + attemptsCount);
    }

    @FXML
    public void updateTextInput() {
        manager.setUserName(textfName.getText());
        boolean emptyName = textfName.getText().length() == 0;
        if (forceInfos) {
            labelBrowse.setDisable(emptyName);
            textfSavePath.setDisable(emptyName);
            buttonBrowse.setDisable(emptyName);
            buttonStart.setDisable(emptyName || !manager.hasSaveFile());
        }
    }

    @FXML
    public void browseFilesAndSave() {

        updateSettings();
        // File
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(manager.getSaveFileSuggestion());
        File file = fileChooser.showSaveDialog(Stage.getWindows().get(0));
        if (file != null) {
            if (manager.saveData(file))
                textfSavePath.setText(file.getPath());
            else {
                Utils.systemAlert(Alert.AlertType.ERROR, "Erreur inconnue de Pyzzle", """
                    Une erreur inconnue est survenue.
                    Essayez de redémarrer le logiciel.""");
            }
        }
        if (forceInfos) {
            buttonStart.setDisable(!manager.hasUserName() || !manager.hasSaveFile());
        }
    }

    @FXML
    public void startPack() {
        updateSettings();
        manager.startFirstExercise();
    }

    private void updateSettings() {
        AppSettings.getInstance().userName = textfName.getText();
        manager.setUserName(textfName.getText());
        AppSettings.getInstance().save();
    }
}
