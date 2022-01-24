package nl.saad.scrabble.protocol;

/**
 * Protocol for Networked Scrabble Application.
 * 
 * @author Wim Kamerman
 */
public class ProtocolMessages {

	/**
	 * Delimiter used to separate arguments sent over the network.
	 */
	public static final String DELIMITER = ";";

	/**
	 * Sent as last line in a multi-line response to indicate the end of the text.
	 */
	public static final String EOT = "--EOT--";

	/** Used for the server-client handshake */
	public static final char HELLO = 'h';

	/**
	 * The following chars are both used by the TUI to receive user input, and the
	 * server and client to distinguish messages.
	 */
	public static final char EXIT = 'x';
	public static final char IN = 'i';
	public static final char OUT = 'o';
	public static final char ROOM = 'r';
	public static final char ACT = 'a';
	public static final char PRINT = 'p';
	public static final char BILL = 'b';
	public static final char HELP = 'h';

}
