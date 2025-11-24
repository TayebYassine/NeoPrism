package com.prism.components.definition;

import com.prism.components.extended.JKineticScrollPane;
import com.prism.components.textarea.ImageViewer;
import com.prism.components.textarea.TextArea;
import com.prism.managers.TextAreaManager;
import com.prism.utils.Languages;
import com.prism.utils.ResourceUtil;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PrismFile {
    private File file;

    private TextArea textArea;
    private ImageViewer imageViewer;
    private JPanel customJPanel;

    private JKineticScrollPane scrollPane;
    private boolean savedChanges = true;
    private boolean isHomepage = false;

    private List<TextAreaManager.Problem> problems = new ArrayList<>();

    public PrismFile(File file) {
        this.file = file;
    }

    public PrismFile(File file, TextArea textArea) {
        this.file = file;
        this.textArea = textArea;
    }

    public PrismFile(File file, ImageViewer imageViewer) {
        this.file = file;
        this.imageViewer = imageViewer;
    }

    public PrismFile(File file, JPanel customJPanel) {
        this.file = file;
        this.customJPanel = customJPanel;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public TextArea getTextArea() {
        return textArea;
    }

    public void setTextArea(TextArea textArea) {
        this.textArea = textArea;
    }

    public ImageViewer getImageViewer() {
        return imageViewer;
    }

    public JPanel getCustomJPanel() {
        return customJPanel;
    }

    public boolean isSaved() {
        return this.savedChanges;
    }

    public void setSaved(boolean savedChanges) {
        this.savedChanges = savedChanges;
    }

    public void setHomepage(boolean isHomepage) {
        this.isHomepage = isHomepage;
    }

    public JKineticScrollPane getScrollPane() {
        return scrollPane;
    }

    public void setScrollPane(JKineticScrollPane scrollPane) {
        this.scrollPane = scrollPane;
    }

    public String getName() {
        return this.file == null ? (isHomepage ? "Homepage" : "Untitled") : this.file.getName();
    }

    public String getAbsolutePath() {
        return this.file == null ? null : this.file.getAbsolutePath();
    }

    public boolean isText() {
        return this.textArea != null;
    }

    public boolean isImage() {
        return this.imageViewer != null;
    }

    public boolean isCustom() {
        return this.customJPanel != null;
    }

    public boolean isHomepage() {
        return isHomepage;
    }

    public ImageIcon getIcon() {
        return isHomepage ? ResourceUtil.getIconFromSVG("icons/ui/home.svg", 16, 16) : Languages.getIcon(file);
    }

    public List<TextAreaManager.Problem> getProblems() {
        return problems;
    }

    public void setProblems(List<TextAreaManager.Problem> problems) {
        this.problems = problems;
    }

    public void addProblem(TextAreaManager.Problem problem) {
        this.problems.add(problem);
    }
}
