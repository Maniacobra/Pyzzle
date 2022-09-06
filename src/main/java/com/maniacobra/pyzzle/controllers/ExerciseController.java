package com.maniacobra.pyzzle.controllers;

import com.maniacobra.pyzzle.models.*;
import com.maniacobra.pyzzle.properties.AppIdentity;
import com.maniacobra.pyzzle.views.blockeditor.BlockEditor;
import com.maniacobra.pyzzle.views.blockeditor.WordBlock;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import javax.xml.stream.EventFilter;
import java.util.ArrayList;

public class ExerciseController {

    private ExerciseModel model = new ExerciseModel();

    // Text zones
    @FXML
    private TextFlow textFlowConsole;
    @FXML
    private TextArea textAreaCode;
    @FXML
    private TextFlow textFlowObjectives;
    // Text
    @FXML
    private Text textNumber;
    @FXML
    private Text textName;
    @FXML
    private Text textCompletion;
    @FXML
    private Text textAttempts;
    @FXML
    private Text textScore;
    // Buttons
    @FXML
    private Button buttonExecution;
    @FXML
    private Button buttonReset;
    @FXML
    private Button buttonPrevious;
    @FXML
    private Button buttonNext;
    @FXML
    private Button buttonSolution;
    // Canvas
    @FXML
    private Canvas canvasCodeEditor;
    @FXML
    private Canvas canvasWordSelection;
    // Main
    @FXML
    private AnchorPane anchorPane;

    private ExerciseManager manager = null;

    private ExerciseNodes nodes;
    private BlockEditor codeEditor;
    private BlockEditor wordSelection;

    private WordBlock selectedBlock = null;
    private Canvas blockCanvas = null;

    private int mouseX = 0;
    private int mouseY = 0;

    private EventHandler<MouseEvent> lastHandler = null;

    public void initialize() {

        nodes = new ExerciseNodes(textAreaCode, textFlowConsole, textFlowObjectives,
                textNumber, textName, textCompletion, textAttempts, textScore,
                buttonExecution, buttonReset, buttonPrevious, buttonNext, buttonSolution);
        setEditors();

        // Event filters

        anchorPane.addEventFilter(MouseEvent.MOUSE_MOVED, this::updateMouse);
        anchorPane.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::updateMouse);

        anchorPane.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            if (blockCanvas != null) {
                if (mouseEvent.isPrimaryButtonDown() && codeEditor.insertWord(selectedBlock.getWord()))
                    updateCode();
                else {
                    wordSelection.returnWord(selectedBlock.getWord());
                    wordSelection.draw();
                }
                removeSelection();
            }
        });
    }

    // Interface actions

    @FXML
    public void runCode() {

        textFlowConsole.getChildren().clear();
        codeEditor.setHighlightedLines(CodeRunner.getInstance().getErrorLines());
        if (model.proceedExecution(nodes)) {
            codeEditor.lock(false);
            wordSelection.lock(false);
            removeSelection();
        }
        codeEditor.draw();
    }

    @FXML
    public void reset() {

        setEditors();
        removeSelection();
        textAreaCode.clear();
    }

    @FXML
    public void goToNext() {

        if (manager != null && (model.isLocked() || AppIdentity.debugMode))
            manager.goToExercise((BorderPane) anchorPane.getParent(), model.getExerciseNumber() + 1);
    }

    @FXML
    public void goToPrevious() {

        if (manager != null)
            manager.goToExercise((BorderPane) anchorPane.getParent(), model.getExerciseNumber() - 1);
    }

    @FXML
    public void displaySolution() {

        if (!model.isLocked() && !AppIdentity.debugMode)
            return;

        setEditors();
        ArrayList<ArrayList<Word>> solution = model.getSolutionOrPlaced(buttonSolution);
        codeEditor.lock(model.isSolutionDisplayed());
        wordSelection.lock(model.isSolutionDisplayed());
        codeEditor.fill(solution);
        for (ArrayList<Word> line : solution)
            for (Word word : line)
                wordSelection.decountWord(word.getId());
        wordSelection.draw();
        updateCode();
    }

    // Block editor

    private void updateMouse(MouseEvent mouseEvent) {

        mouseX = (int) mouseEvent.getX();
        mouseY = (int) mouseEvent.getY();
        if (blockCanvas != null) {
            if (mouseX < anchorPane.getWidth() - blockCanvas.getWidth() / 2 - 5 && mouseX > blockCanvas.getWidth() / 2)
                blockCanvas.setLayoutX(mouseX - blockCanvas.getWidth() / 2);
            if (mouseY < anchorPane.getHeight() - blockCanvas.getHeight() / 2 - 5 && mouseY > blockCanvas.getHeight() / 2)
                blockCanvas.setLayoutY(mouseY - blockCanvas.getHeight() / 2);
        }
    }

    private void setEditors() {

        if (codeEditor != null)
            codeEditor.delete();
        if (wordSelection != null)
            wordSelection.delete();
        codeEditor = new BlockEditor(canvasCodeEditor, this, true);
        wordSelection = new BlockEditor(canvasWordSelection, this, false);
        // Events
        if (lastHandler != null)
            canvasCodeEditor.removeEventFilter(MouseEvent.MOUSE_MOVED, lastHandler);
        lastHandler = mouseEvent -> {
            if (blockCanvas != null)
                codeEditor.previewPosition(mouseX, mouseY);
        };
        canvasCodeEditor.addEventFilter(MouseEvent.MOUSE_MOVED, lastHandler);
        // Draw
        wordSelection.fill(model.getWords());
        codeEditor.draw();
    }

    public void setSelectedBlock(WordBlock block) {

        if (model.isLocked())
            return;
        anchorPane.getChildren().remove(blockCanvas);

        blockCanvas = new Canvas(block.getWidthNoUsage() + 3, WordBlock.blockHeight + 3);
        selectedBlock = new WordBlock(block.getWord().getCopy(true), 1, 1, blockCanvas.getGraphicsContext2D(), false);
        anchorPane.getChildren().add(blockCanvas);
        selectedBlock.draw(blockCanvas.getGraphicsContext2D());

        blockCanvas.setMouseTransparent(true);
        blockCanvas.setLayoutX(mouseX - blockCanvas.getWidth() / 2);
        blockCanvas.setLayoutY(mouseY - blockCanvas.getHeight() / 2);
        blockCanvas.setOpacity(0.7);
    }

    public void updateCode() {

        textAreaCode.setText(codeEditor.getFullText());
        model.setIdsPlaced(codeEditor.getIds());
    }

    public void returnWord(WordBlock block) {

        wordSelection.returnWord(block.getWord());
        wordSelection.draw();
        textAreaCode.setText(codeEditor.getFullText());
    }

    // Access for Manager

    public boolean loadConfig(ExerciseConfig config) {

        if (model.loadExercise(config, nodes)) {
            wordSelection.fill(model.getWords());
            return true;
        }
        model = new ExerciseModel();
        return false;
    }

    public ExerciseModel managerConnection(ExerciseManager mainModel) {

        this.manager = mainModel;
        return model;
    }

    public void delete() {

        if (codeEditor != null)
            codeEditor.delete();
        if (wordSelection != null)
            wordSelection.delete();
        model = null;
        manager = null;
        System.gc();
    }

    public void updateScore(float score) {
        model.setTotalScore(score, nodes);
    }

    // PRIVATE

    private void removeSelection() {

        if (blockCanvas != null) {
            anchorPane.getChildren().remove(blockCanvas);
            blockCanvas = null;
        }
        selectedBlock = null;
        codeEditor.stopPreview();
    }
}