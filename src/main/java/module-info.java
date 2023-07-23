module com.maniacobra.pyzzle {
    requires javafx.controls;
    requires javafx.fxml;
    requires json.simple;
    requires java.desktop;

    exports com.maniacobra.pyzzle.views;
    opens com.maniacobra.pyzzle.views to javafx.fxml;
    exports com.maniacobra.pyzzle.controllers;
    opens com.maniacobra.pyzzle.controllers to javafx.fxml;
    exports com.maniacobra.pyzzle.views.blockeditor;
    opens com.maniacobra.pyzzle.views.blockeditor to javafx.fxml;
    exports com.maniacobra.pyzzle.models;
    opens com.maniacobra.pyzzle.models to javafx.fxml;
    exports com.maniacobra.pyzzle.properties;
    opens com.maniacobra.pyzzle.properties to javafx.fxml;
    exports com.maniacobra.pyzzle.resources;
    opens com.maniacobra.pyzzle.resources to javafx.fxml;
    exports com.maniacobra.pyzzle.utils;
    opens com.maniacobra.pyzzle.utils to javafx.fxml;
}