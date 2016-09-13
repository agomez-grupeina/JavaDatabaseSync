/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import persistence.OSPersistence;

/**
 *
 * @author agomez
 */
public class DBConnection {

    private static final String db_name = "database";
    private static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    private static final String CONNECTION_URL = "jdbc:derby:" + db_name + ";create=true";
    private final String db_location = System.getProperty("user.dir");

    private Connection connection;

    public static DBConnection instance = new DBConnection();

    private DBConnection() {

    }

    /*Creamos este metodo dado que necesitamos que la base de datos se 
     inicialice en el momento que arrancamos la aplicación. Si dejamos 
     el código de init en el constructor, no se ejecuta hasta que hacemos 
     una llamada a alguno de los métodos de la clase. Por ese motivo, utilizamos 
     el método init para arrancar la BD cuando se lanza la aplicación.*/
    public void init() {
        initDatabase();
        //Conectamos
        connect();
        //Creamos tablas
        crearTablas();
    }

    private void initDatabase() {
        //Set folder where derby database will be stored
        Properties p = System.getProperties();
        p.setProperty("derby.system.home", db_location);
    }

    private void connect() {
        try {
            //Inicializamos la conexion, lo que creará la base de datos si no existe previamente (create=true en CONNECTION_URL)
            connection = DriverManager.getConnection(CONNECTION_URL);
            if (connection != null) {
                System.out.println("Conexión realizada correctamente!!");
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void disconnect() {
        try {
            connection.close();
            /*Hacemos el shutdown de la base de datos (según documentacion, 
             siempre lanza exception pero he comprobado que de este modo 
             los ID autoincrementados son consecutivos (si no cerramos así, 
             derby por defecto reserva 100 espacios para ID por lo que la siguiente 
             vez que arrancasemos la aplicación el primer ID que nos daría sería el 
             101, 201, o el que corresponda.*/
            DriverManager.getConnection("jdbc:derby:" + db_name + ";shutdown=true");

            System.out.println("Conexion cerrada correctamente.");
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Metodo que crea las tablas en BD en caso de que no existan previamente
    private void crearTablas() {
        //Declaramos tablas y sentencias que vamos a ejecutar para crearlas
        Map<String, String> tablas = new HashMap<>();
        tablas.put(OSPersistence.OS_TABLE_NAME, "CREATE TABLE " + OSPersistence.OS_TABLE_NAME + "("
                + "id INTEGER not null primary key GENERATED BY DEFAULT AS IDENTITY(START WITH 1, INCREMENT BY 1), "
                + "input1 VARCHAR(50), "
                + "input2 VARCHAR(50), "
                + "input3 TIMESTAMP, "
                + "ultimaModificacion TIMESTAMP, "
                + "asignadoA INTEGER)");
        tablas.put("usuarios", "CREATE TABLE usuarios("
                + "id INTEGER not null primary key GENERATED ALWAYS AS IDENTITY(START WITH 1, INCREMENT BY 1), "
                + "username VARCHAR(50), "
                + "password VARCHAR(255), "
                + "salt VARCHAR(255), "
                + "admin SMALLINT)");

        for (Entry<String, String> entry : tablas.entrySet()) {
            try {
                Statement s = connection.createStatement();
                String sql = entry.getValue();

                //Si no existe, la creamos
                if (!existeTabla(entry.getKey())) {
                    s.execute(sql);
                    System.out.println("Se ha creado la tabla " + entry.getKey());
                } else {
                    System.out.println("La tabla " + entry.getKey() + " ya existe");
                }
            } catch (SQLException ex) {
                Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    //Funcion auxiliar para comprobar si una tabla dada existe en la base de datos
    private boolean existeTabla(String nombre_tabla) {
        boolean existe = true;
        try {
            //Comprobamos si la tabla ya existe
            DatabaseMetaData dbmeta = connection.getMetaData();
            ResultSet rs = dbmeta.getTables(null, null, nombre_tabla.toUpperCase(), null);
            if (!rs.next()) {
                existe = false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return existe;
    }
    

    public Connection getConnection() {
        return connection;
    }
    
    

}
