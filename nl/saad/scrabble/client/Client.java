package nl.saad.scrabble.client;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

import nl.saad.scrabble.exceptions.ExitProgram;
import nl.saad.scrabble.exceptions.ProtocolException;
import nl.saad.scrabble.exceptions.ServerUnavailableException;
import nl.saad.scrabble.protocol.ClientProtocol;
import nl.saad.scrabble.protocol.Protocol;
import nl.saad.scrabble.server.view.utils.ANSI;


public class Client implements Runnable, ClientProtocol {

	private Socket serverSock;
	private BufferedWriter out;
	private ClientReciever reciever;
	private ClientTUI view;

	/**
	 * Constructs a new ScrabbleClient. Initialises the view.
	 */
	public Client() {
		view = new ClientTUI();
	}

	/**
	 * Starts a new ScrabbleClient by creating a connection, followed by the
	 * HELLO handshake as defined in the protocol. After a successful
	 * connection and handshake, the view is started. The view asks for
	 * used input and handles all further calls to methods of this class.
	 *
	 * When errors occur, or when the user terminates a server connection, the
	 * user is asked whether a new connection should be made.
	 */
	public void run() {
		try {
			createConnection();
			inputLoop();

		} catch (ExitProgram e) {
			view.showMessage("Error occurred: " + e.getMessage());
		}
	}

	/**
	 * Creates a connection to the server. Requests the IP and port to
	 * connect to at the view (TUI).
	 *
	 * The method continues to ask for an IP and port and attempts to connect
	 * until a connection is established or until the user indicates to exit
	 * the program.
	 *
	 * @throws ExitProgram if a connection is not established and the user
	 * 				       indicates to want to exit the program.
	 * @ensures serverSock contains a valid socket connection to a server
	 */
	public void createConnection() throws ExitProgram {
		clearConnection();
		while (serverSock == null) {
			String host = "127.0.0.1";

			int port = 4000; //view.getInt("Please enter the server port:");

			// try to open a Socket to the server
			try {
				InetAddress addr = InetAddress.getByName(host);
				view.showMessage("Attempting to connect to " + addr + ":" + port + "...");
				serverSock = new Socket(addr, port);
				out = new BufferedWriter(new OutputStreamWriter(serverSock.getOutputStream()));

				reciever = new ClientReciever(serverSock);
				new Thread(reciever).start();

			} catch (IOException e) {
				view.showMessage("ERROR: could not create a socket on "  + host + " and port " + port + ".");
			}
		}
	}

	void inputLoop() {
		try {
			while (true) {
				String command = view.getCommand();
				while (!(handleUserInput(command))) {
					command = view.getCommand();
				}
				if (command.equals("EXIT")) {
					break;
				}
			}
		}
		catch (Exception e) {
			view.showMessage(e.getMessage());
		}
	}

	/**
	 * Resets the serverSocket and In- and OutputStreams to null.
	 *
	 * Always make sure to close current connections via shutdown()
	 * before calling this method!
	 */
	public void clearConnection() {
		serverSock = null;
		if (reciever != null) reciever.setIn(null);
		out = null;
	}


	public boolean handleUserInput(String command) throws ExitProgram, ServerUnavailableException {
		String[] args = command.split(" ");
		String type = args[0].toUpperCase();
		switch (type) { // abbrevs for testing
			case "RG":
				type = "REQUESTGAME";
				break;
			case "SC":
				type = "SENDCHAT";
				break;
			case "MM":
				type = "MAKEMOVE";
				break;
			case "A":
				type = "ANNOUNCE";
				break;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(type);

		if (args.length >= 2) {
			sb.append(Protocol.UNIT_SEPARATOR).append(args[1].toUpperCase());
		}

		if (args.length >= 3) {
			sb.append(Protocol.UNIT_SEPARATOR).append(args[2].toUpperCase());
		}

		if (args.length >= 4) {
			sb.append(Protocol.UNIT_SEPARATOR).append(args[3].toUpperCase());
		}

		if (args.length >= 5) {
			sb.append(Protocol.UNIT_SEPARATOR).append(args[4].toUpperCase());
		}

		sb.append(Protocol.MESSAGE_SEPARATOR);

		String formattedCommand = sb.toString();

		switch (type) {
			case "REQUESTGAME":
				doRequestGame(formattedCommand);
				break;
			case "SENDCHAT":
				doSendChat(formattedCommand);
				break;
			case "MAKEMOVE":
				doMakeMove(formattedCommand);
				break;
			case "EXIT":
				sendExit(formattedCommand);
				break;
			case "ANNOUNCE":
				doSendAnnounce(formattedCommand);
				break;
			default:
				view.showMessage("Invalid command: " + formattedCommand);
				return false;
		}

		return true;
	}


	/**
	 * Sends a message to the connected server, followed by a new line.
	 * The stream is then flushed.
	 *
	 * @param msg the message to write to the OutputStream.
	 * @throws ServerUnavailableException if IO errors occur.
	 */
	public synchronized void sendMessage(String msg)
			throws ServerUnavailableException {
		if (out != null) {
			try {
				out.write(msg);
				out.newLine();
				out.flush();
			} catch (IOException e) {
				view.showMessage(e.getMessage());
				throw new ServerUnavailableException("Could not write to server.");
			}
		} else {
			throw new ServerUnavailableException("Could not write to server.");
		}
	}


	/**
	 * Closes the connection by closing the In- and OutputStreams, as
	 * well as the serverSocket.
	 */
	public void closeConnection() {
		view.showMessage("Closing the connection...");
		try {
			reciever.closeIn();
			out.close();
			serverSock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void doRequestGame(String c) throws ServerUnavailableException {
		sendMessage(c);
	}

	@Override
	public void doMakeMove(String c) throws ServerUnavailableException {
		sendMessage(c);
	}

	@Override
	public void doSendChat(String c) throws ServerUnavailableException {
		sendMessage(c);
	}

	@Override
	public void doSendAnnounce(String c) throws ServerUnavailableException {
		sendMessage(c);
	}


	@Override
	public void sendExit(String c) throws ServerUnavailableException {
		sendMessage(c);
		closeConnection();
	}

	/**
	 * This method starts a new ScrabbleClient.
	 *
	 * @param args
	 */
	public static void main(String[] args) throws ExitProgram, ServerUnavailableException, ProtocolException {
		Client client = new Client();
		System.out.println(ANSI.GREEN + "SCRABBLE CLIENT."  + ANSI.RESET);
		new Thread(client).start();
	}

}
