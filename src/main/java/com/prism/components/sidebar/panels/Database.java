package com.prism.components.sidebar.panels;

import com.prism.Prism;
import com.prism.components.definition.ConfigKey;
import com.prism.components.definition.DatabaseProvider;
import com.prism.components.extended.JDefaultKineticScrollPane;
import com.prism.components.frames.DatabasesFrame;
import com.prism.components.frames.WarningDialog;
import com.prism.components.textarea.TextArea;
import com.prism.managers.FileManager;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Vector;

public class Database extends JPanel {
    public static final Prism prism = Prism.getInstance();

    private final TextArea sqlEditor = new TextArea(true);
    private final JTextArea output = new JTextArea(6, 30);
    private final JTable resultTable = new JTable();

    private Connection connection;

    public Database() {
        super(new BorderLayout());

        JSplitPane leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                createSqlPanel(),
                createOutputPanel());
        leftSplit.setResizeWeight(0.7);
        leftSplit.setDividerSize(5);

        JDefaultKineticScrollPane tableScroll = new JDefaultKineticScrollPane(resultTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder(prism.getLanguage().get(27)));

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                leftSplit, tableScroll);
        mainSplit.setResizeWeight(0.4);
        mainSplit.setDividerSize(7);

        add(mainSplit, BorderLayout.CENTER);
    }

    private static TableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();
        Vector<String> colNames = new Vector<>();
        for (int i = 1; i <= cols; i++) colNames.add(meta.getColumnLabel(i));

        Vector<Vector<Object>> data = new Vector<>();
        while (rs.next()) {
            Vector<Object> row = new Vector<>();
            for (int i = 1; i <= cols; i++) row.add(rs.getObject(i));
            data.add(row);
        }
        return new DefaultTableModel(data, colNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JPanel createSqlPanel() {
        sqlEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        sqlEditor.addSyntaxHighlighting();
        sqlEditor.setText(prism.getConfig().getString(ConfigKey.DATABASE_SQL_EDITOR_TEXT, "SELECT * FROM Table_name;"));

        JDefaultKineticScrollPane sp = new JDefaultKineticScrollPane(sqlEditor);

        sp.setBorder(BorderFactory.createTitledBorder(prism.getLanguage().get(28)));
        JPanel p = new JPanel(new BorderLayout());

        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private JPanel createOutputPanel() {
        output.setEditable(false);

        JDefaultKineticScrollPane sp = new JDefaultKineticScrollPane(output);
        sp.setBorder(BorderFactory.createTitledBorder(prism.getLanguage().get(29)));
        JPanel p = new JPanel(new BorderLayout());
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    public void onConnect() {
        DatabaseProvider provider = prism.getDatabaseToolbar().getSelectedProvider();

        switch (provider) {
            case SQLite -> {
                try {
                    Class.forName("org.sqlite.JDBC");
                } catch (ClassNotFoundException e) {
                    JOptionPane.showMessageDialog(prism, prism.getLanguage().get(114), prism.getLanguage().get(10004), JOptionPane.ERROR_MESSAGE);

                    return;
                }

                JFileChooser fc = new JFileChooser(FileManager.getRootDirectory());

                if (fc.showOpenDialog(prism) == JFileChooser.APPROVE_OPTION) {
                    try {
                        if (connection != null) {
                            connection.close();
                        }

                        connection = DriverManager.getConnection("jdbc:sqlite:" + fc.getSelectedFile().getAbsolutePath());

                        appendOutput("Connected to SQLite!");

                        prism.getDatabaseToolbar().updateComponent();
                    } catch (SQLException ex) {
                        appendOutput("ERROR: " + ex.getMessage());

                        new WarningDialog(prism, ex);
                    }
                }
            }

            case MySQL -> {
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                } catch (ClassNotFoundException e) {
                    JOptionPane.showMessageDialog(prism,
                            prism.getLanguage().get(114),
                            prism.getLanguage().get(10004), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                new DatabasesFrame(DatabaseProvider.MySQL);
            }
        }
    }

    public void onRun() {
        if (connection == null) {
            appendOutput("ERROR: No connection was established.");
            return;
        }

        String sql = sqlEditor.getSelectedText();
        if (sql == null || sql.isBlank()) sql = sqlEditor.getText();
        if (sql.isBlank()) return;

        try (Statement st = connection.createStatement()) {
            boolean hasResult = st.execute(sql);

            if (hasResult) {
                ResultSet rs = st.getResultSet();

                resultTable.setModel(buildTableModel(rs));
                appendOutput("Query returned " + resultTable.getRowCount() + " rows.");
            } else {
                int upd = st.getUpdateCount();

                appendOutput("Update count: " + upd);
                resultTable.setModel(new DefaultTableModel());
            }

            prism.getDatabaseToolbar().updateComponent();
        } catch (SQLException ex) {
            appendOutput("ERROR: " + ex.getMessage());
        }
    }

    public boolean isConnected() {
        return connection != null;
    }

    public void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
                connection = null;

                appendOutput("Connection closed.");

                prism.getDatabaseToolbar().updateComponent();
            }
        } catch (SQLException ex) {
            appendOutput("ERROR: " + ex.getMessage());
        }
    }

    public boolean connectWithParameters(String host, String port, String user, String pass) {
        String url = String.format("jdbc:mysql://%s:%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                host, port);

        try {
            if (connection != null) {
                connection.close();
            }

            connection = DriverManager.getConnection(url, user, pass);

            appendOutput("Connected to MySQL!");

            prism.getDatabaseToolbar().updateComponent();

            return true;
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(prism,
                    prism.getLanguage().get(115),
                    prism.getLanguage().get(10005), JOptionPane.WARNING_MESSAGE);

            appendOutput("ERROR: " + ex.getMessage());

            new WarningDialog(prism, ex);
        }

        return false;
    }

    public boolean verifyConnectionWithParameters(String host, String port, String user, String pass) {
        String url = String.format("jdbc:mysql://%s:%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                host, port);

        try {
            DriverManager.getConnection(url, user, pass);

            return true;
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(prism,
                    prism.getLanguage().get(115),
                    prism.getLanguage().get(10005), JOptionPane.WARNING_MESSAGE);
        }

        return false;
    }

    public TextArea getSqlEditor() {
        return sqlEditor;
    }

    private void appendOutput(String txt) {
        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = currentTime.format(formatter);

        SwingUtilities.invokeLater(() -> {
            output.append("[" + formattedTime + "] " + txt + "\n");
            output.setCaretPosition(output.getDocument().getLength());
        });
    }
}