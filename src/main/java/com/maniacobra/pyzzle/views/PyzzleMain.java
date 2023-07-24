package com.maniacobra.pyzzle.views;

import com.maniacobra.pyzzle.properties.FilePaths;
import com.maniacobra.pyzzle.properties.AppSettings;
import com.maniacobra.pyzzle.resources.CodeRunner;
import com.maniacobra.pyzzle.properties.AppProperties;
import com.maniacobra.pyzzle.utils.Utils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
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

        // Setup
        if (!FilePaths.load()) {
            Utils.systemAlert(Alert.AlertType.ERROR, "Pyzzle : Impossible de démarrer",
                    "Pyzzle n'est pas parvenu à préparer son démarrage correctement, vérifiez l'installation et les permissions du logiciel.");
            System.exit(1);
        }
        AppSettings.getInstance().load();
        // Tests
        CodeRunner.getInstance().pythonTest();

        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);

        // Setup
        FXMLLoader fxmlLoader = new FXMLLoader(PyzzleMain.class.getResource("main-view.fxml"));
        Parent root = fxmlLoader.load();
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

/* TO-DO
 *
 * === PETITS TRUCS ===
 *
 * Afficher nom utilisateur
 * Bulles d'aide pour chaque exercice
 * Upgrade popups (auto line break)
 * Logo*
 * Version check
 * Site web
 * Machintosh
 * "Mode exercice" dans le coin
 *
 * === GROS TRUCS ===
 *
 * Page d'accueil
 * Intro exercice*
 * Éditeur d'énoncés
 * Analyze des notes
 *
 * === BUGS ===
 *
 * Couleur zone de construction
 * Caractères spéciaux
 * Changement de disposition de fenêtre à chaque exercice
 *
 */