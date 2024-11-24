# Online-Multiplayer-Game-

Description
This project is a Java-based online multiplayer gaming system built using networking concepts and graphical user interfaces (GUIs). It allows players to connect to a server, interact in real time, and play a game involving scrambled word challenges. Players can join or leave the game dynamically, and their actions are reflected in real time to other players.

The project explores the use of:

* Java Sockets API for communication over a network.
* Swing for GUI design and event-based programming.
* Multithreading for handling concurrent player connections.

**Features**

The system consists of two main components:

1. Server: Handles connections, player management, and game state.
2. Client: Provides the player interface and communicates with the server.

**Core Functionalities**
1. Connect:
Players register their username and connect to the server.
The server displays a list of connected players.

3. Pair Request:
Players signal their readiness to play by pressing a "Play" button.
The server groups players into a game room.

5. Player Joined:
Players joining the game are announced to others in the same game room.

7. Game Started:
The server starts the game when enough players are present or after a 30-second timer.
Players see the current game state and other players' scores.

9. Player Leave Request:
A player can leave the game or disconnect, and their name is removed from the player list.

11. Game Ended:
The server announces the winner or declares no winner if the game timer ends without a winner.

**Technologies Used**
1. Java Networking:
  Sockets for TCP-based communication.
2. Swing:
  GUI toolkit for Java, providing an interactive user interface.
3. Threads:
  For managing multiple simultaneous connections.
4. Collections Framework:
  For maintaining player lists and game state.

**How to Run**

Prerequisites
  1.Java Development Kit (JDK) version 8 or later.
  2.A network setup where the server and clients can communicate.

**Steps to Run**

1. Clone the Repository:

bash
git clone https://github.com/your-username/multiplayer-online-game.git
cd multiplayer-online-game

2. Compile the Code:
bash
javac -d bin src/xxx/*.java

3. Start the Server: Run the server program:
bash
java -cp bin xxx.GameServer

4. Start Clients: Run the client program for each player:

bash
java -cp bin xxx.Player

5.Play the Game:

Connect as multiple players.
Follow on-screen instructions to join and play the game.
