
package joon;



import java.io.*;
import java.net.*;

public class PlayerHandler implements Runnable {
    private Socket socket;
    private GameServer server;
    private PrintWriter out;
    private BufferedReader in;
    private String playerName;
    private int score ; // Player's score

    public PlayerHandler(Socket socket, GameServer server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.score = this.score; 
    }

    public void sendMessage(String message) {
        out.println(message); 
    }

    public String getPlayerName() {
        return playerName; 
    }

    public int getScore() {
        return score; 
    }

    public void incrementScore(int increment) {
        score += increment; 
    }

    @Override
    public void run() {
        try {
            playerName = in.readLine(); 
            server.broadcast("Player " + playerName + " has joined the game!");
            server.log("Player " + playerName + " has connected.");
            server.broadcastConnectedPlayers(); 

            String message;
            while ((message = in.readLine()) != null) { 
                if (message.startsWith("Player ")) { 
                    String[] parts = message.split(":");
                    if (parts.length > 1) {
                        String guess = parts[1].trim();
                        server.processGuess(playerName, guess); 
                    }
                } else  if (message.equals("PLAY")) {
                        server.processPlayRequest(this); // Handle the PLAY request
                }else {
                    server.broadcast(playerName + ": " + message); 
                }
            }
        } catch (IOException e) {
            server.log("Connection with " + playerName + " lost.");
        } finally {
            server.removePlayer(this); 
            try {
                socket.close(); // Close the socket
            } catch (IOException e) {
                server.log("Error closing socket: " + e.getMessage());
            }
        }
    }
}

