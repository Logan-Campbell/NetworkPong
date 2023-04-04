package Shared;

import java.awt.event.KeyEvent;
import java.io.Serializable;

/**
 *
 * @author logan
 */
public class ClientMessage implements Serializable {
    public int playerMove;
    
    public ClientMessage(KeyEvent playerMove){
        this.playerMove = playerMove.getKeyCode();
    }
}
