/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networking;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import persistence.OSPersistence;
import utils.Constants;

/**
 *
 * @author agomez
 */
public class UDPServer implements Runnable {

    private final int BUFFER_SIZE = Constants.BUFFER_SIZE;
    private final int PORT = Constants.PORT;

    private final OSPersistence ospersistence;
    
    protected DatagramSocket socket = null;
    protected BufferedReader in = null;
    
    public UDPServer(OSPersistence ospersistence){
        this.ospersistence = ospersistence;
    }
    
    @Override
    public void run() {
        while (true) {
//            System.out.println("Hilo actual UDPServer: " + Thread.currentThread());
            //System.out.println("Dentro del bucle del server");

            try {
                byte[] buffer = new byte[BUFFER_SIZE];

                socket = new DatagramSocket(PORT);

                // receive request
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);//socket.receive() detiene la ejecucion hasta recibir conexion (como el socket.accept() en TCP)

                
                /*Una vez recibido el paquete, el programa reanuda la ejecución del 
                servidor. Hacemos que cada vez que el servidor reciba un paquete 
                genere un nuevo hilo y ejecute la UDPServerTask*/
                new Thread(new UDPServerTask(socket, packet, buffer, ospersistence)).start();
                
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("Server Error:" + e.getMessage());
            }
        }
//        socket.close();
    }
}
