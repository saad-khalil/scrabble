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


	void doSendAnnounce(String c) throws ServerUnavailableException;


	public void sendExit(String c) throws ServerUnavailableException;

}
