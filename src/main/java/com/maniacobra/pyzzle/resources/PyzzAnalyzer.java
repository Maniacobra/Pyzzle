package com.maniacobra.pyzzle.resources;

import com.maniacobra.pyzzle.utils.Popups;
import com.maniacobra.pyzzle.utils.Utils;
import javafx.scene.control.Alert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PyzzAnalyzer {

    // Static

    private static final PyzzAnalyzer instance = new PyzzAnalyzer();

    public static PyzzAnalyzer getInstance() {
        return instance;
    }

    // Class

    private AnalysisResult AnalyzeSingleFile(File file) {

        try {
            String strData;
            if (file.getName().endsWith(".json"))
                strData = PyzzFileManager.getInstance().readNormal(file);
            else
                strData = PyzzFileManager.getInstance().decode(file);

            JSONParser parser = new JSONParser();
            JSONObject data = (JSONObject) parser.parse(strData);
            JSONObject completion = (JSONObject) data.get("completion");
            JSONArray exercises = (JSONArray) completion.get("exercises");
            // File data
            String packName = (String) data.get("pack_name");
            int examMode = Utils.getInt(data, "strict_mode");
            UUID uuid = UUID.fromString((String) data.get("uuid"));
            String version = (String) data.get("pyzzle_version");
            // Completion data
            int nbWin = 0;
            int nbEmpty = 0;
            float totalScore = Utils.getFloat(completion, "total_score");
            int fileAttempts = Utils.getInt(completion, "attempt");
            String userName = (String) completion.get("user_name");
            ArrayList<ExerciseSummary> summaries = new ArrayList<>();
            // Exercises
            for (Object obj : exercises) {
                JSONObject ex = (JSONObject) obj;
                if (ex.containsKey("empty"))
                    nbEmpty++;
                else {
                    float score = Utils.getFloat(ex, "score");
                    boolean win = (boolean) ex.get("win");
                    int attemptsLeft = Utils.getInt(ex, "attempts");
                    nbWin += win ? 1 : 0;
                    summaries.add(new ExerciseSummary(score, win, attemptsLeft));
                }
            }
            return new AnalysisResult(packName, examMode == 2, uuid, version, userName, nbWin,
                    nbEmpty, totalScore, fileAttempts, summaries);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error reading file : " + file.getName());
        }
        return null;
    }

    public boolean AnalyzeFiles(List<File> files) {

        File outDir = null;
        StringBuilder outData = new StringBuilder();
        outData.append("Nom,Exercices non commencés,Score,Exercices complets,Nombre d'ouvertures du fichier\n");
        int success = 0;
        for (File file : files) {
            if (outDir == null)
                outDir = file.getParentFile();
            AnalysisResult aResult = AnalyzeSingleFile(file);
            if (aResult != null) {
                success++;
                outData.append(String.format("%s,%d,%f,%d,%d\n", aResult.userName(), aResult.nbEmpty(), aResult.score(), aResult.nbWin(), aResult.fileAttempts()));
            }
        }
        if (success == 0)
            return false;

        // Saving
        String outPath = outDir.getAbsolutePath() + "/correction.csv";
        try (FileWriter outWriter = new FileWriter(outPath)) {
            outWriter.write(outData.toString());
            Utils.systemAlert(Alert.AlertType.INFORMATION, "Analyse effectuée",
                    "Fichier .csv sauvegardé : " + outPath);
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
