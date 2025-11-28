package com.prism.templates;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class AppJavaSwingGUI extends AppBase {
	private String SAMPLE = """
			import javax.swing.JFrame;
			import javax.swing.JLabel;
			import javax.swing.JButton;
			import javax.swing.JPanel;
			import java.awt.FlowLayout;
			import java.awt.event.ActionEvent;
			import java.awt.event.ActionListener;
			
			public class MySimpleGUI extends JFrame {
			
			    // Constructor to set up the GUI
			    public MySimpleGUI() {
			        // Set the title of the frame
			        setTitle("My Simple GUI");
			
			        // Set the default close operation (exit on close)
			        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			        // Set the size of the frame
			        setSize(400, 200);
			
			        // Center the frame on the screen
			        setLocationRelativeTo(null);
			
			        // Create a JPanel to hold components
			        JPanel panel = new JPanel();
			        panel.setLayout(new FlowLayout()); // Use FlowLayout for simple arrangement
			
			        // Create a JLabel
			        JLabel label = new JLabel("Welcome to my GUI!");
			        panel.add(label);
			
			        // Create a JButton
			        JButton button = new JButton("Click Me!");
			        // Add an ActionListener to the button
			        button.addActionListener(new ActionListener() {
			            @Override
			            public void actionPerformed(ActionEvent e) {
			                label.setText("Button Clicked!"); // Change label text on click
			            }
			        });
			        panel.add(button);
			
			        // Add the panel to the frame's content pane
			        add(panel);
			
			        // Make the frame visible
			        setVisible(true);
			    }
			
			    public static void main(String[] args) {
			        // Run the GUI creation on the Event-Dispatching Thread (EDT) for thread safety
			        javax.swing.SwingUtilities.invokeLater(new Runnable() {
			            public void run() {
			                new MySimpleGUI(); // Create an instance of the GUI
			            }
			        });
			    }
			}
			""";

	public AppJavaSwingGUI() {
		super("Java: Swing GUI", "icons/languages/java.svg");
	}

	@Override
	public void create(File directory) {
		File mainFile = new File(directory, "MySimpleGUI.java");

		if (!mainFile.exists()) {
			try {
				mainFile.createNewFile();
			} catch (IOException e) {
				return;
			}
		}

		try (FileWriter fw = new FileWriter(mainFile)) {
			fw.write(SAMPLE);
		} catch (IOException _) {

		}
	}
}
