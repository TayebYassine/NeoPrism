package com.prism.templates;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class AppCppGUI extends AppBase {
	private String SAMPLE_1 = """
			#ifndef MYAPP_H
			#define MYAPP_H
			
			#include <wx/wx.h> // Include all necessary wxWidgets headers
			
			// Define a new application type
			class MyApp : public wxApp {
			public:
			    virtual bool OnInit(); // Called when the application starts
			};
			
			// Define a new frame type
			class MyFrame : public wxFrame {
			public:
			    MyFrame(const wxString& title, const wxPoint& pos, const wxSize& size);
			
			private:
			    void OnHello(wxCommandEvent& event); // Event handler for the "Hello" button
			    void OnExit(wxCommandEvent& event);  // Event handler for the "Exit" menu item
			    void OnAbout(wxCommandEvent& event); // Event handler for the "About" menu item
			
			    DECLARE_EVENT_TABLE() // Declare event table for event handling
			};
			
			enum {
			    ID_Hello = 1 // Custom ID for the "Hello" button
			};
			
			#endif // MYAPP_H
			""";
	private String SAMPLE_2 = """
			#include "main.h"
			
			// Define the event table
			BEGIN_EVENT_TABLE(MyFrame, wxFrame)
			    EVT_MENU(wxID_EXIT,  MyFrame::OnExit)
			    EVT_MENU(wxID_ABOUT, MyFrame::OnAbout)
			    EVT_BUTTON(ID_Hello, MyFrame::OnHello)
			END_EVENT_TABLE()
			
			// Implement the application class
			IMPLEMENT_APP(MyApp)
			
			bool MyApp::OnInit() {
			    MyFrame *frame = new MyFrame("Hello wxWidgets", wxPoint(50, 50), wxSize(450, 340));
			    frame->Show(true);
			    return true;
			}
			
			// Implement the frame class
			MyFrame::MyFrame(const wxString& title, const wxPoint& pos, const wxSize& size)
			    : wxFrame(NULL, wxID_ANY, title, pos, size) {
			    wxMenu *menuFile = new wxMenu;
			    menuFile->Append(ID_Hello, "&Hello...\\tCtrl-H", "Help string shown in status bar for this menu item");
			    menuFile->AppendSeparator();
			    menuFile->Append(wxID_EXIT);
			
			    wxMenu *menuHelp = new wxMenu;
			    menuHelp->Append(wxID_ABOUT);
			
			    wxMenuBar *menuBar = new wxMenuBar;
			    menuBar->Append(menuFile, "&File");
			    menuBar->Append(menuHelp, "&Help");
			
			    SetMenuBar(menuBar);
			
			    CreateStatusBar();
			    SetStatusText("Welcome to wxWidgets!");
			
			    // Create a button
			    wxButton* helloButton = new wxButton(this, ID_Hello, "Say Hello", wxPoint(150, 150), wxSize(100, 30));
			}
			
			void MyFrame::OnExit(wxCommandEvent& event) {
			    Close(true); // Close the frame
			}
			
			void MyFrame::OnAbout(wxCommandEvent& event) {
			    wxMessageBox("This is a wxWidgets 'Hello World' example",
			                 "About Hello World", wxOK | wxICON_INFORMATION);
			}
			
			void MyFrame::OnHello(wxCommandEvent& event) {
			    wxLogMessage("Hello from wxWidgets!"); // Log a message
			}
			""";

	public AppCppGUI() {
		super("C++: wxWidgets GUI", "icons/languages/cpp.svg");
	}

	@Override
	public void create(File directory) {
		File mainFile = new File(directory, "main.cpp");
		File headerFile = new File(directory, "main.h");

		if (!mainFile.exists()) {
			try {
				mainFile.createNewFile();
			} catch (IOException e) {
				return;
			}
		}

		if (!headerFile.exists()) {
			try {
				headerFile.createNewFile();
			} catch (IOException e) {
				return;
			}
		}

		try (FileWriter fw = new FileWriter(mainFile)) {
			fw.write(SAMPLE_2);
		} catch (IOException _) {

		}

		try (FileWriter fw = new FileWriter(headerFile)) {
			fw.write(SAMPLE_1);
		} catch (IOException _) {

		}
	}
}
