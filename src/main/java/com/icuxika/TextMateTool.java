package com.icuxika;

import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.registry.IGrammarSource;
import org.eclipse.tm4e.core.registry.Registry;

import java.util.Objects;

public class TextMateTool {

    static void main(String[] args) {
        final var registry = new Registry();
        final IGrammar grammar = registry.addGrammar(IGrammarSource.fromString(
                IGrammarSource.ContentType.JSON,
                Objects.requireNonNull(AppResource.readStringFromResource("/richtext/syntaxes/java.tmLanguage.json")))
        );
        final var result = grammar.tokenizeLine("""
                package com.icuxika;
                
                public class Launcher {
                
                    static void main(String[] args) {
                        MainApp.main(args);
                    }
                }
                """);
        for (var token : result.getTokens()) {
            System.out.println(token);
        }
    }
}
