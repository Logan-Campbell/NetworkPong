package Shared;

import java.awt.event.KeyEvent;
import java.io.Serializable;

/**
 *
 * @author logan
 */
public class ClientMessage implements Serializable {
    public int playerMove;
    
    public ClientMessage(int playerMove){
        this.playerMove = playerMove;
    }
}
