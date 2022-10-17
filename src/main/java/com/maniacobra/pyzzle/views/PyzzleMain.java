package com.maniacobra.pyzzle.views;

import com.maniacobra.pyzzle.models.CodeRunner;
import com.maniacobra.pyzzle.properties.AppIdentity;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class PyzzleMain extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        CodeRunner.getInstance().pythonTest();

        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);

        // Launch
        FXMLLoader fxmlLoader = new FXMLLoader(PyzzleMain.class.getResource("main-view.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(PyzzleMain.class.getResource("style.css")).toExternalForm());
        stage.setTitle(AppIdentity.name);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}

/* TO-DO
 * Test Python
 * Message d'accueil
 */