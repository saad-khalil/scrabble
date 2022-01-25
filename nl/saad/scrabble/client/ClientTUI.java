package nl.saad.scrabble.client;

import nl.saad.scrabble.exceptions.ExitProgram;
import nl.saad.scrabble.exceptions.ServerUnavailableException;
import nl.saad.scrabble.protocol.Protocol;
import nl.saad.scrabble.server.view.ServerView;
import nl.saad.scrabble.server.view.utils.ANSI;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.Scanner;

public class ClientTUI implements ClientView {

	/** The PrintWriter to write messages to */
	private PrintWriter console;
	private Scanner scan;
	/**
	 * Constructs a new ScrabbleServerTUI with printer console and scanner for input. Initializes the console.
	 */
	public ClientTUI() {
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

	@Override
	public void printHelpMenu() {
		console.println("""
				REQUESTGAME
				STARTGAME
				MAKEMOVE
				SENDCHAT
				EXIT
				""");
	}

	public String getCommand() { return scan.nextLine(); }

	public void printBoard(String textBoard) { // text String board to ANSI
		int N = 15;
		String[] rows = textBoard.split(";");

		for (String row : rows) {
			String[] rowValues = textBoard.split(",");

			for (String cell : rowValues) {
				String bgColor = ANSI.BLACK_BACKGROUND;
				String fgColor = ANSI.WHITE;
				switch (cell) {
					case "C":
						bgColor = ANSI.PURPLE_BACKGROUND_BRIGHT;
					case "2L":
						bgColor = ANSI.CYAN_BACKGROUND_BRIGHT;
					case "2W":
						bgColor = ANSI.PURPLE_BACKGROUND_BRIGHT;
					case "3L":
						bgColor = ANSI.BLUE_BACKGROUND;
					case "3W":
						bgColor = ANSI.RED_BACKGROUND_BRIGHT;
					default:
						fgColor = ANSI.BLACK;

				}
				console.print( bgColor + fgColor + cell);
			}
			console.println();
		}
		console.println(ANSI.BLACK_BACKGROUND + ANSI.WHITE); // reset colors?
	}

}
