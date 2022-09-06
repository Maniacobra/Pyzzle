package com.maniacobra.pyzzle.models;

import javafx.scene.paint.Color;

public class Word {

    private final WordType type;
    private final String text;
    private int usages;
    private final int id;

    public Word(WordType type, String text, int usages, int id) {

        this.type = type;
        this.text = text;
        this.usages = usages;
        this.id = id;
    }

    public boolean use() {

        if (usages > 0)
            usages--;
        return usages == 0;
    }

    public WordType getType() {
        return type;
    }

    public boolean merge(Word word) {
        if (word.id == id) {
            if (usages != -1)
                usages++;
            return true;
        }
        return false;
    }

    public String getText() {
        return text;
    }

    public Word getCopy(boolean singleUse) {
        return new Word(type, text, singleUse ? 1 : usages, id);
    }

    public int getUsages() {
        return usages;
    }

    public int getId() {
        return id;
    }

    public Color getColor() {
        int index = type.ordinal();
        if (index < typeColors.length)
            return typeColors[index];
        System.out.println("ERROR : Missing colors.");
        return Color.LIGHTGRAY;
    }

    // TYPE

    public enum WordType {
        DEFAULT,
        INPUT,
        VARIABLE,
        VALUE,
        OPERATOR,
        KEYWORD,
        FUNCTION,
        TABULATION,
        GROUP
    }

    private static final Color[] typeColors = {
            Color.LIGHTGRAY,
            Color.rgb(212, 189, 255), // Input
            Color.rgb(255, 180, 177), // Variable
            Color.rgb(250, 247, 177), // Value
            Color.LIGHTGRAY, // Operator
            Color.rgb(177, 199, 250), // Keyword
            Color.rgb(213, 247, 210), // Function
            Color.rgb(190, 190, 190), // Tabulation
            Color.rgb(245, 225, 239), // Group
    };
}
