package com.maniacobra.pyzzle.resources;

import com.maniacobra.pyzzle.Launcher;
import com.maniacobra.pyzzle.models.ExecutionResult;
import com.maniacobra.pyzzle.properties.AppSettings;
import com.maniacobra.pyzzle.properties.AppStyle;
import com.maniacobra.pyzzle.properties.FilePaths;
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

    // Static

    private static final int prefixLines = 2;
    private static final CodeRunner instance = new CodeRunner();

    public static CodeRunner getInstance() {
        return instance;
    }

    // Class

    private final Stack<Integer> errorLines = new Stack<>();
    private String errorType = null;
    private final Random rand = new Random();
    private long processId = 0;
    private List<String> workingCommand = null;

    private CodeRunner() {
        System.getenv("APPDATA");
    }

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

        processId = rand.nextLong();
        // Make code
        String[] lines = pyCode.split("\n");
        StringBuilder pyBuilder = new StringBuilder();
        pyBuilder.append("def run(");
        for (String input : inputs) {
            pyBuilder.append(input);
            pyBuilder.append("=0, ");
        }
        pyBuilder.append("):\n\tprint(\"##").append(processId).append("\")");
        for (String line : lines) {
            pyBuilder.append("\n\t");
            pyBuilder.append(line);
        }
        if (lines.length == 1 && Objects.equals(lines[0], ""))
            pyBuilder.append("pass");
        try {
            // Python file
            BufferedWriter out = new BufferedWriter(new FileWriter(FilePaths.getInstance().getTempPyFile()));
            out.write(pyBuilder.toString());
            out.close();
        } catch (Exception e) {
            e.printStackTrace(Launcher.output);
        }
    }

    public ExecutionResult runCode(TextFlow consoleText, Text dsDeclaration, List<String> dataset, List<String> objective) {

        ExecutionResult result = SUCCESS;
        dsDeclaration.setFill(AppStyle.Colors.resultValid);
        consoleText.getChildren().add(dsDeclaration);
        // Build process
        List<String> command = getCommand();
        if (command == null)
            return FATAL;
        ArrayList<String> commandExec = new ArrayList<>(command);
        commandExec.add(FilePaths.getInstance().getExecutePyPath());
        // Launch process
        if (dataset != null)
            commandExec.addAll(dataset);
        try {
            ProcessBuilder pb = new ProcessBuilder(commandExec);
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
                    if (!line.startsWith("##")) {
                        if (line.equals("!!!!!")) {
                            dsDeclaration.setFill(AppStyle.Colors.resultError);
                            errorState = 1;
                        }
                        else {
                            System.out.println("[ERROR] Invalid first line for Python execution : " + line);
                            return FATAL;
                        }
                    }
                    else {
                        try {
                            long number = Long.parseLong(line.substring(2));
                            if (number != processId) {
                                System.out.println("[ERROR] Invalid process identifier for Python execution : " + line);
                                return FATAL;
                            }
                        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                            e.printStackTrace(Launcher.output);
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
                                int lineNb = Integer.parseInt(line) - prefixLines;
                                errorLines.add(lineNb);
                                addToTextFlow(consoleText, String.format("[ Erreur de type '%s' à la line n°%d du code. ]\n\n", errorType, lineNb), AppStyle.Colors.resultError, false);
                            } catch (NumberFormatException e) {
                                e.printStackTrace(Launcher.output);
                            }
                            return EXCEPTION;
                        }
                    }
                    else {
                        if (line.equals("!!!!!")) {
                            dsDeclaration.setFill(AppStyle.Colors.resultError);
                            errorState = 1;
                        }
                        else {
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
            e.printStackTrace(Launcher.output);
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
                    "Erreur de syntaxe :\nCette erreur se produit lorsque votre code est mal formaté, vérifiez les opérateurs, le positionnement des mot-clés et les sauts de ligne. Votre code n'a pas pu s'exécuter.\n(Notez que la ligne identifiée de l'erreur n'est pas toujours celle qui cause problème.)";
            case "IndentationError" ->
                    "Erreur d'indentation :\nCette erreur se produit lorsque votre code contient des tabulations positionnées ou manquantes, votre code n'a pas pu s'exécuter.";
            case "NameError" ->
                    "Erreur de nom :\nCette erreur se produit lorsque votre code essaie de récupérer la valeur d'une variable qui n'existe pas.";
            case "TypeError" ->
                    "Erreur de type :\nCette erreur se produit lorsque votre code essaie d'effectuer une opération sur une variable du mauvais type.";
            default -> "Vérifiez votre code.";
        };
        Popups.showPopup(title, contentIntro, content);
    }

    public boolean pythonTest() {

        // PYTHON VERSION

        List<String> command = getCommand();
        if (command == null) {
            if (AppSettings.getInstance().autoArgs) {
                if (FilePaths.getInstance().getPythonExePath() != null) {
                    Utils.systemAlert(Alert.AlertType.ERROR, "Python 3 introuvable",
                            """
                                    La version pré-intégrée de Python de ce logiciel n'est pas accessible, et Python est introuvable dans votre système.
                                    Voici ce que vous pouvez tenter de faire :
                                    - Vérifier les permissions du logiciel (essayez d'exécuter en tant qu'administrateur).
                                    - Désinstaller et réinstaller Pyzzle.
                                    - Télécharger et installer la dernière version de Python sur votre système (www.python.org).
                                    - Si Python est déjà installé sur votre système, essayez de configurer manuellement les arguments de terminal dans le menu 'Préférences'.
                                    """);
                }
                else {
                    Utils.systemAlert(Alert.AlertType.ERROR, "Python 3 est introuvable",
                    """
                            Pyzzle installé sur un système autre que Windows a comme pré-requis que Python 3 soit installé.
                            Veuillez télécharger et installer la dernière version de Python sur votre système : www.python.org
                            Si Python est déjà installé, vous pouvez tenter de configurer manuellement les arguments de terminal dans le menu 'Préférences'.
                            Vous pouvez également vérifier les permissions du logiciel.
                            """);
                }
            }
            else
                Utils.systemAlert(Alert.AlertType.ERROR, "Pyzzle ne peut pas fonctionner",
                        """
                                Les arguments de terminal définis manuellement semblent être incorrects.
                                Veuillez aller dans le menu Préférences et changer ces arguments ou cocher 'Trouver Python automatiquement'.""");
            return false;
        }

        // INSTALLATION TEST

        prepareCode("print(sample + 1)", List.of("sample"));
        ExecutionResult result = runCode(new TextFlow(), new Text(), List.of("5"), List.of("6"));
        if (result != SUCCESS) {
            Utils.systemAlert(Alert.AlertType.ERROR, "Pyzzle ne peut pas fonctionner",
                    """
                            Pyzzle semble avoir des difficultés pour fonctionner correctement.
                            Veuillez fermer le programme, puis l'exécuter en tant qu'administrateur.
                            Si c'est déjà le cas, alors le logiciel est peut-être mal installé, essayez donc une ré-installation.""");
            return false;
        }
        System.out.println("Script execution is working !");
        return true;
    }

    public List<String> getCommand() {

        if (workingCommand != null)
            return workingCommand;
        String osName = System.getProperty("os.name").toLowerCase();

        // POSSIBLE COMMANDS

        ArrayList<List<String>> possibleCommands = new ArrayList<>();
        if (AppSettings.getInstance().autoArgs) {
            if (FilePaths.getInstance().getPythonExePath() != null) {
                possibleCommands.add(List.of(FilePaths.getInstance().getPythonExePath()));
                possibleCommands.add(List.of("cmd", "/c", "py"));
            }
            possibleCommands.add(List.of("python3"));
            possibleCommands.add(List.of("python"));
        }
        else
            possibleCommands.add(AppSettings.getInstance().terminalArgs);

        // TEST COMMANDS

        System.out.println("=== FINDING THE CORRECT COMMAND ARGUMENTS ===");
        for (List<String> command : possibleCommands) {
            boolean installed = true;
            ArrayList<String> commandVer = new ArrayList<>(command);
            commandVer.add("--version");
            // Message
            StringBuilder builder = new StringBuilder();
            builder.append("Attempt with arguments :");
            for (String arg : command) {
                builder.append(" ");
                builder.append(arg);
            }
            System.out.println(builder);
            try {
                // Launch process
                ProcessBuilder pb = new ProcessBuilder(commandVer);
                pb.redirectErrorStream(true);
                Process p = pb.start();
                BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                List<String> lines = in.lines().toList();
                if (lines.size() != 0) {
                    String ver = lines.get(0);
                    if (!ver.contains("Python 3."))
                        installed = false;
                    System.out.println(ver);
                }
                else
                    installed = false;
            } catch (Exception e) {
                System.out.println("(Failure)");
                installed = false;
            }
            if (installed) {
                workingCommand = command;
                System.out.println(command);
                return command;
            }
        }
        return null;
    }

    public void resetCommand() {
        workingCommand = null;
    }
}