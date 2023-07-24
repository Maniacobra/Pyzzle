package com.maniacobra.pyzzle.models;

import com.maniacobra.pyzzle.Launcher;
import com.maniacobra.pyzzle.controllers.ExerciseController;
import com.maniacobra.pyzzle.controllers.IntroController;
import com.maniacobra.pyzzle.properties.AppProperties;
import com.maniacobra.pyzzle.properties.AppSettings;
import com.maniacobra.pyzzle.resources.PyzzFileManager;
import com.maniacobra.pyzzle.utils.Utils;
import com.maniacobra.pyzzle.views.PyzzleMain;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
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

    private enum OpeningResult {
        OK,
        UNKNOWN_ERROR,
        OPENING_ERROR,
        PARSING_ERROR,
        DATA_ERROR,
        SAVING_ERROR
    }

    // Pack infos
    private float maxScore;
    private int packLength;
    private String packName;
    private String packFileName;
    private String authorName;
    private String userName;
    private int strictMode;

    // Pack completion memory
    private JSONObject loadedData = null;
    private final ArrayList<JSONObject> exercises = new ArrayList<>();
    private ExerciseController currentController = null;
    private final HashMap<Integer, JSONObject> savedCompletion = new HashMap<>();

    // Saving
    private File saveFile = null;

    // UI
    private BorderPane mainPane = null;
    private AnchorPane currentPane = null;
    private BorderPane introPane = null;

    // Pane saving
    private final boolean paneSaving = false;
    private final HashMap<Integer, ExerciseController> loadedControllers = new HashMap<>();
    private final HashMap<Integer, AnchorPane> loadedPanes = new HashMap<>();

    public ExerciseManager() {
        setDefaultInfos();
    }

    private void setDefaultInfos() {
        maxScore = 0;
        packLength = 0;
        packName = "Sans nom";
        packFileName = "Sans_nom";
        authorName = null;
        userName = null;
        strictMode = 0;
    }

    public boolean openFile(File file, BorderPane mainPane) {

        this.mainPane = mainPane;
        System.out.println("Opening : " + file.getAbsolutePath());
        try {
            String data;
            if (file.getName().endsWith(".json"))
                data = PyzzFileManager.getInstance().readNormal(file);
            else
                data = PyzzFileManager.getInstance().decode(file);
            JSONParser parser = new JSONParser();
            JSONObject jsonData = (JSONObject) parser.parse(data);
            String fileType = jsonData.get("file_type").toString();
            boolean hasCompletion = fileType.equals("opened_pack");
            if (fileType.equals("pack") || hasCompletion) {
                OpeningResult result = loadPack(jsonData, hasCompletion);
                if (result != OpeningResult.OK) {
                    displayErrorMessage(result);
                    return false;
                }
                else {
                    // Pack is working !
                    packFileName = file.getName();
                    AppSettings.getInstance().lastOpenedPath = file.getAbsolutePath();
                    if (hasCompletion)
                        saveFile = file;
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace(Launcher.output);
            displayErrorMessage(OpeningResult.OPENING_ERROR);
        } catch (ParseException e) {
            e.printStackTrace(Launcher.output);
            displayErrorMessage(OpeningResult.PARSING_ERROR);
        } catch (Exception e) {
            e.printStackTrace(Launcher.output);
            displayErrorMessage(OpeningResult.UNKNOWN_ERROR);
        }
        return false;
    }

    public void goToExercise(BorderPane mainPane, int number) {

        this.mainPane = mainPane;

        if (number < 0 || number >= exercises.size())
            return;

        savedCompletion.put(currentController.getModel().getExerciseNumber(), currentController.getModel().getJson());

        float currentScore = currentController == null ? 0.f : currentController.getModel().getTotalScore();
        // Panesaving
        if (loadedPanes.containsKey(number) && loadedControllers.containsKey(number)) {
            mainPane.setCenter(loadedPanes.get(number));
            currentController = loadedControllers.get(number);
            currentController.updateScore(currentScore);
            return;
        }

        JSONObject exerciseCompletion = savedCompletion.get(number);
        ExerciseConfig config = new ExerciseConfig(exercises.get(number), number, exercises.size(), currentScore, maxScore, exerciseCompletion);

        OpeningResult result = loadExercise(config, true);
        if (result != OpeningResult.OK)
            displayErrorMessage(result);
    }

    public boolean saveData(File file) {
        saveFile = file;
        return saveData();
    }

    @SuppressWarnings("unchecked")
    public boolean saveData() {

        updateName();
        if (loadedData == null || saveFile == null)
            return false;

        savedCompletion.put(currentController.getModel().getExerciseNumber(), currentController.getModel().getJson());

        // Make json
        JSONObject data = loadedData;
        data.put("file_type", "opened_pack");
        JSONObject completion = new JSONObject();
        completion.put("total_score", currentController.getModel().getTotalScore());
        completion.put("last_panel", currentController.getModel().getExerciseNumber());
        completion.put("user_name", userName);

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
        try {
            PyzzFileManager.getInstance().encode(saveFile, data.toJSONString());
            AppSettings.getInstance().lastOpenedPath = saveFile.getAbsolutePath();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            displayErrorMessage(OpeningResult.SAVING_ERROR);
        }
        return false;
    }

    public void startFirstExercise() {
        saveData();
        mainPane.setCenter(currentPane);
        introPane = null;
        System.gc();
    }

    public String getSaveFileSuggestion() {

        String[] splitted = packFileName.split("\\.");
        String fileName = splitted.length > 0 ? splitted[0] : packFileName;
        if (userName == null)
            return fileName + "." + AppProperties.openedExtension;
        return fileName + " " + Utils.convertToFileName(userName) + "." + AppProperties.openedExtension;
    }

    public boolean hasSaveFile() {
        return saveFile != null;
    }

    public Pane getCurrentPane() {
        return introPane != null ? introPane : currentPane;
    }

    public void setUserName(String userName) {
        if (userName.length() == 0)
            this.userName = null;
        else
            this.userName = userName;
    }

    public boolean isExamMode() {
        return strictMode >= 2;
    }

    public boolean hasUserName() {
        return userName != null;
    }

    // PRIVATE

    private OpeningResult loadPack(JSONObject jsonData, boolean hasCompletion) {
        resetPack(mainPane);
        try {
            loadedData = jsonData;
            // Infos
            packName = (String) jsonData.get("pack_name");
            authorName = (String) jsonData.get("author");
            strictMode = Utils.getInt(jsonData, "strict_mode");
            // Load exercises
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
            JSONObject exerciseCompletion = null;
            if (hasCompletion) {
                JSONObject completion = (JSONObject) jsonData.get("completion");
                starting = Utils.getInt(completion, "last_panel");
                totalScore = Utils.getFloat(completion, "total_score");
                userName = (String) completion.get("user_name");
                int i = 0;
                for (Object completed : (JSONArray) completion.get("exercises")) {
                    savedCompletion.put(i, (JSONObject) completed);
                    i++;
                }
                exerciseCompletion = (JSONObject) ((JSONArray) completion.get("exercises")).get(starting);
            }
            // Start
            ExerciseConfig firstExercise = new ExerciseConfig(exercises.get(starting), starting, exercises.size(), totalScore, maxScore, exerciseCompletion);
            loadExercise(firstExercise, hasCompletion);
            if (strictMode >= 2) {
                AppSettings.getInstance().autoSave = true;
                AppSettings.getInstance().updateName = false;
            }
            if (!hasCompletion)
                loadIntro(mainPane);
            else {
                StringBuilder builder = new StringBuilder();
                if (packName != null && !packName.isEmpty())
                    builder.append("Titre du pack d'exercices :\n").append(packName).append("\n\n");
                if (userName != null && !userName.isEmpty())
                    builder.append("Nom de l'utilisateur : ").append(userName).append("\n\n");
                if (authorName != null && !authorName.isEmpty())
                    builder.append("Nom du créateur du pack : ").append(authorName);
                if (!builder.isEmpty())
                    Utils.systemAlert(Alert.AlertType.INFORMATION, "Pyzzle : Informations sur le pack", builder.toString());
            }

            return OpeningResult.OK;
        }
        catch (NullPointerException e) {
            e.printStackTrace(Launcher.output);
            return OpeningResult.DATA_ERROR;
        }
        catch (Exception e) {
            e.printStackTrace();
            return OpeningResult.UNKNOWN_ERROR;
        }
    }

    private OpeningResult loadExercise(ExerciseConfig config, boolean enableUI) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(PyzzleMain.class.getResource("exercise-view.fxml"));
            if (!paneSaving) {
                if (currentController != null)
                    currentController.clearMemory();
                if (enableUI)
                    mainPane.setCenter(null);
                System.gc();
            }
            AnchorPane pane = fxmlLoader.load();
            currentPane = pane;
            ExerciseController controller = fxmlLoader.getController();
            if (!controller.loadConfig(config))
                return OpeningResult.DATA_ERROR;
            currentController = controller;
            currentController.setManger(this);
            if (paneSaving) {
                loadedPanes.put(config.exerciseNumber(), pane);
                loadedControllers.put(config.exerciseNumber(), currentController);
            }
            if (enableUI) {
                mainPane.setCenter(pane);
                updateName();
            }
            return OpeningResult.OK;
        }
        catch (Exception e) {
            e.printStackTrace(Launcher.output);
            return OpeningResult.UNKNOWN_ERROR;
        }
    }

    private void loadIntro(BorderPane borderPane) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(PyzzleMain.class.getResource("intro-view.fxml"));
        introPane = fxmlLoader.load();
        IntroController controller = fxmlLoader.getController();
        controller.init(this, packName, authorName, strictMode > 0, strictMode >= 2);
        borderPane.setCenter(introPane);
    }

    private void updateName() {
        AppSettings settings = AppSettings.getInstance();
        String prefix = "Nom inscrit : ";
        if (settings.updateName && strictMode < 2 && !settings.userName.equals(userName)) {
            userName = settings.userName;
            currentController.updateUserName(prefix + userName);
            Utils.systemAlert(Alert.AlertType.INFORMATION, "Nom changé",
                    "Le NOM / Prénom inscrit dans ce Pack d'exercices vient d'être automatiquement mis à jour par les paramètres :\n" + userName);
        }
        else
            currentController.updateUserName(prefix + userName);
    }

    private void displayErrorMessage(OpeningResult oResult) {

        switch (oResult) {
            case UNKNOWN_ERROR -> Utils.systemAlert(Alert.AlertType.ERROR, "Pyzzle : Erreur",
                    "Une erreur inconnue, interne au logiciel, s'est produite lors du chagement de l'exercice.");
            case OPENING_ERROR -> Utils.systemAlert(Alert.AlertType.ERROR, "Pyzze : Erreur lors de l'ouverture du fichier",
                    "Pyzzle n'est pas parvenu à ouvrir ce fichier, vérifiez les permissions du programme et l'accessibilité du fichier.");
            case PARSING_ERROR -> Utils.systemAlert(Alert.AlertType.ERROR, "Pyzzle : Erreur lors du chargement de l'exercice",
                    "Le fichier semble être corrompu ou de format invalide. Impossible d'ouvrir");
            case DATA_ERROR -> Utils.systemAlert(Alert.AlertType.ERROR, "Pyzzle : Erreur lors du chargement de l'exercice",
                    "Le fichier semble avoir des informations manquantes, mal formattées ou invalides. Impossible d'ouvrir.");
            case SAVING_ERROR -> Utils.systemAlert(Alert.AlertType.ERROR, "Pyzzle : Impossible de sauvegarder",
                    "Une erreur inconnue s'est produite lors de la sauvegarde de l'exercice, vérifiez les permissions.");
        }
    }

    private void resetPack(BorderPane borderPane) {
        // Reset memory
        exercises.clear();
        if (currentController != null) {
            currentController.clearMemory();
            currentController = null;
        }
        savedCompletion.clear();
        // Reset save
        saveFile = null;
        // Reset UI
        currentPane = null;
        introPane = null;
        borderPane.setCenter(null);
        // Reset pane saving
        for (ExerciseController controller : loadedControllers.values())
            controller.clearMemory();
        loadedControllers.clear();
        loadedPanes.clear();
        System.gc();
        // Reset infos
        setDefaultInfos();
    }
}
