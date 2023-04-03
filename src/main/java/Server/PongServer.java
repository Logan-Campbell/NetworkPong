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
                Game.Player player1 = game.new Player(sock.accept(), "Player 1");
                Game.Player player2 = game.new Player(sock.accept(), "Player 2");
                player1.setOpponent(player2);
                player2.setOpponent(player1);
                player1.start();
                player2.start();
                sleep(1000);
            }
        } finally {
            sock.close();
        }
    }
}


class Game {
    
    private GameState gameState;
            
            
    public Game() {
        int playerStart_y = GameState.WINDOW_HEIGHT/2 - GameState.PLAYER_HEIGHT/2;
        gameState = new GameState(playerStart_y,playerStart_y, 
                GameState.WINDOW_WIDTH/2,GameState.WINDOW_HEIGHT/2);
    }
    
    public synchronized void updateGameState(GameState gameState){
        this.gameState = gameState;
    }
    class Player extends Thread {
        
        Socket socket;
        Player opponent;
        ObjectInputStream input;
        ObjectOutputStream output;
        String name;
        
        public Player(Socket socket, String name){
            this.socket = socket;
            this.name = name;
            try {
                output = new ObjectOutputStream(socket.getOutputStream());
                input = new ObjectInputStream(socket.getInputStream());
                System.out.println("Player: " + name + " Connected!");  
                output.writeObject("WELCOME " + name);
            } catch (Exception e){
                System.out.println(e);
            } finally {
                try{
                    input.close();
                    output.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }
        
        @Override
        public void run() {
            try{
                output.writeObject("PLAYERS CONNECTED");

                while(true){
                    output.writeObject(gameState);
                    handlePlayerMove((ClientMessage) input.readObject());
                }
                
            } catch (IOException e) {
                System.out.println("Player died: " + e);
                e.printStackTrace();
            }catch (ClassNotFoundException e) {
                System.out.println("Class not found: " + e);
            }catch(Exception e){
                System.out.println(e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
        
        public void setOpponent(Player opponent){
            this.opponent = opponent;
        }
        
        public void handlePlayerMove(ClientMessage message){
            Random rand = new Random();
            float move_x = rand.nextFloat(gameState.ball_x, 3.0f);
            float move_y = rand.nextFloat(gameState.ball_y, 3.0f);
            if(rand.nextBoolean())
                move_x *= -1;
            if(rand.nextBoolean())
                move_y *= -1;
            
            gameState.ball_x += move_x;
            gameState.ball_y += move_y;
        }
    }
}