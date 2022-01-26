package nl.saad.scrabble.client;

import nl.saad.scrabble.exceptions.ExitProgram;
import nl.saad.scrabble.exceptions.ServerUnavailableException;
import nl.saad.scrabble.protocol.Protocol;
import nl.saad.scrabble.server.view.ServerView;
import nl.saad.scrabble.server.view.utils.ANSI;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.Locale;
import java.util.Objects;
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
		console.println(message.replace(String.valueOf(Protocol.MESSAGE_SEPARATOR), ""));
	}

	@Override
	public String getString(String question) {
		console.println(question);
		return scan.next();
	}

	@Override
	public int getInt(String question) {
		console.println(question);
		return scan.nextInt();
	}

	@Override
	public boolean getBoolean(String question) {
		console.println(question);
		return scan.nextBoolean();
	}

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

	public String getCommand() {
		return scan.nextLine();
	}

	public void printBoard(String textBoard) { // text String board to ANSI
		int N = 15;
		String[] rows = textBoard.split("%");
		char[] alphabet = "abcdefghijklmno".toUpperCase().toCharArray();

		console.print("     ");
		for (int k = 0; k < N; k++) {
			console.print("    " + alphabet[k] + "   ");
		}
		console.println();

		String fgColor = ANSI.BLACK;
		String fgColorBold = ANSI.BLACK_BOLD;

		int i = 0;
		for (String row : rows) {
			String[] rowValues = row.split(",");
			int j = 0;

			if (i < 10) {
				console.print(" ");
			}
			console.print("  " + i + "  ");

			for (String cell : rowValues) {
				String bgColor = ANSI.WHITE_BACKGROUND; // empty

				String slotType = Protocol.TEXT_LAYOUT[i][j];
				switch (slotType) {
					case "C":
					case "2W":
						bgColor = ANSI.PURPLE_BACKGROUND;
						break;
					case "2L":
						bgColor = ANSI.CYAN_BACKGROUND;
						break;
					case "3L":
						bgColor = ANSI.BLUE_BACKGROUND;
						break;
					case "3W":
						bgColor = ANSI.RED_BACKGROUND;
						break;
				}
				if (cell.equals(" ")) {
					cell = "   " + alphabet[j] + i + (i < 10 ? "   " : "  ");
					console.print(fgColor);
				}
				else {
					bgColor = ANSI.YELLOW_BACKGROUND;
					cell = "    " + cell + "   ";
					console.print(fgColorBold);
				}

				console.print( bgColor + cell);
				j++;
			}
			console.println(ANSI.RESET);
			i++;

		}
		console.println(ANSI.RESET); // reset colors?
	}

}
