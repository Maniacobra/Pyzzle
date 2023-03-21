package com.maniacobra.pyzzle.models;

import com.maniacobra.pyzzle.Launcher;
import com.maniacobra.pyzzle.controllers.ExerciseController;
import com.maniacobra.pyzzle.properties.AppProperties;
import com.maniacobra.pyzzle.resources.PyzzFileManager;
import com.maniacobra.pyzzle.utils.Utils;
import com.maniacobra.pyzzle.views.PyzzleMain;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExerciseManager {

    private JSONObject loadedData = null;
    private final ArrayList<JSONObject> exercises = new ArrayList<>();
    private boolean hasCompletion = false;

    private ExerciseController currentController = null;
    private float maxScore = 0;
    private int packLength = 0;
    private String packName = "Sans nom";

    private final HashMap<Integer, JSONObject> savedCompletion = new HashMap<>();

    private File openedFile = null;
    private File saveFile = null;

    // Pane saving
    private final boolean paneSaving = false;
    private final HashMap<Integer, ExerciseController> loadedControllers = new HashMap<>();
    private final HashMap<Integer, AnchorPane> loadedPanes = new HashMap<>();

    public boolean openFile(File file, BorderPane borderPane) {
        /*
         * 0 = Aucun problème
         * 1 = Problème du pack
         * 2 = Problème d'interface
         * 3 = Problème d'exercice
         */
        System.out.println("Opening : " + file.getAbsolutePath());
        try {
            String data;
            if (file.getName().endsWith(".json"))
                data = PyzzFileManager.getInstance().readNormal(file);
            else
                data = PyzzFileManager.getInstance().decode(file);
            JSONParser parser = new JSONParser();
            loadedData = (JSONObject) parser.parse(data);
            String fileType = loadedData.get("file_type").toString();
            hasCompletion = fileType.equals("opened_pack");
            if (fileType.equals("pack") || hasCompletion) {
                int result = loadPack(loadedData, borderPane, hasCompletion);
                if (result != 0)
                    displayErrorMessage(result);
                else
                    packName = file.getName();
                openedFile = file;
                if (hasCompletion)
                    saveFile = openedFile;
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace(Launcher.output);
            Utils.systemAlert(Alert.AlertType.ERROR, "Erreur lors de l'ouverture du fichier",
                    "Le logiciel n'est pas parvenu à ouvrir ce fichier, vérifiez les permissions.");
        } catch (ParseException e) {
            e.printStackTrace(Launcher.output);
            Utils.systemAlert(Alert.AlertType.ERROR, "Erreur lors de l'ouverture du fichier",
                    "Un problème est survenu lors de la lecture des données du fichier.");
        } catch (Exception e) {
            e.printStackTrace(Launcher.output);
            displayErrorMessage(1);
        }
        return false;
    }

    public int loadPack(JSONObject jsonData, BorderPane borderPane, boolean hasCompletion) {
        /*
         * 0 = Success
         * 1 = Loading error
         * 2 = Graphics error
         */
        try {
            resetPack(borderPane);
            maxScore = 0;
            for (Object obj : (JSONArray) jsonData.get("exercises")) {
                JSONObject jsonExo = (JSONObject) obj;
                exercises.add(jsonExo);
                maxScore += Utils.getFloat(jsonExo, "coef");
                packLength++;
            }
            int starting = 0;
            float totalScore = 0.f;
            // Completion
            JSONObject completion = (JSONObject) jsonData.get("completion");
            if (completion != null) {
                starting = Utils.getInt(completion, "last_panel");
                totalScore = Utils.getFloat(completion, "total_score");
                int i = 0;
                for (Object completed : (JSONArray) completion.get("exercises")) {
                    savedCompletion.put(i, (JSONObject) completed);
                    i++;
                }
            }

            // First exercise
            JSONObject exerciseCompletion = null;
            if (hasCompletion)
                exerciseCompletion = (JSONObject) ((JSONArray) ((JSONObject) loadedData.get("completion")).get("exercises")).get(starting);
            ExerciseConfig config = new ExerciseConfig(exercises.get(starting), starting, exercises.size(), totalScore, maxScore, exerciseCompletion);
            return loadExercise(borderPane, config);
        }
        catch (Exception e) {
            e.printStackTrace(Launcher.output);
            return 1;
        }
    }

    public void goToExercise(BorderPane borderPane, int number) {

        if (number < 0 || number >= exercises.size())
            return;

        savedCompletion.put(currentController.getModel().getExerciseNumber(), currentController.getModel().getJson());

        float currentScore = currentController == null ? 0.f : currentController.getModel().getTotalScore();
        // Panesaving
        if (loadedPanes.containsKey(number) && loadedControllers.containsKey(number)) {
            borderPane.setCenter(loadedPanes.get(number));
            currentController = loadedControllers.get(number);
            currentController.updateScore(currentScore);
            return;
        }

        JSONObject exerciseCompletion = savedCompletion.get(number);
        ExerciseConfig config = new ExerciseConfig(exercises.get(number), number, exercises.size(), currentScore, maxScore, exerciseCompletion);

        int result = loadExercise(borderPane, config);
        if (result != 0)
            displayErrorMessage(result);
    }

    public void saveData(File file) {
        saveFile = file;
        saveData();
    }

    public void saveData() {

        if (loadedData == null || saveFile == null)
            return;

        savedCompletion.put(currentController.getModel().getExerciseNumber(), currentController.getModel().getJson());

        // Make json
        JSONObject data = new JSONObject();
        data.put("file_type", "opened_pack");
        data.put("exercises", loadedData.get("exercises"));
        JSONObject completion = new JSONObject();
        completion.put("total_score", currentController.getModel().getTotalScore());
        completion.put("last_panel", currentController.getModel().getExerciseNumber());

        // Completed exercises
        JSONArray completedExercises = new JSONArray();
        for (int i = 0; i < packLength; i++) {
            JSONObject obj = new JSONObject();
            obj.put("empty", true);
            completedExercises.add(obj);
        }
        for (Map.Entry<Integer, JSONObject> entry : savedCompletion.entrySet())
            completedExercises.set(entry.getKey(), entry.getValue());
        completion.put("exercises", completedExercises);
        data.put("completion", completion);

        PyzzFileManager.getInstance().encode(saveFile, data.toJSONString());
    }

    public String getSaveFileName() {

        String[] splitted = packName.split("\\.");
        if (splitted.length > 0)
            return splitted[0] + "." + AppProperties.openedExtension;
        return "Sans_nom." + AppProperties.openedExtension;
    }

    public boolean hasSaveFile() {
        return saveFile != null;
    }

    // PRIVATE

    private int loadExercise(BorderPane borderPane, ExerciseConfig config) {

        FXMLLoader fxmlLoader = new FXMLLoader(PyzzleMain.class.getResource("exercise-view.fxml"));
        try {
            if (!paneSaving) {
                if (currentController != null)
                    currentController.clearMemory();
                borderPane.setCenter(null);
                System.gc();
            }
            AnchorPane pane = fxmlLoader.load();
            ExerciseController controller = fxmlLoader.getController();
            if (!controller.loadConfig(config))
                return 3;
            currentController = controller;
            currentController.setManger(this);
            if (paneSaving) {
                loadedPanes.put(config.exerciseNumber(), pane);
                loadedControllers.put(config.exerciseNumber(), currentController);
            }
            borderPane.setCenter(pane);
            return 0;
        }
        catch (IOException e) {
            e.printStackTrace(Launcher.output);
            return 2;
        }
    }

    private void displayErrorMessage(int code) {

        if (code == 1)
            Utils.systemAlert(Alert.AlertType.ERROR, "Erreur lors de l'ouverture du fichier",
                    "Le fichier semble avoir des données manquantes, mal formattées ou invalides. Impossible d'ouvrir.");
        else if (code == 2)
            Utils.systemAlert(Alert.AlertType.ERROR, "Erreur lors du chargement l'exercise",
                    "Une erreur inconnue s'est produite lors du chargement de l'interface.");
        else if (code == 3)
            Utils.systemAlert(Alert.AlertType.ERROR, "Erreur lors du chargement de l'exercice",
                    "Impossible de charger l'exercice, des données du fichier semblent manquantes, mal formattées ou invalides.");
    }

    private void resetPack(BorderPane borderPane) {

        exercises.clear();
        for (ExerciseController controller : loadedControllers.values())
            controller.clearMemory();
        loadedControllers.clear();
        loadedPanes.clear();
        if (currentController != null) {
            currentController.clearMemory();
            currentController = null;
        }
        maxScore = 0;
        packLength = 0;
        borderPane.setCenter(null);
        savedCompletion.clear();
        packName = "Sans nom";
    }
}
