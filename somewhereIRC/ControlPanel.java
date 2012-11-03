package somewhereIRC;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
** Control panel GUI class of the SomewhereIRC client.
**
** @author Philip Ng
** @version 1.0
*/
public class ControlPanel extends JFrame {

	//Constants
	private final String os = System.getProperty("os.name");
	
	//Class variables
	private JButton connectButton;
	private JTextField hostField, portField;
	
	/**
	** Constructor that just sets the title.
	** @param title The panel title
	** @param cb The connect button
	*/
	public ControlPanel(String title, JButton cb) {
		super(title);
		connectButton = cb;
	}
	
	/**
	** Returns the contents of the host field.
	** @return contents of the host field
	*/
	public String getHost() {
		return hostField.getText();
	}
	
	/**
	** Returns the contents of the port field.
	** @return contents of the port field
	*/
	public Integer getPort() {
		return Integer.valueOf(portField.getText());
	}

	/**
	** Initializes the main window GUI.
	** @param os String holding the OS name, used for OS-specific customizations
	*/
	private void createMainWindow(String os) {
		//Set window properties
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Add main menu
		createMainMenu(os);
		
		//Set up window layout
		GridBagLayout layout = new GridBagLayout();
		getContentPane().setLayout(layout);
		
		//Set up layout constraints
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		
		//Create label and text field for the host
		JLabel hostLabel = new JLabel("Host:");
		constraints.gridx = 0; constraints.gridy = 1;
		add(hostLabel,constraints);
		hostField = new JTextField(15);
		constraints.gridx = 1; constraints.gridy = 1;
		add(hostField,constraints);
		
		//Create label and text field for the port
		JLabel portLabel = new JLabel("Port:");
		constraints.gridx = 0; constraints.gridy = 2;
		add(portLabel,constraints);
		portField = new JTextField("6667");
		constraints.gridx = 1; constraints.gridy = 2;
		add(portField,constraints);
		
		//Create connection button
		constraints.gridx = 0; constraints.gridy = 3;
		constraints.gridwidth = 2; constraints.ipady = 5;
		add(connectButton,constraints);
		constraints.gridwidth = 1; constraints.ipady = 0;
		
		//Display the window
		pack();
		setVisible(true);
	}
	
	/**
	** Initializes the main menu GUI.
	** @param os String holding the OS name, used for OS-specific customizations
	*/
	private void createMainMenu(String os) {
		//Main menu initialization
		JMenuBar mainMenubar = new JMenuBar();
		mainMenubar.setOpaque(true);
		
		//File Menu
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		mainMenubar.add(fileMenu);
		
		//File Menu Option- Quit
		JMenuItem quit = new JMenuItem("Quit");
		quit.setMnemonic(KeyEvent.VK_Q);
		quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,ActionEvent.CTRL_MASK));
		quit.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			}
		);
		fileMenu.add(quit);
		
		//Window Menu
		JMenu windowMenu = new JMenu("Window");
		windowMenu.setMnemonic(KeyEvent.VK_W);
		mainMenubar.add(windowMenu);
		
		//Window Menu Option- Stay on Top
		JMenuItem stayOnTop = new JMenuItem("Toggle Stay on Top");
		stayOnTop.setMnemonic(KeyEvent.VK_T);
		stayOnTop.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,ActionEvent.CTRL_MASK));
		stayOnTop.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (isAlwaysOnTop()) setAlwaysOnTop(false);
					else setAlwaysOnTop(true);
				}
			}
		);
		windowMenu.add(stayOnTop);
		
		//Add main menu to window
		setJMenuBar(mainMenubar);
	}
	
	/**
	** Creates the initialized window and components.
	*/
	public void run() {
		javax.swing.SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {
					createMainWindow(os);
				}
			}
		);
	}

}