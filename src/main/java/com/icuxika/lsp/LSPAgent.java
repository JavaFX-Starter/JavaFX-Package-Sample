package com.icuxika.lsp;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
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
    private static final String JAVA_HOME = "C:\\CommandLineTools\\Java\\jdk-21";

    private Path tempWorkspace;
    private Process process;
    private Future<Void> listening;
    private LanguageServer languageServer;
    private Path codeFile;
    private String uri;
    private final AtomicInteger version = new AtomicInteger(1);

    private Consumer<DiagnosticMessage> diagnosticMessageConsumer;

    public static final String DEMO_CODE = """
            package com.example;
            
            /**
             * Hello world!
             *
             */
            public class App {
                public static void main(String[] args) {
                    System.out.println("Hello World!");
                }
            }
            
            """;

    private final String POM_XML = """
            <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                <modelVersion>4.0.0</modelVersion>
            
                <groupId>com.oracle.demo.richtext</groupId>
                <artifactId>lsp-demo</artifactId>
                <version>1.0-SNAPSHOT</version>
                <packaging>jar</packaging>
            
                <name>lsp-demo</name>
            
                <properties>
                    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                    <maven.compiler.release>21</maven.compiler.release>
                    <maven.compiler.source>21</maven.compiler.source>
                    <maven.compiler.target>21</maven.compiler.target>
                </properties>
            
                <dependencies>
                </dependencies>
            </project>
            """;

    public void initialize() {
        try {
            LOGGER.info("启动LSP服务器");
            tempWorkspace = Files.createTempDirectory("JavaFX-Package-Sample-LSP_");
            LOGGER.info("LSP服务器工作目录: {}", tempWorkspace);

            String projectName = UUID.randomUUID().toString();
            Path project = tempWorkspace.resolve(projectName);
            Files.createDirectories(project);

            Path pom = project.resolve("pom.xml");
            Files.writeString(pom, POM_XML);

            Path src = project.resolve("src/main/java/com/example");
            Files.createDirectories(src);

            codeFile = src.resolve("App.java");
            Files.writeString(codeFile, DEMO_CODE);
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

            InitializeParams initializeParams = new InitializeParams();
            initializeParams.setWorkspaceFolders(List.of(new WorkspaceFolder(project.toUri().toString(), projectName)));
            Map<String, Object> initializationOptions = Map.of(
                    "settings", Map.of(
                            "java", Map.of(
                                    "home", JAVA_HOME,
                                    "configuration", Map.of(
                                            "runtimes", List.of(
                                                    Map.of("name", "JavaSE-21", "path", JAVA_HOME, "default", true)
                                            ),
                                            "updateBuildConfiguration", "automatic"
                                    )
                            )
                    )
            );
            // https://github.com/eclipse-jdtls/eclipse.jdt.ls/wiki/Running-the-JAVA-LS-server-from-the-command-line
            initializeParams.setInitializationOptions(initializationOptions);

            ClientCapabilities clientCapabilities = new ClientCapabilities();

            WorkspaceClientCapabilities workspaceClientCapabilities = new WorkspaceClientCapabilities();
            workspaceClientCapabilities.setWorkspaceFolders(true);
            workspaceClientCapabilities.setDidChangeConfiguration(new DidChangeConfigurationCapabilities(true));
            clientCapabilities.setWorkspace(workspaceClientCapabilities);

            TextDocumentClientCapabilities textDocumentClientCapabilities = new TextDocumentClientCapabilities();
            CompletionCapabilities completionCapabilities = new CompletionCapabilities();
            completionCapabilities.setCompletionItem(new CompletionItemCapabilities(true));
            textDocumentClientCapabilities.setCompletion(completionCapabilities);
//            clientCapabilities.setTextDocument(textDocumentClientCapabilities);

            initializeParams.setCapabilities(clientCapabilities);
            languageServer.initialize(initializeParams).get();
            languageServer.initialized(new InitializedParams());

            Thread.sleep(2000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendOpenTextDocument(String text) {
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
                    LOGGER.error(diagnostic.getMessage());
                    if (!diagnostic.getMessage().contains("only syntax errors are reported")) {
                        LOGGER.trace(diagnostic.getRange().toString());
                        if (diagnostic.getRange().getStart().getLine() != 0 &&
                                diagnostic.getRange().getStart().getCharacter() != 0 &&
                                diagnostic.getRange().getEnd().getLine() != 0 &&
                                diagnostic.getRange().getEnd().getCharacter() != 0)
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

        @Override
        public CompletableFuture<Void> registerCapability(RegistrationParams params) {
            return CompletableFuture.completedFuture(null);
        }
    }
}
