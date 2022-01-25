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
	public String doMakeMove(String c);

	/**
	 *
	 *
	 * @requires
	 * @param
	 * @return
	 */
	public String doNotifyChat(String c);

	/**
	 *
	 *
	 * @requires
	 * @param
	 * @return
	 */
	public String doNewTiles(String c);

	/**
	 *
	 *
	 * @requires
	 * @param
	 * @return
	 */
	public String doPlayerDisconnected(String c);

	/**
	 *
	 *
	 * @requires
	 * @param
	 * @return
	 */
	public String doGameOver();

	/**
	 *
	 *
	 * @requires
	 * @param
	 * @return
	 */
	public String doAnnounce(String c);

	/**
	 *
	 *
	 * @requires
	 * @param
	 * @return
	 */
	public String doStartGame(String c);

	/**
	 *
	 *
	 * @requires
	 * @param
	 * @return
	 */
	public String doRequestGame(int numPlayers);

}
