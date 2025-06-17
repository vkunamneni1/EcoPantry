module com.vedakunamneni.click {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.google.gson;
    requires java.sql;
    requires java.desktop;

    opens com.vedakunamneni.click to javafx.fxml, com.google.gson;
    opens com.vedakunamneni.click.controllers to javafx.fxml;
    exports com.vedakunamneni.click;
    exports com.vedakunamneni.click.controllers;
}