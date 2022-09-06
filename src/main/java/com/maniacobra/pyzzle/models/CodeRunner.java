package com.maniacobra.pyzzle.models;

import com.maniacobra.pyzzle.properties.AppStyle;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.*;

import static com.maniacobra.pyzzle.models.ExecutionResult.*;
import static com.maniacobra.pyzzle.models.ExecutionResult.FATAL;
import static com.maniacobra.pyzzle.utils.TextUtils.addToTextFlow;

public class CodeRunner {
    // Singleton
    private static CodeRunner instance = new CodeRunner();

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

    public void prepareCode(String pyCode, ArrayList<String> inputs) {

        // Make code
        String[] lines = pyCode.split("\n");
        StringBuilder pyBuilder = new StringBuilder();
        pyBuilder.append("def run(");
        for (String input : inputs) {
            pyBuilder.append(input);
            pyBuilder.append("=0, ");
        }
        pyBuilder.append("):");
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

    public ExecutionResult runCode(TextFlow consoleText, Text dsDeclaration, List<String> dataset, List<String> objective) {

        ExecutionResult result = SUCCESS;
        dsDeclaration.setFill(AppStyle.Colors.resultValid);
        consoleText.getChildren().add(dsDeclaration);
        // Process
        ArrayList<String> params = new ArrayList<>();
        params.add("cmd");
        params.add("/c");
        params.add("py");
        params.add("py/execute.py");
        if (dataset != null)
            params.addAll(dataset);
        try {
            ProcessBuilder pb = new ProcessBuilder(params);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

            // Interpret results
            List<String> lines = in.lines().toList();
            int i = 0;
            int errorState = 0;
            for (String line : lines) {
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
                        }
                        catch (NumberFormatException e) {
                            e.printStackTrace();
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
                        if (i >= objective.size()) {
                            color = AppStyle.Colors.resultUnperfect;
                            if (result == SUCCESS) {
                                result = OVERFLOW;
                                dsDeclaration.setFill(color);
                            }
                        } else if (!objective.get(i).equals(line)) {
                            color = AppStyle.Colors.resultWrong;
                            result = WRONG;
                            dsDeclaration.setFill(color);
                        }
                        addToTextFlow(consoleText, line + '\n', color, false);
                    }
                }
                i++;
            }
            while (i < objective.size()) {
                if (result != WRONG) {
                    result = INCOMPLETE;
                    dsDeclaration.setFill(AppStyle.Colors.resultWrong);
                }
                addToTextFlow(consoleText, "(vide)\n", AppStyle.Colors.resultEmpty, false);
                i++;
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

        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setResizable(false);
        window.setTitle(title);

        Label label1 = new Label(contentIntro);
        label1.setFont(Font.font("Arial", FontWeight.BOLD, label1.getFont().getSize()));
        Label label2 = new Label(content);
        Button button = new Button("OK");

        button.setOnAction(e -> window.close());
        VBox layout = new VBox(10);

        layout.getChildren().addAll(label1, label2, button);
        layout.setAlignment(Pos.CENTER);

        int width = 400;
        int height = content.split("\n").length * 30 + 60;
        Scene scene = new Scene(layout, width, height);
        window.setScene(scene);
        window.showAndWait();
    }
}