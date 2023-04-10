/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

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
        
        String ipAddress = (String)JOptionPane.showInputDialog(frame,
                                "Input IP Address of server:",
                                "Server IP",
                                JOptionPane.PLAIN_MESSAGE);
        
        if(ipAddress.equals(""))
            ipAddress = SERVER_HOST;
        
        PongPanel panel = (PongPanel) frame.getContentPane().add(new PongPanel(ipAddress, port));
        frame.setBounds(350,300, 400, 200);
        frame.pack();
        frame.setVisible(true);   

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                panel.close();
                e.getWindow().dispose();
            }
        });
    }
}
