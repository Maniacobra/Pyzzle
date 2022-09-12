package com.maniacobra.pyzzle.models;

import org.json.simple.JSONObject;

public record ExerciseConfig(JSONObject data, int exerciseNumber, int totalExercises, float totalScore, float maxScore, JSONObject completion) {
}
