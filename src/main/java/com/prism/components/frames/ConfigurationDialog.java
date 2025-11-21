package com.prism.components.frames;

import com.prism.Prism;
import com.prism.components.definition.ConfigKey;
import com.prism.components.definition.Language;
import com.prism.components.extended.JDefaultKineticScrollPane;
import com.prism.components.textarea.TextArea;
import com.prism.managers.FileManager;
import com.prism.utils.ResourceUtil;
import com.prism.utils.Theme;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.swing.tree.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigurationDialog extends JFrame {

	private static final String JAVA_SAMPLE = """
			import java.util.*;
			import java.util.stream.*;
			
			public class Example {
			    public static void main(String[] args) {
			        List<String> names = Arrays.asList("Bob", "Carol", "Ted", "Alice");
			        Map<String, Integer> lengths = names.stream()
			            .filter(n -> n.length() > 3)
			            .collect(Collectors.toMap(n -> n, String::length));
			
			        lengths.forEach((k, v) -> System.out.printf("%s -> %d%n", k, v));
			
			        try {
			            int result = divide(10, 2);
			            System.out.println("10 / 2 = " + result);
			        } catch (ArithmeticException ex) {
			            System.err.println("Division error: " + ex.getMessage());
			        }
			    }
			
			    public static int divide(int a, int b) {
			        if (b == 0) throw new ArithmeticException("division by zero");
			        return a / b;
			    }
			}
			""";
	private static final String C_SAMPLE = """
			#include <stdio.h>
			#include <stdlib.h>
			#include <string.h>
			
			#define MAX_NAME 64
			
			typedef struct {
			    char name[MAX_NAME];
			    int age;
			} Person;
			
			void greet(const Person *p) {
			    if (!p) return;
			    printf("Hello, %s! You are %d years old.\\n", p->name, p->age);
			}
			
			int main(void) {
			    Person alice;
			    strncpy(alice.name, "Alice", MAX_NAME - 1);
			    alice.name[MAX_NAME-1] = '\\0';
			    alice.age = 30;
			
			    for (int i = 0; i < 3; ++i) {
			        greet(&alice);
			    }
			
			    int *arr = (int*)malloc(5 * sizeof(int));
			    if (!arr) return EXIT_FAILURE;
			    for (int i = 0; i < 5; ++i) arr[i] = i * i;
			    for (int i = 0; i < 5; ++i) printf("arr[%d] = %d\\n", i, arr[i]);
			
			    free(arr);
			    return 0;
			}
			""";
	private static final String GROOVY_SAMPLE = """
			#!/usr/bin/env groovy
			
			class Person {
			    String name
			    int age
			    String toString() { "Person(name: $name, age: $age)" }
			}
			
			def people = [
			    new Person(name: 'Sam', age: 25),
			    new Person(name: 'Dana', age: 42),
			    new Person(name: 'Lee', age: 19)
			]
			
			people.each { p ->
			    println "Name: ${p.name}, Next year: ${p.age + 1}"
			}
			
			def adults = people.findAll { it.age >= 21 }
			println "Adults: ${adults*.name}"
			
			def config = [host: 'localhost', port: 8080]
			println "Connecting to ${config.host}:${config.port}"
			""";
	private static final String TYPESCRIPT_SAMPLE = """
			enum Role {
			  Guest = "GUEST",
			  User = "USER",
			  Admin = "ADMIN"
			}
			
			interface User {
			  id: number;
			  name: string;
			  role?: Role;
			}
			
			function identity<T>(value: T): T {
			  return value;
			}
			
			async function fetchUser(id: number): Promise<User> {
			  // fake async
			  return new Promise((resolve) => {
			    setTimeout(() => resolve({ id, name: `User${id}`, role: Role.User }), 100);
			  });
			}
			
			(async () => {
			  const u = await fetchUser(7);
			  console.log(`Fetched: ${u.name} (${u.role})`);
			  const same = identity<User>(u);
			  console.log(same);
			})();
			""";
	private static final String HTML_SAMPLE = """
			<!doctype html>
			<html lang="en">
			<head>
			  <meta charset="utf-8" />
			  <title>Highlight Test</title>
			  <meta name="viewport" content="width=device-width,initial-scale=1" />
			  <style>
			    body { font-family: system-ui, sans-serif; margin: 2rem; }
			    .card { border-radius: 8px; box-shadow: 0 2px 6px rgba(0,0,0,0.08); padding: 1rem; }
			  </style>
			</head>
			<body>
			  <main class="card">
			    <h1>Token Highlighting</h1>
			    <p data-note="example">This page contains a form and some inline script to test HTML + JS token highlighting.</p>
			
			    <form id="frm" onsubmit="return handleSubmit(event)">
			      <label for="name">Name:</label>
			      <input id="name" name="name" type="text" placeholder="Enter name" required />
			      <button type="submit">Send</button>
			    </form>
			
			    <pre id="out"></pre>
			  </main>
			
			  <script>
			    function handleSubmit(e) {
			      e.preventDefault();
			      const name = document.getElementById('name').value;
			      const msg = `Hello, ${name}! — ${new Date().toISOString()}`;
			      document.getElementById('out').textContent = msg;
			      return false;
			    }
			  </script>
			</body>
			</html>
			""";
	private final Prism prism = Prism.getInstance();
	private final CardLayout cardLayout = new CardLayout();
	private final JPanel rightPanel = new JPanel(cardLayout);
	/* ---------- UI panels ---------- */
	private final GeneralPanel generalPanel = new GeneralPanel();
	private final EditorPanel editorPanel = new EditorPanel();
	private final TerminalPanel terminalPanel = new TerminalPanel();
	private final InterfacePanel interfacePanel = new InterfacePanel();
	private final SyntaxHighlightingPanel syntaxHighlightingPanel = new SyntaxHighlightingPanel();
	private final AutocompletePanel autocompletePanel = new AutocompletePanel();
	private final ServicesPanel servicesPanel = new ServicesPanel();
	private final CPanel cPanel = new CPanel();
	private final CPPPanel cppPanel = new CPPPanel();
	private final JavaPanel javaPanel = new JavaPanel();

	private JButton applyBtn;
	private boolean requireRestart = false;
	private boolean requireRefresh = false;

	/* ------------------ CENTER (cards) ------------------ */ {
		/* executed in instance-initializer so we can reference ‘this’ */
		rightPanel.setBorder(BorderFactory.createTitledBorder(" Configuration > General "));
		rightPanel.add(generalPanel, "General");
		rightPanel.add(interfacePanel, "Interface");
		rightPanel.add(editorPanel, "Editor");
		rightPanel.add(terminalPanel, "Terminal");
		rightPanel.add(syntaxHighlightingPanel, "Syntax Highlighting");
		rightPanel.add(autocompletePanel, "Autocomplete");
		rightPanel.add(servicesPanel, "Services");
		rightPanel.add(cPanel, "C");
		rightPanel.add(cppPanel, "C++");
		rightPanel.add(javaPanel, "Java");
	}

	public ConfigurationDialog() {
		super("Configuration Settings");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(900, 600);
		setLocationRelativeTo(null);
		setResizable(false);
		setIconImages(ResourceUtil.getAppIcon());

		JPanel content = new JPanel(new BorderLayout(10, 10));
		content.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(content);

		content.add(createWestPanel(), BorderLayout.WEST);
		content.add(rightPanel, BorderLayout.CENTER);
		content.add(createSouthPanel(), BorderLayout.SOUTH);

		registerTreeListener();

		if (prism.getConfig().getInt(ConfigKey.LANGUAGE, 0) != Language.ENGLISH_US.getId()) {
			JOptionPane.showMessageDialog(prism, prism.getLanguage().get(220), prism.getLanguage().get(219), JOptionPane.INFORMATION_MESSAGE);
		}

		setVisible(true);
	}

	/* ------------------ WEST (tree) ------------------ */
	private JScrollPane createWestPanel() {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Configuration");

		DefaultMutableTreeNode general = new DefaultMutableTreeNode("General");
		DefaultMutableTreeNode interface_ = new DefaultMutableTreeNode("Interface");
		DefaultMutableTreeNode editor = new DefaultMutableTreeNode("Editor");
		DefaultMutableTreeNode terminal = new DefaultMutableTreeNode("Terminal");

		DefaultMutableTreeNode syntaxHighlighting = new DefaultMutableTreeNode("Syntax Highlighting");
		DefaultMutableTreeNode autocomplete = new DefaultMutableTreeNode("Autocomplete");
		DefaultMutableTreeNode services = new DefaultMutableTreeNode("Services");
		DefaultMutableTreeNode languages = new DefaultMutableTreeNode("Languages");

		languages.add(new DefaultMutableTreeNode("C"));
		languages.add(new DefaultMutableTreeNode("C++"));
		languages.add(new DefaultMutableTreeNode("Java"));

		editor.add(syntaxHighlighting);
		editor.add(autocomplete);
		editor.add(services);
		editor.add(languages);

		root.add(general);
		root.add(interface_);
		root.add(editor);
		root.add(terminal);

		JTree tree = new JTree(root);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setCellRenderer(new NullIconTreeCellRenderer());
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);

		tree.addTreeSelectionListener((event) -> {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

			if (node != null) {
				StringBuilder path = new StringBuilder(node.toString());
				TreeNode parent = node.getParent();

				while (parent != null) {
					path.insert(0, parent.toString() + " > ");
					parent = parent.getParent();
				}

				rightPanel.setBorder(BorderFactory.createTitledBorder(" " + path.toString() + " "));
			}
		});

		for (int i = 0; i < tree.getRowCount(); i++) tree.expandRow(i);

		JDefaultKineticScrollPane sp = new JDefaultKineticScrollPane(tree);
		sp.setPreferredSize(new Dimension(200, 0));
		sp.setBorder(BorderFactory.createTitledBorder("Categories"));
		return sp;
	}

	/* ------------------ SOUTH (buttons) ------------------ */
	private JPanel createSouthPanel() {
		JButton okBtn = new JButton("OK");
		okBtn.setPreferredSize(new Dimension(80, 25));
		okBtn.addActionListener(e -> closeDialog());

		JButton cancelBtn = new JButton("Cancel");
		cancelBtn.setPreferredSize(new Dimension(80, 25));
		cancelBtn.addActionListener(e -> cancelChanges());

		applyBtn = new JButton("Apply");
		applyBtn.setPreferredSize(new Dimension(80, 25));
		applyBtn.setEnabled(false);
		applyBtn.addActionListener(e -> applyChanges());

		JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		p.add(okBtn);
		p.add(cancelBtn);
		p.add(applyBtn);
		return p;
	}

	/* --------------------------------------------------------------------- */
	/* ------------------  P A N E L   I M P L E M E N T A T I O N S  ----- */
	/* --------------------------------------------------------------------- */

	private void checkForAdditionalRequirements() {
		if (requireRestart) {
			JOptionPane.showMessageDialog(prism, prism.getLanguage().get(216), prism.getLanguage().get(215), JOptionPane.WARNING_MESSAGE);

			FileManager.saveAllFiles();

			prism.prepareClosing();

			setVisible(false);
			dispose();

			System.exit(0);
		} else if (requireRefresh) {
			JOptionPane.showMessageDialog(prism, prism.getLanguage().get(218), prism.getLanguage().get(217), JOptionPane.WARNING_MESSAGE);

			prism.getTextAreaTabbedPane().refresh();
		}
	}

	private void closeDialog() {
		prism.getConfig().save();

		checkForAdditionalRequirements();

		setVisible(false);
		dispose();
	}

	private void cancelChanges() {
		prism.getConfig().mergeConfigToBackup();

		setVisible(false);
		dispose();
	}

	private void applyChanges() {
		prism.getConfig().mergeBackupToConfig();

		prism.getConfig().save();

		applyBtn.setEnabled(false);
	}

	/* ------------------ tree listener ------------------ */
	private void registerTreeListener() {
		JTree tree = (JTree) ((JScrollPane) getContentPane().getComponent(0)).getViewport().getView();
		tree.addTreeSelectionListener(e -> {
			DefaultMutableTreeNode n = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
			if (n != null) cardLayout.show(rightPanel, n.toString());
		});
	}

	private JPanel customSeparator(String text, Color color) {
		JLabel label = new JLabel(text);
		label.setForeground(Theme.invertColorIfDarkThemeSet(color));

		JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);

		return pair(label, separator);
	}

	private JCheckBox checkbox(String text, ConfigKey key, boolean def, boolean... requiresRefresh) {
		JCheckBox c = new JCheckBox(text);
		c.setSelected(prism.getConfig().getBoolean(key, def));
		c.addActionListener(e -> {
			prism.getConfig().set(key, c.isSelected(), false);

			enableApplyButton();

			if (requiresRefresh != null && requiresRefresh.length > 0 && requiresRefresh[0]) {
				setRequireRefresh(true);
			}
		});

		return c;
	}

	private JSpinner spinner(ConfigKey key, int def, int min, int max, int step) {
		JSpinner s = fixedSpinner(new SpinnerNumberModel(prism.getConfig().getInt(key, def), min, max, step));
		s.addChangeListener((ChangeEvent e) -> {
			prism.getConfig().set(key, (int) s.getValue(), false);

			enableApplyButton();
		});
		return s;
	}

	private JPanel pair(Component... cmps) {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		for (Component c : cmps) p.add(c);
		p.setAlignmentX(Component.LEFT_ALIGNMENT);
		p.setBorder(new EmptyBorder(2, 0, 2, 0));
		return p;
	}

	private JComboBox<String> fixedCombo(String[] items) {
		JComboBox<String> c = new JComboBox<>(items);
		c.setMaximumSize(new Dimension(180, c.getPreferredSize().height)); // <-- limit width
		c.setAlignmentX(Component.LEFT_ALIGNMENT);
		return c;
	}

	private JSpinner fixedSpinner(SpinnerNumberModel m) {
		JSpinner s = new JSpinner(m);
		s.setMaximumSize(new Dimension(80, s.getPreferredSize().height)); // <-- limit width
		s.setAlignmentX(Component.LEFT_ALIGNMENT);
		return s;
	}

	private void setRequireRestart(boolean value) {
		this.requireRestart = value;
	}

	private void setRequireRefresh(boolean value) {
		this.requireRefresh = value;
	}

	private void enableApplyButton() {
		applyBtn.setEnabled(true);
	}

	/* ------------------ renderer helper ------------------ */
	public static class NullIconTreeCellRenderer extends DefaultTreeCellRenderer {
		public NullIconTreeCellRenderer() {
			setLeafIcon(null);
			setOpenIcon(null);
			setClosedIcon(null);
		}
	}

	private static final class JavaPanel extends JPanel {
		JavaPanel() {
			build();
		}

		private void build() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setBorder(new EmptyBorder(5, 5, 5, 5));


		}
	}

	/* ------------ GeneralPanel ------------ */
	private final class GeneralPanel extends JPanel {
		GeneralPanel() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setBorder(new EmptyBorder(5, 5, 5, 5));

			JCheckBox updates = checkbox("Check for Updates", ConfigKey.CHECK_FOR_UPDATES, false);

			JCheckBox warn = checkbox("Warn before opening files larger than (MB): ", ConfigKey.WARN_BEFORE_OPENING_LARGE_FILES, true);
			JSpinner size = spinner(ConfigKey.MAX_FILE_SIZE_FOR_WARNING, 10, 1, 500, 1);
			size.setEnabled(warn.isSelected());
			warn.addActionListener(e -> size.setEnabled(warn.isSelected()));

			add(customSeparator("General: ", Color.decode("#0084ff")));

			add(pair(checkbox("Open recent files on startup", ConfigKey.OPEN_RECENT_FILES, true, true)));
			add(pair(updates));

			add(Box.createVerticalStrut(20));
			add(customSeparator("Other: ", Color.decode("#0084ff")));

			add(pair(warn, size));

		}
	}

	/* ------------ EditorPanel ------------ */
	private final class EditorPanel extends JPanel {
		EditorPanel() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setBorder(new EmptyBorder(5, 5, 5, 5));

			add(customSeparator("Gutter: ", Color.decode("#0084ff")));

			add(pair(checkbox("Show line numbers", ConfigKey.SHOW_LINE_NUMBERS, true, true)));
			add(pair(checkbox("Bookmarks", ConfigKey.BOOK_MARKS, true, true)));

			add(Box.createVerticalStrut(20));
			add(customSeparator("Text Area: ", Color.decode("#0084ff")));

			add(pair(checkbox("Anti-Aliasing", ConfigKey.ANTI_ALIASING_ENABLED, true, true)));

			JCheckBox autoIndent = checkbox("Auto-Indent, Tab size: ", ConfigKey.AUTO_INDENT_ENABLED, true, true);
			JSpinner tab = spinner(ConfigKey.TAB_SIZE, 4, 1, 8, 1);
			tab.setEnabled(autoIndent.isSelected());
			autoIndent.addActionListener(e -> tab.setEnabled(autoIndent.isSelected()));
			add(pair(autoIndent, tab));

			add(pair(checkbox("Close curly braces", ConfigKey.CLOSE_CURLY_BRACES, true, true)));
			add(pair(checkbox("Close markup tags", ConfigKey.CLOSE_MARKUP_TAGS, true, true)));
			add(pair(checkbox("Bracket matching", ConfigKey.BRACKET_MATCHING_ENABLED, true, true)));
			add(pair(checkbox("Mark occurrences", ConfigKey.MARK_OCCURRENCES, true, true)));
			add(pair(checkbox("Fade current line highlight", ConfigKey.FADE_CURRENT_LINE_HIGHLIGHT, true, true)));
			add(pair(checkbox("Highlight current line", ConfigKey.HIGHLIGHT_CURRENT_LINE, true, true)));
			add(pair(checkbox("Word wrap", ConfigKey.WORD_WRAP_ENABLED, false, true)));
			add(pair(checkbox("Word wrap style", ConfigKey.WORD_WRAP_STYLE_WORD, true, true)));
			add(pair(checkbox("Code folding", ConfigKey.CODE_FOLDING_ENABLED, true, true)));
			add(pair(checkbox("Show matched bracket popup", ConfigKey.SHOW_MATCHED_BRACKET_POPUP, true, true)));
		}
	}

	/* ------------ TerminalPanel ------------ */
	private final class TerminalPanel extends JPanel {
		TerminalPanel() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setBorder(new EmptyBorder(5, 5, 5, 5));

			add(customSeparator("Terminal: ", Color.decode("#0084ff")));

			add(pair(checkbox("Show Process output code", ConfigKey.SHOW_PROCESS_TERMINATION_CODE_OUTPUT, true)));

			JComboBox<String> defaultShellCombo = fixedCombo(new String[]{
					"Command Prompt",
					"Powershell"
			});
			defaultShellCombo.setSelectedIndex(prism.getConfig().getInt(ConfigKey.DEFAULT_TERMINAL_SHELL, 0));
			defaultShellCombo.addActionListener(e -> {
				prism.getConfig().set(ConfigKey.DEFAULT_TERMINAL_SHELL, defaultShellCombo.getSelectedIndex(), false);

				enableApplyButton();
			});

			add(pair(new JLabel("Default shell: "), defaultShellCombo));
		}
	}

	/* ------------ InterfacePanel ------------ */
	private final class InterfacePanel extends JPanel {
		InterfacePanel() {
			build();
		}

		private void build() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setBorder(new EmptyBorder(5, 5, 5, 5));

			JComboBox<String> languageCombo = fixedCombo(new String[]{
					"English (US)",
					"French"
			});
			languageCombo.setSelectedIndex(prism.getConfig().getInt(ConfigKey.LANGUAGE, 0));
			languageCombo.addActionListener(e -> {
				prism.getConfig().set(ConfigKey.LANGUAGE, languageCombo.getSelectedIndex(), false);

				enableApplyButton();
				setRequireRestart(true);
			});

			JComboBox<String> themeCombo = fixedCombo(new String[]{
					"Windows",
					"Light",
					"Dark"
			});
			themeCombo.setSelectedIndex(prism.getConfig().getInt(ConfigKey.THEME, 1));
			themeCombo.addActionListener(e -> {
				prism.getConfig().set(ConfigKey.THEME, themeCombo.getSelectedIndex(), false);

				enableApplyButton();
				setRequireRestart(true);
			});

			add(pair(new JLabel("Remember! Any changes to the following settings will require Prism to be restarted.")));

			add(Box.createVerticalStrut(20));
			add(customSeparator("Interface: ", Color.decode("#0084ff")));

			add(pair(new JLabel("Language: "), languageCombo));

			add(Box.createVerticalStrut(20));
			add(customSeparator("Themes: ", Color.decode("#0084ff")));

			add(pair(new JLabel("Theme: "), themeCombo));
		}
	}

	/* ------------ SyntaxHighlightingPanel ------------ */
	private final class SyntaxHighlightingPanel extends JPanel {
		private final Map<String, ConfigKey> tokenMap = new LinkedHashMap<>();
		private final JComboBox<String> tokenCombo = fixedCombo(new String[]{});
		private final JButton colorBtn = new JButton();

		SyntaxHighlightingPanel() {
			initTokenMap();
			build();
		}

		private void initTokenMap() {
			tokenMap.put("Annotation", ConfigKey.ANNOTATION);
			tokenMap.put("Reserved Word", ConfigKey.RESERVED_WORD);
			tokenMap.put("String Double Quote", ConfigKey.STRING_DOUBLE_QUOTE);
			tokenMap.put("Character", ConfigKey.CHARACTER);
			tokenMap.put("Backquote", ConfigKey.BACKQUOTE);
			tokenMap.put("Boolean", ConfigKey.BOOLEAN);
			tokenMap.put("Number Integer/Decimal", ConfigKey.NUMBER_INTEGER_DECIMAL);
			tokenMap.put("Number Float", ConfigKey.NUMBER_FLOAT);
			tokenMap.put("Number Hexadecimal", ConfigKey.NUMBER_HEXADECIMAL);
			tokenMap.put("Regular Expression", ConfigKey.REGULAR_EXPRESSION);
			tokenMap.put("Multi-line Comment", ConfigKey.MULTI_LINE_COMMENT);
			tokenMap.put("Documentation Comment", ConfigKey.DOCUMENTATION_COMMENT);
			tokenMap.put("EOL Comment", ConfigKey.EOL_COMMENT);
			tokenMap.put("Separator", ConfigKey.SEPARATOR);
			tokenMap.put("Operator", ConfigKey.OPERATOR);
			tokenMap.put("Identifier", ConfigKey.IDENTIFIER);
			tokenMap.put("Variable", ConfigKey.VARIABLE);
			tokenMap.put("Function", ConfigKey.FUNCTION);
			tokenMap.put("Preprocessor", ConfigKey.PREPROCESSOR);
			tokenMap.put("Markup CData", ConfigKey.MARKUP_CDATA);
			tokenMap.put("Markup Comment", ConfigKey.MARKUP_COMMENT);
			tokenMap.put("Markup DTD", ConfigKey.MARKUP_DTD);
			tokenMap.put("Markup Tag Attribute", ConfigKey.MARKUP_TAG_ATTRIBUTE);
			tokenMap.put("Markup Tag Attribute Value", ConfigKey.MARKUP_TAG_ATTRIBUTE_VALUE);
			tokenMap.put("Markup Tag Delimiter", ConfigKey.MARKUP_TAG_DELIMITER);
			tokenMap.put("Markup Tag Name", ConfigKey.MARKUP_TAG_NAME);
		}

		private void build() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setBorder(new EmptyBorder(5, 5, 5, 5));

			tokenCombo.setModel(new DefaultComboBoxModel<>(tokenMap.keySet().toArray(String[]::new)));
			colorBtn.setOpaque(true);
			tokenCombo.addActionListener(e -> syncColorButton());
			colorBtn.addActionListener(e -> pickColor());

			add(customSeparator("Overriding: ", Color.decode("#0084ff")));

			add(pair(checkbox("Invert colors for Dark theme?", ConfigKey.INVERT_TEXTAREA_TOKEN_COLORS_FOR_DARK_THEME, true, true)));

			add(Box.createVerticalStrut(20));
			add(customSeparator("Highlighters: ", Color.decode("#0084ff")));

			add(pair(new JLabel("Token: "), tokenCombo, Box.createHorizontalStrut(5), colorBtn));

			String[] samples = {C_SAMPLE, JAVA_SAMPLE, GROOVY_SAMPLE, TYPESCRIPT_SAMPLE, HTML_SAMPLE};
			String[] styles = {SyntaxConstants.SYNTAX_STYLE_C, SyntaxConstants.SYNTAX_STYLE_JAVA, SyntaxConstants.SYNTAX_STYLE_GROOVY, SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT, SyntaxConstants.SYNTAX_STYLE_HTML};

			JComboBox<String> lang = fixedCombo(new String[]{"C", "Java", "Groovy", "TypeScript", "HTML"});
			TextArea ta = new TextArea(true);
			ta.setEditable(false);
			ta.setText(samples[0]);
			ta.setSyntaxEditingStyle(styles[0]);
			ta.addSyntaxHighlighting();

			lang.addActionListener(e -> {
				int i = lang.getSelectedIndex();
				ta.setText(samples[i]);
				ta.setSyntaxEditingStyle(styles[i]);
				ta.addSyntaxHighlighting();
			});

			add(pair(new JLabel("Preview: "), lang));
			add(pair(new JDefaultKineticScrollPane(ta)));

			syncColorButton();
		}

		private void syncColorButton() {
			ConfigKey k = tokenMap.get(tokenCombo.getSelectedItem());
			if (k == null) return;
			String hex = prism.getConfig().getString(k, getDefault(k));

			Color c;

			if (prism.getConfig().getBoolean(ConfigKey.INVERT_TEXTAREA_TOKEN_COLORS_FOR_DARK_THEME, true)) {
				c = Theme.invertColorIfDarkThemeSet(Color.decode(hex));
			} else {
				c = Color.decode(hex);
			}

			colorBtn.setText(hex);
			colorBtn.setForeground(c);

			setRequireRefresh(true);
		}

		private void pickColor() {
			ConfigKey k = tokenMap.get(tokenCombo.getSelectedItem());
			if (k == null) return;
			Color chosen = JColorChooser.showDialog(this, "Choose colour for " + tokenCombo.getSelectedItem(),
					colorBtn.getForeground());
			if (chosen == null) return;
			String hex = String.format("#%06X", chosen.getRGB() & 0xFFFFFF);
			prism.getConfig().set(k, hex, false);

			enableApplyButton();
			syncColorButton();
		}

		private String getDefault(ConfigKey k) {
			return switch (k) {
				case RESERVED_WORD -> "#9050B0";
				case STRING_DOUBLE_QUOTE, CHARACTER, BACKQUOTE -> "#669C4D";
				case BOOLEAN -> "#3F8A8F";
				case NUMBER_INTEGER_DECIMAL, NUMBER_FLOAT, NUMBER_HEXADECIMAL -> "#B35055";
				case REGULAR_EXPRESSION -> "#B89550";
				case MULTI_LINE_COMMENT, DOCUMENTATION_COMMENT, EOL_COMMENT -> "#4B5260";
				case FUNCTION -> "#4C8DBF";
				default -> "#000000";
			};
		}
	}

	/* ------------ AutocompletePanel ------------ */
	private final class AutocompletePanel extends JPanel {
		AutocompletePanel() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setBorder(new EmptyBorder(5, 5, 5, 5));

			JCheckBox master = checkbox("Enable Autocomplete", ConfigKey.AUTOCOMPLETE_ENABLED, true);
			JCheckBox popup = checkbox("Autocomplete automatic popup menu", ConfigKey.AUTOCOMPLETE_AUTO_POPUP_ENABLED, true);
			popup.setEnabled(master.isSelected());
			master.addActionListener(e -> popup.setEnabled(master.isSelected()));

			add(customSeparator("Autocomplete: ", Color.decode("#0084ff")));

			add(pair(master));
			add(pair(popup));
		}
	}

	/* ------------ ServicesPanel ------------ */
	private final class ServicesPanel extends JPanel {
		ServicesPanel() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setBorder(new EmptyBorder(5, 5, 5, 5));

			JCheckBox master = checkbox("Enable Services", ConfigKey.ALLOW_SERVICES, true);
			JCheckBox a = checkbox("Syntax problems checker", ConfigKey.ALLOW_SERVICE_SYNTAX_CHECKER, true);
			a.setEnabled(master.isSelected());
			master.addActionListener(e -> a.setEnabled(master.isSelected()));

			add(customSeparator("Services: ", Color.decode("#0084ff")));

			add(pair(master));
			add(pair(a));
		}
	}

	/* ------------ CPanel ------------ */
	private final class CPanel extends JPanel {
		private final JTextField compilerPathField = new JTextField(20);

		CPanel() {
			build();
		}

		private void build() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setBorder(new EmptyBorder(5, 5, 5, 5));

			Dimension pref = compilerPathField.getPreferredSize();
			compilerPathField.setMaximumSize(new Dimension(Integer.MAX_VALUE, pref.height));
			compilerPathField.setText(prism.getConfig().getString(ConfigKey.LANGUAGE_C_GNU_GCC_COMPILER_PATH, ""));
			compilerPathField.setEditable(false);

			JButton browseButton = new JButton("Browse...");
			browseButton.addActionListener(e -> chooseCompilerPath());

			JCheckBox providedInPath = checkbox("GCC provided in PATH?", ConfigKey.LANGUAGE_C_GNU_GCC_COMPILER_PROVIDED_IN_PATH_ENV, true);
			browseButton.setEnabled(!providedInPath.isSelected());
			providedInPath.addActionListener(e -> browseButton.setEnabled(!providedInPath.isSelected()));

			add(new JLabel("The service for C requires GNU Compiler Collection (GCC)."));
			add(new JLabel("Install MinGW from SourceForge to install the compilers."));

			add(Box.createVerticalStrut(20));
			add(customSeparator("Compiler: ", Color.decode("#0084ff")));

			add(pair(providedInPath));
			add(pair(new JLabel("GCC Compiler Executable (.exe) Path: "), Box.createRigidArea(new Dimension(5, 0)), compilerPathField, Box.createRigidArea(new Dimension(5, 0)), browseButton));
		}

		private void chooseCompilerPath() {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileFilter(new FileNameExtensionFilter(
					"GNU Executable Files (*.exe)", "exe"));
			chooser.setDialogTitle("Select GCC Compiler Executable");

			int result = chooser.showOpenDialog(this);
			if (result == JFileChooser.APPROVE_OPTION) {
				String selectedPath = chooser.getSelectedFile().getAbsolutePath();
				compilerPathField.setText(selectedPath);

				prism.getConfig().set(ConfigKey.LANGUAGE_C_GNU_GCC_COMPILER_PATH, selectedPath, false);

				enableApplyButton();
			}
		}
	}

	/* ------------ CPPPanel ------------ */
	private final class CPPPanel extends JPanel {
		private final JTextField compilerPathField = new JTextField(20);

		CPPPanel() {
			build();
		}

		private void build() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setBorder(new EmptyBorder(5, 5, 5, 5));

			Dimension pref = compilerPathField.getPreferredSize();
			compilerPathField.setMaximumSize(new Dimension(Integer.MAX_VALUE, pref.height));
			compilerPathField.setText(prism.getConfig().getString(ConfigKey.LANGUAGE_CPP_GNU_GPP_COMPILER_PATH, ""));
			compilerPathField.setEditable(false);

			JButton browseButton = new JButton("Browse...");
			browseButton.addActionListener(e -> chooseCompilerPath());

			JCheckBox providedInPath = checkbox("G++ provided in PATH?", ConfigKey.LANGUAGE_CPP_GNU_GPP_COMPILER_PROVIDED_IN_PATH_ENV, true);
			browseButton.setEnabled(!providedInPath.isSelected());
			providedInPath.addActionListener(e -> browseButton.setEnabled(!providedInPath.isSelected()));

			add(new JLabel("The service for C++ requires GNU Compiler Collection (GCC)."));
			add(new JLabel("Install MinGW from SourceForge to install the compilers."));

			add(Box.createVerticalStrut(20));
			add(customSeparator("Compiler: ", Color.decode("#0084ff")));

			add(pair(providedInPath));
			add(pair(new JLabel("G++ Compiler Executable (.exe) Path: "), Box.createRigidArea(new Dimension(5, 0)), compilerPathField, Box.createRigidArea(new Dimension(5, 0)), browseButton));
		}

		private void chooseCompilerPath() {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileFilter(new FileNameExtensionFilter(
					"GNU Executable Files (*.exe)", "exe"));
			chooser.setDialogTitle("Select G++ Compiler Executable");

			int result = chooser.showOpenDialog(this);
			if (result == JFileChooser.APPROVE_OPTION) {
				String selectedPath = chooser.getSelectedFile().getAbsolutePath();

				compilerPathField.setText(selectedPath);

				prism.getConfig().set(ConfigKey.LANGUAGE_CPP_GNU_GPP_COMPILER_PATH, selectedPath, false);

				enableApplyButton();
			}
		}
	}
}