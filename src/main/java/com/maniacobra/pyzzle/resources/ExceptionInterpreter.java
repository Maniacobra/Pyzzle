package com.maniacobra.pyzzle.resources;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ExceptionInterpreter {

    public static void exceptionPopup(String type, int line) {

        String content = "";

        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setResizable(false);
        window.setTitle("");

        Label label = new Label(content);
        Button button = new Button("OK");

        button.setOnAction(e -> window.close());
        VBox layout = new VBox(10);

        layout.getChildren().addAll(label, button);
        layout.setAlignment(Pos.CENTER);

        int width = content.split("\n")[0].length() * 6;
        int height = content.split("\n").length * 30 + 30;
        Scene scene = new Scene(layout, width, height);
        window.setScene(scene);
        window.showAndWait();
    }
}
