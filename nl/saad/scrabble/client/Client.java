package nl.saad.scrabble.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;

import nl.saad.scrabble.exceptions.ExitProgram;
import nl.saad.scrabble.exceptions.ProtocolException;
import nl.saad.scrabble.exceptions.ServerUnavailableException;
import nl.saad.scrabble.protocol.ClientProtocol;
import nl.saad.scrabble.protocol.Protocol;
import nl.saad.scrabble.server.view.ServerTUI;


public class Client implements ClientProtocol {
	
	private Socket serverSock;
	private BufferedReader in;
	private BufferedWriter out;
	private ClientTUI view;

	/**
	 * Constructs a new ScrabbleClient. Initialises the view.
	 */
	public Client() {
		view = new ClientTUI();
		try {
            createConnection();
            handleAnnounce();
			start();
        } catch (ExitProgram | ServerUnavailableException | ProtocolException e) {
            System.out.println("Error occurred");
        }
        view.showMessage("Do you want a new connection ? y/n");
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
	public void start() throws ExitProgram, ServerUnavailableException {
		// To be implemented
		view.showMessage("Enter command: ");
		String userCommand = view.getCommand();
		while (!Objects.equals(userCommand, "EXIT")) {
			handleUserInput(userCommand);
		}
	}

	/**
	 * Split the user input on a space and handle it accordingly.
	 * - If the input is valid, take the corresponding action (for example,
	 *   when "i Name" is called, send a checkIn request for Name)
	 * - If the input is invalid, show a message to the user and print the help menu.
	 *
	 * @param input The user input.
	 * @throws ExitProgram               	When the user has indicated to exit the
	 *                                    	program.
	 * @throws ServerUnavailableException 	if an IO error occurs in taking the
	 *                                    	corresponding actions.
	 */
	public void handleUserInput(String command) throws ExitProgram, ServerUnavailableException {
		String[] args = command.split(" ");
		String type = args[0];

		StringBuilder sb = new StringBuilder();
		sb.append(type);

		if (args.length >= 2) {
			sb.append(Protocol.UNIT_SEPARATOR).append(args[1]);
		}

		if (args.length == 3) {
			sb.append(Protocol.UNIT_SEPARATOR).append(args[2]);
		}
		sb.append(Protocol.MESSAGE_SEPARATOR);

		String formattedCommand = sb.toString();

		switch (type) { // action
			case "REQUESTGAME":
				doRequestGame(formattedCommand);
			case "SENDCHAT":
				doSendChat(formattedCommand);
			case "MAKEMOVE":
				doInformMove(formattedCommand);
			case "EXIT":
				sendExit(formattedCommand);
			default:
				view.showMessage("Invalid command.");
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
			int port = 8888;

			// try to open a Socket to the server
			try {
				InetAddress addr = InetAddress.getByName(host);
				System.out.println("Attempting to connect to " + addr + ":" 
					+ port + "...");
				serverSock = new Socket(addr, port);
				in = new BufferedReader(new InputStreamReader(
						serverSock.getInputStream()));
				out = new BufferedWriter(new OutputStreamWriter(
						serverSock.getOutputStream()));
			} catch (IOException e) {
				System.out.println("ERROR: could not create a socket on " 
					+ host + " and port " + port + ".");

				//Do you want to try again? (ask user, to be implemented)
//				if(false) {
//					throw new ExitProgram("User indicated to exit.");
//				}
			}
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
		in = null;
		out = null;
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
				System.out.println(e.getMessage());
				throw new ServerUnavailableException("Could not write to server.");
			}
		} else {
			throw new ServerUnavailableException("Could not write to server.");
		}
	}

	/**
	 * Reads and returns one line from the server.
	 * 
	 * @return the line sent by the server.
	 * @throws ServerUnavailableException if IO errors occur.
	 */
	public String readLineFromServer() throws ServerUnavailableException {
		if (in != null) {
			try {
				// Read and return answer from Server
				String answer = in.readLine();
				if (answer == null) {
					throw new ServerUnavailableException("Could not read from server.");
				}
				return answer;
			} catch (IOException e) {
				throw new ServerUnavailableException("Could not read from server.");
			}
		} else {
			throw new ServerUnavailableException("Could not read from server.");
		}
	}

	/**
	 * Reads and returns multiple lines from the server until the end of 
	 * the text is indicated using a line containing ProtocolMessages.EOT.
	 * 
	 * @return the concatenated lines sent by the server.
	 * @throws ServerUnavailableException if IO errors occur.
	 */
	public String readMultipleLinesFromServer() throws ServerUnavailableException {
		if (in != null) {
			try {
				// Read and return answer from Server
				StringBuilder sb = new StringBuilder();
				for (String line = in.readLine(); line != null && !line.equals(Protocol.MESSAGE_SEPARATOR);
					 line = in.readLine()) {
					sb.append(line + System.lineSeparator());
				}
				return sb.toString();
			} catch (IOException e) {
				throw new ServerUnavailableException("Could not read from server.");
			}
		} else {
			throw new ServerUnavailableException("Could not read from server.");
		}
	}

	/**
	 * Closes the connection by closing the In- and OutputStreams, as 
	 * well as the serverSocket.
	 */
	public void closeConnection() {
		System.out.println("Closing the connection...");
		try {
			in.close();
			out.close();
			serverSock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handleAnnounce() throws ServerUnavailableException, ProtocolException {
		String msg = readLineFromServer();
		view.showMessage("[SERVER]: " + msg);
	}

	@Override
	public void handleInformQueue() throws ServerUnavailableException, ProtocolException {
//		sendMessage(String.valueOf(ProtocolMessages.HELLO));
//		if (readLineFromServer().contains(String.valueOf(ProtocolMessages.HELLO))) {
//			System.out.println("Welcome to the Hotel booking system of hotel! Press 'h' for help menu: ");
//		} else {
//			throw new ProtocolException("Can't do the handshake");
//		}
		String msg = readLineFromServer();
	}

	@Override
	public void handleNotifyTurn() throws ServerUnavailableException, ProtocolException {
		String msg = readLineFromServer();
	}

	@Override
	public void handleNewTiles() throws ServerUnavailableException, ProtocolException {
		String msg = readLineFromServer();
		view.printBoard(msg);
	}

	@Override
	public void doRequestGame(String c) throws ServerUnavailableException {
		sendMessage(c);
	}

	@Override
	public void doInformMove(String c) throws ServerUnavailableException {
		sendMessage(c);
	}

	@Override
	public void doSendChat(String c) throws ServerUnavailableException {
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
	public static void main(String[] args) throws ExitProgram, ServerUnavailableException {
		(new Client()).start();
	}

}
