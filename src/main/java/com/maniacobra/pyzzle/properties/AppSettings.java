package com.maniacobra.pyzzle.properties;

import java.io.File;
import java.util.ArrayList;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.maniacobra.pyzzle.utils.Utils;
import javafx.scene.control.Alert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * LISTE SETTINGS
 *
 * Auto save
 * Drag and drop
 *
 * Avancé :
 * Auto args
 * Args
 *
 * Caché :
 * Last opened
 *
 */
public class AppSettings {

    private static final AppSettings instance = new AppSettings();

    public static AppSettings getInstance() {
        return instance;
    }

    // CLASS

    public String userName = "";
    public boolean updateName = false;
    public boolean autoSave = true;
    public boolean dragAndDrop = true;
    public boolean autoArgs = true;
    public ArrayList<String> terminalArgs = new ArrayList<>();
    public String lastOpenedPath = null;

    private AppSettings() {
    }

    @SuppressWarnings("unchecked")
    public void save() {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userName", userName);
        jsonObject.put("updateName", updateName);
        jsonObject.put("autoSave", autoSave);
        jsonObject.put("dragAndDrop", dragAndDrop);
        jsonObject.put("autoArgs", autoArgs);
        jsonObject.put("lastOpened", lastOpenedPath);

        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(terminalArgs);
        jsonObject.put("terminalArgs", jsonArray);

        try (FileWriter file = new FileWriter(FilePaths.getInstance().getSettingsFile())) {
            file.write(jsonObject.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
            Utils.systemAlert(Alert.AlertType.ERROR, "Pyzzle : Impossible de sauvegarder les paramètres",
                    "Une erreur inconnue s'est produite lors de la sauvegarde des paramètres, vérifiez les permissions.");
        }
    }

    private boolean getBool(JSONObject jsonObject, String key, boolean defaultVal) {
        if (!jsonObject.containsKey(key))
            return defaultVal;
        if (jsonObject.get(key) instanceof Boolean) {
            return (boolean) jsonObject.get(key);
        }
        return defaultVal;
    }

    private String getString(JSONObject jsonObject, String key, String defaultVal) {
        if (!jsonObject.containsKey(key))
            return defaultVal;
        if (jsonObject.get(key) instanceof String) {
            return (String) jsonObject.get(key);
        }
        return defaultVal;
    }

    public void load() {

        File file = FilePaths.getInstance().getSettingsFile();
        if (!file.exists()) {
            System.out.println("Settings files doesn't exist, creating...");
            save();
            return;
        }

        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(file)) {
            Object obj = parser.parse(reader);

            JSONObject jsonObject = (JSONObject) obj;

            userName = getString(jsonObject, "userName", userName);
            updateName = getBool(jsonObject, "updateName", updateName);
            autoSave = getBool(jsonObject, "autoSave", autoSave);
            dragAndDrop = getBool(jsonObject, "dragAndDrop", dragAndDrop);
            autoArgs = getBool(jsonObject, "autoArgs", autoArgs);
            lastOpenedPath = getString(jsonObject, "lastOpenedPath", lastOpenedPath);

            terminalArgs.clear();
            JSONArray jsonArray = (JSONArray) jsonObject.get("terminalArgs");
            for (Object item : jsonArray) {
                terminalArgs.add((String) item);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Utils.systemAlert(Alert.AlertType.ERROR, "Pyzzle : Impossible de charger les paramètres",
                    "Une erreur inconnue s'est produite lors du chargement des paramètres, vérifiez les permissions du logiciel.");
        } catch (ParseException e) {
            e.printStackTrace();
            Utils.systemAlert(Alert.AlertType.ERROR, "Pyzzle : Impossible de charger les paramètres",
                    "Une erreur inconnue s'est produite lors du chargement des paramètres, le fichier est peut-être corrompu.");
        }
        save();
    }
}