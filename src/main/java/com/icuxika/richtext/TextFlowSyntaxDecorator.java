package com.icuxika.richtext;

import com.icuxika.AppResource;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.grammar.IStateStack;
import org.eclipse.tm4e.core.internal.grammar.ScopeStack;
import org.eclipse.tm4e.core.internal.theme.StyleAttributes;
import org.eclipse.tm4e.core.internal.theme.Theme;
import org.eclipse.tm4e.core.internal.theme.raw.RawThemeReader;
import org.eclipse.tm4e.core.registry.IGrammarSource;
import org.eclipse.tm4e.core.registry.IThemeSource;
import org.eclipse.tm4e.core.registry.Registry;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextFlowSyntaxDecorator {

    private final IGrammar grammar;

    private final Theme theme;

    String editorBackgroundString = "#FFFFFF";
    String editorForegroundString = "#FFFFFF";

    public TextFlowSyntaxDecorator(String syntaxResource, String themeResource) {
        final var registry = new Registry();
        grammar = registry.addGrammar(IGrammarSource.fromString(
                IGrammarSource.ContentType.JSON,
                Objects.requireNonNull(AppResource.readStringFromResource(syntaxResource)))
        );

        try {
            String themeJson = Objects.requireNonNull(AppResource.readStringFromResource(themeResource));

            Pattern backgroundPattern = Pattern.compile("\"editor\\.background\"\\s*:\\s*\"(#[0-9a-fA-F]{3,8})\"");
            Matcher backgroundMatcher = backgroundPattern.matcher(themeJson);
            if (backgroundMatcher.find()) {
                editorBackgroundString = backgroundMatcher.group(1);
            }

            Pattern foregroundPattern = Pattern.compile("\"editor\\.foreground\"\\s*:\\s*\"(#[0-9a-fA-F]{3,8})\"");
            Matcher foregroundMatcher = foregroundPattern.matcher(themeJson);
            if (foregroundMatcher.find()) {
                editorForegroundString = foregroundMatcher.group(1);
            }

            theme = Theme.createFromRawTheme(RawThemeReader.readTheme(IThemeSource.fromString(
                    IThemeSource.ContentType.JSON,
                    themeJson)
            ), null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public TextFlow highlight(String code) {
        TextFlow textFlow = new TextFlow();

        final var prevStack = new AtomicReference<IStateStack>();

        Pattern.compile("\\r?\\n").splitAsStream(code).forEach(line -> {
            final var tokenized = grammar.tokenizeLine(line, prevStack.get(), null);
            prevStack.set(tokenized.getRuleStack());
            if (!line.isEmpty()) {
                int lastEnd = 0;
                for (var token : tokenized.getTokens()) {
                    int start = token.getStartIndex();
                    int end = token.getEndIndex();

                    if (start > lastEnd) {
                        Text text = new Text(line.substring(lastEnd, start));
                        text.setFill(Color.web(editorForegroundString));
                        textFlow.getChildren().add(text);
                    }

                    String scope = token.getScopes().isEmpty() ? "" : token.getScopes().getLast();
                    Text text = new Text(line.substring(start, end));
                    text.setFill(getColor(scope));
                    textFlow.getChildren().add(text);
                    lastEnd = end;
                }
                if (lastEnd < line.length()) {
                    Text text = new Text(line.substring(lastEnd));
                    text.setFill(Color.web(editorForegroundString));
                    textFlow.getChildren().add(text);
                }
            }
            textFlow.getChildren().add(new Text(System.lineSeparator()));
        });

        textFlow.setBackground(new Background(new BackgroundFill(Color.web(editorBackgroundString), CornerRadii.EMPTY, Insets.EMPTY)));
        textFlow.setStyle("""
                -fx-font-family: "HarmonyOS Sans SC";
                -fx-font-size: 14;
                """);
        return textFlow;
    }

    private Color getColor(String scope) {
        StyleAttributes styleAttributes = theme.match(ScopeStack.from(scope));
        if (styleAttributes != null) {
            String foregroundColor = theme.getColorMap().get(styleAttributes.foregroundId);
            if (!foregroundColor.isEmpty()) {
                return Color.web(foregroundColor);
            }
        }
        return Color.web(editorForegroundString);
    }
}
