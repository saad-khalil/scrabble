package nl.saad.scrabble.server.controller;

import nl.saad.scrabble.protocol.Protocol;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import static java.lang.Integer.parseInt;


public class ClientHandler implements Runnable {

	/** The socket and In- and OutputStreams */
	private BufferedReader in;
	private BufferedWriter out;
	private Socket sock;
	
	/** The connected Server */
	private Server srv;

	/** Name of this ClientHandler */
	private String name;

	/**
	 * Constructs a new ScrabbleClientHandler. Opens the In- and OutputStreams.
	 * 
	 * @param sock The client socket
	 * @param srv  The connected server
	 * @param name The name of this ClientHandler
	 */
	public ClientHandler(Socket sock, Server srv, String name) {
		try {
			in = new BufferedReader(
					new InputStreamReader(sock.getInputStream()));
			out = new BufferedWriter(
					new OutputStreamWriter(sock.getOutputStream()));
			this.sock = sock;
			this.srv = srv;
			this.name = name;
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
				out.newLine();
				out.flush();
				msg = in.readLine();
			}
			shutdown();
		} catch (IOException e) {
			shutdown();
		}
	}

	/**
	 * Handles commands received from the client by calling the according 
	 * methods at the ScrabbleServer. For example, when the message "i Name" 
	 * is received, the method doIn() of ScrabbleServer should be called 
	 * and the output must be sent to the client.
	 * 
	 * If the received input is not valid, send an "Unknown Command" 
	 * message to the server.
	 * 
	 * @param msg command from client
	 * @throws IOException if an IO errors occur.
	 */
	private void handleCommand(String msg) throws IOException {
		// To be implemented
		String[] message = msg.split(String.valueOf(Protocol.UNIT_SEPARATOR));
		String command = "q";
		String param1 = null;
		String param2 = null;
		if (message.length > 0) {
			command = message[0];
		}
		if (message.length > 1) {
			param1 = message[1];
		}
		if (message.length > 2) {
			param2 = message[2];
		}
		switch (command) {
			case "REQUESTGAME":
				this.out.write(srv.doRequestGame(parseInt(param1)));
				break;
			case "STARTGAME":
				this.out.write(srv.doStartGame(param1));
				break;
			case "INFORMMOVE":
				this.out.write(srv.doMakeMove(param1));
				break;
			case "SENDCHAT":
				this.out.write(srv.doNotifyChat(param1));
				break;
			case "EXIT":
				shutdown();
				this.out.write(srv.doPlayerDisconnected(name));
				break;
			default:
				this.out.write("Unknown Command");
				break;
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
		srv.removeClient(this);
	}
}
