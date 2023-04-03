/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;
import javax.swing.JFrame;

/**
 *
 * @author Logan Campbell
 */
public class PongClient {
    private static final String SERVER_HOST = "localhost";
    private static final int PORT = 8901;
    
    public static void main(String[] args){
        String serverHost = args.length >= 1 ? args[0] : SERVER_HOST;
        int port = args.length >= 2 ? Integer.parseInt(args[1]) : PORT;
        JFrame frame = new JFrame ("Pong");
        frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new PongPanel(serverHost, port));
        frame.setBounds(350,300, 400, 200);
        frame.pack();
        frame.setVisible(true);   
    }
}
