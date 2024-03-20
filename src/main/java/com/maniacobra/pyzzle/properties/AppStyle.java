package com.maniacobra.pyzzle.properties;

import javafx.scene.paint.Color;

public class AppStyle {
    public static class Colors {
        // Block editor
        public static final Color canvasBackground = Color.WHITE;
        public static final Color canvasBackgroundLocked = Color.rgb(230, 230, 230);
        public static final Color canvasBackgroundSolution = Color.rgb(230, 230, 245);
        public static final Color blockText = Color.BLACK;
        public static final Color previewLine = Color.BLUE;
        public static final Color highLighedLine = Color.rgb(245, 200, 200);

        // Console
        public static final Color objectives = Color.BLACK;
        public static final Color info = Color.DARKGRAY;
        public static final Color resultValid = Color.GREEN;
        public static final Color resultWrong = Color.RED;
        public static final Color resultUnperfect = Color.DARKORANGE;
        public static final Color resultEmpty = Color.DARKSALMON;
        public static final Color resultEmptyCorrect = Color.DARKGRAY;
        public static final Color resultError = Color.PURPLE;

        // Texts
        public static final Color fullCompletion = Color.GREEN;

        // Methods
        public static Color mix(Color c1, Color c2, double coef) {
            double r = c1.getRed() * coef + c2.getRed() * (1 - coef);
            double g = c1.getGreen() * coef + c2.getGreen() * (1 - coef);
            double b = c1.getBlue() * coef + c2.getBlue() * (1 - coef);
            return Color.color(r, g, b);
        }
    }
    public static class Values {
        // Text
        public static final int consoleFontSize = 12;
        public static final int blockSep = 6;
        public static final int blockFontSize = 14;
    }
}
