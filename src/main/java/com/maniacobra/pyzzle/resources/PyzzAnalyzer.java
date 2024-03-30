package com.maniacobra.pyzzle.resources;

import com.maniacobra.pyzzle.utils.Utils;
import javafx.scene.control.Alert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
            JSONArray untouchedExercises = (JSONArray) data.get("exercises");
            // File data
            String packName = (String) data.get("pack_name");
            int examMode = Utils.getInt(data, "strict_mode");
            UUID uuid = UUID.fromString((String) data.get("uuid"));
            String version = (String) data.get("pyzzle_version");
            float maxScore = 0.f;
            for (Object obj : untouchedExercises) {
                JSONObject ex = (JSONObject) obj;
                float coef = Utils.getFloat(ex, "coef");
                maxScore += coef;
            }
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
            return new AnalysisResult(file.getName(), packName, examMode == 2, uuid, maxScore, version,
                    userName, nbWin, nbEmpty, totalScore, fileAttempts, summaries);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error reading file : " + file.getName());
        }
        return null;
    }

    public boolean AnalyzeFiles(List<File> files) {

        // Analysis
        boolean suspect = false;
        ArrayList<AnalysisResult> results = new ArrayList<>();
        HashMap<String, UUID> packUUIDs = new HashMap<>();
        File outDir = null;
        int success = 0;
        StringBuilder failures = new StringBuilder();
        for (File file : files) {
            if (outDir == null)
                outDir = file.getParentFile();
            AnalysisResult aResult = AnalyzeSingleFile(file);
            if (aResult != null) {
                success++;
                results.add(aResult);
                UUID uuid = packUUIDs.get(aResult.packName());
                if (uuid == null)
                    packUUIDs.put(aResult.packName(), aResult.uuid());
                else if (!uuid.equals(aResult.uuid())) {
                    System.out.printf("Incoherent UUID ! %s -> %s ; %s%n", aResult.packName(), uuid, aResult.uuid());
                    suspect = true;
                }
            }
            else
                failures.append(file.getName()).append("\n");
        }
        if (success == 0)
            return false;

        // Warnings
        if (suspect)
            Utils.systemAlert(Alert.AlertType.WARNING, "Fichiers suspects", "Plusieurs fichiers concernant le même pack on des identifiants différents, " +
                    "cela pourrait indiquer des tentatives de triche. Les identifiants seront affichés dans la correction, " +
                    "comparez les fichiers avec noms de packs identiques mais identifiants différents.");

        // Writing
        StringBuilder outData = new StringBuilder();
        outData.append("Nom du fichier,Nom du pack,Nom de l'utilisateur,Exercices non commencés,Score"
                + ",Score /20,Exercices complets,Nombre d'ouvertures du fichier,Examen ?,Version de Pyzzle");
        if (suspect)
            outData.append(",Identifiants");
        outData.append("\n");
        for (AnalysisResult aResult : results) {
            outData.append(String.format("%s,%s,%s,%d,%s,%s,%d,%d,%s,%s%s\n", aResult.fileName(), aResult.packName(), aResult.userName(), aResult.nbEmpty(),
                    Utils.nbToStr(aResult.score()), Utils.nbToStr(aResult.getOutOf20()), aResult.nbWin(), aResult.fileAttempts(), aResult.version(),
                    aResult.examMode() ? "Oui" : "Non", suspect? "," + aResult.uuid() : ""));
        }

        // Saving
        String outPath = outDir.getAbsolutePath() + "/correction.csv";
        try (FileWriter outWriter = new FileWriter(outPath)) {
            outWriter.write(outData.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Utils.systemAlert(Alert.AlertType.INFORMATION, "Correction effectuée",
        "Fichier sauvegardé, vous pouvez l'ouvrir avec Excel ou autre logiciel : " + outPath +
                (failures.isEmpty() ? "" : "\n\nATTENTION, il y a eu un problème lors du chargement de ces fichiers :\n\n" + failures));
        return true;
    }
}
