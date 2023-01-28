package com.maniacobra.pyzzle;

import com.maniacobra.pyzzle.properties.AppProperties;
import com.maniacobra.pyzzle.utils.Utils;
import com.maniacobra.pyzzle.views.PyzzleMain;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class Launcher {

    public static PrintStream output = System.out;

    public static void main(String[] args) {

        // Create local folders
        File files = new File(AppProperties.localFilesPath);
        if (!files.exists()) {
            try {
                Utils.copyDirectory(new File(AppProperties.defaultLocalFiles), new File(AppProperties.localFilesPath));
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        // Log
        if (AppProperties.logEnabled) {
            try {
                File logFile = new File(AppProperties.logFile);
                logFile.delete();
                logFile.createNewFile();
                output = new PrintStream(logFile);
                System.setOut(output);
            } catch (IOException e) {
                e.printStackTrace(Launcher.output);
            }
        }

        // LAUNCH
        PyzzleMain.main(args);
    }
}
