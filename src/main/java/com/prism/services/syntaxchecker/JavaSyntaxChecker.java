package com.prism.services.syntaxchecker;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prism.Prism;
import com.prism.components.definition.PrismFile;
import com.prism.managers.TextAreaManager;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;

import javax.tools.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class JavaSyntaxChecker {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JavaCompiler COMPILER = ToolProvider.getSystemJavaCompiler();

    public static final class Problem {
        @JsonProperty("line")
        public int line;
        @JsonProperty("col")
        public int col;
        @JsonProperty("msg")
        public String msg;
        @JsonProperty("kind")
        public String kind;
    }

    public static void install(PrismFile pf, RSyntaxTextArea area) {
        area.addParser(new AbstractParser() {
            @Override
            public ParseResult parse(RSyntaxDocument doc, String style) {
                try {
                    String src = doc.getText(0, doc.getLength());

                    List<Problem> problems = compile(pf.getFile().getName(), src);

                    pf.setProblems(new ArrayList<>());

                    DefaultParseResult res = new DefaultParseResult(this);

                    problems.forEach(p -> {
                        int line = p.line - 1;
                        int col = Math.max(0, p.col - 1);;

                        int offset;
                        int length;
                        try {
                            offset = area.getLineStartOffset(line) + col;
                            length = guessTokenLength(src, line, col);
                        } catch (Exception ex) {
                            offset = -1;
                            length = 1;
                        }

                        res.addNotice(new DefaultParserNotice(this, p.msg, line, offset, length));

                        pf.addProblem(new TextAreaManager.Problem(p.msg, p.line - 1));
                    });

                    Prism.getInstance().getProblems().updateTreeData();

                    return res;
                } catch (Exception ignored) {
                    return new DefaultParseResult(this);
                }
            }
        });
    }

    private static List<Problem> compile(String fileName, String source) {
        DiagnosticCollector<JavaFileObject> diag = new DiagnosticCollector<>();
        SimpleJavaFileObject file =
                new SimpleJavaFileObject(
                        URI.create(String.format("string:///%s", fileName)), JavaFileObject.Kind.SOURCE) {
                    @Override
                    public CharSequence getCharContent(boolean ignore) {
                        return source;
                    }
                };

        COMPILER.getTask(null, null, diag, null, null, List.of(file)).call();

        return diag.getDiagnostics().stream()
                .map(d -> {
                    Problem p = new Problem();

                    p.line = (int) d.getLineNumber();
                    p.col = (int) d.getColumnNumber();
                    p.msg = d.getMessage(Locale.ENGLISH);
                    p.kind = d.getKind().name();

                    return p;
                })
                .collect(Collectors.toList());
    }

    private static int guessTokenLength(String src, int line, int col) {
        if (line < 0 || col < 0 || src.isEmpty()) return 1;

        int p = 0;
        for (int l = 0; l < line; l++) {
            int nl = src.indexOf('\n', p);
            if (nl == -1) return 1;
            p = nl + 1;
        }
        int lineStart = p;
        int tokenStart = lineStart + col;
        if (tokenStart >= src.length()) return 1;

        p = tokenStart;
        char ch = src.charAt(p);

        if (Character.isJavaIdentifierStart(ch)) {
            while (p < src.length() && Character.isJavaIdentifierPart(src.charAt(p))) p++;
            return p - tokenStart;
        }

        if (Character.isDigit(ch) || ch == '.') {
            while (p < src.length()
                    && (Character.isDigit(src.charAt(p)) || src.charAt(p) == '.')) p++;
            return p - tokenStart;
        }

        if ("+-*/=<>!&|^%~.,;()[]{}".indexOf(ch) >= 0) {
            while (p < src.length()
                    && "+-*/=<>!&|^%~.,;()[]{}".indexOf(src.charAt(p)) >= 0) p++;
            return p - tokenStart;
        }

        if (ch == '"' || ch == '\'') {
            char quote = ch;
            p++;
            while (p < src.length()) {
                if (src.charAt(p) == '\\') {
                    p += 2;
                } else if (src.charAt(p) == quote) {
                    p++;
                    break;
                } else {
                    p++;
                }
            }
            return p - tokenStart;
        }

        return 1;
    }
}
