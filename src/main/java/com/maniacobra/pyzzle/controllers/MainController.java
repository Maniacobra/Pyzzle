package com.maniacobra.pyzzle.controllers;

import com.maniacobra.pyzzle.models.ExerciseManager;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class MainController {

    private final ExerciseManager model = new ExerciseManager();
    @FXML
    private BorderPane borderPane;
    @FXML
    private MenuItem saveMenuItem;

    @FXML
    public void menuOpenFile() {

        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(Stage.getWindows().get(0));
        if (selectedFile != null)
            if (model.openFile(selectedFile, borderPane)) {
                saveMenuItem.setDisable(false);
            }
    }

    @FXML
    public void menuSave() {

        DirectoryChooser dirChooser = new DirectoryChooser();
        File selectedDir = dirChooser.showDialog(Stage.getWindows().get(0));
        if (selectedDir != null)
            model.saveData(selectedDir);
    }

    public void initialize() {
        //model.openFile(new File("C:\\Users\\hamon\\Desktop\\exercises\\logique.json"), borderPane);
    }
}
