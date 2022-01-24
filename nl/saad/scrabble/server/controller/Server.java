package nl.saad.scrabble.server.controller;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import nl.saad.scrabble.exceptions.ExitProgram;
import nl.saad.scrabble.protocol.ServerProtocol;
import nl.saad.scrabble.server.view.ServerTUI;

/**
 * Server TUI for Networked Scrabble Application
 * 
 * Intended Functionality: interactively set up & monitor a new server
 * 
 * @author Wim Kamerman
 */
public class Server implements Runnable, ServerProtocol {

	/** The ServerSocket of this ScrabbleServer */
	private ServerSocket ssock;

	/** List of ScrabbleClientHandlers, one for each connected client */
	private List<ClientHandler> clients;
	
	/** Next client number, increasing for every new connection */
	private int next_client_no;

	/** The view of this ScrabbleServer */
	private ServerTUI view;

	private GameController gameController;


	/**
	 * Constructs a new ScrabbleServer. Initializes the clients list, 
	 * the view and the next_client_no.
	 */
	public Server() {
		clients = new ArrayList<>();
		view = new ServerTUI();
		next_client_no = 1;
	}

	/**
	 * Opens a new socket by calling {@link #setup()} and starts a new
	 * ScrabbleClientHandler for every connecting client.
	 * 
	 * If {@link #setup()} throws a ExitProgram exception, stop the program. 
	 * In case of any other errors, ask the user whether the setup should be 
	 * ran again to open a new socket.
	 */
	public void run() {
		boolean openNewSocket = true;
		while (openNewSocket) {
			try {
				// Sets up the scrabble application
				setup();

				while (true) {
					Socket sock = ssock.accept();
					String name = "Client " 
							+ String.format("%02d", next_client_no++);
					view.showMessage("New client [" + name + "] connected!");
					ClientHandler handler = new ClientHandler(sock, this, name);
					new Thread(handler).start();
					clients.add(handler);
				}

			} catch (ExitProgram e1) {
				// If setup() throws an ExitProgram exception, 
				// stop the program.
				openNewSocket = false;
			} catch (IOException e) {
				System.out.println("A server IO error occurred: " 
						+ e.getMessage());

				if (!view.getBoolean("Do you want to open a new socket?")) {
					openNewSocket = false;
				}
			}
		}
		view.showMessage("See you later!");
	}

	/**
	 * Sets up a new Scrabble using {@link #setupScrabble()} and opens a new 
	 * ServerSocket at localhost on a user-defined port.
	 * 
	 * The user is asked to input a port, after which a socket is attempted 
	 * to be opened. If the attempt succeeds, the method ends, If the 
	 * attempt fails, the user decides to try again, after which an 
	 * ExitProgram exception is thrown or a new port is entered.
	 * 
	 * @throws ExitProgram if a connection can not be created on the given 
	 *                     port and the user decides to exit the program.
	 * @ensures a serverSocket is opened.
	 */
	public void setup() throws ExitProgram {
		// First, initialize the Scrabble.
		setupScrabble();

		ssock = null;
		while (ssock == null) {
			int port = view.getInt("Please enter the server port.");

			// try to open a new ServerSocket
			try {
				view.showMessage("Attempting to open a socket at 127.0.0.1 "
						+ "on port " + port + "...");
				ssock = new ServerSocket(port, 0, 
						InetAddress.getByName("127.0.0.1"));
				view.showMessage("Server started at port " + port);
			} catch (IOException e) {
				view.showMessage("ERROR: could not create a socket on "
						+ "127.0.0.1" + " and port " + port + ".");

				if (!view.getBoolean("Do you want to try again?")) {
					throw new ExitProgram("User indicated to exit the "
							+ "program.");
				}
			}
		}
	}
	
	/**
	 * Asks the user for a scrabble name and initializes
	 * a new Scrabble with this name.
	 */
	public void setupScrabble() {
		// To be implemented.
	}
	
	/**
	 * Removes a clientHandler from the client list.
	 * @requires client != null
	 * @param client
	 */
	public void removeClient(ClientHandler client) {
		this.clients.remove(client);
	}

	// ------------------ Server Methods --------------------------

	@Override
	public String getHello() {
		// To be implemented
	}
	
	@Override
	public synchronized String doIn(String cmd) {
		// To be implemented
	}

	@Override
	public synchronized String doOut(String cmd) {
		// To be implemented
	}

	@Override
	public synchronized String doRoom(String cmd) {
		// To be implemented
	}

	@Override
	public synchronized String doAct(String cmd1, String cmd2) {
		// To be implemented
	}

	@Override
	public synchronized String doBill(String cmd1, String cmd2) {
		// To be implemented
	}

	@Override
	public synchronized String doPrint() {
		// To be implemented
	}

	// ------------------ Main --------------------------

	/** Start a new ScrabbleServer */
	public static void main(String[] args) {
		Server server = new Server();
		System.out.println("Welcome to the Scrabble Server! Starting...");
		new Thread(server).start();
	}
	
}
