package com.maniacobra.pyzzle.resources;

import java.io.File;
import java.util.List;

public class PyzzAnalyzer {

    // Static

    private static final PyzzAnalyzer instance = new PyzzAnalyzer();

    public static PyzzAnalyzer getInstance() {
        return instance;
    }

    // Class

    public boolean AnalyzeFiles(List<File> files) {
        return false;
    }
}
