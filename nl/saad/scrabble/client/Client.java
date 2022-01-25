package nl.saad.scrabble.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

import nl.saad.scrabble.exceptions.ExitProgram;
import nl.saad.scrabble.exceptions.ProtocolException;
import nl.saad.scrabble.exceptions.ServerUnavailableException;
import nl.saad.scrabble.protocol.ClientProtocol;
import nl.saad.scrabble.protocol.Protocol;
import nl.saad.scrabble.server.view.utils.ANSI;

class Reciever implements  Runnable {

	private ClientTUI view;
	private BufferedReader in;

	public Reciever(BufferedReader i) {
		this.in = i;
		view = new ClientTUI();
	}

	void setIn(BufferedReader i) { this.in = i; }

	BufferedReader getIn() { return this.in; }

	public void run() {
		String msg;
		view.showMessage("Running");
		try {
			msg = readLineFromServer();
			while (msg != null) {
//				view.showMessage("> Incoming: " + msg);
				handleCommand(msg);
				msg = readLineFromServer();
			}
			System.exit(1);
		} catch (Exception e) {
			System.exit(1);
		}
	}


	void handleCommand(String msg) {
		msg = msg.replace(String.valueOf(Protocol.MESSAGE_SEPARATOR), ""); // remove terminator
		String[] args = msg.split(String.valueOf(Protocol.UNIT_SEPARATOR));
		String type = args[0];
		if (args.length == 1) {
			view.showMessage("Command does not follow protocol: " + msg);
			return;
		}

		StringBuilder sb = new StringBuilder(args[1]);
		for (int i = 2; i < args.length; i++) {
			sb.append(" ").append(args[i]);
		}
		String content = sb.toString();

		switch(type) {
			case "SERVER":
			case "NOTIFYTURN":
			case "NOTIFYCHAT":
			case "PLAYERDISCONNECTED":
			case "GAMEOVER":
			case "INFORMMOVE":
			case "INFORMCONNECT":
			case "INFORMQUEUE":
			case "STARTGAME":
			case "WELCOME":
				view.showMessage("["+type+"]: " + content);
				break;
			case "ERROR":
				view.showMessage(ANSI.RED+"["+type+"]: " + content+ ANSI.RESET);
				break;
			case "SENDBOARD":
				view.printBoard(content.toString());
				break;
			default:
				view.showMessage("Unknown command: " + msg);
				break;
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
}




public class Client implements Runnable, ClientProtocol {

	private Socket serverSock;
	private BufferedWriter out;
	private Reciever reciever;
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

				reciever = new Reciever(new BufferedReader(new InputStreamReader(serverSock.getInputStream())));
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
				view.showMessage("Invalid command: " + command);
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
			reciever.getIn().close();
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
