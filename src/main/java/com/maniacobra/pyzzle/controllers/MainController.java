package com.maniacobra.pyzzle.controllers;

import com.maniacobra.pyzzle.properties.AppSettings;
import com.maniacobra.pyzzle.properties.FilePaths;
import com.maniacobra.pyzzle.resources.CodeRunner;
import com.maniacobra.pyzzle.models.ExerciseManager;
import com.maniacobra.pyzzle.properties.AppProperties;
import com.maniacobra.pyzzle.resources.PyzzAnalyzer;
import com.maniacobra.pyzzle.utils.Popups;
import com.maniacobra.pyzzle.utils.Utils;
import com.maniacobra.pyzzle.views.PyzzleMain;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@SuppressWarnings("unused")
public class MainController {

    private final ExerciseManager manager = new ExerciseManager();

    @FXML
    private BorderPane borderPane;
    @FXML
    private MenuItem menuItemSave;
    @FXML
    private MenuItem menuItemQuickSave;
    @FXML
    private Button buttonContinue;

    @FXML
    public void menuOpenFile() {

        CodeRunner.getInstance().pythonTest();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(FilePaths.getInstance().getPackFile());
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Fichier Pyzzle", List.of(AppProperties.extension, AppProperties.openedExtension));
        fileChooser.setSelectedExtensionFilter(filter);
        File selectedFile = fileChooser.showOpenDialog(Stage.getWindows().get(0));
        if (selectedFile != null)
            if (manager.openFile(selectedFile, borderPane))
                updateFileMenu();
    }

    @FXML
    public void menuSave() {

        if (manager.isExamMode())
            return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(manager.getSaveFileSuggestion());
        File file = fileChooser.showSaveDialog(Stage.getWindows().get(0));
        if (file != null) {
            manager.saveData(file);
            menuItemQuickSave.setDisable(false);
        }
    }

    @FXML
    public void updateFileMenu() {
        menuItemSave.setDisable(manager.isExamMode() || !manager.isLoaded());
        menuItemQuickSave.setDisable(!manager.hasSaveFile());
    }

    @FXML
    public void menuQuickSave() {
        manager.saveData();
    }

    @FXML
    public void menuAnalysis() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionnez les fichiers " + AppProperties.openedExtension);
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Fichier Pyzzle ouvert", List.of(AppProperties.openedExtension)));
        List<File> files = fileChooser.showOpenMultipleDialog(Stage.getWindows().get(0));
        if (!files.isEmpty()) {
            if (!PyzzAnalyzer.getInstance().AnalyzeFiles(files)) {
                Utils.systemAlert(Alert.AlertType.ERROR, "Erreur de l'analyse",
                        "Impossible de sauvegarder le fichier .csv ou l'analyse a échouée pour tous les fichiers.");
            }
        }
    }

    @FXML
    public void menuSettings() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(PyzzleMain.class.getResource("settings-view.fxml"));
            Pane pane = fxmlLoader.load();
            SettingsController controller = fxmlLoader.getController();
            String returnText;
            Pane returnPane;
            if (manager.isLoaded()) {
                returnText = "Retour à l'exercice";
                returnPane = manager.getCurrentPane();
            }
            else {
                returnText = "Retour à l'écran d'accueil";
                returnPane = (Pane) borderPane.getCenter();
            }
            controller.init(returnPane, borderPane, returnText, manager.isExamMode());
            borderPane.setCenter(pane);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void menuAbout() {
        Popups.showPopup("À propos", "Pyzzle par Maniacobra", """
                
                Version Prototype 1.0
                [date]
                
                Site web : https://maniacobra.com
                Contact mail : maniacobra@orange.fr
                
                Logiciel programmé en Java (JavaFX), avec intégration d'interpréteur Python.
                
                Ce programme est la version prototype d'un concept de logiciel éducatif pour apprendre le Python.
                La version complète du logiciel requiert de nombreuses nouveautés et améliorations, le développement est à ce jour interrompu.
                
                Consultez https://maniacobra.com/pyzzle pour vérifier s'il existe une mise à jour.
                """, 550, 16, 55);
    }

    @FXML
    public void editorInfos() {
        Popups.showPopup("Informations sur l'éditeur", "Pyzzle ne propose pas encore d'éditeur graphique", """
                
                La création et modification d'exercices peut se faire via l'écriture de fichiers au format .json, qui sont convertis en .pyzl par Pyzzle à leur chargement.
                
                Allez sur le site maniacobra.com/pyzzle et téléchargez le pack de fichiers .pyzzle
                """, 650, 16, 65);
    }

    @FXML
    public void openLastFile() {
        if (AppSettings.getInstance().lastOpenedPath == null)
            return;

        File file = new File(AppSettings.getInstance().lastOpenedPath);
        if (manager.openFile(file, borderPane))
            updateFileMenu();
    }

    public void initialize() {

        if (PyzzleMain.getFileArg() != null)
            if (manager.openFile(PyzzleMain.getFileArg(), borderPane))
                updateFileMenu();

        // Welcome
        if (AppSettings.getInstance().lastOpenedPath == null && buttonContinue != null)
            buttonContinue.setDisable(true);
    }
}
