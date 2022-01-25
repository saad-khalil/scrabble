package nl.saad.scrabble.protocol;

import nl.saad.scrabble.exceptions.ProtocolException;
import nl.saad.scrabble.exceptions.ServerUnavailableException;

/**
 * Defines the methods that the Scrabble Client should support.
 * 
 * @author Wim Kamerman
 */
public interface ClientProtocol {


	/**
	 *
	 *
	 * @requires
	 * @param
	 * @return
	 * @throws ProtocolException          if the server response is invalid.
	 */
	public void handleAnnounce() throws ServerUnavailableException, ProtocolException;


	/**
	 *
	 *
	 * @requires
	 * @param
	 * @return
	 * @throws ProtocolException          if the server response is invalid.
	 */
	public void handleNotifyChat() throws ServerUnavailableException, ProtocolException;

	/**
	 *
	 *
	 * @requires
	 * @param
	 * @return
	 * @throws ProtocolException          if the server response is invalid.
	 */
	public void handleNotifyTurn() throws ServerUnavailableException, ProtocolException;

	/**
	 *
	 *
	 * @requires
	 * @param
	 * @return
	 * @throws ProtocolException          if the server response is invalid.
	 */
	public void handleNewTiles() throws ServerUnavailableException, ProtocolException;

	/**
	 *
	 *
	 * @requires
	 * @param
	 * @return
	 * @throws ProtocolException          if the server response is invalid.
	 */
	public void doRequestGame(String c) throws ServerUnavailableException;

	/**
	 *
	 *
	 * @requires
	 * @param
	 * @return
	 * @throws ProtocolException          if the server response is invalid.
	 */
	public void doMakeMove(String c) throws ServerUnavailableException;

	/**
	 *
	 *
	 * @requires
	 * @param
	 * @return
	 * @throws ProtocolException          if the server response is invalid.
	 */
	public void doSendChat(String c) throws ServerUnavailableException;


	/**
	 * Sends a message to the server indicating that this client will exit:
	 * ProtocolMessages.EXIT;
	 * 
	 * Both the server and the client then close the connection. The client does
	 * this using the {@link #closeConnection()} method.
	 * 
	 * @throws ServerUnavailableException if IO errors occur.
	 */
	public void sendExit(String c) throws ServerUnavailableException;

}
