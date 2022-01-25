package nl.saad.scrabble.server.controller;

import nl.saad.scrabble.protocol.Protocol;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Objects;

import static java.lang.Integer.parseInt;


public class ClientHandler implements Runnable {

	/** The socket and In- and OutputStreams */
	private BufferedReader in;
	private BufferedWriter out;
	private Socket sock;
	/** The connected Server */
	private Server srv;
	private int clientID;
	/** Name of this ClientHandler */
	private String name;
	private boolean canChat;
	private boolean ready;



	public String getName() { return name; }

	public int getClientID() { return clientID; }

	public boolean isReady() { return ready; }

	public boolean canChat() { return canChat; }

	/**
	 * Constructs a new ScrabbleClientHandler. Opens the In- and OutputStreams.
	 * 
	 * @param sock The client socket
	 * @param srv  The connected server
	 * @param name The name of this ClientHandler
	 */
	public ClientHandler(Socket sock, Server srv, String name, int id) {
		canChat = false;
		try {
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			this.sock = sock;
			this.srv = srv;
			this.name = name;
			this.clientID = id;
		} catch (IOException e) {
			shutdown();
		}
	}


	/**
	 * Continuously listens to client input and forwards the input to the
	 * {@link #handleCommand(String)} method.
	 */
	public void run() {
		String msg;
		try {
			msg = in.readLine();
			while (msg != null) {
				System.out.println("> [" + name + "] Incoming: " + msg);
				handleCommand(msg);
				msg = in.readLine();
			}
			shutdown();
		} catch (IOException e) {
			shutdown();
		}
	}


	private void handleCommand(String msg) {
		// To be implemented
		msg = msg.replace(String.valueOf(Protocol.MESSAGE_SEPARATOR), "");
		String[] args = msg.split(String.valueOf(Protocol.UNIT_SEPARATOR));
		String type = args[0];
		String param1 = null;
		String param2 = null;
		String param3 = null;
		String param4 = null;

		if (args.length >= 2) {
			param1 = args[1];
		}
		if (args.length >= 3) {
			param2 = args[2];
		}
		if (args.length >= 4) {
			param3 = args[3];
		}
		if (args.length >= 5) {
			param4 = args[4];
		}

		try {
			switch (type) {
				case "REQUESTGAME":
					if (name == null) {
						sendError("You have not registered yourself.");
						break;
					}
					int numPlayers = 2;
					if (param1 != null) {
						numPlayers = parseInt(param1);
					}
					if (numPlayers < 2) {
						sendError(Protocol.Error.E010.getDescription());
					}

					ready = true;
					boolean informed = srv.doInformQueue(clientID, numPlayers);
					if (!informed) { // requested players were different
						sendError(Protocol.Error.E010.getDescription());
					}
					break;

				case "MAKEMOVE":
					String err = null;
					if (param1 == "WORD") {
						err = srv.doMoveWord(clientID, param2, param3, param4);
					}
					else if (param1 == "SWAP") {
						err = srv.doMoveSwap(clientID,param2);
					}
					else {
						sendError(Protocol.Error.E003.getDescription());
						break;
					}

					if (err != null) { // got an error doing the move
						sendError(err);
						break;
					}
					break;

				case "SENDCHAT":
					if (name == null) {
						sendError("You have not registered yourself.");
						break;
					}
					if (!canChat) {
						sendError( "Sorry, you did not enable chat support...");
					}
					else {
						srv.doNotifyChat(name, param1);
					}
					break;

				case "ANNOUNCE":
					if (name != null) {
						sendError("You have already registered.");
						break;
					}
					if (param2 != null) { // chat feature not needed
						if (!Objects.equals(param2, "CHAT")) {
							sendError(Protocol.Error.E003.getDescription());
							break;
						}
						canChat = true;
					}
					else {
						canChat = false;
					}
					if (!srv.isNameUnique(param1)) {
						sendError(Protocol.Error.E001.getDescription());
						break;
					}
					name = param1;
					sendMessage(srv.doWelcome(name, canChat));
					break;

				case "EXIT":
					shutdown();
					sendMessage(srv.doPlayerDisconnected(name));
					break;

				default:
					sendError("Unknown Command.");
					break;
			}
		}
		catch (Exception e) {
			sendError(e.getMessage());
		}

	}

	public void sendError(String msg) {
		sendMessage("ERROR" + Protocol.UNIT_SEPARATOR + msg  + Protocol.MESSAGE_SEPARATOR);
	}

	public void sendMessage(String msg) {
		if (out != null) {
			try {
				out.write(msg);
				out.newLine();
				out.flush();
			} catch (IOException e) {
				System.out.println(e.getMessage());
//				sendMessage("[ERROR] " + e.getMessage() + + Protocol.MESSAGE_SEPARATOR);
			}
		} else {
			sendError("Out is null.");
		}
	}

	/**
	 * Shut down the connection to this client by closing the socket and 
	 * the In- and OutputStreams.
	 */
	private void shutdown() {
		System.out.println("> [" + name + "] Shutting down.");
		try {
			in.close();
			out.close();
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		srv.doDisconnect(clientID);
		srv.removeClient(this);
	}
}
