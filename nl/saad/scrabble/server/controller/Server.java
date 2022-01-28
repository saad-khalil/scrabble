package nl.saad.scrabble.server.controller;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import nl.saad.scrabble.exceptions.ExitProgram;
import nl.saad.scrabble.protocol.Protocol;
import nl.saad.scrabble.protocol.ServerProtocol;
import nl.saad.scrabble.server.view.ServerTUI;
import nl.saad.scrabble.server.view.utils.ANSI;

import static java.lang.Integer.parseInt;

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
		next_client_no = 0;
		ssock = null;
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
					String idName = "Client " + String.format("%02d", (next_client_no+1));
					view.showMessage("New client [" + idName + "] connected!");
					ClientHandler handler = new ClientHandler(sock, this, null, next_client_no);
					new Thread(handler).start();
					clients.add(handler);
					doPLAYERCONNECTED("New client [" + idName + "] connected!");
					gameController.addPlayerToOrder(next_client_no); // add client idx to player order
					next_client_no++;
				}

			} catch (ExitProgram e1) {
				// If setup() throws an ExitProgram exception, 
				// stop the program.
				openNewSocket = false;
			} catch (IOException e) {
				System.out.println("A server IO error occurred: " + e.getMessage());

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
			int port = 4000; // view.getInt("Please enter the server port:");

			// try to opening a new ServerSocket
			try {
				view.showMessage("Attempting to open a socket at 127.0.0.1 on port " + port + "...");
				ssock = new ServerSocket(port, 0, InetAddress.getByName("127.0.0.1"));
				view.showMessage("Server started at port " + port);
			} catch (IOException e) {
				view.showMessage("ERROR: could not create a socket on 127.0.0.1 and port " + port + ".");

				if (!view.getBoolean("Do you want to try again?")) {
					throw new ExitProgram("User indicated to exit the program.");
				}
			}
		}
	}

	// initializes Scrabble
	public void setupScrabble() {
		// To be implemented.
		gameController = new GameController();
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

	public boolean isNameUnique(String name) {
		for (ClientHandler client : clients) {
			if (Objects.equals(client.getName(), name)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void doPLAYERCONNECTED(String recentConnect) {
		String msg = "Current Clients: " + clients.size() + ". " + recentConnect;
		for (ClientHandler client : clients) { // broadcast
			client.sendMessage("PLAYERCONNECTED" + Protocol.UNIT_SEPARATOR + msg + Protocol.MESSAGE_SEPARATOR);
		}
	}

	@Override
	public synchronized String doBroadcast(String protocol, String msg) {
		for (ClientHandler client : clients) { // broadcast
			client.sendMessage(protocol + Protocol.UNIT_SEPARATOR + msg + Protocol.MESSAGE_SEPARATOR);
		}
		return msg;
	}

	@Override
	public String doInformMove(String move) {
		doBroadcast("INFORMMOVE", move);
		return move;
	}


	@Override
	public String doMoveWord(int clientID, String colRow, String direction, String letters) {
		if (gameController.getTurnPlayerID() != clientID) {
			return "Not your turn";
		}

		for (int i = 0; i < letters.length(); i++) {
			if (letters.charAt(i) == '!') {
				return "You cannot send a blank (!) tile. Send a letter instead of it that you need.";
			}
		}

		int r = parseInt(colRow.substring(1));
		int c = colRow.charAt(0) - 'A';
		char dir = direction.toUpperCase().charAt(0);

		return gameController.makeMoveWord(clientID, r, c, dir, letters);
	}

	public String doMoveSwap(int clientID, String letters) {
		if (gameController.getTurnPlayerID() != clientID) { // not your turn
			return "Not your turn.";
		}

		return gameController.makeMoveSwap(clientID, letters);
	}

	@Override
	public synchronized void doNotifyChat(String name, String msg) {
		for (ClientHandler client : clients) { // broadcast
			if (client.canChat()) { // filter messages to those who support chatting
				client.sendMessage("NOTIFYCHAT" + Protocol.UNIT_SEPARATOR + "("+name + "):" +  Protocol.UNIT_SEPARATOR + "\"" + msg + "\"" + Protocol.MESSAGE_SEPARATOR);
			}
		}
	}


	public void doNotifyTurn() {
		int turnPlayerID = gameController.getTurnPlayerID();

		for (ClientHandler client : clients) {
			if (client.isReady()) {
				boolean isTurn = client.getClientID() == turnPlayerID;
				String handLetters = gameController.getPlayerHandLetters(client.getClientID());
				client.sendMessage("NOTIFYTURN" + Protocol.UNIT_SEPARATOR +  (isTurn ? '1' : '0') + Protocol.UNIT_SEPARATOR + client.getName() +  Protocol.MESSAGE_SEPARATOR);
				client.sendMessage("YOURTILES" + Protocol.UNIT_SEPARATOR + handLetters + Protocol.MESSAGE_SEPARATOR);

			}
		}
	}


	public String doWelcome(String name, boolean canChat) {
		String welcomeMsg = "WELCOME" + Protocol.UNIT_SEPARATOR + name;
		if (canChat) {
			welcomeMsg +=  Protocol.UNIT_SEPARATOR + "CHAT";
		}
		welcomeMsg += Protocol.MESSAGE_SEPARATOR;
		return  welcomeMsg;
	}

	public boolean allPlayersReady() {
		for (ClientHandler client : clients) {
			if (!client.isReady()) {
				return  false;
			}
		}
		return true;
	}

	public int getCountReady() {
		int countReady = 0;
		for (ClientHandler client : clients) {
			if (client.isReady()) {
				countReady++;
			}
		}
		return  countReady;
	}

	public void startGame() { // start game after announcing
		gameController.startGame();
		view.showMessage("Game started!");
		doSendBoard();
		doNotifyTurn(); // first turn
	}

	public void doNextTurn() { // after a successful move (new tiles will be drawn in case of both WORD and SWAP)
		// send new tiles to current player (who did the turn)
		int currentPID = gameController.getTurnPlayerID();
		String currentMove = gameController.getTurnMove();
		int currentScore = gameController.getTurnScore();
		String currentName = "";

		// SEND NEWTILES
		for (ClientHandler client : clients) {
			if (client.getClientID() == currentPID) {
				client.sendMessage("NEWTILES" + Protocol.UNIT_SEPARATOR + gameController.getDrawnTiles() + Protocol.MESSAGE_SEPARATOR);
				currentName = client.getName();
				break;
			}
		}

		// inform everyone about the new move, points scored and current scoreboard
		doBroadcast("INFORMMOVE",currentName + Protocol.UNIT_SEPARATOR + currentMove);
		doBroadcast("POINTSSCORED",currentName + " scored " + currentScore + " points this turn.");

		// check if game is over after this turn
		if (gameController.isGameOver()) {
			doGameOver("WIN");
			return;
		}
		else {
			doBroadcast("SCOREBOARD", getScoreboardString());
		}

		gameController.nextTurn();
		doSendBoard();
		doNotifyTurn();
	}

	public String getScoreboardString() { // Alice-US-0-US-Bob-US-30
		StringBuilder sbScores = new StringBuilder();
		int i = 0;
		for (ClientHandler client : clients) {
			String name = client.getName();
			int score = gameController.getPlayerScore(client.getClientID());
			sbScores.append(name).append(Protocol.UNIT_SEPARATOR).append(score);
			if (i != clients.size() - 1) { // in between
				sbScores.append(Protocol.UNIT_SEPARATOR);
			}
			i++;
		}
		return sbScores.toString();
	}




	@Override
	public synchronized boolean doInformQueue(int playerIdx, int requestedNumPlayers) {
		int countReady = getCountReady();
		// UNCOMMENT LATER
		if (countReady >= 2 &&  requestedNumPlayers != gameController.getNumPlayers()) { // requested once, cannot change number of players in queue
			return false;
		}

		ArrayList<Integer> playerOrder = gameController.getPlayerOrder();

		gameController.setNumPlayers(requestedNumPlayers);

		for (ClientHandler client : clients) {
			if (playerOrder.contains(client.getClientID())) {
				client.sendMessage("INFORMQUEUE" + Protocol.UNIT_SEPARATOR + String.valueOf(countReady) + Protocol.UNIT_SEPARATOR + requestedNumPlayers + Protocol.MESSAGE_SEPARATOR);
			}
		}


		if (countReady >= requestedNumPlayers) {

			StringBuilder sb = new StringBuilder();
			int i = 0;
			for (int pi : playerOrder) {
				for (ClientHandler client : clients) {
					if (client.getClientID() == pi) {
						sb.append(client.getName());
					}
				}
				if (i != playerOrder.size() - 1) {
					sb.append(Protocol.UNIT_SEPARATOR);
				}
				i++;
			}
			String orderedPlayerNames = sb.toString();
			doBroadcast("STARTGAME", orderedPlayerNames);
			startGame();
		}


		return true;
	}

	@Override
	public synchronized void doSendBoard() {
		String textBoard = gameController.getTextBoard();
		doBroadcast("SENDBOARD",  textBoard);
	}

	@Override
	public void doGameOver(String type) {
		if (type.equals("WIN") || type.equals("DISCONNECT")) {
			doBroadcast("GAMEOVER", type + Protocol.UNIT_SEPARATOR + getScoreboardString());
			gameController.reset();
		}
	}

	public synchronized void doDisconnect(int clientID, String clientName) {

		int np = gameController.getNumPlayers();
		doBroadcast("PLAYERDISCONNECTED",  clientName);

		if (!gameController.isGameRunning()) { // if game not running (queue)
			doBroadcast("INFORMQUEUE", String.valueOf(getCountReady()) + Protocol.UNIT_SEPARATOR + np);
		}
		else { // if game running then its now its over...
			doGameOver("DISCONNECT"); // disconnect type gameover with scoreboard
		}
		gameController.removePlayerFromOrder(clientID);
	}



	// ------------------ Main --------------------------

	/** Start a new ScrabbleServer */
	public static void main(String[] args) {
		Server server = new Server();
		System.out.println(ANSI.PURPLE + "SCRABBLE SERVER" + ANSI.RESET);
		new Thread(server).start();
	}
	
}
