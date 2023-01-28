package com.maniacobra.pyzzle.views;

import com.maniacobra.pyzzle.resources.CodeRunner;
import com.maniacobra.pyzzle.properties.AppProperties;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class PyzzleMain extends Application {

    public static File fileArg;

    @Override
    public void start(Stage stage) throws IOException {

        CodeRunner.getInstance().pythonTest();

        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);

        // Launch
        FXMLLoader fxmlLoader = new FXMLLoader(PyzzleMain.class.getResource("main-view.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(PyzzleMain.class.getResource("style.css")).toExternalForm());
        stage.setTitle(AppProperties.name);
        stage.setScene(scene);
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
 * PRIORITAIRE :
 *
 * Message d'accueil
 * Demande de nom + enregistrement
 * Raccourcis éditeur bloc
 * Meilleur drag & drop
 *
 * Fonctionnalités dans les préférences :
 *  - Paramètres arguments terminal
 *  - Couleurs
 *  - Auto-save
 *
 * NEXT :
 *
 * Éditeur d'énoncés
 * Visualisation tableau des notes
 * Bug : Changement de disposition de fenêtre à chaque exercice
 */