package somewhereIRC;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PipedOutputStream;
import java.io.PipedInputStream;

import java.io.IOException;

/**
** Chat window GUI class of the SomewhereIRC client.
**
** @author Philip Ng
** @version 1.0
*/
public class ChatWindow extends JFrame implements Runnable {
	
	//Class variables
	private JTextField input;
	private JTextArea display;
	private JScrollPane displayScrollPane;
	private BufferedReader myInput;
	private PipedOutputStream pos;
	private PipedInputStream pis; //Not used right now, due to the temporary display solution
	
	/**
	** Constructor that sets the title and pipes.
	** @param title The panel title
	** @param pos The piped output stream to the delegate
	** @param pis The piped input stream to the delegate
	*/
	public ChatWindow(String title, PipedOutputStream pos, PipedInputStream pis, JTextArea dis) {
		super(title);
		this.pos = pos;
		this.pis = pis;
		myInput = new BufferedReader( new InputStreamReader(this.pis) );
		display = dis;
	}
	
	/**
	** Text Field Listener class, with the actions on input
	*/
	private class TextFieldListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String in = input.getText().concat("\n");
			display.append(in);
			try {
				pos.write(in.getBytes(),0,in.length());
				pos.flush();
			} catch (IOException ie) {
				display.append("ERROR: Message was not sent to server.");
			}
			input.setText("");
		}
	}

	/**
	** Initializes the chat window GUI.
	*/
	private void createChatWindow() {
		//Set window properties
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Set up components
		input = new JTextField();
		//display = new JTextArea();
		displayScrollPane = new JScrollPane(display);
		displayScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		displayScrollPane.setPreferredSize(new Dimension(400,300));
		
		//Set up window layout
		GridBagLayout layout = new GridBagLayout();
		getContentPane().setLayout(layout);
		
		//Set up layout constraints
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		
		//Set up the listener
		TextFieldListener inputListener = new TextFieldListener();
		input.addActionListener(inputListener);
		
		//Fix display options
		display.setEditable(false);
		display.setLineWrap(true);
		display.setWrapStyleWord(true);
		
		//Add all the field components to the window
		constraints.gridx = 0; constraints.gridy = 0;
		add(displayScrollPane,constraints);
		constraints.gridx = 0; constraints.gridy = 1;
		add(input,constraints);
		
		//Display the window
		pack();
		setVisible(true);
	}
	
	/**
	** Creates the initialized window and components.
	*/
	public void run() {
		javax.swing.SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {
					createChatWindow();
				}
			}
		);
	}

}