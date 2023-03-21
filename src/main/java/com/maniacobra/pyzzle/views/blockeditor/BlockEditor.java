package com.maniacobra.pyzzle.views.blockeditor;

import com.maniacobra.pyzzle.controllers.ExerciseController;
import com.maniacobra.pyzzle.models.Word;
import com.maniacobra.pyzzle.properties.AppStyle;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class BlockEditor {

    private final boolean dragAndDrop;

    private static final int sep = AppStyle.Values.blockSep;
    private static final int margin = (int) (sep * 2.5f);

    private final Canvas canvas;
    private final GraphicsContext gc;
    private final ArrayList<ArrayList<WordBlock>> blocks = new ArrayList<>();
    private Stack<Integer> highlightedLines = null;

    private int previewLine = -1;
    private int previewPos = 0;
    private int previewPosPixels = 0;
    private boolean previewMakeLine = false;
    private boolean ignoreNextClick = false;
    private final boolean construction;
    private boolean locked = false;
    private boolean solution = false;

    private final EventHandler<MouseEvent> eventHandlerPressed;
    private final EventHandler<MouseEvent> eventHandlerExited;

    public BlockEditor(Canvas canvas, ExerciseController controller, boolean construction, boolean dragAndDrop) {

        this.dragAndDrop = dragAndDrop;
        this.construction = construction;
        this.canvas = canvas;
        gc = canvas.getGraphicsContext2D();

        eventHandlerPressed = mouseEvent -> {

            if (locked)
                return;
            if (ignoreNextClick) {
                ignoreNextClick = false;
                return;
            }

            int x = (int) mouseEvent.getX();
            int y = (int) mouseEvent.getY();
            int i = 0;
            for (ArrayList<WordBlock> blockLine : blocks) {
                for (WordBlock block : blockLine) {
                    if (block.collide(x, y)) {
                        boolean leftClick = mouseEvent.isPrimaryButtonDown();
                        boolean remove = false;
                        if (leftClick) {
                            controller.setSelectedBlock(block);
                            if (block.getWord().use() && construction)
                                remove = true;
                        }
                        else { // Right
                            if (construction) {
                                controller.returnWord(block);
                                remove = true;
                            }
                            else if (block.getWord().getUsages() != 0) { // Word selection
                                block.getWord().use();
                                controller.quickInsertion(block);
                            }
                        }
                        if (remove) {
                            blockLine.remove(block);
                            if (blockLine.size() == 0)
                                blocks.remove(blockLine);
                            updateLinesFrom(i);
                            controller.updateCode();
                        }

                        if (controller.hasSelectedBlock() && construction)
                            previewPosition(x, y);
                        draw();
                        return;
                    }
                }
                i++;
            }
        };

        eventHandlerExited = mouseEvent -> {
            if (locked)
                return;
            previewLine = -1;
            draw();
        };

        canvas.addEventFilter(MouseEvent.MOUSE_PRESSED, eventHandlerPressed);
        canvas.addEventFilter(MouseEvent.MOUSE_EXITED, eventHandlerExited);
    }

    public void fill(ArrayList<ArrayList<Word>> words) {

        int i = 0;
        for (List<Word> wordLine : words) {
            fillLine(wordLine, i);
            i++;
        }
        draw();
    }

    private void fillLine(List<Word> wordLine, int lineNb) {

        ArrayList<WordBlock> blockLine;
        if (lineNb >= blocks.size()) {
            blockLine = new ArrayList<>();
            blocks.add(blockLine);
        }
        else {
            blockLine = blocks.get(lineNb);
            blockLine.clear();
        }
        int x = margin;
        int y = lineNb * (WordBlock.blockHeight + sep) + margin;
        for (Word word : wordLine) {
            WordBlock block = new WordBlock(word, x, y, gc, !construction);
            blockLine.add(block);
            x += block.getWidth() + sep;
        }
        highlightedLines = null;
    }

    public void previewPosition(int x, int y) {

        boolean oldMakeLine = previewMakeLine;
        int oldLine = previewLine;
        int oldPos = previewPos;

        float line = (y - margin - (WordBlock.blockHeight + sep) / 2.f) / (float) (WordBlock.blockHeight + sep);
        previewLine = (int) line;
        if (previewLine >= blocks.size()) {
            previewLine = blocks.size();
            previewMakeLine = true;
        }
        else {
            if (line - previewLine < 0.2)
                previewMakeLine = true;
            else if (line - previewLine > 0.8) {
                previewLine++;
                previewMakeLine = true;
            }
            else {
                previewMakeLine = false;
                ArrayList<WordBlock> blockLine = blocks.get(previewLine);
                int i = 0;
                previewPos = -1;
                for (WordBlock block : blockLine) {
                    if (block.getX() + block.getWidth() / 2 > x) {
                        previewPos = i;
                        previewPosPixels = block.getX() - sep / 2;
                        break;
                    }
                    else
                        i++;
                }
                if (previewPos == -1 && blockLine.size() > 0) {
                    previewPos = blockLine.size();
                    WordBlock block = blockLine.get(blockLine.size() - 1);
                    previewPosPixels = block.getX() + block.getWidth() + sep / 2;
                }
            }
        }

        if (oldMakeLine != previewMakeLine || oldLine != previewLine || oldPos != previewPos)
            draw();
    }

    public boolean insertWord(Word word) {

        if (previewLine == -1)
            return false;

        ArrayList<Word> wordLine = new ArrayList<>();
        if (previewLine >= blocks.size()) {
            wordLine.add(word);
            fillLine(wordLine, previewLine);
        }
        else if (previewMakeLine) {
            wordLine.add(word);
            blocks.add(previewLine, new ArrayList<>());
            fillLine(wordLine, previewLine);
            updateLinesFrom(previewLine + 1);
        }
        else {
            // Re-build the line to change
            ArrayList<WordBlock> blockLine = blocks.get(previewLine);
            int i = 0;
            for (WordBlock block : blockLine) {
                if (i == previewPos)
                    wordLine.add(word);
                wordLine.add(block.getWord());
                i++;
            }
            if (previewPos >= blockLine.size())
                wordLine.add(word);
            fillLine(wordLine, previewLine);
        }
        if (!dragAndDrop)
            ignoreNextClick = true;
        previewLine = -1;
        draw();
        return true;
    }

    public void returnWord(Word word) {

        for (ArrayList<WordBlock> blockLine : blocks)
            for (WordBlock block : blockLine)
                if (block.getWord().merge(word))
                    return;
    }

    public void quickInsertion(Word word) {

        ArrayList<Word> wordLine = new ArrayList<>();
        int lineNb = 0;
        if (blocks.size() > 0) {
            lineNb = blocks.size() - 1;
            // Re-build the line to change
            ArrayList<WordBlock> blockLine = blocks.get(lineNb);
            for (WordBlock block : blockLine)
                wordLine.add(block.getWord());
        }
        wordLine.add(word);
        fillLine(wordLine, lineNb);
        draw();
    }

    public void quickNewLine(Word word) {

        ArrayList<Word> wordLine = new ArrayList<>();
        wordLine.add(word);
        fillLine(wordLine, blocks.size());
        draw();
    }

    public void decountWord(int id) {

        for (ArrayList<WordBlock> blockLine : blocks)
            for (WordBlock block : blockLine)
                if (block.getWord().getId() == id) {
                    block.getWord().use();
                    return;
                }
        System.out.println(id + " not found.");
    }

    public void stopPreview() {

        if (previewLine != -1) {
            previewLine = -1;
            draw();
        }
        if (!dragAndDrop)
            ignoreNextClick = true;
    }

    public String getFullText() {

        StringBuilder strBuilder = new StringBuilder();
        for (ArrayList<WordBlock> blockLine : blocks) {
            int i = 0;
            for (WordBlock block : blockLine) {
                strBuilder.append(block.getWord().getText());
                i++;
                if (i != blockLine.size())
                    strBuilder.append(" ");
            }
            strBuilder.append("\n");
        }
        return strBuilder.toString();
    }

    public ArrayList<ArrayList<Integer>> getIds() {

        ArrayList<ArrayList<Integer>> ids = new ArrayList<>();
        for (ArrayList<WordBlock> blockLine : blocks) {
            ArrayList<Integer> line = new ArrayList<>();
            ids.add(line);
            for (WordBlock block : blockLine)
                line.add(block.getWord().getId());
        }
        return ids;
    }

    public void setHighlightedLines(Stack<Integer> lines) {
        highlightedLines = lines;
    }

    public void draw() {

        // Background
        gc.setFill(locked ?
                (solution ? AppStyle.Colors.canvasBackgroundSolution : AppStyle.Colors.canvasBackgroundLocked)
                : AppStyle.Colors.canvasBackground);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        if (highlightedLines != null) {
            for (Integer line : highlightedLines) {
                int y = margin + line * (WordBlock.blockHeight + sep) - sep / 2;
                gc.setFill(AppStyle.Colors.highLighedLine);
                gc.fillRect(0, y, canvas.getWidth(), WordBlock.blockHeight + sep);
            }
        }

        // Blocks
        for (ArrayList<WordBlock> blockLine : blocks)
            for (WordBlock block : blockLine)
                block.draw(gc);

        // Preview
        if (previewLine == -1)
            return;
        gc.setFill(AppStyle.Colors.previewLine);
        int y = previewLine * (WordBlock.blockHeight + sep);
        if (previewMakeLine)
            gc.fillRect(0, y - 1 + margin - sep / 2.f, canvas.getWidth(), 2);
        else
            gc.fillRect(previewPosPixels - 1, y + margin, 2, WordBlock.blockHeight);
    }

    public void lock(boolean solution) {

        this.solution = solution;
        locked = true;
        draw();
    }

    public void delete() {
        canvas.removeEventFilter(MouseEvent.MOUSE_PRESSED, eventHandlerPressed);
        canvas.removeEventFilter(MouseEvent.MOUSE_EXITED, eventHandlerExited);
    }

    private void updateLinesFrom(int start) {

        ArrayList<Word> wordLine = new ArrayList<>();
        for (int i = start; i < blocks.size(); i++) {
            wordLine.clear();
            ArrayList<WordBlock> blockLine = blocks.get(i);
            for (WordBlock block : blockLine)
                wordLine.add(block.getWord());
            fillLine(wordLine, i);
        }
    }
}
