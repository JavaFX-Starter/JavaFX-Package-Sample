module sample {

    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires javafx.graphics;
    requires javafx.web;

    requires java.naming;
    requires ch.qos.logback.classic;
    requires MaterialFX;
    requires org.slf4j;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome6;
    requires org.kordamp.ikonli.fluentui;
    requires com.google.gson;
    requires jfx.incubator.richtext;
    requires org.eclipse.tm4e.core;
    requires org.eclipse.lsp4j;
    requires org.eclipse.lsp4j.jsonrpc;

    opens com.icuxika.model to com.google.gson;
    opens com.icuxika.lsp to org.eclipse.lsp4j.jsonrpc;
    opens com.icuxika.controller to javafx.fxml;

    exports com.icuxika;
}