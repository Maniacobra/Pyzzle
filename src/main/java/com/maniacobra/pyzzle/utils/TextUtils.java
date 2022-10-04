package com.maniacobra.pyzzle.utils;

import com.maniacobra.pyzzle.properties.AppStyle;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class TextUtils {

    public static void addToTextFlow(TextFlow textFlow, String str, Color color, boolean bold) {
        Text text = new Text(str);
        if (color != null)
            text.setFill(color);
        if (bold)
            text.setFont(Font.font("Arial", FontWeight.BOLD, AppStyle.Values.consoleFontSize));
        textFlow.getChildren().add(text);
    }

    public static void addToTextFlow(TextFlow textFlow, String str) {
        addToTextFlow(textFlow, str, null, false);
    }

    public static int textWidth(Font font, String s) {
        Text text = new Text(s);
        text.setFont(font);
        return (int) (text.getBoundsInLocal().getWidth());
    }
}
