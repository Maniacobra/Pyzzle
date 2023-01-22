package com.maniacobra.pyzzle.models;

import com.maniacobra.pyzzle.properties.AppStyle;
import com.maniacobra.pyzzle.utils.Popups;
import com.maniacobra.pyzzle.utils.Utils;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.*;

import static com.maniacobra.pyzzle.models.ExecutionResult.*;
import static com.maniacobra.pyzzle.models.ExecutionResult.FATAL;
import static com.maniacobra.pyzzle.utils.TextUtils.addToTextFlow;

public class CodeRunner {
    // Singleton
    private static final CodeRunner instance = new CodeRunner();

    public static CodeRunner getInstance() {
        return instance;
    }

    private final Stack<Integer> errorLines = new Stack<>();
    private String errorType = null;

    public Stack<Integer> getErrorLines() {
        return errorLines;
    }

    public String getErrorType() {
        return errorType;
    }

    public boolean hasError() {
        return !errorLines.isEmpty();
    }

    public void reset() {

        errorLines.clear();
        errorType = null;
    }

    public void prepareCode(String pyCode, List<String> inputs) {

        // Make code
        String[] lines = pyCode.split("\n");
        StringBuilder pyBuilder = new StringBuilder();
        pyBuilder.append("def run(");
        for (String input : inputs) {
            pyBuilder.append(input);
            pyBuilder.append("=0, ");
        }
        pyBuilder.append("):\n\tprint(\"#####\")");
        for (String line : lines) {
            pyBuilder.append("\n\t");
            pyBuilder.append(line);
        }
        if (lines.length == 1 && Objects.equals(lines[0], ""))
            pyBuilder.append("pass");

        try {
            // Python file
            BufferedWriter out = new BufferedWriter(new FileWriter("py/temp.py"));
            out.write(pyBuilder.toString());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Only 1 test */
    public ExecutionResult runCode(TextFlow consoleText, Text dsDeclaration, List<String> dataset, List<String> objective) {

        ExecutionResult result = SUCCESS;
        dsDeclaration.setFill(AppStyle.Colors.resultValid);
        consoleText.getChildren().add(dsDeclaration);
        // Build process
        ArrayList<String> params = getParams();
        if (params == null)
            return FATAL;
        params.add("py/execute.py");
        // Launch process
        if (dataset != null)
            params.addAll(dataset);
        try {
            ProcessBuilder pb = new ProcessBuilder(params);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            // Interpret results
            List<String> lines = in.lines().toList();
            int lineNum = -1;
            int errorState = 0;
            for (String line : lines) {
                if (lineNum == -1) {
                    // First line
                    if (!line.equals("#####")) {
                        if (line.equals("!!!!!")) {
                            dsDeclaration.setFill(AppStyle.Colors.resultError);
                            errorState = 1;
                        }
                        else {
                            System.out.println("[ERROR] Invalid first line for Python execution : " + line);
                            return FATAL;
                        }
                    }
                }
                else {
                    // Other lines
                    if (errorState != 0) {
                        if (errorState == 1) {
                            errorType = line;
                            errorState++;
                        }
                        else if (errorState == 2) {
                            try {
                                int lineNb = Integer.parseInt(line) - 1;
                                errorLines.add(lineNb);
                                addToTextFlow(consoleText, String.format("[ Erreur de type '%s' à la line n°%d du code. ]\n\n", errorType, lineNb), AppStyle.Colors.resultError, false);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                            return EXCEPTION;
                        }
                    }
                    else {
                        if (line.equals("!!!!!")) {
                            dsDeclaration.setFill(AppStyle.Colors.resultError);
                            errorState = 1;
                        } else {
                            Color color = null;
                            if (lineNum >= objective.size()) {
                                color = AppStyle.Colors.resultUnperfect;
                                if (result == SUCCESS) {
                                    result = OVERFLOW;
                                    dsDeclaration.setFill(color);
                                }
                            } else if (!objective.get(lineNum).equals(line)) {
                                color = AppStyle.Colors.resultWrong;
                                result = WRONG;
                                dsDeclaration.setFill(color);
                            }
                            addToTextFlow(consoleText, line + '\n', color, false);
                        }
                    }
                }
                lineNum++;
            }
            if (lineNum == -1) {
                System.out.println("[ERROR] Empty results for Python execution");
                return FATAL;
            }
            while (lineNum < objective.size()) {
                if (result != WRONG) {
                    result = INCOMPLETE;
                    dsDeclaration.setFill(AppStyle.Colors.resultWrong);
                }
                addToTextFlow(consoleText, "(vide)\n", AppStyle.Colors.resultEmpty, false);
                lineNum++;
            }
            addToTextFlow(consoleText, "\n");
        } catch (Exception e) {
            e.printStackTrace();
            return FATAL;
        }
        return result;
    }

    public void exceptionPopup() {

        if (errorType == null)
            return;
        String title = "Erreur de type '" + errorType + "'";
        String contentIntro = String.format("Erreur de type '%s' à la ligne %d", errorType, errorLines.peek());
        String content = switch (errorType) {
            case "SyntaxError" ->
                    "Erreur de syntaxe :\nCette erreur se produit lorsque votre code est mal formaté,\nvérifiez les opérateurs et le positionnement des mot-clés,\nvotre code n'a pas pu s'exécuter.\n(Notez que la ligne identifiée de l'erreur n'est pas\ntoujours celle qui cause problème.)";
            case "IndentationError" ->
                    "Erreur d'indentation :\nCette erreur se produit lorsque votre code contient des tabulations positionnées ou manquantes, votre code n'a pas pu s'exécuter.";
            case "NameError" ->
                    "Erreur de nom :\nCette erreur se produit lorsque votre code essaie de\nrécupérer la valeur d'une variable qui n'existe pas.";
            case "TypeError" ->
                    "Erreur de type :\nCette erreur se produit lorsque votre code essaie d'effectuer\nune opération sur une variable du mauvais type.";
            default -> "Vérifiez votre code.";
        };
        Popups.showPopup(title, contentIntro, content);
    }

    public boolean pythonTest() {

        // BASIC PYTHON TEST

        ArrayList<String> params = getParams();
        if (params == null)
            return false;
        params.add("--version");
        boolean installed = true;
        try {
            ProcessBuilder pb = new ProcessBuilder(params);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            List<String> lines = in.lines().toList();
            if (lines.size() == 0 || !lines.get(0).contains("Python 3."))
                installed = false;
        } catch (Exception e) {
            e.printStackTrace();
            installed = false;
        }
        if (!installed) {
            Utils.systemAlert(Alert.AlertType.ERROR, "Python 3 ne semble pas être installé",
                    """
                            Pyzzle n'est pas parvenu à exécuter Python sur votre ordinateur.
                            Veuillez télécharger et installer la dernière version de Python (www.python.org).
                            Si Python est déjà installé, essayez de configurer manuellement les arguments de commande dans le menu Préférences.
                            Vous ne pouvez pas charger d'exercice.""");
            return false;
        }

        // INSTALLATION TEST

        prepareCode("print(sample + 1)", List.of("sample"));
        ExecutionResult result = runCode(new TextFlow(), new Text(), List.of("5"), List.of("6"));
        if (result != SUCCESS) {
            Utils.systemAlert(Alert.AlertType.ERROR, "Erreur d'installation",
                    """
                            Pyzzle semble avoir des difficultés pour interagir avec Python correctement.
                            Cela peut être lié à une mauvaise installation du logiciel, une mauvaise version de Python,
                            des droits insuffisants ou autre problème du système.
                            Vous ne pouvez pas charger d'exercice.""");
            return false;
        }
        return true;
    }

    public ArrayList<String> getParams() {

        ArrayList<String> params = new ArrayList<>();
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("linux")) {
            // LINUX
            params.add("python3");
        }
        else if (osName.contains("windows")) {
            // WINDOWS
            params.add("cmd");
            params.add("/c");
            params.add("py");
        }
        else if (osName.contains("mac")) {
            // MACINTOSH
            params.add("python");
        }
        else {
            Utils.systemAlert(Alert.AlertType.ERROR, "Système d'exploitation inconnu",
                    "Système d'exploitation inconnu : " + osName +
                            "\nPython ne peut pas être exécuté, veuillez paramétrer manuellement les arguments qu'utilise Pyzzle pour exécuter Python.");
            return null;
        }
        return params;
    }
}