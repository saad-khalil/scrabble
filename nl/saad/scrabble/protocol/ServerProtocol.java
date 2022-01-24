package nl.saad.scrabble.protocol;

/**
 * Defines the methods that the Scrabble Server should support. The results 
 * should be returned to the client.
 * 
 * @author Wim Kamerman
 */
public interface ServerProtocol {

	/**
	 * Returns a String to be sent as a response to a Client HELLO request,
	 * including the name of the scrabble: ProtocolMessages.HELLO +
	 * ProtocolMessages.DELIMITER + (Scrabble Name);
	 * 
	 * @return String to be sent to client as a handshake response.
	 */
	public String getHello();

	/**
	 * Given the name of a guest, the checkIn command of the scrabble application is
	 * called. The result is returned as String and can be: - Parameter is wrong
	 * (guestName is null) - CheckIn failed (no room assigned) - CheckIn successful
	 * + room number
	 * 
	 * @requires guestName != null
	 * @param guestName Name of the guest
	 * @return textual result, to be shown to the user
	 */
	public String doIn(String guestName);

	/**
	 * Given the name of a guest, the checkOut command of the scrabble application is
	 * called. The result is returned as String and can be: - Parameter is wrong
	 * (guestName is null) - CheckOut successful
	 * 
	 * @requires guestName != null
	 * @param guestName Name of the guest
	 * @return textual result, to be shown to the user
	 */
	public String doOut(String guestName);

	/**
	 * Given the name of a guest, the corresponding room is returned. The result is
	 * returned as String and can be: - Parameter is wrong (guestName is null) -
	 * Guest does not have a room - Guest has room + room number
	 * 
	 * @requires guestName != null
	 * @param guestName Name of the guest
	 * @return textual result, to be shown to the user
	 */
	public String doRoom(String guestName);

	/**
	 * Given the name of a guest, the safe in the room of the guest is activated.
	 * The result is returned as String and can be: - Parameters are wrong
	 * (guestName is null or password is required) - Safe has not been activated
	 * (guest has no room) - Safe has been activated
	 * 
	 * @requires guestName != null
	 * @param guestName Name of the guest
	 * @param password  (Optional) Password in case of a protected safe
	 * @return textual result, to be shown to the user
	 */
	public String doAct(String guestName, String password);

	/**
	 * Given the name of a guest and the number of nights of the stay, the bill is
	 * requested. The result is returned as String and can be: - Parameters are
	 * wrong (guestName or nights is null or nights is no integer) - The String of
	 * the bill for the guest
	 * 
	 * @requires guestName != null &&
	 * @param guestName Name of the guest
	 * @param nights    Number of nights of the
	 * @return textual result, to be shown to the user
	 */
	public String doBill(String guestName, String nights);

	/**
	 * Returns the state of the Scrabble, containing an overview of the rooms, its
	 * guests and the state of the safes.
	 * 
	 * @return the string representation of the Scrabble
	 */
	public String doPrint();

}
