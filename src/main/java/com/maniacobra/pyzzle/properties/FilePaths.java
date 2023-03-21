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
    private final boolean isLinux;

    private static FilePaths instance = new FilePaths();

    public static boolean load() {
        instance = new FilePaths();
        return instance.createLocalFolder();
    }

    public static FilePaths getInstance() {
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
        pythonExePath = "python/python.exe";

        String pyFilesPath = localFilesPath + ".py/";
        executePy = pyFilesPath + "execute.py";
        tempPy = pyFilesPath + "temp.py";
        packFolder = localFilesPath + "Packs/";
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

    public String getExecutePyPath() {
        return executePy;
    }

    public String getPythonExePath() {
        return pythonExePath;
    }

    private boolean createLocalFolder() {

        if (!isLinux) {
            System.out.println("Checking local folder at : " + localFilesPath);
            File files = new File(localFilesPath);
            if (!files.exists()) {
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
