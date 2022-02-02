package nl.saad.scrabble.client.view;

import java.net.InetAddress;

import nl.saad.scrabble.exceptions.ExitProgram;
import nl.saad.scrabble.exceptions.ServerUnavailableException;

/**
 * Interface for the Scrabble Client View.
 * 
 * @author Wim Kamerman
 */
public interface ClientView {

	/**
	 * Writes the given message to standard output.
	 * 
	 * @param message the message to write to the standard output.
	 */
	public void showMessage(String message);


	/**
	 * Prints the question and asks the user to input a String.
	 * 
	 * @param question The question to show to the user
	 * @return The user input as a String
	 */
	public String getString(String question);

	/**
	 * Prints the question and asks the user to input an Integer.
	 * 
	 * @param question The question to show to the user
	 * @return The written Integer.
	 */
	public int getInt(String question);

	/**
	 * Prints the question and asks the user for a yes/no answer.
	 * 
	 * @param question The question to show to the user
	 * @return The user input as boolean.
	 */
	public boolean getBoolean(String question);

	/**
	 * Prints the help menu with available input options.
	 */
	public void printHelpMenu();

}
