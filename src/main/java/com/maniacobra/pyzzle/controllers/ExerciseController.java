package com.maniacobra.pyzzle.controllers;

import com.maniacobra.pyzzle.models.*;
import com.maniacobra.pyzzle.properties.AppProperties;
import com.maniacobra.pyzzle.properties.AppSettings;
import com.maniacobra.pyzzle.resources.CodeRunner;
import com.maniacobra.pyzzle.views.PyzzleMain;
import com.maniacobra.pyzzle.views.blockeditor.BlockEditor;
import com.maniacobra.pyzzle.views.blockeditor.WordBlock;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

import static com.maniacobra.pyzzle.utils.TextUtils.addToTextFlow;

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

    public void initialize() {

        nodes = new ExerciseNodes(textAreaCode, textFlowConsole, textFlowObjectives,
                textNumber, textName, textCompletion, textAttempts, textScore,
                buttonExecution, buttonReset, buttonPrevious, buttonNext, buttonSolution);
        setEditors();

        // Event filters

        anchorPane.addEventFilter(MouseEvent.MOUSE_MOVED, this::updateMouse);
        anchorPane.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::updateMouse);
        anchorPane.addEventFilter(MouseEvent.MOUSE_PRESSED, this::updateMouse);
        anchorPane.addEventFilter(MouseEvent.MOUSE_RELEASED, this::updateMouse);

        // No drag-and-drop
        anchorPane.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            if (!AppSettings.getInstance().dragAndDrop && blockCanvas != null) {
                if (mouseEvent.getButton() == MouseButton.PRIMARY && codeEditor.insertWord(selectedBlock.getWord()))
                    updateCode();
                else {
                    wordSelection.returnWord(selectedBlock.getWord());
                    wordSelection.draw();
                }
                removeSelection();
            }
        });
        // With drag-and-drop
        anchorPane.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseEvent -> {
            if (AppSettings.getInstance().dragAndDrop && blockCanvas != null && mouseEvent.getButton() == MouseButton.PRIMARY) {
                if (codeEditor.insertWord(selectedBlock.getWord()))
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
        if (model.proceedExecution(nodes))
            lock();
        codeEditor.draw();
        // AUTO-SAVE
        if (AppSettings.getInstance().autoSave)
            manager.saveData();
    }

    @FXML
    public void reset() {

        setEditors();
        removeSelection();
        textAreaCode.clear();
    }

    @FXML
    public void goToNext() {

        if (manager != null && (model.isLocked() || AppProperties.debugMode))
            manager.goToExercise((BorderPane) anchorPane.getParent(), model.getExerciseNumber() + 1);
    }

    @FXML
    public void goToPrevious() {

        if (manager != null)
            manager.goToExercise((BorderPane) anchorPane.getParent(), model.getExerciseNumber() - 1);
    }

    @FXML
    public void displaySolution() {

        if (!model.isLocked() && !AppProperties.debugMode)
            return;

        setEditors();
        model.switchSolution();
        ArrayList<ArrayList<Word>> solution = model.getSolutionOrPlaced();
        codeEditor.lock(model.isSolutionDisplayed());
        wordSelection.lock(model.isSolutionDisplayed());
        codeEditor.fill(solution);
        for (ArrayList<Word> line : solution)
            for (Word word : line)
                wordSelection.decountWord(word.getId());
        wordSelection.draw();
        updateCode();

        // Nodes
        if (!model.isSolutionDisplayed())
            buttonSolution.setText("Voir la solution");
        else
            buttonSolution.setText("Retour");
    }

    // Block editor

    private void updateMouse(MouseEvent mouseEvent) {

        mouseX = (int) mouseEvent.getX();
        mouseY = (int) mouseEvent.getY();
        if (blockCanvas != null) {
            positionSelectedBlock();
            // Preview only when above code editor
            Node picked = mouseEvent.getPickResult().getIntersectedNode();
            if (picked != null && picked.equals(canvasCodeEditor))
                codeEditor.previewPosition(mouseX, mouseY);
            else
                codeEditor.stopPreview();
        }
    }

    private void setEditors() {

        if (codeEditor != null)
            codeEditor.delete();
        if (wordSelection != null)
            wordSelection.delete();
        codeEditor = new BlockEditor(canvasCodeEditor, this, true);
        wordSelection = new BlockEditor(canvasWordSelection, this, false);
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
        blockCanvas.setOpacity(0.7);
        positionSelectedBlock();
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

    public void quickInsertion(WordBlock block) {

        Word word = block.getWord().getCopy(true);
        if (PyzzleMain.isShiftPressed())
            codeEditor.quickNewLine(word);
        else
            codeEditor.quickInsertion(word);
        updateCode();
    }

    public boolean hasSelectedBlock() {
        return selectedBlock != null;
    }

    // Access for Manager

    public boolean loadConfig(ExerciseConfig config) {

        if (model.loadExercise(config, nodes)) {
            if (model.isLocked())
                lock();
            wordSelection.fill(model.getWords());
            if (config.completion() != null) {
                codeEditor.fill(model.getSolutionOrPlaced());
                for (int id : model.getIdsPlaced())
                    wordSelection.decountWord(id);
                wordSelection.draw();
                JSONArray resultText = (JSONArray) config.completion().get("result_text");
                if (resultText != null) {
                    for (Object obj : resultText) {
                        JSONObject text = (JSONObject) obj;
                        addToTextFlow(textFlowConsole, (String) text.get("text"), Color.web((String) text.get("color")), (boolean) text.get("bold"));
                    }
                }
            }
            updateCode();
            return true;
        }
        model = new ExerciseModel();
        return false;
    }

    public ExerciseModel getModel() {
        return model;
    }

    public void setManger(ExerciseManager manager) {
        this.manager = manager;
    }

    public void clearMemory() {

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

    private void positionSelectedBlock() {

        int halfX = (int)(blockCanvas.getWidth() / 2);
        int halfY = (int)(blockCanvas.getHeight() / 2);
        int maxX = (int)(anchorPane.getWidth() - blockCanvas.getWidth() - 2);
        int maxY = (int)(anchorPane.getHeight() - blockCanvas.getHeight() - 2);

        int x = mouseX - halfX;
        int y = mouseY - halfY;

        if (x > maxX)
            x = maxX;
        else if (x < 0)
            x = 0;
        if (y > maxY)
            y = maxY;
        else if (y < 0)
            y = 0;
        blockCanvas.setLayoutX(x);
        blockCanvas.setLayoutY(y);
    }

    private void lock() {

        codeEditor.lock(false);
        wordSelection.lock(false);
        removeSelection();
    }
}