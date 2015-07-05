//Java IRC Client

//Depends on jerklib.jar, please add this to classpath
//Set to freenode IRC, can be changed to any server

import java.awt.Color; //Color for messages
import java.awt.event.ActionEvent; //Events for buttons and text fields                                                                                                                                                   
import java.awt.event.ActionListener; //Enables event listening in GUI

//Use the swing library to handle GUI
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane; //Console or text output
import javax.swing.text.Style;
import javax.swing.text.StyleConstants; //To manage text output in message pane
import javax.swing.text.StyledDocument;

import jerklib.ConnectionManager;  //Takes care of IRC connection to server and initiate session
import jerklib.ProfileImpl; //Passed to ConnectionManager, profiles represents client in connection
import jerklib.Session; //IRC connection to channel, handles all IRC related events
import jerklib.events.ChannelMsgEvent; //Event to handle incoming information and messages
import jerklib.events.IRCEvent; //Event interface for IRC events
import jerklib.events.IRCEvent.Type;
import jerklib.events.listeners.IRCEventListener; //Implemented to handle incoming IRC events

/**
 * This class represents the graphical user interface aspect of the
 * program. It communicates with the IRCListener. The GUI consists of
 * a simple program that lets you choose a nickname, channel and 
 * send messages. It has a console-like display using a text pane that
 * not only has events but also has all events and messages by the console.
 * Also, it uses different colors to simplify incoming and outgoing messages.
 */ 
class ChatPanel extends JPanel implements ActionListener {

   //Different colors below using Swing styles
	private Style red;
	private Style black;
	private Style orange;
	private Style blue;
	private Style green;
	
   //Channel information for console output stored locally for efficiently
   private String channel;
   private String username;
   private String server;
   
   //ChatPanel has references to the IRC objects to mainly help with the sending
   private IRCListener listener;
   private ConnectionManager manager;
   private Session session;

   //GUI elements with ActionListeners/user info need to be referred to in class variables 
	private JTextField usernameField;
	private JTextField messageField;
	private JTextField channelField;

	private JButton connectButton;
	private JButton disconnectButton;
	private JButton sendButton;

	private JTextPane messageConsole;
   
	public ChatPanel(String server) {
   
		setLayout(null);
      
      this.server = server;
		
      //Username Identifier
		JLabel usernameLabel = new JLabel("Username:");
		usernameLabel.setBounds(6, 12, 66, 16);
		add(usernameLabel);
		
      //Username input field
		usernameField = new JTextField();
		usernameField.setBounds(72, 6, 152, 28);
		add(usernameField);
		usernameField.setColumns(10);
		
      //Connection Button with Action Listener
		connectButton = new JButton("Connect to Server");
		connectButton.setBounds(71, 47, 147, 29);
      connectButton.addActionListener(this);
		add(connectButton);
		
      //Disconnection button with Action Listener
		disconnectButton = new JButton("Disconnect and Close");
		disconnectButton.setEnabled(false);
		disconnectButton.setBounds(211, 47, 173, 29);
      disconnectButton.addActionListener(this);
		add(disconnectButton);
      		
      //Channel Identifier
		JLabel channelLabel = new JLabel("Channel:");
		channelLabel.setBounds(232, 12, 61, 16);
		add(channelLabel);
		
      //Message Console is a JTextPane, editable by program
		messageConsole = new JTextPane();
		messageConsole.setText(" > Awaiting server connection...");
		messageConsole.setToolTipText("Chat will be populated here.");
		messageConsole.setBounds(17, 123, 416, 168);
      
      //Adding color styles to message console
      orange = messageConsole.addStyle("Orange Style", null);
      StyleConstants.setForeground(orange, Color.ORANGE);
      
      red = messageConsole.addStyle("Red Style", null);
      StyleConstants.setForeground(red, Color.RED); 
      
      blue = messageConsole.addStyle("Blue Style", null);
      StyleConstants.setForeground(blue, Color.BLUE);
      
      green = messageConsole.addStyle("Green Style", null);
      StyleConstants.setForeground(green, Color.GREEN);
      
      black = messageConsole.addStyle("Black Style", null);
      StyleConstants.setForeground(black, Color.BLACK); 
            
      //Only output by program, not user-editable
      messageConsole.setEditable(false);
      
		add(messageConsole);
		
      //Message identifier
		JLabel messageLabel = new JLabel("Message:");
		messageLabel.setBounds(26, 87, 71, 16);
		add(messageLabel);
		
      //Message field with action listener (enter/carriage-return)
		messageField = new JTextField();
		messageField.setColumns(10);
		messageField.setBounds(85, 82, 244, 28);
      messageField.addActionListener(this);
      messageField.setEnabled(false);
		add(messageField);
     
		//Send button with Action Listener
		sendButton = new JButton("Send");
		sendButton.setBounds(331, 82, 102, 29);
      sendButton.setEnabled(false);
      sendButton.addActionListener(this);
		add(sendButton);
		
      
      //Channel field
		channelField = new JTextField();
		channelField.setColumns(10);
		channelField.setBounds(292, 6, 152, 28);
		add(channelField);
	}

   
   /**
    * This method is crucial. GUI events that involve Action Listeners as 
    * mentioned above are passed to this method, and the events are handled
    * based on their sources. User-controlled actions are taken care of in
    * the conditionals below.
    */
   public void actionPerformed(ActionEvent e) {
   
      //Connect to server
      if (e.getSource() == connectButton) {
         addConsoleOutput(" > Connection initiated.", "black");
         connectButton.setEnabled(false);
         disconnectButton.setEnabled(true);
         username = usernameField.getText();
         
         //Various profiles ensure user gets at least variation of nickname he/she wants
         manager = new ConnectionManager(new ProfileImpl(username, (username + "_2"), (username + "_3"), (username + "_4")));
         
         //ConnectionManager will try to make connection to server
         session = manager.requestConnection(server);
         username = session.getNick();
         channel = channelField.getText();
         listener = new IRCListener("#" + channel); 
         session.addIRCEventListener(listener);
         listener.setGui(this);
      }
      
      //Simple disconnect from server then close program
      if (e.getSource() == disconnectButton) {
         session.close("User has disconnected.");
         System.exit(0);
      }
      
      //Message send operation
      if (e.getSource() == sendButton) {
         session.channelSay(("#" + channel), messageField.getText());
         String text = " <" + username + ">: " + messageField.getText();
         addConsoleOutput(text, "green");
         messageField.setText("");
      } 
      
      //Same message send operation but works if user presses "Enter/Return" in message field
      if (e.getSource() == messageField) {
         session.channelSay(("#" + channel), messageField.getText());
         String text = " <" + username + ">: " + messageField.getText();
         addConsoleOutput(text, "green");
         messageField.setText("");
      } 
   }
   
   //Utility method makes it easy to add content to console output
   public void addConsoleOutput(String text, String color) {
      StyledDocument console = messageConsole.getStyledDocument();
      
      try { 
         if (color.equalsIgnoreCase("Orange"))
            console.insertString(0, text + "\n", orange);
         if (color.equalsIgnoreCase("Red"))
            console.insertString(0, text + "\n", red);
         if (color.equalsIgnoreCase("Blue"))
            console.insertString(0, text + "\n", blue);
         if (color.equalsIgnoreCase("Green"))
            console.insertString(0, text + "\n", green);
         if (color.equalsIgnoreCase("Black"))
            console.insertString(0, text + "\n", black);      
      } catch(Exception e) { 
         System.out.println(e);
      }
   }
   
   //Method is called when ConnectionManager Session successfully connects to channel
   public void enableChat() {
      connectButton.setEnabled(false);
      messageField.setEnabled(true);
      sendButton.setEnabled(true);
      disconnectButton.setEnabled(true);
      usernameField.setEditable(false);
      channelField.setEditable(false);
   }
}

/**
 * This class is an event listener so it is always working as IRC events
 * come in. It implements the jerklib IRCEventListener so it is always
 * listening for IRC events. It also stores references to the GUI so it 
 * can update the console output. Instantiated once ConnectionManager
 * successfully connects to channel.
 */
class IRCListener implements IRCEventListener
{	

   ChatPanel gui;
   String channelName;
   
   public void setGui(ChatPanel gui)
   {
      this.gui = gui;
   }
   
   public IRCListener(String name) {
      channelName = name;
   }

	public void recieveEvent(IRCEvent event) {
      //Connection successful event
		if (event.getType() == Type.CONNECT_COMPLETE)
		{
			event.getSession().joinChannel(channelName);
         gui.addConsoleOutput(" > Connection successful. Waiting for authorization...", "black");
		}
      //Any channel, generally user, message
		else if (event.getType() == Type.CHANNEL_MESSAGE)
		{
			ChannelMsgEvent channelEvent = (ChannelMsgEvent) event;
         String text = "<" + channelEvent.getNick() + ">"+ ": " + channelEvent.getMessage();
         gui.addConsoleOutput((" " + text), "blue");
		}
      //Successful joining of channel
		else if (event.getType() == Type.JOIN_COMPLETE)
		{
			//JoinCompleteEvent joinEvent = (JoinCompleteEvent) event;
         gui.addConsoleOutput(" > Welcome! Chat send/receive enabled.", "black");
         gui.enableChat(); 
		}
      //Print all other IRC events, generally MOTD and other initial connection messages
		else
		{  
			gui.addConsoleOutput(" > " + event.getType() + " " + event.getRawEventData(), "red");
		}
	}
}

/**
 * This class instantiates the application and sets up the GUI for success. The GUI events
 * end up instantiating the above IRCEvent class, so the main method really only has to
 * worry about the GUI. A JFrame and JPanel are used, part of the default Swing library.
 */
public class ChatPanel_Walia {
   
   //App is intended to allow user to communicate on Freenode, while user gets to choose channel
   private static final String SERVER_URL = "irc.freenode.net";
   
   public static void main(String[] args) {
   
      JFrame app = new JFrame("Walia's FreeNode IRC Client");
      //App is supposed to be run alongside other windows, small chat panel
      app.setSize(450,330);
      app.add(new ChatPanel(SERVER_URL)); 
      app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);        
      app.setVisible(true); 
   } 
}