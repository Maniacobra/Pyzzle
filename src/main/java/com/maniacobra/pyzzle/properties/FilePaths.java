package com.maniacobra.pyzzle.properties;

import com.maniacobra.pyzzle.utils.Utils;

import java.io.File;
import java.io.IOException;

public class FilePaths {

    private final String localFilesPath;

    private final String defaultLocalFiles;
    private final String logFile;
    private final String pythonExePath;
    private final String executePy;
    private final String tempPy;
    private final String packFolder;
    private final String settingsPath;
    private final String idsPath;
    private final boolean isLinux;

    private static FilePaths instance = null;

    public static boolean load() {
        instance = new FilePaths();
        return instance.createLocalFolder();
    }

    public static FilePaths getInstance() {
        if (instance == null)
            instance = new FilePaths();
        return instance;
    }

    // NON STATIC

    public FilePaths() {

        isLinux = System.getProperty("os.name").toLowerCase().contains("linux");
        if (isLinux) {
            localFilesPath = "./";
        }
        else {
            String localFolder = System.getenv("APPDATA");
            if (localFolder == null)
                localFolder = System.getProperty("user.home");
            localFolder = localFolder.replace("\\", "/");
            localFilesPath = localFolder + "/Pyzzle/";
        }
        defaultLocalFiles = "DefaultAppDataFiles";
        logFile = localFilesPath + "log.txt";

        // Pre-integrated Python
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("windows"))
            pythonExePath = "python/python.exe";
        else
            pythonExePath = null;

        String pyFilesPath = localFilesPath + ".py/";
        executePy = pyFilesPath + "execute.py";
        tempPy = pyFilesPath + "temp.py";
        packFolder = localFilesPath + "Packs/";

        settingsPath = localFilesPath + "settings.json";
        idsPath = localFilesPath + "info";
    }

    public File getLogFile() {
        return new File(logFile);
    }

    public File getTempPyFile() {
        return new File(tempPy);
    }

    public File getPackFile() {
        return new File(packFolder);
    }

    public File getSettingsFile() {
        return new File(settingsPath);
    }

    public File getIdsFile() {
        return new File(idsPath);
    }

    public String getExecutePyPath() {
        return executePy;
    }

    public String getPythonExePath() {
        return pythonExePath;
    }

    private boolean createLocalFolder() {

        if (!isLinux) {
            System.out.println("Checking local folder at : " + localFilesPath);
            File dir = new File(localFilesPath);
            if (!dir.exists()) {
                try {
                    Utils.copyDirectory(new File(defaultLocalFiles), new File(localFilesPath));
                    System.out.println("Folder created.");
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return true;
    }
}
