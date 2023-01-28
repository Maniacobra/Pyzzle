package com.maniacobra.pyzzle.properties;

public class AppProperties {
    public static final boolean debugMode = false;
    public static final boolean logEnabled = false;

    public static final String name = "Pyzzle";
    public static final String version = "indev";

    public static final String localFilesPath = System.getenv("APPDATA") + "\\Pyzzle\\";

    public static final String defaultLocalFiles = "DefaultAppDataFiles";
    public static final String logFile = AppProperties.localFilesPath + "log.txt";
    public static final String pythonExePath = "python\\python.exe";
    public static final String pyFilesPath = localFilesPath + ".py\\";
    public static final String executePy = pyFilesPath + "execute.py";
    public static final String tempPy = pyFilesPath + "temp.py";
    public static final String packFolder = localFilesPath + "Packs\\";

    public static final String extension = "pyzz";
    public static final String openedExtension = "spyzz";
}
