module com.vedakunamneni.click {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.vedakunamneni.click to javafx.fxml;
    opens com.vedakunamneni.click.controllers to javafx.fxml;
    exports com.vedakunamneni.click;
}
