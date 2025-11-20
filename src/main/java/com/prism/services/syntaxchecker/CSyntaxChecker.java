package com.prism.services.syntaxchecker;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.prism.Prism;
import com.prism.components.definition.PrismFile;
import com.prism.components.textarea.TextArea;
import com.prism.managers.TextAreaManager;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CSyntaxChecker {
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

    private static final Pattern GCC_DIAG =
            Pattern.compile("^(?<file>.+?):(?<line>\\d+):(?<col>\\d+):\\s*(?<kind>error|warning|note):\\s*(?<msg>.+)$");

    public static void install(PrismFile pf, TextArea area, Path gccBinary) {
        area.addParser(new AbstractParser() {
            @Override
            public ParseResult parse(RSyntaxDocument doc, String style) {
                try {
                    String src = doc.getText(0, doc.getLength());

                    List<Problem> list = check(src, gccBinary);

                    pf.setProblems(new ArrayList<>());

                    DefaultParseResult res = new DefaultParseResult(this);

                    list.forEach(p -> {
                        int line = p.line - 1;
                        int col = Math.max(0, p.col - 1);

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

    private static List<Problem> check(String source, Path gcc) throws IOException {
        Path tmp = Files.createTempFile("rsyntax", ".c");
        Files.write(tmp, source.getBytes(StandardCharsets.UTF_8));

        String exe = (gcc == null) ? "gcc" : gcc.toAbsolutePath().toString();
        ProcessBuilder pb = new ProcessBuilder(
                exe, "-fsyntax-only", "-xc", tmp.toString())
                .redirectErrorStream(true);

        List<Problem> out = new ArrayList<>();
        try (BufferedReader br =
                     new BufferedReader(new InputStreamReader(pb.start().getInputStream()))) {
            String l;
            while ((l = br.readLine()) != null) {
                Matcher m = GCC_DIAG.matcher(l);
                if (m.find()) {
                    Problem p = new Problem();

                    p.line = Integer.parseInt(m.group("line"));
                    p.col = Integer.parseInt(m.group("col"));
                    p.msg = m.group("msg");
                    p.kind = m.group("kind").toUpperCase(Locale.ROOT);

                    out.add(p);
                }
            }
        } finally {
            Files.deleteIfExists(tmp);
        }
        return out;
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

        if (Character.isJavaIdentifierStart(ch) || ch == '_') {
            while (p < src.length()
                    && (Character.isJavaIdentifierPart(src.charAt(p)) || src.charAt(p) == '_'))
                p++;
            return p - tokenStart;
        }

        if (Character.isDigit(ch)) {
            if (ch == '0' && p + 1 < src.length()
                    && (src.charAt(p + 1) == 'x' || src.charAt(p + 1) == 'X')) {
                p += 2;                      // skip 0x
                while (p < src.length()
                        && Character.digit(src.charAt(p), 16) >= 0) p++;
            } else if (ch == '0' && p + 1 < src.length()
                    && Character.isDigit(src.charAt(p + 1))) {
                do {
                    p++;
                } while (p < src.length()
                        && Character.digit(src.charAt(p), 8) >= 0);
            } else {
                while (p < src.length() && Character.isDigit(src.charAt(p))) p++;
            }

            if (p < src.length()
                    && (src.charAt(p) == '.' || src.charAt(p) == 'e' || src.charAt(p) == 'E'
                    || src.charAt(p) == 'f' || src.charAt(p) == 'F'
                    || src.charAt(p) == 'l' || src.charAt(p) == 'L')) {
                if (src.charAt(p) == '.') {
                    p++;
                    while (p < src.length() && Character.isDigit(src.charAt(p))) p++;
                }
                if (p < src.length()
                        && (src.charAt(p) == 'e' || src.charAt(p) == 'E')) {
                    p++;
                    if (p < src.length()
                            && (src.charAt(p) == '+' || src.charAt(p) == '-')) p++;
                    while (p < src.length() && Character.isDigit(src.charAt(p))) p++;
                }
                if (p < src.length()
                        && (src.charAt(p) == 'f' || src.charAt(p) == 'F'
                        || src.charAt(p) == 'l' || src.charAt(p) == 'L')) p++;
            }
            return p - tokenStart;
        }

        if (ch == '\'') {
            p++;
            if (p < src.length() && src.charAt(p) == '\\') p += 2;
            else p++;
            if (p < src.length() && src.charAt(p) == '\'') p++;
            return p - tokenStart;
        }

        if (ch == '"') {
            p++;
            while (p < src.length() && src.charAt(p) != '"') {
                if (src.charAt(p) == '\\') p += 2;
                else p++;
            }
            if (p < src.length() && src.charAt(p) == '"') p++;
            return p - tokenStart;
        }

        String[] twoChar = {"->", "++", "--", "<<", ">>", "<=", ">=", "==", "!=", "&&",
                "||", "+=", "-=", "*=", "/=", "%=", "&=", "^=", "|=", "##"};
        String[] threeChar = {"<<=", ">>="};
        if (p + 2 < src.length()) {
            String triple = src.substring(p, p + 3);
            for (String t : threeChar)
                if (triple.equals(t)) return 3;
        }
        if (p + 1 < src.length()) {
            String pair = src.substring(p, p + 2);
            for (String t : twoChar)
                if (pair.equals(t)) return 2;
        }

        if ("+-*/=<>!&|^%~.,;()[]{}?:#".indexOf(ch) >= 0) return 1;

        return 1;
    }
}