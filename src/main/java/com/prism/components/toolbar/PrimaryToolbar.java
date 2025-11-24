package com.prism.components.toolbar;

import com.prism.Prism;
import com.prism.components.definition.PrismFile;
import com.prism.components.frames.TextDifferFrame;
import com.prism.services.LanguageService;
import com.prism.components.textarea.TextArea;
import com.prism.managers.FileManager;
import com.prism.managers.TextAreaManager;
import com.prism.services.Service;
import com.prism.utils.Languages;
import com.prism.utils.ResourceUtil;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.File;

public class PrimaryToolbar extends JToolBar {
    private static final Prism prism = Prism.getInstance();

    public JButton buttonNewFile;
    public JButton buttonFileOpen;
    public JButton buttonFolder;
    public JButton buttonSave;
    public JButton buttonSaveAll;
    public JButton buttonCopy;
    public JButton buttonPaste;
    public JButton buttonCut;
    public JButton buttonUndo;
    public JButton buttonRedo;
    public JButton buttonZoomIn;
    public JButton buttonZoomOut;
    public JButton buttonRefreshTextArea;
    public JButton buttonTextDiff;
    public JButton buttonLanguageService;

    public PrimaryToolbar() {
        setFloatable(false);
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setBorder(new EmptyBorder(5, 5, 5, 0));

        buttonNewFile = createButton(ResourceUtil.getIconFromSVG("icons/ui/file-plus.svg"), prism.getLanguage().get(152));
        buttonNewFile.addActionListener((_) -> {
            FileManager.openNewFile();
        });

        buttonFileOpen = createButton(ResourceUtil.getIconFromSVG("icons/ui/file-import.svg"), prism.getLanguage().get(156));
        buttonFileOpen.addActionListener((_) -> {
            FileManager.openFile();
        });

        buttonFolder = createButton(ResourceUtil.getIconFromSVG("icons/ui/folder-open.svg"), prism.getLanguage().get(157));
        buttonFolder.addActionListener((_) -> {
            FileManager.openDirectory();
        });

        buttonSave = createButton(ResourceUtil.getIconFromSVG("icons/ui/save.svg"), prism.getLanguage().get(158));
        buttonSave.addActionListener((_) -> {
            PrismFile file = Prism.getInstance().getTextAreaTabbedPane().getCurrentFile();

            FileManager.saveFile(file);
        });

        buttonSaveAll = createButton(ResourceUtil.getIconFromSVG("icons/ui/save-all.svg"), prism.getLanguage().get(160));
        buttonSaveAll.addActionListener((_) -> {
            FileManager.saveAllFiles();
        });

        buttonCopy = createButton(ResourceUtil.getIconFromSVG("icons/ui/copy.svg"), prism.getLanguage().get(40));
        buttonCopy.addActionListener((_) -> {
            com.prism.components.textarea.TextArea textArea = prism.getTextAreaTabbedPane().getCurrentFile().getTextArea();

            if (textArea != null) {
                textArea.copy();
            }
        });

        buttonPaste = createButton(ResourceUtil.getIconFromSVG("icons/ui/clipboard.svg"), prism.getLanguage().get(41));
        buttonPaste.addActionListener((_) -> {
            com.prism.components.textarea.TextArea textArea = prism.getTextAreaTabbedPane().getCurrentFile().getTextArea();

            if (textArea != null) {
                textArea.paste();
            }
        });

        buttonCut = createButton(ResourceUtil.getIconFromSVG("icons/ui/cut.svg"), prism.getLanguage().get(166));
        buttonCut.addActionListener((_) -> {
            com.prism.components.textarea.TextArea textArea = prism.getTextAreaTabbedPane().getCurrentFile().getTextArea();

            if (textArea != null) {
                textArea.cut();
            }
        });

        buttonUndo = createButton(ResourceUtil.getIconFromSVG("icons/ui/undo.svg"), prism.getLanguage().get(164));
        buttonUndo.addActionListener((_) -> {

            com.prism.components.textarea.TextArea textArea = prism.getTextAreaTabbedPane().getCurrentFile().getTextArea();

            if (textArea != null && textArea.canUndo()) {
                textArea.undoLastAction();
            }
        });

        buttonRedo = createButton(ResourceUtil.getIconFromSVG("icons/ui/redo.svg"), prism.getLanguage().get(165));
        buttonRedo.addActionListener((_) -> {
            com.prism.components.textarea.TextArea textArea = prism.getTextAreaTabbedPane().getCurrentFile().getTextArea();

            if (textArea != null && textArea.canRedo()) {
                textArea.redoLastAction();
            }
        });

        buttonZoomIn = createButton(ResourceUtil.getIconFromSVG("icons/ui/zoom-in.svg"), prism.getLanguage().get(193));
        buttonZoomIn.addActionListener((_) -> {
            TextAreaManager.zoomIn();
        });

        buttonZoomOut = createButton(ResourceUtil.getIconFromSVG("icons/ui/zoom-out.svg"), prism.getLanguage().get(194));
        buttonZoomOut.addActionListener((_) -> {
            TextAreaManager.zoomOut();
        });

        buttonRefreshTextArea = createButton(ResourceUtil.getIconFromSVG("icons/ui/refresh.svg"), prism.getLanguage().get(49));
        buttonRefreshTextArea.addActionListener((_) -> {
            prism.getTextAreaTabbedPane().refresh();
        });

        buttonTextDiff = createButton(ResourceUtil.getIconFromSVG("icons/ui/file-diff.svg"), prism.getLanguage().get(195));
        buttonTextDiff.addActionListener((_) -> {
            PrismFile prismFile = prism.getTextAreaTabbedPane().getCurrentFile();
            com.prism.components.textarea.TextArea textArea = prismFile.getTextArea();
            File file = prismFile.getFile();

            if (file == null) {
                return;
            }

            String oldText = FileManager.getOriginalText(file);

            if (oldText == null) {
                return;
            }

            if (textArea != null) {
                new TextDifferFrame(file, oldText, file, textArea.getText());
            }
        });

        buttonLanguageService = createButton(ResourceUtil.getIconFromSVG("icons/ui/tool.svg"), prism.getLanguage().get(196));
        buttonLanguageService.addActionListener((_) -> {
            PrismFile prismFile = prism.getTextAreaTabbedPane().getCurrentFile();
            com.prism.components.textarea.TextArea textArea = prismFile.getTextArea();
            File file = prismFile.getFile();

            if (file == null) {
                return;
            }

            Service service = Languages.getService(file);

            if (service == null) {
                JOptionPane.showMessageDialog(prism, prism.getLanguage().get(198),
                        prism.getLanguage().get(197),
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            new LanguageService(service, buttonLanguageService);
        });

        add(buttonNewFile);
        add(Box.createRigidArea(new Dimension(4, 0)));
        add(buttonFileOpen);
        add(Box.createRigidArea(new Dimension(4, 0)));
        add(buttonFolder);
        add(Box.createRigidArea(new Dimension(4, 0)));
        add(buttonSave);
        add(Box.createRigidArea(new Dimension(4, 0)));
        add(buttonSaveAll);

        add(Box.createRigidArea(new Dimension(4, 0)));
        addSeparator(new Dimension(4, 20));
        add(Box.createRigidArea(new Dimension(4, 0)));

        add(buttonCut);
        add(Box.createRigidArea(new Dimension(4, 0)));
        add(buttonCopy);
        add(Box.createRigidArea(new Dimension(4, 0)));
        add(buttonPaste);

        add(Box.createRigidArea(new Dimension(4, 0)));
        addSeparator(new Dimension(4, 20));
        add(Box.createRigidArea(new Dimension(4, 0)));

        add(buttonUndo);
        add(Box.createRigidArea(new Dimension(4, 0)));
        add(buttonRedo);

        add(Box.createRigidArea(new Dimension(4, 0)));
        addSeparator(new Dimension(4, 20));
        add(Box.createRigidArea(new Dimension(4, 0)));

        add(buttonZoomIn);
        add(Box.createRigidArea(new Dimension(4, 0)));
        add(buttonZoomOut);

        add(Box.createRigidArea(new Dimension(4, 0)));
        addSeparator(new Dimension(4, 20));
        add(Box.createRigidArea(new Dimension(4, 0)));

        add(buttonRefreshTextArea);
        add(Box.createRigidArea(new Dimension(4, 0)));
        add(buttonTextDiff);
        add(Box.createRigidArea(new Dimension(4, 0)));
        add(buttonLanguageService);

        JPanel panel = new JPanel();
        add(panel, BorderLayout.CENTER);
    }

    public void updateComponent() {
        PrismFile prismFile = prism.getTextAreaTabbedPane().getCurrentFile();

        if (prismFile == null) {
            return;
        }

        if (prismFile.isText()) {
            TextArea textArea = prismFile.getTextArea();

            buttonRedo.setEnabled(textArea.canRedo());
            buttonUndo.setEnabled(textArea.canUndo());

            buttonSave.setEnabled(!prismFile.isSaved());
        }

        File file = prismFile.getFile();

        buttonCut.setEnabled(prismFile.isText());
        buttonCopy.setEnabled(prismFile.isText());
        buttonPaste.setEnabled(prismFile.isText());
        buttonZoomIn.setEnabled(prismFile.isText());
        buttonZoomOut.setEnabled(prismFile.isText());
        buttonTextDiff.setEnabled(prismFile.isText() && file != null);

        if (prismFile.isText() && file != null) {
            Service service = Languages.getService(file);

            buttonLanguageService.setEnabled(service != null);
        } else {
            buttonLanguageService.setEnabled(false);
        }
    }

    private JButton createButton(ImageIcon buttonIcon, String tooltip) {
        JButton button = new JButton();

        if (tooltip != null) {
            button.setToolTipText(tooltip);
        }

        button.setPreferredSize(new Dimension(24, 24));

        if (buttonIcon != null) {
            Image scaledImage = buttonIcon.getImage().getScaledInstance(16, 16, Image.SCALE_FAST);
            button.setIcon(new ImageIcon(scaledImage));
        }

        button.setFocusPainted(true);

        return button;
    }
}
