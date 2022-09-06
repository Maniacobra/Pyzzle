package com.maniacobra.pyzzle.models;

import com.maniacobra.pyzzle.controllers.ExerciseController;
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
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ExerciseManager {

    private final boolean paneSaving = true;

    private final ArrayList<JSONObject> exercises = new ArrayList<>();

    private final HashMap<Integer, ExerciseController> loadedControllers = new HashMap<>();
    private final HashMap<Integer, AnchorPane> loadedPanes = new HashMap<>();

    private ExerciseController currentController = null;
    private float maxScore = 0;

    public void openFile(File file, BorderPane borderPane) {
        /*
         * 0 = Aucun problème
         * 1 = Problème du pack
         * 2 = Problème d'interface
         * 3 = Problème d'exercice
         */
        try (FileReader reader = new FileReader(file)) {
            JSONParser parser = new JSONParser();
            JSONObject data = (JSONObject) parser.parse(reader);
            if (data.get("file_type").toString().equals("pack")) {
                int result = loadPack(data, borderPane);
                if (result != 0)
                    displayErrorMessage(result);
            }
            else {
                System.out.println("CONTENU");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Utils.systemAlert(Alert.AlertType.ERROR, "Erreur lors de l'ouverture du fichier",
                    "Le logiciel n'est pas parvenu à ouvrir ce fichier, vérifiez les permissions.");
        } catch (ParseException e) {
            e.printStackTrace();
            Utils.systemAlert(Alert.AlertType.ERROR, "Erreur lors de l'ouverture du fichier",
                    "Un problème est survenu lors de la lecture des données du fichier.");
        } catch (Exception e) {
            e.printStackTrace();
            displayErrorMessage(1);
        }
    }

    public int loadPack(JSONObject data, BorderPane borderPane) {
        /*
         * 0 = Success
         * 1 = Loading error
         * 2 = Graphics error
         */
        try {
            resetPack();
            maxScore = 0;
            for (Object obj : (JSONArray) data.get("exercises")) {
                JSONObject jsonExo = (JSONObject) obj;
                exercises.add(jsonExo);
                maxScore += (Double) jsonExo.get("coef");
            }
            ExerciseConfig config = new ExerciseConfig(exercises.get(0), 0, exercises.size(), 0.f, maxScore);
            return loadExercise(borderPane, config);
        }
        catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    public void goToExercise(BorderPane borderPane, int number) {

        if (number < 0 || number >= exercises.size())
            return;

        float currentScore = currentController == null ? 0.f : currentController.managerConnection(this).getTotalScore();
        if (loadedPanes.containsKey(number) && loadedControllers.containsKey(number)) {
            borderPane.setCenter(loadedPanes.get(number));
            currentController = loadedControllers.get(number);
            currentController.updateScore(currentScore);
            return;
        }

        ExerciseConfig config = new ExerciseConfig(exercises.get(number), number, exercises.size(), currentScore, maxScore);

        int result = loadExercise(borderPane, config);
        if (result != 0)
            displayErrorMessage(result);
    }

    // PRIVATE

    private int loadExercise(BorderPane borderPane, ExerciseConfig config) {

        FXMLLoader fxmlLoader = new FXMLLoader(PyzzleMain.class.getResource("exercise-view.fxml"));
        try {
            if (!paneSaving) {
                if (currentController != null)
                    currentController.delete();
                borderPane.setCenter(null);
                System.gc();
            }
            AnchorPane pane = fxmlLoader.load();
            ExerciseController controller = fxmlLoader.getController();
            if (!controller.loadConfig(config))
                return 3;
            currentController = controller;
            currentController.managerConnection(this);
            if (paneSaving) {
                loadedPanes.put(config.exerciseNumber(), pane);
                loadedControllers.put(config.exerciseNumber(), currentController);
            }
            borderPane.setCenter(pane);
            return 0;
        }
        catch (IOException e) {
            e.printStackTrace();
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

    private void resetPack() {

        exercises.clear();
        for (ExerciseController controller : loadedControllers.values())
            controller.delete();
        loadedControllers.clear();
        loadedPanes.clear();
        currentController = null;
    }
}
