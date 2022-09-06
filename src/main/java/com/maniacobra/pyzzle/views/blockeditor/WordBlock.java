package com.maniacobra.pyzzle.views.blockeditor;

import com.maniacobra.pyzzle.models.Word;
import com.maniacobra.pyzzle.utils.TextUtils;
import com.maniacobra.pyzzle.properties.AppStyle;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class WordBlock {

    private static final int fontSize = AppStyle.Values.blockFontSize;
    public static final int blockHeight = (int) (fontSize * 1.5);

    private final Word word;
    private final Color color;
    private final int x;
    private final int y;
    private final int width;
    private final boolean showUsages;

    public WordBlock(Word word, int x, int y, GraphicsContext gc, boolean showUsages) {

        this.word = word;
        this.x = x;
        this.y = y;
        color = word.getColor();
        this.showUsages = showUsages;

        gc.setFont(new Font("Arial", WordBlock.fontSize));
        width = TextUtils.textWidth(gc.getFont(), word.getText());
    }

    public void draw(GraphicsContext gc) {

        gc.setFill(word.getUsages() == 0 ? AppStyle.Colors.mix(color, AppStyle.Colors.canvasBackground, 0.35) : color);
        gc.fillRect(x, y, width + fontSize, blockHeight);
        if (showUsages) {
            gc.setFill(word.getUsages() == 0 ? AppStyle.Colors.mix(Color.BLACK, AppStyle.Colors.canvasBackground, 0.35) : Color.BLACK);
            gc.fillRect(x + width + fontSize, y + blockHeight * 0.1, fontSize * 0.9, blockHeight * 0.8);
            gc.setFill(Color.WHITE);

            String text;
            if (word.getUsages() == -1)
                text = "∞";
            else
                text = String.valueOf(word.getUsages());
            gc.fillText(text, x + width + fontSize * 1.1, y + blockHeight * 0.75);
        }

        if (word.getType() != Word.WordType.TABULATION) {
            Color tempColor = word.getUsages() == 0 ? AppStyle.Colors.mix(AppStyle.Colors.blockText, AppStyle.Colors.canvasBackground, 0.35) : AppStyle.Colors.blockText;
            gc.setFill(tempColor);
            gc.setStroke(tempColor);
            gc.setLineWidth(2);
            gc.strokeRect(x, y, width + fontSize, blockHeight);
            gc.fillText(word.getText(), x + fontSize / 2.f, y + fontSize);
        }
    }

    public boolean collide(int x, int y) {
        return word.getUsages() != 0 && x > this.x && y > this.y && x < this.x + getWidth() && y < this.y + blockHeight;
    }

    public int getWidth() {
        return width + (int) (fontSize * (showUsages ? 1.8 : 1));
    }

    public int getWidthNoUsage() {
        return width + fontSize;
    }

    public Word getWord() {
        return word;
    }

    public int getX() {
        return x;
    }
}