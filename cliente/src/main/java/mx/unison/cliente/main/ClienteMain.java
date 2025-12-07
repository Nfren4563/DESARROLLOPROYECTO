package mx.unison.cliente.main;

import mx.unison.servidor.conexion.servidor;
import mx.unison.cliente.main.appFrame;
import javax.swing.*;

public class ClienteMain {
    public static void main(String[] args) {

        new Thread(() -> {
            servidor s = new servidor(5000);
            s.start();
        }).start();
        
        try {
            Thread.sleep(1000); 
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new appFrame().setVisible(true);
        });
    }
}
