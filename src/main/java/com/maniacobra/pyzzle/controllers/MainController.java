package com.maniacobra.pyzzle.controllers;

import com.maniacobra.pyzzle.properties.AppSettings;
import com.maniacobra.pyzzle.properties.FilePaths;
import com.maniacobra.pyzzle.resources.CodeRunner;
import com.maniacobra.pyzzle.models.ExerciseManager;
import com.maniacobra.pyzzle.properties.AppProperties;
import com.maniacobra.pyzzle.resources.PyzzAnalyzer;
import com.maniacobra.pyzzle.views.PyzzleMain;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
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
    private Button buttonIntro;
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
            PyzzAnalyzer.getInstance().AnalyzeFiles(files);
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
        System.out.println("Ok About");
    }

    public void initialize() {

        if (PyzzleMain.getFileArg() != null)
            if (manager.openFile(PyzzleMain.getFileArg(), borderPane))
                updateFileMenu();

        // Welcome
        if (AppSettings.getInstance().lastOpenedPath != null) {
            if (buttonIntro != null)
                buttonIntro.setVisible(false);
        }
        else {
            if (buttonContinue != null)
                buttonContinue.setVisible(false);
        }
    }
}
