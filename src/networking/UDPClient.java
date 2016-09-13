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
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import persistence.OSPersistence;
import utils.Constants;

/**
 *
 * @author agomez
 */
public class UDPClient implements Runnable {

    private final OSPersistence ospersistence;
    private final int PORT = Constants.PORT;

    private DatagramSocket socket;

    public UDPClient(OSPersistence ospersistence) {
        this.ospersistence = ospersistence;
    }

    /*Metemos el código del cliente UDP en un runnable para poder lanzar un 
     request al servidor cada X tiempo usando un ScheduledExecutorService 
     para llamarlo*/
    @Override
    public void run() {

        System.out.println("Hilo actual UDPClient: " + Thread.currentThread());
        System.out.println("Dentro del bucle del cliente");

        //Buffer donde almacenaremos los datos recibidos
        byte[] buffer;
        //Variable donde almacenar los datos locales antes de pasarlos al constructor
        //del Datagrampacket
        byte[] localData;

        try {
            //obtenemos el socket
            socket = new DatagramSocket();

            //obtenemos datos que vamos a enviar mediante el request al servidor
            localData = ospersistence.exportarDatos();

            //enviamos request
            InetAddress address = InetAddress.getByName("localhost");
            DatagramPacket packet = new DatagramPacket(localData, localData.length, address, PORT);
            System.out.println("Client: enviando request");
            socket.send(packet);
            System.out.println("Client: enviado request");

            //recibimos response
            buffer = new byte[Constants.BUFFER_SIZE];
            packet = new DatagramPacket(buffer, buffer.length);
            //Llamamos a receive, lo que mantiene al cliente en espera de 
            //respuesta por parte del servidor
            System.out.println("Client: recibiendo response");
            socket.receive(packet);
            System.out.println("Client: recibido response");

            //tratamiento de response (sincronizar BD)
            System.out.println("Client: sincronizando localmente");
            ospersistence.sincronizarLocalmente(packet.getData(), localData);
            System.out.println("Client: sincronizado localmente");

        } catch (SocketException ex) {
            Logger.getLogger(UDPClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UDPClient.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            //Cerramos socket
            if (socket != null) {
                socket.close();
            }
        }
    }
}
