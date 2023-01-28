package com.maniacobra.pyzzle.controllers;

import com.maniacobra.pyzzle.resources.CodeRunner;
import com.maniacobra.pyzzle.models.ExerciseManager;
import com.maniacobra.pyzzle.properties.AppProperties;
import com.maniacobra.pyzzle.views.PyzzleMain;
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

    @FXML
    public void menuOpenFile() {

        if (!CodeRunner.getInstance().pythonTest())
            return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(AppProperties.packFolder));
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Fichier Pyzzle", List.of(AppProperties.extension, AppProperties.openedExtension));
        fileChooser.setSelectedExtensionFilter(filter);
        File selectedFile = fileChooser.showOpenDialog(Stage.getWindows().get(0));
        if (selectedFile != null)
            if (manager.openFile(selectedFile, borderPane))
                packLoaded();
    }

    @FXML
    public void menuSave() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(manager.getSaveFileName());
        File file = fileChooser.showSaveDialog(Stage.getWindows().get(0));
        if (file != null) {
            manager.saveData(file);
            quickSaveMenuItem.setDisable(false);
        }
    }

    @FXML
    public void menuQuickSave() {
        manager.saveData();
    }

    @FXML
    public void menuAdvPref() {
        //
    }

    public void initialize() {

        if (PyzzleMain.fileArg != null)
            if (manager.openFile(PyzzleMain.fileArg, borderPane))
                packLoaded();
    }

    private void packLoaded() {

        saveMenuItem.setDisable(false);
        if (manager.hasSaveFile()) {
            quickSaveMenuItem.setDisable(false);
        }
    }
}
