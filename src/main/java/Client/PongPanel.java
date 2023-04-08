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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javax.swing.JPanel;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.EOFException;
import static java.lang.Thread.sleep;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

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

    private JLabel p1ScoreLabel;
    private JLabel p2ScoreLabel;
    
    public PongPanel(String serverHost, int port) {
        try {
            System.out.println("Setting Up Game...");
            this.socket = new Socket(serverHost, port);

            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());

            setPreferredSize(new Dimension(WIDTH, HEIGHT));
            setBackground(Color.black);

            p1ScoreLabel = new JLabel("0", JLabel.RIGHT);
            p2ScoreLabel = new JLabel("0", JLabel.LEFT);
            p1ScoreLabel.setForeground(Color.WHITE);
            p2ScoreLabel.setForeground(Color.WHITE);
            add(p1ScoreLabel);
            add(p2ScoreLabel);
            
            int playerStart_y = HEIGHT / 2 - GameState.PLAYER_HEIGHT / 2;
            setState(new GameState(playerStart_y, playerStart_y, WIDTH / 2, HEIGHT / 2));
            System.out.println("Game Setup Complete!");

            String response;
            Game game = new Game(this);
            while (true) {
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
                    sleep(GameState.TICK_RATE);
                }
                System.out.println("loop...");
            }

            
        } catch (IOException e) {
            System.out.println("IOException! : " + e);
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
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

        p1ScoreLabel.setText(String.valueOf(gameState.player1_score));
        p2ScoreLabel.setText(String.valueOf(gameState.player2_score));
        //Left Player
        Shape leftPlayer = new Rectangle.Float(GameState.player1_x(),
                gameState.player1_y, GameState.PLAYER_WIDTH, GameState.PLAYER_HEIGHT);
        //Right Player
        Shape rightPlayer = new Rectangle.Float(GameState.player2_x(),
                gameState.player2_y, GameState.PLAYER_WIDTH, GameState.PLAYER_HEIGHT);
        //Ball
        Shape ball = new Ellipse2D.Float(gameState.ball_x, gameState.ball_y,
                GameState.BALL_WIDTH, GameState.BALL_WIDTH);
        

        g2.draw(leftPlayer);
        g2.draw(rightPlayer);
        g2.draw(ball);
    }

    class Game extends Thread {
        JPanel panel;
        
        public Game(JPanel panel){
            this.panel = panel;
        }
        
        @Override
        public void run(){
            Timer keyInputTimer = null;
            try {
                System.out.println("Game started!");
                //panel.addKeyListener(new KeyEventHandler());
                
                
                //Player Input
                HashSet<Integer> pressedKeys = new HashSet<Integer>();
                
                keyInputTimer = new Timer(GameState.TICK_RATE, 
                        new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                for(Integer key : pressedKeys){
                                    try {
                                        output.writeObject(new ClientMessage(key));
                                        output.reset();
                                    } catch (IOException ex) {
                                        Logger.getLogger(PongPanel.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                                
                            }
                        });
                keyInputTimer.start();
                
                panel.getInputMap().put(KeyStroke.getKeyStroke("UP"),
                        "up_pressed");
                panel.getInputMap().put(KeyStroke.getKeyStroke("released UP"),
                        "up_released");
                panel.getInputMap().put(KeyStroke.getKeyStroke("DOWN"),
                        "down_pressed");
                panel.getInputMap().put(KeyStroke.getKeyStroke("released DOWN"),
                        "down_released");
                panel.getActionMap().put("up_pressed",
                        new AbstractAction() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                pressedKeys.add(GameState.UP_KEY);
                            }
                });
                panel.getActionMap().put("up_released",
                         new AbstractAction() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                pressedKeys.remove(GameState.UP_KEY);
                            }
                });
                
                panel.getActionMap().put("down_pressed",
                        new AbstractAction() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                pressedKeys.add(GameState.DOWN_KEY);
                            }
                });
                panel.getActionMap().put("down_released",
                         new AbstractAction() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                pressedKeys.remove(GameState.DOWN_KEY);
                            }
                });

                panel.setFocusable(true);
                panel.requestFocusInWindow();

                //Read GameState from the server and update the panel.
                while (true) {
                    setState((GameState) input.readObject());
                }
            } catch (Exception e) {

            } finally{
                try {
                    if (socket != null) {
                        socket.close();
                    }
                    if(keyInputTimer != null){
                        keyInputTimer.stop();
                    }
                } catch (IOException e) {
                    System.out.println(e);
                }
                
                System.out.println("Game Ended!");
            }
        }
    }
}
