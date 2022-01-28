package nl.saad.scrabble.client;

import nl.saad.scrabble.exceptions.ServerUnavailableException;
import nl.saad.scrabble.protocol.Protocol;
import nl.saad.scrabble.server.view.utils.ANSI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

class ClientReciever implements Runnable {

    private ClientTUI view;
    private BufferedReader in;

    public ClientReciever(Socket serverSock) {
        try {
            this.in = new BufferedReader(new InputStreamReader(serverSock.getInputStream()));
        } catch (Exception e) {
            System.exit(1);
        }
        view = new ClientTUI();
    }

    void setIn(BufferedReader i) {
        this.in = i;
    }

    void closeIn() {
        try {
            this.in.close();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void run() {
        String msg;
        view.showMessage("Running");
        try {
            msg = readLineFromServer();
            while (msg != null) {
//                view.showMessage("> Incoming: " + msg);
                handleCommand(msg);
                msg = readLineFromServer();
            }
            System.exit(1);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    public synchronized void handleCommand(String msg) {
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

        switch (type) {
            case "SERVER":
            case "NOTIFYTURN":
            case "NOTIFYCHAT":
            case "PLAYERDISCONNECTED":
            case "GAMEOVER":
            case "INFORMMOVE":
            case "PLAYERCONNECTED":
            case "INFORMQUEUE":
            case "STARTGAME":
            case "YOURTILES":
            case "POINTSSCORED":
            case "SCOREBOARD":
            case "NEWTILES":
            case "WELCOME":
                view.showMessage("[" + type + "]: " + content);
                break;
            case "ERROR":
                view.showMessage(ANSI.RED + "[" + type + "]: " + content + ANSI.RESET);
                break;
            case "SENDBOARD":
                view.printBoard(msg.substring(10));
                break;
            default:
                view.showMessage("Unknown command: " + content);
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
                for (String line = in.readLine(); line != null && !line.equals(String.valueOf(Protocol.MESSAGE_SEPARATOR)); line = in.readLine()) {
                    sb.append(line).append(System.lineSeparator());
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
