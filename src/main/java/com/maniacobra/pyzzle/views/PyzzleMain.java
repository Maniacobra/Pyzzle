package com.maniacobra.pyzzle.views;

import com.maniacobra.pyzzle.controllers.MainController;
import com.maniacobra.pyzzle.resources.IdsRegistry;
import com.maniacobra.pyzzle.properties.FilePaths;
import com.maniacobra.pyzzle.properties.AppSettings;
import com.maniacobra.pyzzle.resources.CodeRunner;
import com.maniacobra.pyzzle.properties.AppProperties;
import com.maniacobra.pyzzle.utils.Utils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class PyzzleMain extends Application {

    private static File fileArg;
    private static boolean shiftPressed = false;

    public static File getFileArg() {
        return fileArg;
    }

    public static boolean isShiftPressed() {
        return shiftPressed;
    }

    @Override
    public void start(Stage stage) throws IOException {

        // Singletons
        if (!FilePaths.load()) {
            Utils.systemAlert(Alert.AlertType.ERROR, "Pyzzle : Impossible de démarrer",
                    "Pyzzle n'est pas parvenu à préparer son démarrage correctement, vérifiez l'installation et les permissions du logiciel.");
            System.exit(1);
        }
        AppSettings.getInstance().load();
        IdsRegistry.getInstance().load();
        // Tests
        CodeRunner.getInstance().pythonTest();
        // Assertions
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);

        // Create controller + load FXML
        MainController controller = new MainController();
        FXMLLoader rootFxmlLoader = new FXMLLoader(PyzzleMain.class.getResource("main-view.fxml"));
        FXMLLoader welcomeFxmlLoader = new FXMLLoader(PyzzleMain.class.getResource("welcome-view.fxml"));
        rootFxmlLoader.setController(controller);
        welcomeFxmlLoader.setController(controller);

        // Setup stage
        BorderPane root = rootFxmlLoader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(PyzzleMain.class.getResource("style.css")).toExternalForm());
        stage.setMaximized(true);
        stage.setTitle(AppProperties.name);
        stage.setScene(scene);

        // Keys
        scene.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.SHIFT))
                shiftPressed = true;
        });
        scene.setOnKeyReleased(ke -> {
            if (ke.getCode().equals(KeyCode.SHIFT))
                shiftPressed = false;
        });

        // Welcome area
        Pane welcomePane = welcomeFxmlLoader.load();
        root.setCenter(welcomePane);

        // Show
        stage.show();
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            System.out.println("Pyzzle opened with args :");
            for (String arg : args)
                System.out.println(arg);
            fileArg = new File(args[0]);
        }
        launch();
    }
}