
package joon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;

public class GameServer {
    private ServerSocket serverSocket;
    private List<PlayerHandler> players = new ArrayList<>();
    private Set<String> pastClients = new HashSet<>();
    private JTextArea serverLog;
    private int currentWordIndex = 0;
    private List<String> scrambledWords;
    private List<String> correctWords;
    private Timer quizTimer;
    private boolean gameStarted = false;

    public GameServer(int port, int maxPlayers, JTextArea serverLog) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.players = new ArrayList<>();
        this.serverLog = serverLog;

        scrambledWords = Arrays.asList("atsfc", "erenv", "cmaare", "egnrao", "llnoem");
        correctWords = Arrays.asList("facts", "never", "camera", "orange", "lemon");

        startQuizTimer(); // Start the quiz timer
    }

    public void start() {
        log("Game server started...");
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                if (players.size() < 10) {
                    PlayerHandler playerHandler = new PlayerHandler(clientSocket, this);
                    players.add(playerHandler);
                    new Thread(playerHandler).start();
//                    if (players.size() >= 2 && !gameStarted) {
//                        startGameForPlayers(); // Start the game for all players if there are at least two players
//                    }
                } else {
                    clientSocket.close();
                    log("Connection rejected: maximum players reached.");
                }
            } catch (IOException e) {
                log("Error: " + e.getMessage());
            }
        }
    }

public synchronized void processPlayRequest(PlayerHandler player) {
    

    if (players.size() >= 3) {
         startGameForPlayers(); // Start the game immediately if the room is full
         
    } else {
        player.sendMessage("Waiting for players");
    }
}     



//    private void startGame() {
//        gameStarted = true;
//        log("Game started.");
//        broadcastToWaitingRoom("GAMESTARTED");
//        currentQuestionIndex = 0;
//        scores.clear();
//        waitingRoom.forEach(player -> scores.put(player.getPlayerName(), 0));
//        askNextQuestion();
//    }
    
 private void startQuizTimer() {
       // 30-second timer
        quizTimer = new Timer(60000, e -> moveToNextWord());
        quizTimer.setRepeats(true);
        quizTimer.start();
}


private void moveToNextWord() {
    if (currentWordIndex < scrambledWords.size()) {
        broadcast("Server: Time left - " + (quizTimer.getInitialDelay() / 1000) + " seconds"); // Broadcast timer
        broadcast("Server: " + scrambledWords.get(currentWordIndex)); // Broadcast new word
        currentWordIndex++;
    } else {
        int maxScore = Integer.MIN_VALUE;
        String winner = "";
        for (PlayerHandler player : players) {
            if (player.getScore() > maxScore) {
                maxScore = player.getScore();
                winner = player.getPlayerName();
            }
        }
        // Check if there's only one player left
        if (players.size() == 1) {
            winner = players.get(0).getPlayerName();
            maxScore = players.get(0).getScore();
        }
        if(maxScore>0){
            broadcast("Server: Game over! Winner is " + winner + " with a score of " + maxScore);
        }else{
            broadcast("Server: Game over! There is no winner");
        }
        
        quizTimer.stop();
        gameStarted = false;
    }
}


    public void processGuess(String playerName, String guess) {
        String correctWord = correctWords.get(currentWordIndex - 1);
        if (guess.equalsIgnoreCase(correctWord)) {
            broadcast("Server: Player " + playerName + " answered correctly with '" + correctWord + "'");
            updateScore(playerName, 1); 
            moveToNextWord(); 
        } else {
            broadcast("Server: Player " + playerName + " guessed '" + guess + "', which is incorrect.");
        }
    }

    public void broadcast(String message) {
        for (PlayerHandler player : players) {
            player.sendMessage(message);
        }
    }

    public void broadcastConnectedPlayers() {
        Set<String> playerNames = getConnectedPlayerNames();
        String joinedNames = String.join(",", playerNames);
        broadcast("Players Online: " + joinedNames);
    }

    public Set<String> getConnectedPlayerNames() {
        Set<String> playerNames = new HashSet<>();
        for (PlayerHandler player : players) {
            playerNames.add(player.getPlayerName());
        }
        return playerNames;
    }

    private void updateScore(String playerName, int increment) {
        for (PlayerHandler player : players) {
            if (player.getPlayerName().equals(playerName)) {
                player.incrementScore(increment); 
                broadcast("Player " + playerName + " scores = " + player.getScore()); 
            }
        }
    }

    public void log(String message) {
        serverLog.append(message + "\n");
    }

    public void removePlayer(PlayerHandler player) {
        players.remove(player);
        broadcastConnectedPlayers(); 
        if (players.size() == 1 && gameStarted) {
            players.get(0).sendMessage("Opponent has withdrawn. There is no winner!");
            gameStarted = false;
        }
    }

    private void startGameForPlayers() {
        gameStarted = true;
        String playerNames = String.join(",", getConnectedPlayerNames());
        for (PlayerHandler player : players) {
            player.sendMessage("Game Starting: " + playerNames);
        }
        quizTimer.start(); 
    }

    public static void main(String[] args) {
       // Create a frame with a custom title
        JFrame serverFrame = new JFrame("🎮🕹️ MAIN SERVER 🎮🕹️");
        serverFrame.setSize(1000, 1000);
        serverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set a modern-looking background color and layout
        serverFrame.getContentPane().setBackground(new Color(64,224,208)); 
        serverFrame.setLayout(new BorderLayout());

        // Create a text area for logs with custom font and color
        JTextArea serverLog = new JTextArea();
        serverLog.setEditable(false);
        serverLog.setBackground(new Color(64,224,208));  
        serverLog.setForeground(new Color(128, 128, 128));  
        serverLog.setFont(new Font("Consolas", Font.PLAIN, 40));  // Monospaced font for logs

        // Add scroll pane for the text area
        JScrollPane scrollPane = new JScrollPane(serverLog);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(128, 128, 128), 2)); // Custom border

        // Add a custom title label with padding and styling
        JLabel titleLabel = new JLabel("🖥️CONNECTIONS🖥️", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 50));
        titleLabel.setForeground(Color.black);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add the components to the frame
        serverFrame.add(titleLabel, BorderLayout.NORTH);
        serverFrame.add(scrollPane, BorderLayout.CENTER);

        // Make the frame visible
        serverFrame.setVisible(true);

        // Start the server
        try {
            GameServer server = new GameServer(12345, 10, serverLog); // Server with a limit of 10 players
            server.start();
        } catch (IOException e) {
            serverLog.append("Error: " + e.getMessage() + "\n");
        }
    }
}

