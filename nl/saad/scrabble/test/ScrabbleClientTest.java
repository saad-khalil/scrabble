package nl.saad.scrabble.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.CoreMatchers.containsString;
//import static org.hamcrest.CoreMatchers.not;
import org.junit.jupiter.api.*;

import nl.saad.scrabble.client.Client;
import nl.saad.scrabble.exceptions.ExitProgram;
import nl.saad.scrabble.exceptions.ProtocolException;
import nl.saad.scrabble.exceptions.ServerUnavailableException;

/**
 * Scrabble Client Test for Confirmer Server
 * 
 * @author Wim Kamerman
 */
class ScrabbleClientTest {
	
	private final static ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final static PrintStream originalOut = System.out;
	
	private static final String GUEST1 = "John";
	private static final String GUEST2 = "Lucy";
	private static final String FAKE_GUEST = "Fake";
	private static final String NIGHTS = "3";
	private static final String INVALID_NIGHTS = "a";

	Client client = new Client();
	
	@BeforeAll
	static public void setUpStream() {
	    System.setOut(new PrintStream(outContent));
	}
	
	@Test
	void testClient() throws ExitProgram, ServerUnavailableException, ProtocolException, IOException {
		// Create the connection. Expected: server indicates a new client has connected.
//		client.createConnection();
//		assertThat(outContent.toString(), containsString("Attempting to connect to /127.0.0.1:8888..."));
//		outContent.reset();
//
//		// Do the HELLO handshake. Expect a welcome message.
//		client.handleHello();
//		assertThat(outContent.toString(), containsString("Welcome to the Scrabble booking system of scrabble:"));
//		outContent.reset();
//
//		// Check in a guest
//		client.doIn(GUEST1);
//		assertThat(outContent.toString(), containsString("> Received: i;" + GUEST1 + System.lineSeparator()));
//		outContent.reset();
//
//		// Activate safe of GUEST1
//		client.doAct(GUEST1, "");
//		assertThat(outContent.toString(), containsString("> Received: a;" + GUEST1 + ";" + System.lineSeparator()));
//		outContent.reset();
//
//		// Retrieve room information of GUEST1
//		client.doRoom(GUEST1);
//		assertThat(outContent.toString(), containsString("> Received: r;" + GUEST1 + System.lineSeparator()));
//		outContent.reset();
//
//		// Retrieve room information of a non existing guest
//		client.doRoom(FAKE_GUEST);
//		assertThat(outContent.toString(), containsString("> Received: r;" + FAKE_GUEST + System.lineSeparator()));
//		outContent.reset();
//
//		// Check in a second guest
//		client.doIn(GUEST2);
//		assertThat(outContent.toString(), containsString("> Received: i;" + GUEST2 + System.lineSeparator()));
//		outContent.reset();
//
//		// Retrieve the state of the scrabble
//		client.doPrint();
//		assertThat(outContent.toString(), containsString("Received: p" + System.lineSeparator()));
//		outContent.reset();
//
//		// Get the bill of guest 1 for a certain number of nights
//		client.doBill(GUEST1, NIGHTS);
//		assertThat(outContent.toString(), containsString("Received: b;" + GUEST1 + ";" + NIGHTS + System.lineSeparator()));
//		outContent.reset();
//
//		// Expect a local error message when no integer is given (i.e. no message sent to the server)
//		client.doBill(GUEST1, INVALID_NIGHTS);
//		assertThat(outContent.toString(), not(containsString("Received: b;" + GUEST1 + ";" + INVALID_NIGHTS + System.lineSeparator())));
//		outContent.reset();
//
//		// Check out GUEST1
//		client.doOut(GUEST1);
//		assertThat(outContent.toString(), containsString("> Received: o;" + GUEST1 + System.lineSeparator()));
//		outContent.reset();
		
		// Exit the program
		client.sendExit();
	}
	
	@AfterAll
	static void restoreStream() {
	    System.setOut(originalOut);
	}

}
