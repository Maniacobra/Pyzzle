package com.maniacobra.pyzzle.utils;

import javafx.scene.control.Alert;
import org.json.simple.JSONObject;

import java.awt.*;
import java.io.*;

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

        Object nb = data.get(key);
        if (nb instanceof Long)
            return ((Long) nb).intValue();
        return (Integer) nb;
    }

    public static float getFloat(JSONObject data, String key) {

        Object nb = data.get(key);
        if (nb instanceof Double)
            return ((Double) nb).floatValue();
        return (Float) nb;
    }

    public static void systemAlert(Alert.AlertType type, String title, String content) {

        final Runnable runnable = (Runnable) Toolkit.getDefaultToolkit().getDesktopProperty("win.sound.exclamation");
        if (runnable != null)
            runnable.run();
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void copyDirectory(File sourceLocation, File targetLocation) throws IOException {
        // https://stackoverflow.com/a/1146221/13313951

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }
            String[] children = sourceLocation.list();
            assert children != null;
            for (String child : children) {
                copyDirectory(new File(sourceLocation, child),
                        new File(targetLocation, child));
            }
        }
        else {
            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);
            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0)
                out.write(buf, 0, len);
            in.close();
            out.close();
        }
    }
}
