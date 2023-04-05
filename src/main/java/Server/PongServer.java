/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import Shared.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import static java.lang.Thread.sleep;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


/**
 *
 * @author Logan Campbell
 */
public class PongServer {
    public static void main(String[] args) throws Exception {
        ServerSocket sock = new ServerSocket(8901);
        System.out.println("Pong server Started!");
        try {
            while(true){     
                Game game = new Game();
                Game.Player player1 = game.new Player(sock.accept(), 0, "Player 1");
                Game.Player player2 = game.new Player(sock.accept(), 1, "Player 2");
                player1.setOpponent(player2);
                player2.setOpponent(player1);
                player1.start();
                player2.start();
                game.startGame();
                sleep(1000);
            }
        } finally {
            sock.close();
        }
    }
}


class Game {
    
    private boolean isGameStarted = false;
    
    private GameState gameState;
    private int move_x = 2, move_y = 2;
    private float player_move_y = 6.0f;        
    public Game() {
        int playerStart_y = GameState.WINDOW_HEIGHT/2 - GameState.PLAYER_HEIGHT/2;
        gameState = new GameState(playerStart_y,playerStart_y, 
                GameState.WINDOW_WIDTH/2,GameState.WINDOW_HEIGHT/2);
    }
    
    public void startGame() {
        this.isGameStarted = true;
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                moveBall();
            }
        }, 0, GameState.TICK_RATE);
    }

    public synchronized void updateGameState(GameState gameState) {
        this.gameState = gameState;

    }

    public synchronized void moveBall() {
        if (gameState.ball_x <= 0 || gameState.ball_x
                >= GameState.WINDOW_WIDTH - GameState.BALL_WIDTH) {
            move_x = move_x * -1;
        }

        if (gameState.ball_y <= 0 || gameState.ball_y
                >= GameState.WINDOW_HEIGHT - GameState.BALL_WIDTH) {
            move_y = move_y * -1;
        }

        gameState.ball_x += move_x;
        gameState.ball_y += move_y;
    }
    
    class Player extends Thread {
        
        Socket socket;
        Player opponent;
        ObjectInputStream input;
        ObjectOutputStream output;
        String name;
        int player;
        
        public Player(Socket socket, int player, String name){
            this.socket = socket;
            this.name = name;
            this.player = player;
            try {
                output = new ObjectOutputStream(socket.getOutputStream());
                input = new ObjectInputStream(socket.getInputStream());
                System.out.println("Player: " + name + " Connected!");  
                output.writeObject("WELCOME " + name);
                output.reset();
            } catch (Exception e){
                System.out.println(e);
            }
        }
        
        @Override
        public void run() {
            try {
                System.out.println("Sending start message to player " + name);
                synchronized (output) {
                   output.writeObject("PLAYERS CONNECTED");
                   output.reset(); 
                }
                
                System.out.println("Message sent to player " + name);

                
                Timer timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            synchronized (output) {
                                output.writeObject(gameState);
                                output.reset();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, 0, GameState.TICK_RATE);

                while (true) {
                    //output.writeObject(gameState);
                    handlePlayerMove((ClientMessage) input.readObject());
                    //handlePlayerMove(null);
                    //System.out.println("Handling Input...");
                    sleep(GameState.TICK_RATE);
                }

            } catch (IOException e) {
                System.out.println("Player died: " + e);
                e.printStackTrace();
            }catch (ClassNotFoundException e) {
                System.out.println("Class not found: " + e);
            //}catch(Exception e){
            //    System.out.println(e);
            }catch(InterruptedException e){
                System.out.println(e);
            } finally {
                try {
                    socket.close();
                    input.close();
                    output.close();
                } catch (IOException e) {
                }
            }
        }
        
        public void setOpponent(Player opponent){
            this.opponent = opponent;
        }
        
        public synchronized void handlePlayerMove(ClientMessage message){
            if(player == 0){
                gameState.player1_y += movePlayer(gameState.player1_y, message.playerMove);
            }else{
                gameState.player2_y += movePlayer(gameState.player2_y, message.playerMove);
            }
        }
        
        public float movePlayer(float player_y, int code){
            if(code == 38 && player_y > 0 ){
                return player_move_y * -1.0f;
            }
                
            if(code == 40 && player_y <= GameState.WINDOW_HEIGHT-GameState.PLAYER_HEIGHT){
                return player_move_y;
            }
            
            return 0.0f;
        }
    }
}