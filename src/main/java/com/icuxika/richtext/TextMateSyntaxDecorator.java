package com.icuxika.richtext;

import com.icuxika.AppResource;
import com.icuxika.FXUtil;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import jfx.incubator.scene.control.richtext.CodeArea;
import jfx.incubator.scene.control.richtext.SyntaxDecorator;
import jfx.incubator.scene.control.richtext.TextPos;
import jfx.incubator.scene.control.richtext.model.CodeTextModel;
import jfx.incubator.scene.control.richtext.model.RichParagraph;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.grammar.IStateStack;
import org.eclipse.tm4e.core.internal.grammar.ScopeStack;
import org.eclipse.tm4e.core.internal.theme.StyleAttributes;
import org.eclipse.tm4e.core.internal.theme.Theme;
import org.eclipse.tm4e.core.internal.theme.raw.RawThemeReader;
import org.eclipse.tm4e.core.registry.IGrammarSource;
import org.eclipse.tm4e.core.registry.IThemeSource;
import org.eclipse.tm4e.core.registry.Registry;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextMateSyntaxDecorator implements SyntaxDecorator {

    private final Map<Integer, IStateStack> stateStackMap = new HashMap<>();

    private final Map<String, StyleAttributeMap> styleMap = new HashMap<>();

    private final IGrammar grammar;

    private final Theme theme;

    String editorForegroundString = "#FFFFFF";
    String editorSelectionHighlightBackgroundString = "#ADD6FF80";

    public TextMateSyntaxDecorator(CodeArea codeArea, String syntaxResource, String themeResource) {
        final var registry = new Registry();
        grammar = registry.addGrammar(IGrammarSource.fromString(
                IGrammarSource.ContentType.JSON,
                Objects.requireNonNull(AppResource.readStringFromResource(syntaxResource)))
        );

        String editorBackgroundString = "#FFFFFF";
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

            Pattern selectionHighlightBackgroundPattern = Pattern.compile("\"editor\\.selectionHighlightBackground\"\\s*:\\s*\"(#[0-9a-fA-F]{3,8})\"");
            Matcher selectionHighlightBackgroundMatcher = selectionHighlightBackgroundPattern.matcher(themeJson);
            if (selectionHighlightBackgroundMatcher.find()) {
                editorSelectionHighlightBackgroundString = selectionHighlightBackgroundMatcher.group(1);
            }

            theme = Theme.createFromRawTheme(RawThemeReader.readTheme(IThemeSource.fromString(
                    IThemeSource.ContentType.JSON,
                    themeJson)
            ), null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        codeArea.setBackground(new Background(new BackgroundFill(Color.web(editorBackgroundString), CornerRadii.EMPTY, Insets.EMPTY)));
        codeArea.skinProperty().addListener((_, _, newValue) -> {
            if (newValue != null) {
                FXUtil.runInFX(() -> {
                    // com.sun.jfx.incubator.scene.control.richtext.VFlow
                    Node pathNode = codeArea.lookup(".selection-highlight");
                    if (pathNode instanceof Path path) {
                        path.setStroke(Color.web(editorSelectionHighlightBackgroundString));
                        path.setFill(Color.web(editorSelectionHighlightBackgroundString));
                    }
                });
            }
        });
    }

    @Override
    public RichParagraph createRichParagraph(CodeTextModel model, int index) {
        String text = model.getPlainText(index);
        RichParagraph.Builder builder = RichParagraph.builder();
        if (text.isEmpty()) {
            return builder.build();
        }
        IStateStack prevStack = index > 0 ? stateStackMap.get(index - 1) : null;
        final var result = grammar.tokenizeLine(text, prevStack, null);
        stateStackMap.put(index, result.getRuleStack());

        int textLength = text.length();

        int lastEnd = 0;
        for (var token : result.getTokens()) {
            int start = Math.min(token.getStartIndex(), token.getStartIndex());
            int end = Math.min(token.getEndIndex(), textLength);

            if (start >= textLength || end <= start) {
                continue;
            }

            if (start > lastEnd) {
                builder.addSegment(text, lastEnd, start, null);
            }

            String scope = token.getScopes().isEmpty() ? "" : token.getScopes().getLast();
            builder.addSegment(text, start, end, getStyle(scope));

            lastEnd = end;
        }

        if (lastEnd < text.length()) {
            builder.addSegment(text, lastEnd, textLength, null);
        }
        return builder.build();
    }

    @Override
    public void handleChange(CodeTextModel m, TextPos start, TextPos end, int charsTop, int linesAdded, int charsBottom) {
        int startLine = start.index();
        if (linesAdded != 0) {
            Map<Integer, IStateStack> newStateStackMap = new HashMap<>();
            for (Map.Entry<Integer, IStateStack> entry : stateStackMap.entrySet()) {
                int lineNum = entry.getKey();
                if (lineNum < startLine) {
                    newStateStackMap.put(lineNum, entry.getValue());
                }
            }
            stateStackMap.clear();
            stateStackMap.putAll(newStateStackMap);
        } else {
            stateStackMap.keySet().removeIf(p -> p >= startLine);
        }
    }

    public IGrammar getGrammar() {
        return grammar;
    }

    private StyleAttributeMap getStyle(String scope) {
        return styleMap.computeIfAbsent(scope, s -> StyleAttributeMap.builder().setTextColor(getColor(s)).build());
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
