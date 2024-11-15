package com.maniacobra.pyzzle.models;

import com.maniacobra.pyzzle.Launcher;
import com.maniacobra.pyzzle.controllers.ExerciseNodes;
import com.maniacobra.pyzzle.properties.AppProperties;
import com.maniacobra.pyzzle.properties.AppStyle;
import com.maniacobra.pyzzle.resources.CodeRunner;
import com.maniacobra.pyzzle.utils.Utils;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.maniacobra.pyzzle.utils.TextUtils.*;

public class ExerciseModel {

    private boolean loaded;
    private boolean simple;
    private float coef;
    private int attempts;
    private float initialTotalScore;
    private float totalScore;
    private float score;
    private float maxScore;
    private String name;
    private int exerciseNumber;
    private int totalExercises;
    private boolean locked;
    private boolean solutionDisplayed;
    private boolean win;

    private final ArrayList<String> inputs = new ArrayList<>();
    private final ArrayList<List<String>> datasets = new ArrayList<>();
    private final ArrayList<List<String>> objectives = new ArrayList<>();
    private final ArrayList<ArrayList<Word>> words = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> idsPlaced = new ArrayList<>();
    private final ArrayList<ArrayList<Integer>> idsSolution = new ArrayList<>();

    private JSONArray resultTextJson = new JSONArray();

    public ExerciseModel() {
        resetAll();
    }

    public boolean loadExercise(ExerciseConfig config, ExerciseNodes nodes) {

        // Loading data
        resetAll();
        if (config.data() != null)
            if (!loadJsonData(config.data()))
                return false;
        exerciseNumber = config.exerciseNumber();
        totalExercises = config.totalExercises();

        // Buttons
        if (config.exerciseNumber() <= 0)
            nodes.previousButton().setDisable(true);
        if (!AppProperties.debugMode)
            nodes.nextButton().setDisable(true);
        else
            nodes.solutionButton().setDisable(false);

        // Name
        nodes.numberText().setText(String.format("Exercice %d / %d", config.exerciseNumber() + 1, config.totalExercises()));
        nodes.nameText().setText(name);
        //nodes.nameText().set
        nodes.completionText().setText("Non terminé");

        // Objectives
        nodes.objectivesText().getChildren().clear();
        int i = 0;
        for (List<String> objective : objectives) {
            nodes.objectivesText().getChildren().add(getDsDeclaration(i));
            if (objective.isEmpty())
                addToTextFlow(nodes.objectivesText(), "(vide)\n");
            else
                for (String line : objective)
                    addToTextFlow(nodes.objectivesText(), line + '\n');
            addToTextFlow(nodes.objectivesText(), "\n");
            i++;
        }

        // Completion
        if (config.completion() != null && config.completion().get("empty") == null) {
            JSONObject completion = config.completion();
            attempts = Utils.getInt(completion, "attempts");
            win = (boolean) completion.get("win");
            score = Utils.getFloat(completion, "score");
            idsPlaced.clear();
            for (Object lineObj : (JSONArray) config.completion().get("placed_words")) {
                ArrayList<Integer> line = new ArrayList<>();
                for (Object id : (JSONArray) lineObj) {
                    if (id instanceof Integer)
                        line.add((Integer) id);
                    else if (id instanceof Long)
                        line.add(((Long) id).intValue());
                }
                idsPlaced.add(line);
            }
            resultTextJson = (JSONArray) completion.get("result_text");
        }

        // Score
        initialTotalScore = config.totalScore();
        maxScore = config.maxScore();
        totalScore = initialTotalScore;
        updateScore(nodes);

        // Debug
        if (AppProperties.debugMode)
            nodes.codeText().setEditable(true);

        return true;
    }

    public void setIdsPlaced(ArrayList<ArrayList<Integer>> ids) {

        if (!solutionDisplayed)
            idsPlaced = ids;
    }

    public ArrayList<ArrayList<Word>> getWords() {

        ArrayList<ArrayList<Word>> wordsCopy = new ArrayList<>();
        for (List<Word> wordLine : words) {
            ArrayList<Word> newLine = new ArrayList<>();
            for (Word word : wordLine)
                newLine.add(word.getCopy(false));
            wordsCopy.add(newLine);
        }
        return wordsCopy;
    }

    public ArrayList<ArrayList<Word>> getSolutionOrPlaced() {

        ArrayList<ArrayList<Word>> wordsCopy = new ArrayList<>();
        for (List<Integer> idsLine : (solutionDisplayed ? idsSolution : idsPlaced)) {
            ArrayList<Word> newLine = new ArrayList<>();
            for (Integer id : idsLine)
                for (List<Word> wordLine : words)
                    for (Word word : wordLine)
                        if (word.getId() == id)
                            newLine.add(word.getCopy(true));
            wordsCopy.add(newLine);
        }
        return wordsCopy;
    }

    public void switchSolution() {
        solutionDisplayed = !solutionDisplayed;
    }

    public int getExerciseNumber() {
        return exerciseNumber;
    }

    public float getTotalScore() {
        return totalScore;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setTotalScore(float totalScore, ExerciseNodes nodes) {

        this.totalScore = totalScore;
        updateScore(nodes);
    }

    public boolean isSolutionDisplayed() {
        return solutionDisplayed;
    }

    public boolean proceedExecution(ExerciseNodes nodes) {

        if (!loaded)
            return false;

        // Execution
        CodeRunner.getInstance().reset();
        CodeRunner.getInstance().prepareCode(nodes.codeText().toString(), inputs);
        nodes.consoleText().getChildren().clear();
        CodeRunner.getInstance().prepareCode(nodes.codeText().getText(), inputs);
        float tempScore = 0;

        // Run and score calculation
        Set<String> errors = new HashSet<>();
        int nbSets = simple ? 1 : datasets.size();
        for (int i = 0; i < nbSets; i++) {
            ExecutionResult result = CodeRunner.getInstance().runCode(nodes.consoleText(), getDsDeclaration(i), simple ? null : datasets.get(i), objectives.get(i));
            switch (result) {
                case SUCCESS -> tempScore += 1;
                case OVERFLOW -> tempScore += 0.5;
                case EXCEPTION -> {
                    // Error popup
                    CodeRunner instance = CodeRunner.getInstance();
                    if (instance.hasError() && !errors.contains(instance.getErrorType())) {
                        errors.add(instance.getErrorType());
                        instance.exceptionPopup();
                    }
                }
                case FATAL -> {
                    nodes.consoleText().getChildren().clear();
                    if (CodeRunner.getInstance().pythonTest())
                        Utils.systemAlert(Alert.AlertType.ERROR, "Erreur fatale de Pyzzle",
                                "Une erreur inconnue du logiciel est survenue, cette erreur n'est PAS causée par votre code." +
                                        " Vérifiez l'installation du logiciel.");
                    return false;
                }
            }
        }

        // Scores
        if (attempts > 0)
            attempts--;
        tempScore = tempScore / nbSets;
        boolean worse = score > tempScore;
        if (!worse)
            score = tempScore * coef;
        win = tempScore == 1;
        totalScore = initialTotalScore + score;

        // Display results
        Color color;
        String message;
        if (tempScore == 0) {
            color = AppStyle.Colors.resultWrong;
            message = "ÉCHEC\n\n";
        }
        else if (win) {
            color = AppStyle.Colors.resultValid;
            message = "RÉUSSITE !\n\n";
        }
        else {
            color = AppStyle.Colors.resultUnperfect;
            message = "IMPARFAITS\n\n";
        }
        addToTextFlow(nodes.consoleText(), "\nRÉSULTAT DES TESTS : " + message, color, true);
        addToTextFlow(nodes.consoleText(), String.format("Points gagnés : %s sur %s\n\n",
                Utils.nbToStr(tempScore * coef), Utils.nbToStr(coef)), color, false);
        boolean result = false;
        if (!win) {
            if (worse)
                addToTextFlow(nodes.consoleText(), "Ce score est inférieur au précédent essai et n'est donc pas pris en compte.\n");
            if (attempts > 0)
                addToTextFlow(nodes.consoleText(),
                        String.format("Vous avez %d %s pour tenter de faire mieux.\n", attempts, attempts == 1 ? "essai restant" : "essais restants"),
                        AppStyle.Colors.objectives, true);
            addToTextFlow(nodes.consoleText(), "Info : un seul résultat vide ou différent de celui souhaité\nmène à la perte de tous les points d'un test.", AppStyle.Colors.info, true);
        }
        else
            result = true;
        if (attempts == 0)
            result = true;
        updateScore(nodes);
        textFlowToJson(nodes.consoleText());
        return result;
    }

    @SuppressWarnings("unchecked")
    public JSONObject getJson() {

        JSONObject data = new JSONObject();
        data.put("score", score);
        data.put("attempts", attempts);
        data.put("win", win);
        data.put("result_text", resultTextJson);

        JSONArray idsJson = new JSONArray();
        for (ArrayList<Integer> line : idsPlaced) {
            JSONArray jsonLine = new JSONArray();
            jsonLine.addAll(line);
            idsJson.add(jsonLine);
        }
        data.put("placed_words", idsJson);

        return data;
    }

    public ArrayList<Integer> getIdsPlaced() {
        ArrayList<Integer> ids = new ArrayList<>();
        for (ArrayList<Integer> line : idsPlaced)
            ids.addAll(line);
        return ids;
    }

    // PRIVATE

    private Text getDsDeclaration(int dsNb) {

        StringBuilder strBuilder = new StringBuilder();
        if (simple) {
            strBuilder.append("RÉSULTAT\n\n");
        }
        else {
            strBuilder.append("TEST N°");
            strBuilder.append(dsNb + 1);
            strBuilder.append(" AVEC : ");
            int i = 0;
            for (String input : inputs) {
                strBuilder.append(input);
                strBuilder.append(" = ");
                strBuilder.append(datasets.get(dsNb).get(i));
                if (i < inputs.size() - 1)
                    strBuilder.append(", ");
                i++;
            }
            strBuilder.append("\n\n");
        }

        Text text = new Text(strBuilder.toString());
        text.setTextAlignment(TextAlignment.CENTER);
        text.setFont(Font.font("Arial", FontWeight.BOLD, AppStyle.Values.consoleFontSize));
        return text;
    }

    @SuppressWarnings("unchecked")
    private void textFlowToJson(TextFlow textFlow) {

        resultTextJson.clear();
        textFlow.getChildren().forEach(t -> {
            JSONObject textJson = new JSONObject();
            Text text = (Text) t;
            textJson.put("text", text.getText());
            textJson.put("color", text.getFill().toString());
            textJson.put("bold", text.getFont().getStyle().equals("Bold"));
            resultTextJson.add(textJson);
        });
    }

    private void updateScore(ExerciseNodes nodes) {

        if (!loaded)
            return;

        nodes.scoreText().setText(String.format("Score total de tous les exercices :\n%s / %s points", Utils.nbToStr(totalScore), Utils.nbToStr(maxScore)));
        if (attempts > 0)
            nodes.attemptsText().setText("Essais restants : " + attempts);
        else if (attempts == 0)
            nodes.attemptsText().setText("Aucun essai restant");
        else
            nodes.attemptsText().setText("Essais illimités");

        if (win) {
            nodes.completionText().setText("Réussi à 100% !");
            nodes.completionText().setFill(AppStyle.Colors.fullCompletion);
            lock(nodes);
        }
        else if (attempts == 0) {
            nodes.completionText().setText(String.format("Terminé avec %s sur %s points", Utils.nbToStr(score), Utils.nbToStr(coef)));
            lock(nodes);
        }
    }

    private boolean loadJsonData(JSONObject data) {

        try {
            coef = Utils.getFloat(data, "coef");
            attempts = Utils.getInt(data, "attempts");
            name = data.get("name").toString();
            // Inputs
            if (data.get("inputs") != null)
                for (Object elem : (JSONArray) data.get("inputs"))
                    inputs.add((String) elem);
            // Datasets
            if (data.get("datasets") != null)
                for (Object elem : (JSONArray) data.get("datasets")) {
                    ArrayList<String> ds = new ArrayList<>();
                    datasets.add(ds);
                    for (Object elem2 : (JSONArray) elem)
                        ds.add(String.valueOf(elem2));
                }
            // Objectives
            for (Object elem : (JSONArray) data.get("objectives")) {
                ArrayList<String> line = new ArrayList<>();
                objectives.add(line);
                for (Object elem2 : (JSONArray) elem)
                    line.add(elem2.toString());
            }
            // Words
            ArrayList<Integer> ids = new ArrayList<>();
            for (Object elem : (JSONArray) data.get("words")) {
                ArrayList<Word> wordList = new ArrayList<>();
                words.add(wordList);
                for (Object elem2 : (JSONArray) elem) {
                    JSONObject jsonW = (JSONObject) elem2;
                    int id = Utils.getInt(jsonW, "id");
                    assert !ids.contains(id);
                    ids.add(id);
                    Word word = new Word(Word.WordType.valueOf(jsonW.get("type").toString()), jsonW.get("text").toString(), Utils.getInt(jsonW, "usages"), id);
                    wordList.add(word);
                }
            }
            // Solution
            for (Object elem : (JSONArray) data.get("solution")) {
                ArrayList<Integer> line = new ArrayList<>();
                idsSolution.add(line);
                for (Object elem2 : (JSONArray) elem)
                    line.add(((Long) elem2).intValue());
            }

            if (inputs.size() == 0) {
                assert objectives.size() == 1 && datasets.size() == 0;
                simple = true;
            }
            else {
                simple = false;
                assert datasets.size() == objectives.size();
                for (List<String> ds : datasets)
                    assert ds.size() == inputs.size();
            }
            assert coef >= 0;
            loaded = true;
            return true;
        }
        catch (Exception | AssertionError e) {
            Utils.systemAlert(Alert.AlertType.ERROR, "Erreur lors du chargement de l'exercice",
                    "L'exercice semble avoir des données manquantes ou mal formatées, et n'a pas pu être chargé.");
            e.printStackTrace(Launcher.output);
            return false;
        }
    }

    private void lock(ExerciseNodes nodes) {

        nodes.executionButton().setDisable(true);
        nodes.resetButton().setDisable(true);
        nodes.solutionButton().setDisable(false);
        if (exerciseNumber < totalExercises - 1)
            nodes.nextButton().setDisable(false);
        locked = true;
    }

    private void resetAll() {

        loaded = false;
        simple = false;
        coef = -1;
        attempts = 0;
        initialTotalScore = 0;
        totalScore = 0;
        score = 0;
        maxScore = 0;
        name = "Exercice sans nom";
        exerciseNumber = 0;
        totalExercises = 1;
        inputs.clear();
        datasets.clear();
        objectives.clear();
        words.clear();
        idsPlaced.clear();
        idsSolution.clear();
        locked = false;
        solutionDisplayed = false;
    }
}
