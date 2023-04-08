/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import Shared.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.Thread.sleep;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;


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
                game.addPlayers(player1, player2);
                game.start();
                sleep(1000);
            }
        } finally {
            sock.close();
        }
    }
}


class Game extends Thread{
    private final int COLLISION_DELAY = 10;
    
    private Player player1, player2;
    private boolean isGameStarted = false;
    private boolean isConnected = false;
    
    private GameState gameState;
    private float move_x = GameState.BALL_MOVE_SPEED_X, move_y = GameState.BALL_MOVE_SPEED_Y;
    private float player_move_y = 6.0f;
    
    
    public Game() {
        int playerStart_y = GameState.WINDOW_HEIGHT/2 - GameState.PLAYER_HEIGHT/2;
        gameState = new GameState(playerStart_y,playerStart_y, 
                GameState.WINDOW_WIDTH/2,GameState.WINDOW_HEIGHT/2);
    }
    
    
    @Override
    public void run() {
        if(player1 == null || player2 == null){
            System.out.println("Players not connected!");
            return;
        }
        
        startBall();
        this.isGameStarted = true;
        this.isConnected = true;
        boolean hasCollided = false;
        
        player1.setOpponent(player2);
        player2.setOpponent(player1);
        player1.start();
        player2.start();
        
        int collisionDelay = this.COLLISION_DELAY;
        
        while(isGameStarted && isConnected){
            hasCollided = moveBall(hasCollided);
            
            if(hasCollided){
                collisionDelay--;
            }
            
            if(collisionDelay <= 0){
                hasCollided = false;
                collisionDelay = this.COLLISION_DELAY;
            }
            
            try {
                Thread.sleep(GameState.TICK_RATE);
            } catch (InterruptedException ex) {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void addPlayers(Player player1, Player player2){
        this.player1 = player1;
        this.player2 = player2;
    }
    
    public synchronized void updateGameState(GameState gameState) {
        this.gameState = gameState;

    }

    //Move the ball, changes direction on wall and paddle collsion.
    //Updates score when ball reaches left or right walls.
    public synchronized boolean moveBall(boolean hasCollided) {
        if (gameState.ball_x <= 0){
            gameState.player2_score++;
            startBall(true);
            System.out.println("Player 2 Scored! The score is now: " + gameState.player1_score + " - " + gameState.player2_score);
            return hasCollided;
        }
        
        if(gameState.ball_x >= GameState.WINDOW_WIDTH - GameState.BALL_WIDTH) {
            gameState.player1_score++;
            startBall(false);
            System.out.println("Player 1 Scored! The score is now: " + gameState.player1_score + " - " + gameState.player2_score);
            return hasCollided;
        }

        //Ball hits top or bottom
        if (gameState.ball_y <= 0 || gameState.ball_y
                >= GameState.WINDOW_HEIGHT - GameState.BALL_WIDTH) {
            move_y = move_y * -1;
        }
        
        //Ball hits left paddle
        //Left side of board AND
        //Less than right side of paddle x AND
        //Less than top of paddle AND
        //Greator than bottom of paddle
        if(!hasCollided && gameState.ball_center_x() - GameState.BALL_WIDTH < GameState.WINDOW_WIDTH/2 && 
                gameState.ball_center_x() - GameState.BALL_WIDTH <= GameState.player1_x() + GameState.PLAYER_WIDTH &&
                gameState.ball_center_y() >= gameState.player1_y && 
                gameState.ball_center_y() <= gameState.player1_y + GameState.PLAYER_HEIGHT){
            move_x = move_x * -1.0f;
            move_y = ballMagnitude(gameState.player1_y, gameState.ball_center_y());
            hasCollided = true;
        }

        //Ball hits right paddle
        //Right side of board AND
        //Less than left side of paddle x AND
        //Less than top of paddle AND
        //Greator than bottom of paddle
        if(!hasCollided && gameState.ball_center_x() + GameState.BALL_WIDTH> GameState.WINDOW_WIDTH/2 && 
                gameState.ball_center_x() + GameState.BALL_WIDTH >= GameState.player2_x() - GameState.PLAYER_WIDTH &&
                gameState.ball_center_y() >= gameState.player2_y && 
                gameState.ball_center_y() <= gameState.player2_y + GameState.PLAYER_HEIGHT){
            move_x = move_x * -1.0f;
            move_y = ballMagnitude(gameState.player2_y, gameState.ball_center_y());
            hasCollided = true;
        }
        
        gameState.ball_x += move_x;
        gameState.ball_y += move_y;
        
        return hasCollided;
    }
    
    private float ballMagnitude(float paddle_y, float ball_y){
        float paddleCenter = paddle_y + GameState.PLAYER_HEIGHT/2.0f;
        float dist = paddleCenter - ball_y;
        if(dist < 5.0f && dist > -5.0f)
            return 0.0f;
        float xMax = GameState.PLAYER_HEIGHT/2.0f;
        float normalize = (dist)/(xMax);
        System.out.println("Normalized: " + normalize*2 + " dist: " + dist);
        return normalize*2;
    }
    
    private void startBall(){
        Random rand = new Random();
        startBall(rand.nextBoolean());
    }
    
    private synchronized void startBall(boolean isDirectionRight){
        GameState reset = new GameState(gameState.player1_y,gameState.player2_y, 
                GameState.WINDOW_WIDTH/2, GameState.WINDOW_HEIGHT/2, 
                gameState.player1_score, gameState.player2_score);
        Random rand = new Random();
        move_x = isDirectionRight ? GameState.BALL_MOVE_SPEED_X : GameState.BALL_MOVE_SPEED_X*-1;
        move_y = rand.nextBoolean() ? GameState.BALL_MOVE_SPEED_Y : GameState.BALL_MOVE_SPEED_Y*-1;
        
        updateGameState(reset);
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
                            System.out.println("Writing to " + name + " Failed.");
                            cancel();
                        }
                    }
                }, 0, GameState.TICK_RATE);

                while (true) {
                    handlePlayerMove((ClientMessage) input.readObject());
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
                isConnected = false;
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
            if(code == GameState.UP_KEY && player_y > 0 ){
                return player_move_y * -1.0f;
            }
                
            if(code == GameState.DOWN_KEY  && player_y <= GameState.WINDOW_HEIGHT-GameState.PLAYER_HEIGHT){
                return player_move_y;
            }
            
            return 0.0f;
        }
    }
}