package nl.saad.scrabble.server.view;

import java.io.PrintWriter;
import java.util.Scanner;

public class ServerTUI implements ServerView {
	
	/** The PrintWriter to write messages to */
	private PrintWriter console;
	private Scanner scan;
	/**
	 * Constructs a new ScrabbleServerTUI with printer console and scanner for input. Initializes the console.
	 */
	public ServerTUI() {
		console = new PrintWriter(System.out, true);
		scan = new Scanner(System.in);
	}

	@Override
	public void showMessage(String message) {
		console.println(message);
	}
	
	@Override
	public String getString(String question) { return scan.next(); }

	@Override
	public int getInt(String question) { return scan.nextInt(); }

	@Override
	public boolean getBoolean(String question) { return scan.nextBoolean(); }

}
