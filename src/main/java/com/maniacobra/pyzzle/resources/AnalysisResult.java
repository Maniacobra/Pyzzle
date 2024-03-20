package com.maniacobra.pyzzle.resources;

import java.util.List;
import java.util.UUID;

public record AnalysisResult(String packName, boolean examMode, UUID uuid, String version, String userName, int nbWin, int nbEmpty, float score, int fileAttempts, List<ExerciseSummary> summaries) {
}
