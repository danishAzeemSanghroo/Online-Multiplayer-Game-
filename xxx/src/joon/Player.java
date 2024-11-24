package joon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class Player {
    private JFrame connectFrame;
    private JFrame lobbyFrame;
    private JFrame gameFrame;
    private JTextField nameField;
    private JTextField playerListTextField;
    private JTextArea playerListTextArea;
    private JTextArea gameTextArea; // To display game state and other players' guesses
    private JTextField guessField;
    private JLabel scoreLabel;
    private JLabel timerLabel;
    private JButton submitButton;
    private JButton leaveButton;
    private PrintWriter out;
    private BufferedReader in;
    private String playerName;
    private int score = 0;
    private Socket socket;
    private boolean gameStarted = false;
    private Set<String> uniquePlayers = new HashSet<>();

    // Constructor
    public Player() {
        // Connect Frame Initialization
        connectFrame = new JFrame("🎮🕹️ LET'S FUN 👾🖥️🖱");
        connectFrame.setSize(1000, 1000);
        connectFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set background image for connectFrame
        ImagePanel connectPanel = new ImagePanel("connect_background.jpg"); 
        connectPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 0, 20, 0);

        JLabel welcome = new JLabel("<html><center>🎮🕹️👾🖥️🖱<br> 「 ✦ Enter Your 𝐍𝐚𝐦𝐞 ✦ 」:</center></html>");
        welcome.setForeground(new Color(0, 0, 139));
        welcome.setFont(new Font("Dialog", Font.BOLD, 30));
        gbc.gridx = 0;
        gbc.gridy = 0;
        connectPanel.add(welcome, gbc);

        nameField = new JTextField(15);
        nameField.setFont(new Font("Arial", Font.PLAIN, 20));
        gbc.gridy = 1;
        connectPanel.add(nameField, gbc);

        JButton connectButton = new JButton("PRESS TO ENTER");
        connectButton.setBackground(new Color(0, 70, 80));
        connectButton.setForeground(Color.white);
        connectButton.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridy = 2;
        connectPanel.add(connectButton, gbc);

        connectFrame.add(connectPanel);
        connectFrame.setVisible(true);

        connectButton.addActionListener(e -> connectToServer());
        

        // Lobby Frame Initialization
        lobbyFrame = new JFrame("🗡️ WELCOME TO THE GAME LOBBY 🗡️");
        lobbyFrame.setSize(1000, 1000);
        lobbyFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set background image for lobbyFrame
        ImagePanel lobbyPanel = new ImagePanel("lobby.jpg"); // Ensure "lobby_background.jpg" is in the correct location
        lobbyPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(10, 10, 10, 10);

        // Title label with similar styling as connectFrame
        JLabel lobbyTitle = new JLabel("<html><center>🗡️ GAME LOBBY 🗡️<br> Players Connected:</center></html>");
        lobbyTitle.setForeground(new Color(0, 0, 139));
        lobbyTitle.setFont(new Font("Dialog", Font.BOLD, 24));
        gbc2.gridx = 0;
        gbc2.gridy = 0;
        lobbyPanel.add(lobbyTitle, gbc2);

        playerListTextArea = new JTextArea(10, 40);
        playerListTextArea.setEditable(false);
        playerListTextArea.setFont(new Font("Monospaced", Font.PLAIN, 16));
        playerListTextArea.setBackground(Color.WHITE);  // Semi-transparent background
        playerListTextArea.setForeground(Color.BLACK);

        // Display the connected players in the JTextField, separating names by commas
//        String playerNames = String.join(", ", nameField.getName()); // Assume getConnectedPlayerNames() is available
//        playerListTextArea.setText(playerNames);

        JScrollPane playerListScrollPane = new JScrollPane(playerListTextArea);
        playerListScrollPane.setOpaque(false);
        playerListScrollPane.getViewport().setOpaque(false);

        gbc2.gridy = 1;
        gbc2.gridwidth = 3;
        lobbyPanel.add(playerListScrollPane, gbc2);

        // Play button with similar styling
        JButton playButton = new JButton("START GAME");
        playButton.setBackground(new Color(0, 70, 80));
        playButton.setForeground(Color.white);
        playButton.setFont(new Font("Arial", Font.BOLD, 18));
        gbc2.gridy = 2;
        lobbyPanel.add(playButton, gbc2);
        playButton.addActionListener(e -> {
            if (out != null) {
                out.println("PLAY");
               // showWaitingRoomFrame(); // Show the waiting room frame after sending the play request
            }
        });

        // Add lobbyPanel with background image to the frame
        lobbyFrame.add(lobbyPanel);
        lobbyFrame.setVisible(false);

        
        


        // Game Frame Initialization
        gameFrame = new JFrame("💪 GAMES ARENA 💪");
        gameFrame.setSize(1000, 1000);
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set background image for gameFrame
        ImagePanel gamePanel = new ImagePanel("lobby.jpg"); // Replace with your image file
        gamePanel.setLayout(new GridBagLayout());

        gameTextArea = new JTextArea(10, 40);
        gameTextArea.setEditable(false);
        gameTextArea.setFont(new Font("Monospaced", Font.PLAIN, 16));
        gameTextArea.setBackground(Color.WHITE);
        gameTextArea.setForeground(Color.BLACK);
        JScrollPane gameScrollPane = new JScrollPane(gameTextArea);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gamePanel.add(gameScrollPane, gbc);

        timerLabel = new JLabel("Time left - 60 seconds");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timerLabel.setForeground(Color.white);
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gamePanel.add(timerLabel, gbc);

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        scoreLabel.setForeground(Color.white);
        gbc.gridx = 2;
        gbc.gridy = 1;
        gamePanel.add(scoreLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gamePanel.add(new JLabel("Your Guess:"), gbc);

        guessField = new JTextField(10);
        guessField.setFont(new Font("Arial", Font.PLAIN, 18));
        gbc.gridx = 1;
        gbc.gridy = 2;
        gamePanel.add(guessField, gbc);

        submitButton = new JButton("📥SUBMIT📥");
        submitButton.setBackground(Color.WHITE);
        submitButton.setForeground(Color.black);
        submitButton.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gamePanel.add(submitButton, gbc);

        leaveButton = new JButton("╰┈➤EXIT➡🚪");
        leaveButton.setBackground(Color.WHITE);
        leaveButton.setForeground(Color.black);
        leaveButton.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 2;
        gbc.gridy = 3;
        gamePanel.add(leaveButton, gbc);

        gameFrame.add(gamePanel);

        submitButton.addActionListener(e -> submitGuess());
        leaveButton.addActionListener(e -> leaveGame());

        connectFrame.setVisible(true);
        
    }

    // The remaining methods (connectToServer, listenToServer, updatePlayerList, startGame, submitGuess, and leaveGame) remain unchanged.


    private void connectToServer() {
        playerName = nameField.getText();
        if (playerName.isEmpty()) {
            JOptionPane.showMessageDialog(connectFrame, "‎‧₊˚✧[TYPE YOUR NAME HERE]✧˚₊‧");
            return;
        }

        try {
            socket = new Socket("localhost", 12345); // Connect to server
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(playerName);

            new Thread(this::listenToServer).start();

            connectFrame.setVisible(false);
            lobbyFrame.setVisible(true);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(connectFrame, "Connection failed.");
        }
    }

    private void listenToServer() {
        String message;
        try {
            while ((message = in.readLine()) != null) {
                if (message.startsWith("Players Online:")) {
                    String playerList = message.substring(16); // Skip "PLAYERJOINED "
                updatePlayerList(playerList.replace(",", "\n")); // Replace commas with newlines for display
                //connectToLobbyFrame();
                    
                } else if (message.startsWith("Game Starting: ")) {
                    startGame(message); 
                } else if (message.startsWith("Server: Time left")) {
                    timerLabel.setText(message.substring(8)); 
                } else if (message.startsWith("Server: Game over")) {
                    gameTextArea.append(message.substring(8) + "\n"); 
                } else if (message.startsWith("Waiting")) {
                   JOptionPane.showMessageDialog(lobbyFrame, message);
                }else {
                    gameTextArea.append(message + "\n"); 
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

// private void updatePlayerList(String playerNames) { 
//    String currentText = playerListTextArea.getText();
//    playerListTextArea.append(playerNames + "\n");
//}
// 
private void updatePlayerList(String playerName) { 
    playerListTextArea.setText("");
    if (!uniquePlayers.contains(playerName)) { // Adds only if the player name is unique
        playerListTextArea.append(playerName + "\n");
    }
} 

    private void startGame(String message) {
        if (!gameStarted) {
            gameStarted = true;
            String[] players = message.substring(13).split(",");
            if (players.length == 1) {
                gameTextArea.append("👀⏳🥱 WAITING FOR ANOTHER PLAYER TO JOIN👀⏳🥱\n");
            } else {
                lobbyFrame.setVisible(true);
                gameFrame.setVisible(true);
                for (String player : players) {
                    gameTextArea.append(player + ":🏅 Score - 0\n");
                }
            }
        }
    }

    private void submitGuess() {
        String guess = guessField.getText().toLowerCase();
        if (!guess.isEmpty()) {
            out.println("Player " + playerName + ": " + guess);
            guessField.setText("");
        }
    }

    private void leaveGame() {
        out.println("Player " + playerName + " has left the game ╰┈➤➡🚪.");
        System.exit(0);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        EventQueue.invokeLater(() -> new Player());
    }
}
