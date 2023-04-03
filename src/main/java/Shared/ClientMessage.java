package Shared;

import java.awt.event.KeyEvent;
import java.io.Serializable;

/**
 *
 * @author logan
 */
public class ClientMessage implements Serializable {
    public KeyEvent PlayerMove;
    
    public ClientMessage(KeyEvent PlayerMove){
        this.PlayerMove = PlayerMove;
    }
}
