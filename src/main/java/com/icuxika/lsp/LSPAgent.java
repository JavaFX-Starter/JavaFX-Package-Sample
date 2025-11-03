package com.icuxika.lsp;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * <a href="https://github.com/eclipse-jdtls/eclipse.jdt.ls">Eclipse JDT Language Server</a>
 */
public class LSPAgent {

    private static final Logger LOGGER = LoggerFactory.getLogger(LSPAgent.class);

    private static final String JDT_HOME = "C:\\CommandLineTools\\Java\\jdt-language-server-1.9.0";

    private Path tempWorkspace;
    private Process process;
    private Future<Void> listening;
    private LanguageServer languageServer;
    private Path codeFile;
    private String uri;
    private final AtomicInteger version = new AtomicInteger(1);

    private Consumer<DiagnosticMessage> diagnosticMessageConsumer;

    public void initialize() {
        try {
            LOGGER.info("启动LSP服务器");
            tempWorkspace = Files.createTempDirectory("JavaFX-Package-Sample-LSP_");
            LOGGER.info("LSP服务器工作目录: {}", tempWorkspace);

            Path project = tempWorkspace.resolve(UUID.randomUUID().toString());
            Path src = project.resolve("src");
            Files.createDirectories(src);

            codeFile = src.resolve("Launcher.java");
            uri = codeFile.toUri().toString();

            String jar;
            try (Stream<Path> stream = Files.walk(Path.of(JDT_HOME, "plugins"))) {
                var jarPath = stream.filter(path -> path.getFileName().toString().startsWith("org.eclipse.equinox.launcher_"))
                        .filter(path -> path.getFileName().toString().endsWith(".jar"))
                        .map(Path::toAbsolutePath)
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("未找到 org.eclipse.equinox.launcher_**.jar"));
                jar = jarPath.toString();
            }
            var command = Arrays.asList(
                    "java",
                    "-Declipse.application=org.eclipse.jdt.ls.core.id1",
                    "-Dosgi.bundles.defaultStartLevel=4",
                    "-Declipse.product=org.eclipse.jdt.ls.core.product",
                    "-Dlog.level=ALL",
                    "-Xmx1G",
                    "--add-modules=ALL-SYSTEM",
                    "--add-opens", "java.base/java.util=ALL-UNNAMED",
                    "--add-opens", "java.base/java.lang=ALL-UNNAMED",
                    "-jar", jar,
                    "-configuration", Path.of(JDT_HOME).resolve("config_win").toString(),
                    "-data", tempWorkspace.toString()
            );
            ProcessBuilder processBuilder = new ProcessBuilder(command);
//            processBuilder.inheritIO();
            process = processBuilder.start();
            var launcher = LSPLauncher.createClientLauncher(new JavaLanguageClient(), process.getInputStream(), process.getOutputStream());
            languageServer = launcher.getRemoteProxy();
            listening = launcher.startListening();

            InitializeParams initializedParams = new InitializeParams();
            initializedParams.setCapabilities(new ClientCapabilities());
            languageServer.initialize(initializedParams).get();
            languageServer.initialized(new InitializedParams());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendOpenTextDocument(String text) {
        try {
            Files.writeString(codeFile, text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        DidOpenTextDocumentParams didOpenTextDocumentParams = new DidOpenTextDocumentParams();
        TextDocumentItem textDocumentItem = new TextDocumentItem();
        textDocumentItem.setUri(uri);
        textDocumentItem.setLanguageId("java");
        textDocumentItem.setVersion(version.getAndIncrement());
        textDocumentItem.setText(text);
        didOpenTextDocumentParams.setTextDocument(textDocumentItem);
        languageServer.getTextDocumentService().didOpen(didOpenTextDocumentParams);
    }

    public void sendChangeTextDocument(String text) {
        DidChangeTextDocumentParams didChangeTextDocumentParams = new DidChangeTextDocumentParams();

        VersionedTextDocumentIdentifier versionedTextDocumentIdentifier = new VersionedTextDocumentIdentifier();
        versionedTextDocumentIdentifier.setUri(uri);
        versionedTextDocumentIdentifier.setVersion(version.getAndIncrement());
        didChangeTextDocumentParams.setTextDocument(versionedTextDocumentIdentifier);

        TextDocumentContentChangeEvent textDocumentContentChangeEvent = new TextDocumentContentChangeEvent();
        textDocumentContentChangeEvent.setText(text);
        didChangeTextDocumentParams.setContentChanges(Collections.singletonList(textDocumentContentChangeEvent));
        languageServer.getTextDocumentService().didChange(didChangeTextDocumentParams);
    }

    public void shutdown() {
        try {
            LOGGER.info("关闭LSP服务器");
            if (languageServer != null) {
                languageServer.shutdown().get();
                languageServer.exit();
            }
            if (listening != null) {
                listening.cancel(true);
            }
            if (process != null && process.isAlive()) {
                process.destroy();
            }
            if (tempWorkspace != null) {
                try (Stream<Path> stream = Files.walk(tempWorkspace)) {
                    stream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setDiagnosticMessageConsumer(Consumer<DiagnosticMessage> diagnosticMessageConsumer) {
        this.diagnosticMessageConsumer = diagnosticMessageConsumer;
    }

    private class JavaLanguageClient implements LanguageClient {
        @Override
        public void telemetryEvent(Object o) {

        }

        @Override
        public void publishDiagnostics(PublishDiagnosticsParams publishDiagnosticsParams) {
            publishDiagnosticsParams.getDiagnostics().forEach(new Consumer<Diagnostic>() {
                @Override
                public void accept(Diagnostic diagnostic) {
                    if (!diagnostic.getMessage().contains("only syntax errors are reported")) {
                        LOGGER.error(diagnostic.getMessage());
                        LOGGER.trace(diagnostic.getRange().toString());
                        if (diagnosticMessageConsumer != null) {
                            diagnosticMessageConsumer.accept(new DiagnosticMessage(
                                    diagnostic.getRange().getStart().getLine(),
                                    diagnostic.getRange().getStart().getCharacter(),
                                    diagnostic.getRange().getEnd().getLine(),
                                    diagnostic.getRange().getEnd().getCharacter(),
                                    diagnostic.getMessage()
                            ));
                        }
                    }
                }
            });
        }

        @Override
        public void showMessage(MessageParams messageParams) {

        }

        @Override
        public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams showMessageRequestParams) {
            return null;
        }

        @Override
        public void logMessage(MessageParams messageParams) {

        }

        @JsonRequest("language/status")
        public CompletableFuture<Void> languageStatus(Object object) {
            LOGGER.trace(object.toString());
            return CompletableFuture.completedFuture(null);
        }
    }
}
