/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

import Shared.ClientMessage;
import Shared.GameState;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashSet;
import javax.swing.JPanel;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.InputStreamReader;
import static java.lang.Thread.sleep;

/**
 *
 * @author logan
 */
public class PongPanel extends JPanel {

    private final int WIDTH = GameState.WINDOW_WIDTH, HEIGHT = GameState.WINDOW_HEIGHT;
    private GameState gameState;
    private Socket socket;

    private ObjectInputStream input;
    private ObjectOutputStream output;

    public PongPanel(String serverHost, int port) {
        try {
            System.out.println("Setting Up Game...");
            this.socket = new Socket(serverHost, port);

            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());

            setPreferredSize(new Dimension(WIDTH, HEIGHT));
            setBackground(Color.black);
            int playerStart_y = HEIGHT / 2 - GameState.PLAYER_HEIGHT / 2;
            setState(new GameState(playerStart_y, playerStart_y, WIDTH / 2, HEIGHT / 2));
            Game game = new Game();
            System.out.println("Game Setup Complete!");
            String response;
            while(true){
                try {
                    response = (String) input.readObject();
                    if (response.startsWith("WELCOME")) {
                        System.out.println("Server Message: " + response);
                    }

                    if (response.startsWith("PLAYERS CONNECTED")) {
                        System.out.println("Server Message: " + response);
                        game.start();
                        break;
                    }
                } catch (EOFException e) {
                    sleep(60);
                } 
            }
            
            System.out.println("Game Ended!");
        } catch (IOException e) {
            System.out.println("IOException! : " + e);
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    public void setState(GameState gameState) {
        this.gameState = gameState;
        repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (gameState == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.white);

        //Left Player
        Shape leftPlayer = new Rectangle.Float(GameState.PLAYER_OFFSET,
                gameState.player1_y, GameState.PLAYER_WIDTH, GameState.PLAYER_HEIGHT);
        //Right Player
        Shape rightPlayer = new Rectangle.Float(WIDTH - (GameState.PLAYER_OFFSET + GameState.PLAYER_WIDTH),
                gameState.player2_y, GameState.PLAYER_WIDTH, GameState.PLAYER_HEIGHT);
        //Ball
        //g.drawOval((int) gameState.ball_x, (int) gameState.ball_y,
        //        GameState.BALL_WIDTH , GameState.BALL_WIDTH);
        Shape ball = new Ellipse2D.Float(gameState.ball_x, gameState.ball_y,
                GameState.BALL_WIDTH, GameState.BALL_WIDTH);

        g2.draw(leftPlayer);
        g2.draw(rightPlayer);
        g2.draw(ball);
    }

    class Game extends Thread {

        @Override
        public void run() {
            String response, opponent;
            try {


                while (true) {
                    try {
                        setState((GameState) input.readObject());
                        ClientMessage playerMove = new ClientMessage(null);
                        output.writeObject(playerMove);
                    } catch (Exception e) {
                        System.out.println(e);
                        e.printStackTrace();
                        sleep(500);
                    }

                }
            } catch(Exception e){
                
            }

        }
    }
}
