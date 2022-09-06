package com.maniacobra.pyzzle.controllers;

import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public record ExerciseNodes(TextArea codeText,
                            TextFlow consoleText,
                            TextFlow objectivesText,
                            Text numberText,
                            Text nameText,
                            Text completionText,
                            Text attemptsText,
                            Text scoreText,
                            Button executionButton,
                            Button resetButton,
                            Button previousButton,
                            Button nextButton,
                            Button solutionButton
                            ) {
}
