package nl.saad.scrabble.server.view;

import java.io.PrintWriter;


public class ServerTUI implements ServerView {
	
	/** The PrintWriter to write messages to */
	private PrintWriter console;

	/**
	 * Constructs a new ScrabbleServerTUI. Initializes the console.
	 */
	public ServerTUI() {
		console = new PrintWriter(System.out, true);
	}

	@Override
	public void showMessage(String message) {
		console.println(message);
	}
	
	@Override
	public String getString(String question) {
		// To be implemented
		return "U Parkscrabble";
	}

	@Override
	public int getInt(String question) {
		// To be implemented
		return 8888;
	}

	@Override
	public boolean getBoolean(String question) {
		// To be implemented
		return true;
	}

}
