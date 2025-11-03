package com.icuxika.lsp;

public record DiagnosticMessage(
        int startLine,
        int startCharacter,
        int endLine,
        int endCharacter,
        String errorMsg
) {
}
