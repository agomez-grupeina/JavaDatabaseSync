/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import persistence.OSPersistence;

/**
 *
 * @author agomez
 */
public class UDPServerTask implements Runnable {

    private final OSPersistence ospersistence;
    
    private final DatagramSocket socket;
    private DatagramPacket packet;
    private byte[] buffer;
    

    public UDPServerTask(DatagramSocket socket, DatagramPacket packet, byte[] buffer, OSPersistence ospersistence) {
        this.socket = socket;
        this.packet = packet;
        this.buffer = buffer;
        this.ospersistence = ospersistence;
    }

    @Override
    public void run() {
        try {
            //Extraemos datos de la BD local
            System.out.println("Server: Exportando...");
            buffer = ospersistence.exportarDatos();
            System.out.println("Server: Exportado...");
            
            //sincronizamos bd del servidor con bd del cliente utilizando los 
            //datos que nos ha enviado al realizar el request
            System.out.println("Server: Sincronizando localmente");
            ospersistence.sincronizarLocalmente(packet.getData(), buffer);
            System.out.println("SERVER: Sincronizado localmente");            

            //Mandar como response las rows de la BD local del server
            // send the response to the client at "address" and "port"
            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            packet = new DatagramPacket(buffer, buffer.length, address, port);
            System.out.println("Server. Enviando response");
            socket.send(packet);
            System.out.println("Server. Response enviado. END");
        } catch (IOException ex) {
            Logger.getLogger(UDPServerTask.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
