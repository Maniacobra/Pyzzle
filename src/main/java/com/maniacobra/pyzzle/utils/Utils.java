package com.maniacobra.pyzzle.utils;

import javafx.scene.control.Alert;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import java.awt.*;
import java.util.ArrayList;

public class Utils {

    public static String nbToStr(float nb) {

        if ((int) nb == nb)
            return String.valueOf((int) nb);
        else {
            String[] splitted = String.valueOf(nb).split(",");
            if (splitted.length < 2)
                return String.valueOf(nb);
            String decimal = splitted[1];
            if (decimal.length() > 2)
                decimal = decimal.substring(0, 1);
            return splitted[0] + "." + decimal;
        }
    }

    public static int getInt(JSONObject data, String key) {
        return ((Long) data.get(key)).intValue();
    }

    public static void systemAlert(Alert.AlertType type, String title, String content) {

        final Runnable runnable =
                (Runnable) Toolkit.getDefaultToolkit().getDesktopProperty("win.sound.exclamation");
        if (runnable != null) runnable.run();
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
