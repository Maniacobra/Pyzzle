package com.maniacobra.pyzzle.utils;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Popups {

    public static void showPopup(String title, String contentIntro, String content) {

        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setResizable(false);
        window.setTitle(title);

        Label label1 = new Label(contentIntro);
        label1.setFont(Font.font("Arial", FontWeight.BOLD, label1.getFont().getSize()));
        Label label2 = new Label(content);
        Button button = new Button("OK");

        button.setOnAction(e -> window.close());
        VBox layout = new VBox(10);

        layout.getChildren().addAll(label1, label2, button);
        layout.setAlignment(Pos.CENTER);

        int width = 400;
        int height = content.split("\n").length * 30 + 60;
        Scene scene = new Scene(layout, width, height);
        window.setScene(scene);
        window.showAndWait();
    }
}
