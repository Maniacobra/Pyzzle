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

    public static void showPopup(String title, String contentIntro, String content, int width, int textSize, int charsForBreak) {

        // String process
        int lineBreakCount = 0;
        int charCount = 0;
        StringBuilder strBuilder = new StringBuilder();
        String[] splitted = content.split(" ");
        for (String word : splitted) {
            charCount += word.length();
            strBuilder.append(word);
            if (word.contains("\n")) {
                String[] spltWord = word.split("\n");
                charCount = spltWord[spltWord.length - 1].length();
                lineBreakCount += spltWord.length - 1;
            }
            if (charCount > charsForBreak) {
                strBuilder.append("\n");
                charCount = 0;
                lineBreakCount++;
            }
            else {
                strBuilder.append(" ");
                charCount++;
            }
        }

        // Window

        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setResizable(false);
        window.setTitle(title);

        Label label1 = new Label(contentIntro);
        label1.setFont(Font.font("Arial", FontWeight.BOLD, textSize * 1.4));
        Label label2 = new Label(strBuilder.toString());
        label2.setFont(Font.font("Arial", FontWeight.NORMAL, textSize));
        Button button = new Button("OK");

        button.setOnAction(e -> window.close());
        VBox layout = new VBox(10);

        layout.getChildren().addAll(label1, label2, button);
        layout.setAlignment(Pos.CENTER);
        System.out.println(lineBreakCount);
        int height = lineBreakCount * textSize * 2 + 80;
        Scene scene = new Scene(layout, width, height);
        window.setScene(scene);
        window.showAndWait();
    }
}
