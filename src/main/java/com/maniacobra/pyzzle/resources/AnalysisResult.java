package com.maniacobra.pyzzle.resources;

import java.util.List;
import java.util.UUID;

public record AnalysisResult(String fileName, String packName, boolean examMode, UUID uuid, float maxScore, String version, String userName, int nbWin, int nbEmpty, float score, int fileAttempts, List<ExerciseSummary> summaries) {
    public float getOutOf20() {
        return score / maxScore * 20.f;
    }
}
