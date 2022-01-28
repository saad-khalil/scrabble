package nl.saad.scrabble.protocol;

/**
 * Defines the methods that the Scrabble Server should support. The results 
 * should be returned to the client.
 * 
 * @author Saad Khalil
 */
public interface ServerProtocol {

	/**
	 *
	 *
	 * @requires
	 * @param
	 * @return
	 */
	public String doMoveWord(int clientID, String colRow, String direction, String letters);


	/**
	 *
	 *
	 * @requires
	 * @param
	 * @return
	 */
	public String doMoveSwap(int clientID, String letters);

	/**
	 *
	 *
	 * @requires
	 * @param
	 * @return
	 */
	public void doNotifyChat(String name, String msg);

	/**
	 *
	 *
	 * @requires
	 * @param
	 * @return
	 */
	public String doWelcome(String name, boolean canChat);

	/**
	 *
	 *
	 * @requires
	 * @param
	 */
	public boolean doInformQueue (int playerIdx, int numPlayers);

	public String doInformMove (String c);

	/**
	 *
	 *
	 * @requires
	 * @param
	 * @return
	 */
	public void doSendBoard();


	/**
	 *
	 *
	 * @requires
	 * @param
	 * @return
	 */
	public void doGameOver(String gameOver);

	/**
	 *
	 *
	 * @requires
	 * @param protocol (name)
	 * @return
	 */
	public String doBroadcast(String protocol, String c);

	/**
	 *
	 *
	 * @requires
	 * @param
	 * @return
	 */
	public void doPLAYERCONNECTED(String recentConnect);

}
