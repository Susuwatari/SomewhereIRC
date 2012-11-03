package somewhereIRC;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;

/**
** Top class of the SomewhereIRC client.
** Initializes and starts up everything.
** Will do the thread handling for each connection.
**
** @author Philip Ng
** @version 1.0
*/
public class Client {

	private static ArrayList<Thread> connections = new ArrayList<Thread>();
	private static ControlPanel controlPanel;
		
	/**
	** Adds a new connection to the list, given the hostname.
	** @param host The hostname of the server to connect to.
	*/
	private static void addByHost(String host) {
		SomewhereIRC connection = new SomewhereIRC(host);
		Thread t = new Thread(connection);
		connections.add(t);
		t.run();
	}
	
	/**
	** Adds a new connection to the list, given the hostname and port.
	** @param host The hostname of the server to connect to.
	** @param port The port number to connect to on the server.
	*/
	private static void addByHostAndPort(String host, Integer port) {
		SomewhereIRC connection = new SomewhereIRC(host,port);
		Thread t = new Thread(connection);
		connections.add(t);
		t.start();
	}
	
	/**
	** Defines the behaviors when the connect button is pressed.
	*/
	private static void connectWasActivated() {
		//Try to connect to the server
		addByHostAndPort(controlPanel.getHost(),controlPanel.getPort());
	}
	
	/**
	** Starts main application loop.
	*/
	public static void main(String args[]) {
		//OS specific adjustments - Main menu
		final String os = System.getProperty("os.name");
		if (os.startsWith("Mac")) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name","SomewhereIRC");
		}
		
		//Create the connect button to be linked to the control panel
		JButton connectButton = new JButton("Connect");
		connectButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					connectWasActivated();
				}
			}
		);
		//Keybinding
		connectButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"),"connect");
		connectButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("alt ENTER"),"connect");
		connectButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("shift ENTER"),"connect");
		connectButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl ENTER"),"connect");
		connectButton.getActionMap().put("connect", new AbstractAction() { public void actionPerformed(ActionEvent e) { connectWasActivated(); } });
			
		//Create and start control panel GUI
		controlPanel = new ControlPanel("SomewhereIRC Control Panel",connectButton);
		controlPanel.run();
	}
	
}