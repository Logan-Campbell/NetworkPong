/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Shared;

import java.awt.event.KeyEvent;
import java.io.Serializable;

/**
 *
 * @author logan
 */
public class GameState implements Serializable {
    public static final int PLAYER_WIDTH = 2, PLAYER_HEIGHT = 50, BALL_WIDTH = 10, PLAYER_OFFSET = 20;
    public static final int WINDOW_WIDTH = 400, WINDOW_HEIGHT = 200, TICK_RATE=16; //TICK_RATE= 16ms
    public static final int UP_KEY = KeyEvent.VK_UP, DOWN_KEY = KeyEvent.VK_DOWN;
    public static final float BALL_MOVE_SPEED_X = 3.0f, BALL_MOVE_SPEED_Y = 2.0f;
    public int player1_score;
    public int player2_score;
    
    public float player1_y;
    public float player2_y;
    
    public float ball_x;
    public float ball_y;
    
    public GameState(float p1_y, float p2_y, float b_x, float b_y){
        player1_y = p1_y;
        player2_y = p2_y;
        ball_x = b_x;
        ball_y = b_y;
        player1_score = 0;
        player2_score = 0;
    }
    
    public GameState(float p1_y, float p2_y, float b_x, float b_y, int p1_score, int p2_score){
        player1_y = p1_y;
        player2_y = p2_y;
        ball_x = b_x;
        ball_y = b_y;
        player1_score = p1_score;
        player2_score = p2_score;
    }
    
    public static float player1_x(){
        return PLAYER_OFFSET;
    }
    
    public static float player2_x(){
        return WINDOW_WIDTH - (PLAYER_OFFSET + PLAYER_WIDTH);
    }
    
    public float ball_center_x(){
        return ball_x + BALL_WIDTH/2.0f;
    }
    
    public float ball_center_y(){
        return ball_y + BALL_WIDTH/2.0f;
    }
}
