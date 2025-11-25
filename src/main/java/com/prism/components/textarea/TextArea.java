package com.prism.components.textarea;

import com.prism.Prism;
import com.prism.components.definition.ConfigKey;
import com.prism.managers.AutocompleteManager;
import com.prism.utils.Languages;
import com.prism.utils.Theme;
import org.fife.ui.autocomplete.*;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TextArea extends RSyntaxTextArea {

    private static final Prism prism = Prism.getInstance();

    public TextArea(boolean... noConfigZoom) {
        super();

        setAnimateBracketMatching(false);
        setShowMatchedBracketPopup(prism.getConfig().getBoolean(ConfigKey.SHOW_MATCHED_BRACKET_POPUP, true));
        setCodeFoldingEnabled(prism.getConfig().getBoolean(ConfigKey.CODE_FOLDING_ENABLED, true));
        setAntiAliasingEnabled(prism.getConfig().getBoolean(ConfigKey.ANTI_ALIASING_ENABLED, true));
        setTabSize(prism.getConfig().getInt(ConfigKey.TAB_SIZE, 4));
        setTabsEmulated(true);
        setAutoIndentEnabled(prism.getConfig().getBoolean(ConfigKey.AUTO_INDENT_ENABLED, true));
        setCloseCurlyBraces(prism.getConfig().getBoolean(ConfigKey.CLOSE_CURLY_BRACES, true));
        setCloseMarkupTags(prism.getConfig().getBoolean(ConfigKey.CLOSE_MARKUP_TAGS, true));
        setBracketMatchingEnabled(prism.getConfig().getBoolean(ConfigKey.BRACKET_MATCHING_ENABLED, true));
        setMarkOccurrences(prism.getConfig().getBoolean(ConfigKey.MARK_OCCURRENCES, true));
        setFadeCurrentLineHighlight(prism.getConfig().getBoolean(ConfigKey.FADE_CURRENT_LINE_HIGHLIGHT, true));
        setHighlightCurrentLine(prism.getConfig().getBoolean(ConfigKey.HIGHLIGHT_CURRENT_LINE, true));
        setLineWrap(prism.getConfig().getBoolean(ConfigKey.WORD_WRAP_ENABLED, false));
        setWrapStyleWord(prism.getConfig().getBoolean(ConfigKey.WORD_WRAP_STYLE_WORD, true));

        setHighlightSecondaryLanguages(false);

        if (noConfigZoom.length == 1 && noConfigZoom[0]) {
            setFont(new Font(prism.getConfig().getString(ConfigKey.TEXT_AREA_FONT_NAME, "Consolas"), Font.PLAIN,  12));
        } else {
            setFont(new Font(prism.getConfig().getString(ConfigKey.TEXT_AREA_FONT_NAME, "Consolas"), Font.PLAIN, prism.getConfig().getInt(ConfigKey.TEXTAREA_ZOOM, 12)));
        }

        if (Theme.isDarkTheme()) {
            setBackground(Theme.getSecondaryColor());

            setCaretColor(Color.WHITE);
            setDisabledTextColor(Theme.invertColor(getDisabledTextColor()));
            setSelectedTextColor(Theme.invertColor(getSelectedTextColor()));
            setSelectionColor(Theme.invertColor(getSelectionColor()));
            setMarginLineColor(Theme.invertColor(getMarginLineColor()));
            setMarkOccurrencesColor(Theme.invertColor(getMarkOccurrencesColor()));
            setTabLineColor(Theme.invertColor(getTabLineColor()));
            setCurrentLineHighlightColor(Theme.invertColor(getCurrentLineHighlightColor()));
            setMarkAllHighlightColor(Theme.invertColor(getMarkAllHighlightColor()));
            setMatchedBracketBGColor(Theme.invertColor(getMatchedBracketBGColor()));
            setMatchedBracketBorderColor(Theme.invertColor(getMatchedBracketBorderColor()));
        }
    }

    public void addSyntaxHighlighting() {
        SyntaxScheme scheme = getSyntaxScheme();

        /* -------------- Core language -------------- */
        scheme.getStyle(Token.ANNOTATION).foreground = parse("#A57440");   // darker amber
        scheme.getStyle(Token.RESERVED_WORD).foreground = parse("#9050B0");   // darker violet
        scheme.getStyle(Token.RESERVED_WORD_2).foreground = parse("#9050B0");

        /* Literals */
        scheme.getStyle(Token.LITERAL_STRING_DOUBLE_QUOTE).foreground = parse("#669C4D");   // darker green
        scheme.getStyle(Token.LITERAL_CHAR).foreground = parse("#669C4D");
        scheme.getStyle(Token.LITERAL_BACKQUOTE).foreground = parse("#669C4D");

        scheme.getStyle(Token.LITERAL_BOOLEAN).foreground = parse("#3F8A8F");   // darker cyan

        scheme.getStyle(Token.LITERAL_NUMBER_DECIMAL_INT).foreground = parse("#B35055");   // darker red
        scheme.getStyle(Token.LITERAL_NUMBER_FLOAT).foreground = parse("#B35055");
        scheme.getStyle(Token.LITERAL_NUMBER_HEXADECIMAL).foreground = parse("#B35055");

        scheme.getStyle(Token.REGEX).foreground = parse("#B89550");   // darker yellow

        /* Comments */
        scheme.getStyle(Token.COMMENT_MULTILINE).foreground = parse("#4B5260");   // darker grey
        scheme.getStyle(Token.COMMENT_DOCUMENTATION).foreground = parse("#4B5260");
        scheme.getStyle(Token.COMMENT_EOL).foreground = parse("#4B5260");

        /* Structural */
        scheme.getStyle(Token.SEPARATOR).foreground = parse("#000000");   // BLACK
        scheme.getStyle(Token.OPERATOR).foreground = parse("#000000");   // BLACK
        scheme.getStyle(Token.IDENTIFIER).foreground = parse("#000000");   // BLACK
        scheme.getStyle(Token.VARIABLE).foreground = parse("#B35055");   // same darker red
        scheme.getStyle(Token.FUNCTION).foreground = parse("#4C8DBF");   // darker blue
        scheme.getStyle(Token.PREPROCESSOR).foreground = parse("#9050B0");   // darker violet

        /* -------------- Markup (HTML/XML) -------------- */
        scheme.getStyle(Token.MARKUP_CDATA).foreground = parse("#669C4D");
        scheme.getStyle(Token.MARKUP_COMMENT).foreground = parse("#4B5260");
        scheme.getStyle(Token.MARKUP_DTD).foreground = parse("#B35055");
        scheme.getStyle(Token.MARKUP_TAG_ATTRIBUTE).foreground = parse("#A57440");
        scheme.getStyle(Token.MARKUP_TAG_ATTRIBUTE_VALUE).foreground = parse("#669C4D");
        scheme.getStyle(Token.MARKUP_TAG_DELIMITER).foreground = parse("#000000");   // BLACK
        scheme.getStyle(Token.MARKUP_TAG_NAME).foreground = parse("#B35055");

        setSyntaxScheme(scheme);
    }

    private static Color parse(String hex) {
        if (prism.getConfig().getBoolean(ConfigKey.INVERT_TEXTAREA_TOKEN_COLORS_FOR_DARK_THEME, true)) {
            return Theme.invertColorIfDarkThemeSet(Color.decode(hex));
        } else {
            return Color.decode(hex);
        }
    }

    public void addAutocomplete(File file) {
        try {
            String lang = Languages.getFullName(file);

            if (lang == null) return;

            DefaultCompletionProvider provider = new DefaultCompletionProvider() {
                @Override
                public List<Completion> getCompletions(JTextComponent comp) {
                    RSyntaxTextArea area = (RSyntaxTextArea) comp;

                    String line = area.getText()
                            .substring(area.getLineStartOffsetOfCurrentLine(),
                                    area.getLineEndOffsetOfCurrentLine());

                    int col = area.getCaretOffsetFromLineStart();
                    String left = line.substring(0, col);

                    int dot = left.lastIndexOf('.');
                    if (dot >= 0) {
                        String prefix = left.substring(0, dot);

                        return new ArrayList<>(AutocompleteManager.getChildren(this, lang, prefix));
                    }

                    return super.getCompletions(comp);
                }
            };

            provider.setAutoActivationRules(true, ".");

            AutocompleteManager.installCompletions(provider, lang);

            AutoCompletion ac = new AutoCompletion(provider);
            ac.setAutoActivationEnabled(true);
            ac.setAutoActivationDelay(250);

            ac.setListCellRenderer(new CompletionCellRenderer() {
                private final JLabel label = new JLabel();

                @Override
                protected void prepareForOtherCompletion(JList<?> list,
                                                         Completion c, int index,
                                                         boolean selected, boolean hasFocus) {
                    super.prepareForOtherCompletion(list, c, index, selected, hasFocus);
                    setIcon(super.getIcon());

                    String input = c.getInputText();

                    String desc = c.getSummary();
                    if (desc == null) desc = "";

                    String html = String.format(
                            "<html><body><b>%s</b> - %s</body></html>",
                            input, desc);

                    super.setToolTipText(html);
                }
            });

            ac.install(this);

        } catch (Exception ignore) {
        }
    }

    public void setCursorOnLine(int line) {
        Document doc = getDocument();
        try {
            int lineStartOffset = doc.getDefaultRootElement().getElement(line).getStartOffset();
            setCaretPosition(lineStartOffset);
            requestFocusInWindow();
        } catch (ArrayIndexOutOfBoundsException e) {

        }
    }

    public void replace(String newText) {
        Document doc = getDocument();
        try {
            beginAtomicEdit();

            try {
                doc.remove(0, doc.getLength());
                doc.insertString(0, newText, null);
            } finally {
                endAtomicEdit();
            }
        } catch (BadLocationException ex) {

        }
    }

    public void replace(String newText, boolean rememberLastCaretPosition) {
        Document doc = getDocument();
        try {
            final int pos = getCaretPosition();

            beginAtomicEdit();

            try {
                doc.remove(0, doc.getLength());
                doc.insertString(0, newText, null);

                if (rememberLastCaretPosition) {
                    setCaretPosition(pos);
                }
            } finally {
                endAtomicEdit();
            }
        } catch (BadLocationException ex) {

        }
    }
}
