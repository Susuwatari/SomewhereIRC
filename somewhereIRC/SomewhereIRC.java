package somewhereIRC;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedOutputStream;
import java.io.PipedInputStream;
import java.net.Socket;

import java.util.ArrayList;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.lang.NumberFormatException;
import java.io.IOException;
import java.net.UnknownHostException;

/**
** Main handler class of the SomewhereIRC client.
** Is the top-level handler for an individual connection.
** Will be threaded and given a shared status buffer by the main GUI.
**
** @author Philip Ng
** @version 1.0
*/
public class SomewhereIRC implements Runnable {
	//Enumerations
	public enum Status {
		DISCONNECTED,
		CONNECTED,
		DISCONNECTING,
		CONNECTING
	}
	public enum MessageType {
		RAW,
		CHANNEL,
		SERV,
		USER
	}
	
	//Thread variables
	private boolean running = true;
	
	//Server variables
	private String host;
	private Integer port = 6667;
	
	//User variables (defaulted)
	private String nick = "TestClient";
	private String ident = "Someone";
	private String real = "Someone";
	
	//Socket/Connection variables
	private Socket sock;
	private BufferedReader in, myInput;
	private BufferedWriter out;
	private Status connectionStatus = Status.DISCONNECTED;
	
	//Parser variable
	private RawParser parser;
	
	//Window variables
	private ChatWindow statusWindow;
	private ArrayList<Thread> windows = new ArrayList<Thread>();
	
	//Display variables - I should move this to threading from ChatWindow instead
	private ArrayList<JTextArea> displays = new ArrayList<JTextArea>();
	
	//Pipe variables
	private PipedInputStream windowsIn = new PipedInputStream();
	private ArrayList<PipedOutputStream> windowsOut = new ArrayList<PipedOutputStream>();
	
	/**
	** Constructor given just the server hostname.
	** @param host The hostname of the server to connect to.
	*/
	public SomewhereIRC(String host) {
		this.host = host;
		myInput = new BufferedReader( new InputStreamReader(System.in) );
		parser = new RawParser();
		init();
	}
	
	/**
	** Constructor given just the server information.
	** @param host The hostname of the server to connect to.
	** @param port The port number to use when connecting.
	*/
	public SomewhereIRC(String host, Integer port) {
		this.host = host;
		this.port = port;
		myInput = new BufferedReader( new InputStreamReader(windowsIn) );
		parser = new RawParser();
		init();
	}
	
	/**
	** Initializes the status window GUI.
	*/
	private void createStatusWindow() {
		//Initialize the pipe that the window will use to write back
		//This will hold the read end (windowsIn)
		PipedOutputStream fromWindow = new PipedOutputStream();
		try {
			fromWindow.connect(windowsIn);
		} catch (IOException e) {
			displayError("ERROR: Could not connect read pipe to status window.");
		}
		
		//Initialize the pipe that this will use to write to the window
		//This will hold all the write ends (windowsOut)
		PipedInputStream toWindow = new PipedInputStream();
		PipedOutputStream windowOut = new PipedOutputStream();
		try {
			windowOut.connect(toWindow);
		} catch (IOException e) {
			displayError("ERROR: Could not connect write pipe to status window.");
		}
		windowsOut.add(windowOut);
		
		//Create the status window
		JTextArea display = new JTextArea();
		displays.add(display);
		statusWindow = new ChatWindow("Status Window for " + host, fromWindow, toWindow, display);
		
		Thread t = new Thread(statusWindow);
		windows.add(t);
		t.start();
	}
	
	/**
	** Initialization of an IRCd client session.
	** Connects the socket, opens the streams, and establishes the user's info.
	*/
	private void init() {
		//Start Status Window GUI
		createStatusWindow();
		
		//Connect to the server
		connectSocket();
		
		//Establish user session
		establishSession();
	}
	
	/** 
	** Tries to connect the socket.
	*/
	private void connectSocket() {
		//Try to connect to the socket
		connectionStatus = Status.CONNECTING;
		try {
			sock = new Socket(host,port);
			in = new BufferedReader( new InputStreamReader(sock.getInputStream()) );
			out = new BufferedWriter( new OutputStreamWriter(sock.getOutputStream()) );
		} catch (Exception e) {
			displayError("ERROR: Client could not connect to " + host + " on port " + port + ".");
			display("Retrying connection in 5 seconds...");
			try {
				Thread.sleep(5000);		
			} catch (InterruptedException ie) {}
			connectSocket();
		} 
		connectionStatus = Status.CONNECTED;
		display("Connected to " + host + " on port " + port + ".");
	}
	
	/**
	** Try to establish the session with the IRCd.
	** Sends nickname, ident, and realname information over the socket.
	*/
	private void establishSession() {
		//Send nickname
		sendMessage("NICK " + nick,MessageType.RAW);	
		display("NICK " + nick);
		
		//Send ident and realname
		sendMessage("USER " + ident + " 8 * :" + real,MessageType.RAW);
		display("USER " + ident + " 8 * :" + real);
	}
	
	/**
	** Translate and send a message to the IRCd, using the correct IRC protocol.
	** @param message The message to be sent, in plain format
	** @param msgType The type of message, according to enum messageType
	*/
	private void sendMessage(String message, MessageType msgType) {
		displayDebug("Sending message: " + message);
		switch(msgType) {
			case RAW:
				try {
					out.write(message + "\n");
					out.flush();
				} catch (IOException e) {
					displayError("ERROR: Could not send message [" + message + "]");
				}
				break;
			default:
				displayError("ERROR: Unknown message type for output.");
				break;
		}
	}
	
	/**
	** Display function, sending output to the status window.
	** @param str The string to display
	*/
	private void display(String str) {
		displays.get(0).append(str + "\n");
	}
	
	/**
	** Display function, sending output to the specified window.
	** @param str The string to display
	** @param windowID The ID of the window to display the message in
	*/
	private void display(String str, int windowID) {
		displays.get(windowID).append(str + "\n");
	}
	
	/**
	** Display function, showing error output to the user.
	** Separated for easy transitioning to a GUI later.
	** @param str The string to display
	*/
	private void displayError(String str) {
		System.out.println("\t" + str);
	}
	
	/**
	** Display function, showing debug output to the user.
	** Separated for easy enabling/disabling.
	** @param str The string to display
	*/
	private void displayDebug(String str) {
		System.out.println("\t" + str);
	}
	
	/**
	** Print function (FOR DEBUG).
	*/
	private void print() {
		displayDebug("DEBUG OUTPUT:");
		displayDebug("Host: " + host + " (" + port + ")");
		displayDebug("User: " + nick + "!" + ident + "@.....:" + real);
	}
	
	/**
	** Given an action ID string, perform a client action.
	** @param actionID A string indicating the action to perform.
	** @param actionString The string resulting in this action
	*/
	private void processAction(String actionID, String actionString) {
		//PONG action
		if (actionID.compareTo("PONG") == 0) {
			sendMessage( parser.getPongForPing(actionString), MessageType.RAW );
		}
		
		//QUIT action
		else if (actionID.compareTo("QUIT") == 0) running = false;
		
		//No action
		else if (actionID.compareTo("None") == 0) return;
		else return;
	}
	
	/**
	** Main runtime loop, processing server input unless user sends output with each iteration.
	*/
	private void runtimeLoop() {
		String line;
		try {
		
			//If there is no user output waiting, just keep reading server input
			while (myInput.ready() == false) {
			
				//If there is server input, output it to the user
				if (in.ready()) {
					line = in.readLine();
					
					//Parse the raw input and display it
					display( parser.getMessageForRawInput(line) );
					
					//Based on the input, return the action
					processAction( parser.getActionForRawInput(line), line );
				}
				
			}
			
			//Otherwise, grab the user input
			line = myInput.readLine();
			
			//If it has a corresponding action in the client itself, process it
			processAction( parser.getActionForUserInput(line), line );
			
			//Finally, send and display it
			sendMessage( parser.getRawOutputForUserInput(line), MessageType.RAW );
			//display( parser.getFormattedOutputForUserInput(line) );
			
		} catch (IOException e) {
			displayError("ERROR: Runtime I/O error.");
		}
	}

	/**
	** Run function for this thread.
	*/
	public void run() {
		//OS specific adjustments - Main menu
		final String os = System.getProperty("os.name");
		if (os.startsWith("Mac")) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
		}
	
		//Start the actual IRC connection and processing
		while (running) {
			try {
				Thread.sleep(1000);		
			} catch (InterruptedException e) {}
			switch(connectionStatus) {
				case DISCONNECTED:
					displayDebug("DISCONNECTED");
					init();
					break;
				case CONNECTED:
					displayDebug("CONNECTED");
					runtimeLoop();
					break;
				case DISCONNECTING:
					displayDebug("DISCONNECTING");
					break;
				case CONNECTING:
					displayDebug("CONNECTING");
					break;
				default:
					print();
					break;
			}
		}
	}
	
}