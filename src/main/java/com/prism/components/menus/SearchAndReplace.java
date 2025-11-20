package com.prism.components.menus;

import com.prism.Prism;
import com.prism.components.extended.JExtendedTextField;
import com.prism.utils.ResourceUtil;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class SearchAndReplace extends JPanel {
	private static Prism prism = Prism.getInstance();

	private final JExtendedTextField searchField = new JExtendedTextField(25);
	private final JExtendedTextField replaceField = new JExtendedTextField(15);
	private final JLabel matchLabel = new JLabel(" ");
	private final JButton prevBtn = createBtn("icons/ui/chevron-up.svg", prism.getLanguage().get(22));
	private final JButton nextBtn = createBtn("icons/ui/chevron-down.svg", prism.getLanguage().get(23));
	private final JButton replaceBtn = createBtn("icons/ui/replace.svg", prism.getLanguage().get(24));
	private final JToggleButton caseBtn = new JToggleButton("Aa");
	private final JToggleButton wordBtn = new JToggleButton("\\b");
	private final JToggleButton regexBtn = new JToggleButton(".*");
	private final JCheckBox replaceCheck = new JCheckBox(prism.getLanguage().get(113));

	private final java.util.List<Integer> matchStarts = new ArrayList<>();
	private final java.util.List<Integer> matchEnds = new ArrayList<>();
	private volatile int currentIndex = -1;
	private volatile SwingWorker<List<int[]>, Void> worker; // live search worker

	private static final int MAX_HISTORY = 50;
	private final Deque<String> history = new LinkedList<>();
	private int historyCursor = -1; // -1 = not on history

	public SearchAndReplace() {
		super(new BorderLayout());
		setBorder(new EmptyBorder(4, 4, 4, 4));
		setOpaque(true);

		JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
		left.setOpaque(false);
		searchField.setPlaceholder(prism.getLanguage().get(25));
		left.add(searchField);
		left.add(matchLabel);

		JPanel opts = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
		opts.setOpaque(false);
		Stream.of(caseBtn, wordBtn, regexBtn).forEach(b -> {
			b.setFocusable(false);
			b.setMargin(new Insets(2, 4, 2, 4));
			b.setFont(b.getFont().deriveFont(11f));
			opts.add(b);
		});
		left.add(opts);

		JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
		right.setOpaque(false);
		right.add(prevBtn);
		right.add(nextBtn);
		right.add(replaceCheck);
		right.add(replaceBtn);
		replaceBtn.setEnabled(false);
		replaceField.setEnabled(false);
		replaceField.setVisible(false);
		right.add(replaceField);

		add(left, BorderLayout.WEST);
		add(right, BorderLayout.EAST);

		replaceCheck.addActionListener(e -> toggleReplaceBar());
		ItemListener liveListener = e -> scheduleSearch();
		Stream.of(caseBtn, wordBtn, regexBtn).forEach(b -> b.addItemListener(liveListener));

		searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
			public void insertUpdate(javax.swing.event.DocumentEvent e) {
				scheduleSearch();
			}

			public void removeUpdate(javax.swing.event.DocumentEvent e) {
				scheduleSearch();
			}

			public void changedUpdate(javax.swing.event.DocumentEvent e) {
				scheduleSearch();
			}
		});

		prevBtn.addActionListener(e -> gotoMatch(-1));
		nextBtn.addActionListener(e -> gotoMatch(1));
		replaceBtn.addActionListener(e -> replaceCurrent());

		installKeys();
		toggleReplaceBar();
	}

	private void scheduleSearch() {
		if (worker != null) worker.cancel(true);
		worker = new SwingWorker<>() {
			@Override
			protected List<int[]> doInBackground() {
				return performSearch(searchField.getText());
			}

			@Override
			protected void done() {
				if (isCancelled()) return;
				try {
					updateMatches(get());
				} catch (Exception ex) {

				}
			}
		};
		worker.execute();
	}

	private List<int[]> performSearch(String text) {
		List<int[]> list = new ArrayList<>();
		if (text.isEmpty()) return list;
		com.prism.components.textarea.TextArea ta = getTextArea();
		if (ta == null) return list;

		String src = ta.getText();
		if (src.isEmpty()) return list;

		int flags = caseBtn.isSelected() ? 0 : Pattern.CASE_INSENSITIVE;
		String pattern = regexBtn.isSelected() ? text : Pattern.quote(text);
		if (wordBtn.isSelected()) pattern = "\\b" + pattern + "\\b";

		Matcher m = Pattern.compile(pattern, flags).matcher(src);
		while (m.find() && !worker.isCancelled()) list.add(new int[]{m.start(), m.end()});
		return list;
	}

	private void updateMatches(List<int[]> matches) {
		matchStarts.clear();
		matchEnds.clear();
		matches.forEach(a -> {
			matchStarts.add(a[0]);
			matchEnds.add(a[1]);
		});
		currentIndex = matchStarts.isEmpty() ? -1 : 0;
		updateBadge();
		if (currentIndex != -1) highlightCurrent();
	}

	private void gotoMatch(int dir) {
		if (matchStarts.isEmpty()) return;
		currentIndex = (currentIndex + dir + matchStarts.size()) % matchStarts.size();
		highlightCurrent();
		updateBadge();
	}

	private void replaceCurrent() {
		if (currentIndex == -1) return;
		com.prism.components.textarea.TextArea ta = getTextArea();
		if (ta == null) return;
		ta.replaceRange(replaceField.getText(), matchStarts.get(currentIndex), matchEnds.get(currentIndex));
		scheduleSearch();
	}

	private void highlightCurrent() {
		com.prism.components.textarea.TextArea ta = getTextArea();
		if (ta == null) return;
		ta.select(matchStarts.get(currentIndex), matchEnds.get(currentIndex));
	}

	private void addToHistory(String item) {
		if (item.isBlank()) return;
		history.remove(item);
		history.addFirst(item);
		if (history.size() > MAX_HISTORY) history.removeLast();
		historyCursor = -1;
	}

	private void navigateHistory(boolean up) {
		if (history.isEmpty()) return;
		historyCursor += up ? 1 : -1;
		if (historyCursor < 0) {
			historyCursor = -1;
			searchField.setText("");
			return;
		}
		if (historyCursor >= history.size()) historyCursor = history.size() - 1;
		searchField.setText(history.toArray(new String[0])[historyCursor]);
	}

	private void installKeys() {
		InputMap im = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		ActionMap am = getActionMap();
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "next");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK), "prev");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK), "toggleReplace");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clear");

		am.put("next", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				gotoMatch(1);
			}
		});
		am.put("prev", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				gotoMatch(-1);
			}
		});
		am.put("toggleReplace", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				replaceCheck.doClick();
			}
		});
		am.put("clear", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				searchField.setText("");
			}
		});

		// field keys
		searchField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "histUp");
		searchField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "histDown");
		searchField.getActionMap().put("histUp", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				navigateHistory(true);
			}
		});
		searchField.getActionMap().put("histDown", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				navigateHistory(false);
			}
		});
		replaceField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK), "replace");
		replaceField.getActionMap().put("replace", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				replaceCurrent();
			}
		});
	}

	private void toggleReplaceBar() {
		boolean on = replaceCheck.isSelected();
		replaceField.setVisible(on);
		replaceField.setEnabled(on);
		replaceBtn.setEnabled(on);
		revalidate();
		if (on) replaceField.requestFocus();
	}

	private void updateBadge() {
		if (matchStarts.isEmpty()) {
			matchLabel.setText("—");
			matchLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
		} else {
			matchLabel.setText(String.format("%d/%d", currentIndex + 1, matchStarts.size()));
			matchLabel.setForeground(currentIndex == -1 ? Color.RED : UIManager.getColor("Label.foreground"));
		}
	}

	private JButton createBtn(String svg, String tip) {
		ImageIcon icon = ResourceUtil.getIconFromSVG(svg, 16, 16);

		JButton b = new JButton();
		b.setToolTipText(tip);
		b.setFocusable(false);
		b.setPreferredSize(new Dimension(22, 22));
		b.setMargin(new Insets(0, 0, 0, 0));

		if (icon != null) {
			b.setIcon(icon);
		}

		return b;
	}

	private com.prism.components.textarea.TextArea getTextArea() {
		try {
			return prism.getTextAreaTabbedPane().getCurrentFile().getTextArea();
		} catch (Exception ex) {
			return null;
		}
	}
}