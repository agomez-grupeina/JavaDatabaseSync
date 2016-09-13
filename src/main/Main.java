package main;

import controller.Controller;
import view.Login;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import model.DBConnection;
import networking.UDPServer;
import persistence.OSPersistence;
import view.View;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author agomez
 */
public class Main {

//    public static DBConnection CONNECTION_INSTANCE = new DBConnection();
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Database will be stored on:\n" + System.getProperty("user.dir") + File.separator + "database");
        System.out.println("Hilo actual MAIN: " + Thread.currentThread());


        //Inicializamos controller (cuyo metodo init inicializa toda la app)
        new Controller(new View());

    }

    //        //Conversion de Date a timestamp
//        Calendar c = Calendar.getInstance();
//        c.setTime((Date)tf_date.getValue());
//        
//        lbl_debug.setText(Long.toString(c.getTimeInMillis()));
//        
//        //Inversa
//        c.setTimeInMillis(Long.parseLong(lbl_debug.getText()));
//        lbl_debug.setText(c.getTime().toString());
}
