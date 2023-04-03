/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Shared;

import java.io.Serializable;

/**
 *
 * @author logan
 */
public class GameState implements Serializable {
    public static final int PLAYER_WIDTH = 15, PLAYER_HEIGHT = 50, BALL_WIDTH = 10, PLAYER_OFFSET = 20;
    public static final int WINDOW_WIDTH = 400, WINDOW_HEIGHT = 200;
    public float player1_y;
    public float player2_y;
    
    public float ball_x;
    public float ball_y;
    
    public GameState(float p1_y, float p2_y, float b_x, float b_y){
        player1_y = p1_y;
        player2_y = p2_y;
        ball_x = b_x;
        ball_y = b_y;
    }
}
