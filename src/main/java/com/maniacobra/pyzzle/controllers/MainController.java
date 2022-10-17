package com.maniacobra.pyzzle.controllers;

import com.maniacobra.pyzzle.models.CodeRunner;
import com.maniacobra.pyzzle.models.ExerciseManager;
import com.maniacobra.pyzzle.properties.AppIdentity;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class MainController {

    private final ExerciseManager manager = new ExerciseManager();
    @FXML
    private BorderPane borderPane;
    @FXML
    private MenuItem saveMenuItem;
    @FXML
    private MenuItem quickSaveMenuItem;

    private File saveFile = null;

    @FXML
    public void menuOpenFile() {

        if (!CodeRunner.getInstance().pythonTest())
            return;

        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Fichier Pyzzle", List.of(AppIdentity.extension, AppIdentity.openedExtension));
        fileChooser.setSelectedExtensionFilter(filter);
        File selectedFile = fileChooser.showOpenDialog(Stage.getWindows().get(0));
        if (selectedFile != null)
            if (manager.openFile(selectedFile, borderPane)) {
                saveMenuItem.setDisable(false);
                if (manager.getHasCompletion()) {
                    saveFile = selectedFile;
                    quickSaveMenuItem.setDisable(false);
                }
            }
    }

    @FXML
    public void menuSave() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(manager.getSaveFileName());
        saveFile = fileChooser.showSaveDialog(Stage.getWindows().get(0));
        menuQuickSave();
        if (saveFile != null)
            quickSaveMenuItem.setDisable(false);
    }

    @FXML
    public void menuQuickSave() {

        if (saveFile != null)
            manager.saveData(saveFile);
    }

    public void initialize() {
        //model.openFile(new File("C:\\Users\\hamon\\Desktop\\exercises\\logique.json"), borderPane);
    }
}
