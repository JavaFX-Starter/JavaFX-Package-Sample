package com.icuxika.controller;

import com.icuxika.AppResource;
import com.icuxika.FXUtil;
import com.icuxika.constant.Theme;
import com.icuxika.lsp.DiagnosticMessage;
import com.icuxika.lsp.LSPAgent;
import com.icuxika.richtext.LSPCodeArea;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;

public class CodeEditorController implements Initializable {

    @FXML
    private BorderPane rootContainer;
    @FXML
    private BorderPane contentContainer;
    @FXML
    private Button startLspButton;
    @FXML
    private Button stopLspButton;
    @FXML
    private Button testButton;

    LSPCodeArea lspCodeArea = new LSPCodeArea(AppResource.getTheme() == Theme.LIGHT, LSPAgent.DEMO_CODE);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        contentContainer.setCenter(lspCodeArea);
        startLspButton.setOnAction(_ -> lspCodeArea.startLanguageServer());
        stopLspButton.setOnAction(_ -> lspCodeArea.stopLanguageServer());
        testButton.setOnAction(_ -> {
            lspCodeArea.applyChange(new DiagnosticMessage(
                    8, 25, 5, 26, "Syntax error, insert \";\" to complete BlockStatements"
            ));
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    FXUtil.runInFX(lspCodeArea::clearLastChange);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        });

        FXUtil.createWindowCreatedHook(rootContainer, (_, window) -> window.setOnCloseRequest(_ -> lspCodeArea.stopLanguageServer()));
    }
}
