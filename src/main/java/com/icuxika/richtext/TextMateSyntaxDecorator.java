package com.icuxika.richtext;

import com.icuxika.AppResource;
import javafx.scene.paint.Color;
import jfx.incubator.scene.control.richtext.SyntaxDecorator;
import jfx.incubator.scene.control.richtext.TextPos;
import jfx.incubator.scene.control.richtext.model.CodeTextModel;
import jfx.incubator.scene.control.richtext.model.RichParagraph;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.grammar.IStateStack;
import org.eclipse.tm4e.core.registry.IGrammarSource;
import org.eclipse.tm4e.core.registry.Registry;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class TextMateSyntaxDecorator implements SyntaxDecorator {

    private final AtomicReference<IStateStack> prevStack = new AtomicReference<>();

    private final Map<String, StyleAttributeMap> styleMap = new HashMap<>();

    private final IGrammar grammar;

    public TextMateSyntaxDecorator(String syntaxResource) {
        final var registry = new Registry();
        grammar = registry.addGrammar(IGrammarSource.fromString(
                IGrammarSource.ContentType.JSON,
                Objects.requireNonNull(AppResource.readStringFromResource(syntaxResource)))
        );
    }

    @Override
    public RichParagraph createRichParagraph(CodeTextModel model, int index) {
        String text = model.getPlainText(index);
        RichParagraph.Builder builder = RichParagraph.builder();
        if (text.isEmpty()) {
            return builder.build();
        }
        final var result = grammar.tokenizeLine(text, prevStack.get(), null);
        prevStack.set(result.getRuleStack());

        int lastEnd = 0;
        for (var token : result.getTokens()) {
            int start = token.getStartIndex();
            int end = token.getEndIndex();

            if (start > lastEnd) {
                builder.addSegment(text, lastEnd, start, null);
            }
            String scope = token.getScopes().isEmpty() ? "" : token.getScopes().getLast();
            System.out.println("=============================================================");
            System.out.println("text: " + text.substring(start, end));
            System.out.println(token.getScopes());
            System.out.println("scope: " + scope);
            builder.addSegment(text, start, end, getStyle(scope));

            lastEnd = end;
        }

        if (lastEnd < text.length()) {
            builder.addSegment(text, lastEnd, text.length(), null);
        }
        return builder.build();
    }

    @Override
    public void handleChange(CodeTextModel m, TextPos start, TextPos end, int charsTop, int linesAdded, int charsBottom) {

    }

    public IGrammar getGrammar() {
        return grammar;
    }

    private StyleAttributeMap getStyle(String scope) {
        return styleMap.computeIfAbsent(scope, s -> {
            if (s.contains("keyword")) {
                return StyleAttributeMap.builder().setTextColor(Color.ORANGE).build();
            }
            if (s.contains("storage.modifier.java")) {
                return StyleAttributeMap.builder().setTextColor(Color.ORANGE).build();
            }
            if (s.contains("entity.name.function.java")) {
                return StyleAttributeMap.builder().setTextColor(Color.DODGERBLUE).build();
            }
            return null;
        });
    }
}
