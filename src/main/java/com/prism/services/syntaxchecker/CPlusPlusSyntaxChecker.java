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

public class CPlusPlusSyntaxChecker {
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

    private static final Pattern GPP_DIAG =
            Pattern.compile("^(?<file>.+?):(?<line>\\d+):(?<col>\\d+):\\s*(?<kind>error|warning|note):\\s*(?<msg>.+)$");

    public static void install(PrismFile pf, TextArea area, Path gppBinary) {
        area.addParser(new AbstractParser() {
            @Override
            public ParseResult parse(RSyntaxDocument doc, String style) {
                try {
                    String src = doc.getText(0, doc.getLength());

                    List<Problem> list = check(src, gppBinary);

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

    private static List<Problem> check(String source, Path gpp) throws IOException {
        Path tmp = Files.createTempFile("rsyntax", ".cpp");
        Files.write(tmp, source.getBytes(StandardCharsets.UTF_8));

        String exe = (gpp == null) ? "g++" : gpp.toAbsolutePath().toString();
        ProcessBuilder pb = new ProcessBuilder(
                exe, "-fsyntax-only", "-xc++", tmp.toString())
                .redirectErrorStream(true);

        List<Problem> out = new ArrayList<>();
        try (BufferedReader br =
                     new BufferedReader(new InputStreamReader(pb.start().getInputStream()))) {
            String l;
            while ((l = br.readLine()) != null) {
                Matcher m = GPP_DIAG.matcher(l);
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

        if (Character.isDigit(ch)
                || (ch == '.' && p + 1 < src.length() && Character.isDigit(src.charAt(p + 1)))) {
            boolean isHex = false, isBin = false;
            if (ch == '0' && p + 1 < src.length()) {
                char n = src.charAt(p + 1);
                if (n == 'x' || n == 'X') { isHex = true; p += 2; }
                else if (n == 'b' || n == 'B') { isBin = true; p += 2; }
            }

            while (p < src.length()) {
                char c = src.charAt(p);
                if (isBin && (c == '0' || c == '1')) p++;
                else if (isHex && Character.digit(c, 16) >= 0) p++;
                else if (!isHex && !isBin && Character.isDigit(c)) p++;
                else if (c == '\'') p++;
                else break;
            }

            if (p < src.length() && src.charAt(p) == '.') {
                p++;
                while (p < src.length()) {
                    char c = src.charAt(p);
                    if (isHex && Character.digit(c, 16) >= 0) p++;
                    else if (!isHex && Character.isDigit(c)) p++;
                    else if (c == '\'') p++;
                    else break;
                }
            }

            if (p < src.length()
                    && (src.charAt(p) == 'e' || src.charAt(p) == 'E'
                    || (isHex && (src.charAt(p) == 'p' || src.charAt(p) == 'P')))) {
                p++;
                if (p < src.length()
                        && (src.charAt(p) == '+' || src.charAt(p) == '-')) p++;
                while (p < src.length() && Character.isDigit(src.charAt(p))) p++;
            }

            while (p < src.length()
                    && (src.charAt(p) == 'f' || src.charAt(p) == 'F'
                    || src.charAt(p) == 'l' || src.charAt(p) == 'L'
                    || src.charAt(p) == 'u' || src.charAt(p) == 'U'
                    || src.charAt(p) == 'i' || src.charAt(p) == 'I')) p++;
            return p - tokenStart;
        }

        if (ch == '\'' || (p + 1 < src.length()
                && (src.charAt(p) == 'u' || src.charAt(p) == 'U' || src.charAt(p) == 'L')
                && src.charAt(p + 1) == '\'')) {
            if (ch != '\'') p++;
            p++;
            if (p < src.length() && src.charAt(p) == '\\') p += 2;
            else p++;
            if (p < src.length() && src.charAt(p) == '\'') p++;
            return p - tokenStart;
        }

        if (ch == 'R' && p + 2 < src.length()
                && src.charAt(p + 1) == '"' && src.charAt(p + 2) == '(') {
            p += 3;
            int delim = src.indexOf(")", p);
            if (delim == -1) return 1;
            String d = src.substring(p, delim);
            int close = src.indexOf(")" + d + "\"", delim + 1);
            if (close == -1) return 1;
            p = close + d.length() + 2;
            return p - tokenStart;
        }

        if (ch == '"'
                || (p + 1 < src.length()
                && (src.charAt(p) == 'u' || src.charAt(p) == 'U' || src.charAt(p) == 'L')
                && src.charAt(p + 1) == '"')) {
            if (ch != '"') p++;
            p++;
            while (p < src.length() && src.charAt(p) != '"') {
                if (src.charAt(p) == '\\') p += 2;
                else p++;
            }
            if (p < src.length() && src.charAt(p) == '"') p++;
            return p - tokenStart;
        }

        String[] three = {"<<=", ">>=", "..."};
        String[] two   = {"->*", ".*", ">>", "<<", "++", "--", "==", "!=", "<=",
                ">=", "&&", "||", "+=", "-=", "*=", "/=", "%=", "&=",
                "|=", "^=", "::", "->", ".*", "??", "/*", "//", "*/"};
        if (p + 2 < src.length()) {
            String triple = src.substring(p, p + 3);
            for (String t : three) if (triple.equals(t)) return 3;
        }
        if (p + 1 < src.length()) {
            String pair = src.substring(p, p + 2);
            for (String t : two) if (pair.equals(t)) return 2;
        }

        if ("+-*/=<>!&|^%~.,;()[]{}?:#".indexOf(ch) >= 0) return 1;

        return 1;
    }
}