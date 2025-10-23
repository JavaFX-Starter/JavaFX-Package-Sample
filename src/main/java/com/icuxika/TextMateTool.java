package com.icuxika;

import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.internal.theme.ParsedThemeRule;
import org.eclipse.tm4e.core.internal.theme.Theme;
import org.eclipse.tm4e.core.internal.theme.raw.RawThemeReader;
import org.eclipse.tm4e.core.registry.IGrammarSource;
import org.eclipse.tm4e.core.registry.IThemeSource;
import org.eclipse.tm4e.core.registry.Registry;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class TextMateTool {

    static void main(String[] args) {
        readTheme();
    }

    private static void readGrammar() {
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

    private static void readTheme() {
        try {
            List<ParsedThemeRule> ruleList = Theme.parseTheme(RawThemeReader.readTheme(IThemeSource.fromString(
                    IThemeSource.ContentType.JSON,
                    Objects.requireNonNull(AppResource.readStringFromResource("/richtext/themes/dark_vs.json")))
            ));
            ruleList.forEach(new Consumer<ParsedThemeRule>() {
                @Override
                public void accept(ParsedThemeRule parsedThemeRule) {
                    System.out.println("========================================");
                    System.out.println(parsedThemeRule.foreground);
                    System.out.println(parsedThemeRule.background);
                    System.out.println(parsedThemeRule.scope);
                }
            });

            System.out.println("========================================");
            System.out.println("========================================");
            Theme theme = Theme.createFromRawTheme(RawThemeReader.readTheme(IThemeSource.fromString(
                    IThemeSource.ContentType.JSON,
                    Objects.requireNonNull(AppResource.readStringFromResource("/richtext/themes/dark_vs.json")))
            ), null);
            System.out.println(theme.getColorMap().get(theme.getDefaults().backgroundId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
