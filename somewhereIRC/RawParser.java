package somewhereIRC;

/**
** Parser class of the SomewhereIRC client.
** Used to convert raw text into a corresponding message type, or vice versa.
**
** @author Philip Ng
** @version 1.0
*/
public class RawParser {

	String myRawWhois;
	
	/**
	** Simple constructor.
	*/
	public RawParser() {
		myRawWhois = new String("");
	}
	
	/**
	** Given a full raw whois, return the name.
	** @param whois The whois string
	** @return the corresponding nickname
	*/
	public String getNameFromWhois(String whois) {
		return whois.split("!")[0].substring(1);
	}

	/**
	** Given a raw string adhering to IRC protocol, return the plain text
	** @param raw The raw string
	** @return the plain text
	*/
	public String getMessageForRawInput(String raw) {
		String[] tokens;
				
		//Tokenize the input for easy processing
		tokens = raw.split(" ");
				
		//If it is a PRIVMSG
		if (tokens[1].compareTo("PRIVMSG") == 0) {
			//Start with the sender's name
			String output = getNameFromWhois(tokens[0]);
			//Add on the channel (for now)
			output = output.concat(" (").concat(tokens[2]).concat(")");
			//Add the rest
			if (3 < tokens.length) output = output.concat(" ").concat(tokens[3].substring(1));
			for (int i = 4; i < tokens.length; i++) {
				output = output.concat(" ").concat(tokens[i]);
			}
			return output;
		}
		
		//Otherwise, it is some other raw message, just return it for now
		else return raw;
	}
	
	/**
	** Given a raw string adhering to IRC protocol, return the ID of any corresponding client action.
	** @param raw The raw string
	** @return the string identifier of the action
	*/
	public String getActionForRawInput(String raw) {
		String[] tokens;
		
		//If it is a ping, we need to send a pong
		if (raw.startsWith("PING ")) return new String("PONG");
		
		//Otherwise, do a parsing of the raw message
		else {
			//Tokenize the raw message for easy parsing
			tokens = raw.split(" ");
			
			//If it is raw 001, save the user ID in case we need it
			if (tokens[1].compareTo("001") == 0) {
				myRawWhois = tokens[tokens.length-1];
			}
			
			//Otherwise, there is no corresponding action needed
			else return new String("None");
		}
		
		return new String("None"); //Should not hit this
	}
	
	/**
	** Given a line of plain text, return the raw string adhering to IRC protocol
	** @param message The plain text message
	** @return the raw string
	*/
	public String getRawOutputForUserInput(String message) {
		String[] tokens;
		
		//Tokenize the output for easy processing
		tokens = message.split(" ");
	
		//If it is a quit, we need the QUIT
		if (message.toLowerCase().startsWith("/quit")) {
			String output = new String("QUIT");
			for (int i = 1; i < tokens.length; i++) {
				output = output.concat(" ").concat(tokens[i]);
			}
			return output;
		}
		
		//Otherwise, just return it without processing for now
		else return message;
	}
	
	/**
	** Given a line of plain text, return the ID of any corresponding client action.
	** @param message The plain text message
	** @return the string identifier of the action
	*/
	public String getActionForUserInput(String message) {
		
		//If it is a quit, we should close the associated windows
		if (message.toLowerCase().startsWith("/quit")) return new String("QUIT");
		
		//Otherwise, there is no corresponding action needed
		else return new String("None");
	}
	
	/**
	** Given a ping message, return its pong counterpart.
	** @param ping The ping message
	*/
	public String getPongForPing(String ping) {
		String pong = "PONG ";
		return pong.concat( ping.substring(5) );
	}

}