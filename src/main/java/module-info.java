module sample {

    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires javafx.graphics;
    requires javafx.web;

    requires MaterialFX;
    requires org.slf4j;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.ikonli.fluentui;
    requires com.google.gson;

    opens com.icuxika.model to com.google.gson;

    exports com.icuxika;
}