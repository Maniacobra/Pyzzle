package com.maniacobra.pyzzle;

import com.maniacobra.pyzzle.properties.AppProperties;
import com.maniacobra.pyzzle.properties.FilePaths;
import com.maniacobra.pyzzle.views.PyzzleMain;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class Launcher {

    public static PrintStream output = System.out;

    public static void main(String[] args) {

        // Log
        if (AppProperties.logEnabled) {
            try {
                File logFile = FilePaths.getInstance().getLogFile();
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

/* TO-DO
 *
 * === TO-DO PROTOTYPE ===
 *
 * Exo conditions
 * Exo exam
 *
 * Build release
 *
 * === POST PROTOTYPE ===
 *
 * - Petits trucs -
 *
 * Bulles d'aide pour chaque exercice
 * Logo
 * Site web
 * Machintosh
 * Introduction Pyzzle
 *
 * - Gros trucs -
 *
 * Playtesting
 * Éditeur d'énoncés
 * Améliorations graphiques
 * Beaucoup beaucoup plus de packs d'exercices
 * Page d'accueil
 *
 * - Bugs -
 *
 * Manque de contrôle d'erreurs
 * Caractères spéciaux
 * Changement de disposition de fenêtre à chaque exercice
 *
 */