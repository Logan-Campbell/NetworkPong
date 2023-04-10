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
import java.awt.event.WindowAdapter;
import java.io.EOFException;
import static java.lang.Thread.sleep;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
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
    private JLabel messageLabel;
    private JButton startButton;
    
    private boolean isGameRunning = false;
    
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
            messageLabel = new JLabel("", JLabel.LEFT);
            p1ScoreLabel.setForeground(Color.WHITE);
            p2ScoreLabel.setForeground(Color.WHITE);
            messageLabel.setForeground(Color.WHITE);
            
            startButton = new JButton("Start Game");
            startButton.addActionListener((ActionEvent e) -> {
                System.out.println("Game started by player.");
                isGameRunning = true;
                startButton.setVisible(false);
                messageLabel.setText("Waiting on other player...");
                Game game = new Game(this);
                game.start(); 
                
            });
            
            startButton.setVisible(false);
            
            add(startButton);
            add(p1ScoreLabel);
            add(p2ScoreLabel);
            add(messageLabel);
       
            int playerStart_y = HEIGHT / 2 - GameState.PLAYER_HEIGHT / 2;
            setState(new GameState(playerStart_y, playerStart_y, WIDTH / 2, HEIGHT / 2));
            System.out.println("Game Setup Complete!");

            String response;           
            while (true) {
                try {
                    response = (String) input.readObject();
                    if (response.startsWith("WELCOME")) {
                        System.out.println("Server Message: " + response);
                        messageLabel.setText("Welcome" +  response.substring("WELCOME".length()) + "!");
                    }

                    if (response.startsWith("PLAYERS CONNECTED")) {
                        System.out.println("Server Message: " + response);
                        startButton.setText("Start Game");
                        startButton.setVisible(true);
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

    public void close() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    
    class Game extends Thread {
        JPanel panel;
        //Player Input
        HashSet<Integer> pressedKeys = new HashSet<Integer>();
        
        public Game(JPanel panel){
            this.panel = panel;
            
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
        }
        
        @Override
        public void run(){
            
            Timer keyInputTimer = null;
            try {
                output.writeObject("START GAME");
                output.reset();

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
                //Game Loop
                //Read GameState from the server and update the panel.
                while (!Thread.interrupted()) {
                    Object serverMessage = input.readObject();
                    if(serverMessage instanceof String){
                        String line = (String) serverMessage;
                        System.out.println("Recieved Server Message: " + line);
                        if(line.startsWith("GAME END")){
                            System.out.println(line);
                            isGameRunning = false;
                            messageLabel.setText(line.substring("GAME END".length()) + " Wins!");
                        }
                        if(line.equals("RESTART")){
                            startButton.setVisible(true);
                            startButton.setText("Restart?");
                            break;
                        }
                        continue;
                    }
                    
                    if(!isGameRunning)
                        continue;
                    
                    messageLabel.setText("");
                    setState((GameState) serverMessage);
                }
                
            } catch (Exception e) {
                Logger.getLogger(PongPanel.class.getName()).log(Level.SEVERE, null, e);
            } finally{
                if(keyInputTimer != null){
                    keyInputTimer.stop();
                }
                System.out.println("Game Ended!");
            }
        }
    }
}
