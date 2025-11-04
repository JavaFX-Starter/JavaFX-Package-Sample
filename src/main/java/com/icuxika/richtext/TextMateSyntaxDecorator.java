package com.icuxika.richtext;

import com.icuxika.AppResource;
import com.icuxika.FXUtil;
import com.icuxika.lsp.DiagnosticMessage;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextMateSyntaxDecorator implements SyntaxDecorator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextMateSyntaxDecorator.class);

    private final CodeArea codeArea;

    private final Map<Integer, IStateStack> stateStackMap = new HashMap<>();

    private final Map<String, StyleAttributeMap> styleMap = new HashMap<>();

    private final IGrammar grammar;

    private Theme theme;

    String editorBackgroundString = "#FFFFFF";
    String editorForegroundString = "#FFFFFF";
    String editorSelectionHighlightBackgroundString = "#ADD6FF80";
    String editorInactiveSelectionBackgroundString = "#ADD6FF80";

    private final List<DiagnosticMessage> diagnosticMessageList = new ArrayList<>();

    public TextMateSyntaxDecorator(CodeArea codeArea, String syntaxResource, String themeResource) {
        this.codeArea = codeArea;

        final var registry = new Registry();
        grammar = registry.addGrammar(IGrammarSource.fromString(
                IGrammarSource.ContentType.JSON,
                Objects.requireNonNull(AppResource.readStringFromResource(syntaxResource)))
        );

        readTheme(themeResource);
        applyTheme();
        codeArea.skinProperty().addListener((_, _, newValue) -> {
            if (newValue != null) {
                applyLookupTheme();
            }
        });

        // 开启高亮当前行
        codeArea.setHighlightCurrentParagraph(true);
        // 设置字体
        codeArea.setFont(new Font("HarmonyOS Sans SC", 14));
    }

    public void readTheme(String themeResource) {
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

            Pattern inactiveSelectionBackgroundPattern = Pattern.compile("\"editor\\.inactiveSelectionBackground\"\\s*:\\s*\"(#[0-9a-fA-F]{3,8})\"");
            Matcher inactiveSelectionBackgroundMatcher = inactiveSelectionBackgroundPattern.matcher(themeJson);
            if (inactiveSelectionBackgroundMatcher.find()) {
                editorInactiveSelectionBackgroundString = inactiveSelectionBackgroundMatcher.group(1);
            }

            theme = Theme.createFromRawTheme(RawThemeReader.readTheme(IThemeSource.fromString(
                    IThemeSource.ContentType.JSON,
                    themeJson)
            ), null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void applyTheme() {
        codeArea.setBackground(new Background(new BackgroundFill(Color.web(editorBackgroundString), CornerRadii.EMPTY, Insets.EMPTY)));
        if (codeArea.getSkin() != null) {
            applyLookupTheme();
        }
    }

    private void applyLookupTheme() {
        FXUtil.runInFX(() -> {
            // com.sun.jfx.incubator.scene.control.richtext.VFlow
            // 文本选中区颜色
            Node selectionHighlightNode = codeArea.lookup(".selection-highlight");
            if (selectionHighlightNode instanceof Path path) {
                path.setStroke(Color.web(editorSelectionHighlightBackgroundString));
                path.setFill(Color.web(editorSelectionHighlightBackgroundString));
            }

            // 光标颜色
            Node caretNode = codeArea.lookup(".caret");
            if (caretNode instanceof Path path) {
                path.setStroke(Color.web(editorForegroundString));
                path.setFill(Color.web(editorForegroundString));
            }

            // 光标所在行颜色
            Node caretLineNode = codeArea.lookup(".caret-line");
            if (caretLineNode instanceof Path path) {
                path.setStroke(Color.web(editorInactiveSelectionBackgroundString));
                path.setFill(Color.web(editorInactiveSelectionBackgroundString));
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

        DiagnosticMessage diagnosticMessage = diagnosticMessageList.stream().filter(p -> p.startLine() == index).findFirst().orElse(null);

        int textLength = text.length();

        int lastEnd = 0;
        for (var token : result.getTokens()) {
            int start = Math.min(token.getStartIndex(), textLength);
            int end = Math.min(token.getEndIndex(), textLength);

            if (diagnosticMessage != null &&
                    (diagnosticMessage.startCharacter() >= start && diagnosticMessage.startCharacter() <= end) &&
                    (diagnosticMessage.endCharacter() > start && diagnosticMessage.endCharacter() >= lastEnd && diagnosticMessage.endCharacter() <= end)) {
                LOGGER.trace("[{},{}] of [{}], {}", start, end, text, diagnosticMessage);
                builder.addSegment(text, start, end, StyleAttributeMap.builder().setUnderline(true).setTextColor(Color.RED).build());
            } else {
                if (start >= textLength || end <= start) {
                    continue;
                }

                if (start > lastEnd) {
                    builder.addSegment(text, lastEnd, start, null);
                }

                String scope = token.getScopes().isEmpty() ? "" : token.getScopes().getLast();
                builder.addSegment(text, start, end, getStyle(scope));
            }

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

    public void updateDiagnosticMessage(List<DiagnosticMessage> list) {
        diagnosticMessageList.clear();
        diagnosticMessageList.addAll(list);
    }

    public void refresh() {
        stateStackMap.clear();
        styleMap.clear();
        codeArea.getModel().fireStyleChangeEvent(TextPos.ZERO, codeArea.getModel().getDocumentEnd());
    }
}
