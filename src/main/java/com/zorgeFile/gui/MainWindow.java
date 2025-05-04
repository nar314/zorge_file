package com.zorgeFile.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import com.zorgeFile.core.FileCoder;
import com.zorgeFile.notify.NotifierGUI;

public class MainWindow {

	private static final String ver = "1.0";
	private JFrame frame = new JFrame("Zorge file. " + ver);

	private enum Mode { ENCRYPT, DECRYPT };
	
	private Mode curMode = null;
	private String[] modes = { "Encrypt", "Decrypt" };	
	private JComboBox<String> cbMode = new JComboBox<String>(modes);
	
	private JTextField txtSourceFile = new JTextField(20);
	private JPasswordField txtPsw = new JPasswordField(15);
	private JTextField txtTargetFile = new JTextField(20);
	private JButton btnTarget = new JButton("...");
	private JButton btnSource = new JButton("...");	
	private JCheckBox cbOverwrite = new JCheckBox("Overwrite target file if exist."); 
	private JButton btnDo = new JButton("Encrypt");
	private JLabel lbProgBarOperation = new JLabel("Progress");
	private JProgressBar progBar = new JProgressBar();
	
	private NotifierGUI notifier = null;
	
	private Thread thread = null;
	private Boolean threadIsRunning = false;
	
	public MainWindow() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    @Override public void run() {
		    	if(threadIsRunning) {
		    		MessageBox_Error("Operation is in progress"); // Will it help ?
		    	}
		    }
		});			
	}
	
	private void MessageBox_OK(final String message) {
		JOptionPane.showMessageDialog(null, message, "Information", JOptionPane.INFORMATION_MESSAGE);
	}
	
	private void MessageBox_Error(final String message) {	
		JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	private String selectFile() {
		
		JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(System.getProperty("user.home"))); 

        int rc = fc.showOpenDialog(frame);
        if (rc != JFileChooser.APPROVE_OPTION)
        	return null;
        
        File f = fc.getSelectedFile();
        return f.getAbsolutePath();
	}
	
	private void setHandlers() {
		
		cbMode.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent arg0) {
				comboBoxItemChanged();
			}
		});
		
		btnDo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				if(threadIsRunning) {
					MessageBox_Error("Operation is in progress.");
					return;
				}
				
				thread = new Thread() {
					public void run() {
						threadFunc();
					}
				};
				thread.start();
			}
		});
		
		btnSource.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				final String path = selectFile();
				if(path != null) {
					txtSourceFile.setText(path);
					txtTargetFile.setText(path + ".zf");
				}
			}
		});
		
		btnTarget.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				final String path = selectFile();
				if(path != null)
					txtTargetFile.setText(path);
			}
		});
	}
	
	public void show() {
	
		notifier = new NotifierGUI(progBar, lbProgBarOperation);
		
		progBar.setValue(0);
		progBar.setStringPainted(true);

		txtSourceFile.setText("/tmp/file.txt");
		txtTargetFile.setText("/tmp/file.txt.zf");
				
		setHandlers();
		comboBoxItemChanged();		
		createGUI();
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
		frame.pack();
		frame.setSize(500, 380);
		frame.setMinimumSize(new Dimension(500, 380));
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
	}
	
	private void comboBoxItemChanged() {
		
		int pos = cbMode.getSelectedIndex(); // 0 - encrypt, 1 - decrypt		
		if(pos == 0) {
			btnDo.setText("Encrypt");
			curMode = Mode.ENCRYPT;
		}
		else {
			btnDo.setText("Decrypt");
			curMode = Mode.DECRYPT;
		}
	}
	
	private JPanel createPanelMode() {

		JPanel panel = new JPanel();
		GridLayout layout = new GridLayout(1, 2);
		panel.setLayout(layout);
		
		panel.add(new JLabel("I want to"));
		panel.add(cbMode);
		
		return panel;
	}

	private JPanel createPanelFile(final String label, final JTextField txtField, final JButton btn) {

		JPanel panel = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		panel.setLayout(layout);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;		
		gbc.gridy = 0;
		panel.add(new JLabel(label), gbc);
		
		gbc.gridx = 0;		
		gbc.gridy = 1;		
		gbc.weightx = 0.95;
		gbc.insets = new Insets(0, 0, 0, 5);
		panel.add(txtField, gbc);
		
		gbc.weightx = 0.05;
		gbc.gridx = 1;
		gbc.gridy = 1;
		panel.add(btn, gbc);

		return panel;
	}

	private JPanel createPanelPsw() {

		JPanel panel = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		panel.setLayout(layout);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;		
		gbc.gridy = 0;
		panel.add(new JLabel("Password"), gbc);
		
		gbc.gridx = 0;		
		gbc.gridy = 1;		
		gbc.weightx = 0.95;
		gbc.insets = new Insets(0, 0, 0, 5);
		panel.add(txtPsw, gbc);
		
		return panel;
	}
	
	private void createMenu() {
		
		JMenuBar menuBar = new JMenuBar();
		JMenu menuFile = new JMenu("File");
		JMenuItem menuItemExit = new JMenuItem("Exit");
		JMenuItem menuItemAbout = new JMenuItem("About");
		
		menuItemExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
		    	if(threadIsRunning) {
		    		MessageBox_Error("Operation is in progress. Wait...");
		    		return;
		    	}
				
				frame.setVisible(false);
				frame.dispose();				
			}
		});
		
		menuItemAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				final String about = "Zorge file.\n\n"
						+ "Utility to encrypt and decrypt files. Version " + ver + "\n\n"
						+ "Free and open source.\n\n"
						+ "Build in memory of Richard Sorge.\n"
						+ "https://en.wikipedia.org/wiki/Richard_Sorge\n";
				MessageBox_OK(about);
			}
		});
		
		menuFile.add(menuItemAbout);
		menuFile.addSeparator();
		menuFile.add(menuItemExit);
		menuBar.add(menuFile);
		frame.setJMenuBar(menuBar);
	}
	
	private void createGUI() {
		
		createMenu();
		
		JPanel panel = new JPanel();
		SpringLayout layout = new SpringLayout();
		panel.setLayout(layout);

		JPanel panelMode = createPanelMode();
		JPanel panelSource = createPanelFile("Source file", txtSourceFile, btnSource);
		JPanel panelPsw = createPanelPsw();
		JPanel panelTarget = createPanelFile("Target file", txtTargetFile, btnTarget);
		
		panel.add(panelMode);
		panel.add(panelSource);
		panel.add(panelPsw);
		panel.add(panelTarget);
		panel.add(cbOverwrite);
		panel.add(btnDo);
		panel.add(lbProgBarOperation);
		panel.add(progBar);
		
		layout.putConstraint(SpringLayout.NORTH, panelMode, 5, SpringLayout.NORTH, panel);
		layout.putConstraint(SpringLayout.WEST, panelMode, 5, SpringLayout.WEST, panel);
		
		layout.putConstraint(SpringLayout.NORTH, panelSource, 15, SpringLayout.SOUTH, panelMode);
		layout.putConstraint(SpringLayout.WEST, panelSource, 5, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.EAST, panelSource, 0, SpringLayout.EAST, panel);
		
		layout.putConstraint(SpringLayout.NORTH, panelPsw, 15, SpringLayout.SOUTH, panelSource);
		layout.putConstraint(SpringLayout.WEST, panelPsw, 5, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.EAST, panelPsw, -5, SpringLayout.EAST, panel);

		layout.putConstraint(SpringLayout.NORTH, panelTarget, 15, SpringLayout.SOUTH, panelPsw);
		layout.putConstraint(SpringLayout.WEST, panelTarget, 5, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.EAST, panelTarget, -5, SpringLayout.EAST, panel);

		layout.putConstraint(SpringLayout.NORTH, cbOverwrite, 5, SpringLayout.SOUTH, panelTarget);
		layout.putConstraint(SpringLayout.WEST, cbOverwrite, 5, SpringLayout.WEST, panel);
		
		layout.putConstraint(SpringLayout.NORTH, btnDo, 5, SpringLayout.SOUTH, cbOverwrite);
		layout.putConstraint(SpringLayout.WEST, btnDo, 5, SpringLayout.WEST, panel);
		
		layout.putConstraint(SpringLayout.WEST, lbProgBarOperation, 5, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.EAST, lbProgBarOperation, -5, SpringLayout.EAST, panel);
		layout.putConstraint(SpringLayout.SOUTH, lbProgBarOperation, -5, SpringLayout.NORTH, progBar);
		
		layout.putConstraint(SpringLayout.WEST, progBar, 5, SpringLayout.WEST, panel);
		layout.putConstraint(SpringLayout.EAST, progBar, -5, SpringLayout.EAST, panel);
		layout.putConstraint(SpringLayout.SOUTH, progBar, -5, SpringLayout.SOUTH, panel);
		
		frame.getContentPane().add(panel);
	}
	
	private void threadFunc() {
		
		try {
			threadIsRunning = true;
			
    		FileCoder fc = new FileCoder();
    		fc.setNotifier(notifier);    		
    		fc.setOverwriteMode(cbOverwrite.isSelected());
    		
    		final String source = txtSourceFile.getText();
    		final String target = txtTargetFile.getText();
    		final String psw = new String(txtPsw.getPassword());
    		if(curMode == Mode.ENCRYPT)
    			fc.encrypt(source, psw, target);
    		else
    			fc.decrypt(source, psw, target);
    		
    		MessageBox_OK("Done.");
		}
		catch(Exception e) {
			MessageBox_Error(e.getMessage());			
		}
		finally {
			threadIsRunning = false;
		}
	}
}

// Detect invalid password.
