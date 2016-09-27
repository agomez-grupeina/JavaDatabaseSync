/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.Constants;
import view.Login;

/**
 *
 * @author agomez
 */
public class OSPersistence {

    public static final String OS_TABLE_NAME = "prueba";

    /*Función que guarda el estado actual de la orden que tenemos abierta en 
     el OSForm. Si ya existe la orden, llama a updateOS. De lo contrario, llama 
     a insertOS */
    public boolean saveOS(String input1, String input2, Date input3) {
        if (!osAlreadyExists(input3)) {
            return insertOS(input1, input2, input3);
        } else {
            return updateOS(input1, input2, input3);
        }
    }

    /*Funcion utilizada para realizar inserts de una sola orden. Llamada al 
     pulsar el botón save sobre una orden que es nueva. (auxiliar de saveOS)
     Orden nueva = orden cuya fecha de creación no existe en BD*/
    public boolean insertOS(String input1, String input2, Date input3) {
        try {
            Statement s = DBConnection.instance.getConnection().createStatement();
            String sql = "INSERT INTO " + OS_TABLE_NAME + " (input1, input2, input3, ultimaModificacion) VALUES ('" + input1 + "', '" + input2 + "', '" + new java.sql.Timestamp(input3.getTime()) + "', '" + new java.sql.Timestamp(input3.getTime()) + "')";
            System.out.println("Insertando orden:\n" + sql);
            s.execute(sql);
            System.out.println("Orden insertada correctamente.");
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(OSPersistence.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /*Funcion utilizada para realizar updates de una sola orden. Llamada al 
     pulsar el botón save sobre una orden que no es nueva. (auxiliar de saveOS)
     Orden nueva = orden cuya fecha de creación no existe en BD*/
    public boolean updateOS(String input1, String input2, Date input3) {
        try {
            Statement s = DBConnection.instance.getConnection().createStatement();
            String sql = "UPDATE " + OS_TABLE_NAME + " SET input1 = '" + input1 + "', "
                    + "input2 = '" + input2 + "', "
                    + "ultimaModificacion = '" + new java.sql.Timestamp(new Date().getTime()) + "' "
                    + "WHERE input3 = '" + new java.sql.Timestamp(input3.getTime()) + "'";
            s.execute(sql);
            System.out.println("Orden actualizada correctamente.");
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(OSPersistence.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    //Comprobar de algún modo si la orden existe previamente (haciendo por ejemplo
    //un select a la tabla con la fecha exacta de creacion, si devuelve algo ya existe)
    public boolean osAlreadyExists(Date input3) {
        try {
            Statement s = DBConnection.instance.getConnection().createStatement();
            String sql = "SELECT * FROM " + OS_TABLE_NAME + " WHERE input3 = '" + new java.sql.Timestamp(input3.getTime()) + "'";
            ResultSet rs = s.executeQuery(sql);
            if (rs.next()) {
                return true;
            }

        } catch (SQLException ex) {
            Logger.getLogger(OSPersistence.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    /*Función que se encargará de sincronizar los datos de BD del 
     byte[] datosRecibidos con la BD local.
     Es decir, se encarga de realizar la sincronización de dos máquinas a nivel local.
     Utilizada por las clases UDPServer y UDPClient*/
    public boolean sincronizarLocalmente(byte[] datosRecibidos, byte[] datosLocales) {
        boolean sincronizado;
        //comparar datos locales con los datos recibidos
        String[] recibidos = new String(datosRecibidos).split(Constants.ROW_SEPARATOR);//Cada elemento del array es un String que representa un row completo en BD
        String[] locales = new String(datosLocales).split(Constants.ROW_SEPARATOR);

        System.out.println("sincronizarLocalmente: recibidos");
        System.out.println(new String(datosRecibidos));
        System.out.println("sincronizarLocalmente: locales");
        System.out.println(new String(datosLocales));

        //Datos que ya existen en BD local pero que se han actualizado en BD remota
        ArrayList<String> modificados = new ArrayList();
        //Datos nuevos en BD remota que hay que insertar en la BD local
        ArrayList<String> nuevos = new ArrayList();

        //Comparar ID's para sacar que registros son nuevos y qué registros
        //se actualizan
        Map<String, String> mapaRecibidos = (HashMap) extraerIdOrdenes(recibidos);
        Map<String, String> mapaLocales = (HashMap) extraerIdOrdenes(locales);

        System.out.println("sincronizarLocalmente: MAP recibidos:");
        for (Entry<String, String> entry : mapaRecibidos.entrySet()) {
            System.out.println("key-> " + entry.getKey() + " - value-> " + entry.getValue());
        }
        System.out.println("sincronizarLocalmente: MAP locales:");
        for (Entry<String, String> entry : mapaLocales.entrySet()) {
            System.out.println("key-> " + entry.getKey() + " - value-> " + entry.getValue());
        }

        //Obtenemos el id del usuario actual
        int id = -1;
        try {
            Statement s = DBConnection.instance.getConnection().createStatement();
            String sql = "SELECT id FROM usuarios WHERE username LIKE '" + Login.getUsuarioActual() + "'";
            ResultSet rs = s.executeQuery(sql);
            rs.next();//Sin if(){}, ya que el usuario existe seguro dado que está logueado
            id = rs.getInt("id");
        } catch (SQLException ex) {
            Logger.getLogger(OSPersistence.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Recorremos el map generado a partir de los datos recibidos
        for (Entry<String, String> entry : mapaRecibidos.entrySet()) {
            //Almacenamos en variables locales el valor de los rows local y 
            //recibido.
            /*
             NOTA:En algunos casos, el último campo de la entry venía con multitud de 
             espacios en blanco y ello impedía parsearlo correctamente. Se añaden 
             llamadas a la función trim()*/
            String row_local = mapaLocales.get(entry.getKey().trim());
            String row_recibido = entry.getValue().trim();
            //Si los datos locales contienen el id (no es una orden nueva):
            if (mapaLocales.containsKey(entry.getKey())) {

                //Extraemos y comparamos fechas de modificacion. Si la fecha de
                //modificacion local es anterior a la de los datos recibidos,
                //añadimos el row_recibido a la lista de modificados (ya que debemos
                //actualizar el valor de la BD local con el valor recibido)
                //Creamos un date Format para poder convertir las timestamps de BD en dates de java
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

                try {
                    //Comparamos fecha local con fecha de datos recibidos
                    if (dateFormat.parse(row_local.split(Constants.COLUMN_SEPARATOR)[4]).before(dateFormat.parse(row_recibido.split(Constants.COLUMN_SEPARATOR)[4]))) {
                        modificados.add(row_recibido);
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(OSPersistence.class.getName()).log(Level.SEVERE, null, ex);
                }

            } //En cambio, si los datos locales no contienen el id (es una orden nueva)
            else {
                //Metemos en un try catch, ya que el [5] provoca out of bounds
                try {
                    System.out.println("row recibido before parsing asignadoA:");
                    System.out.println(row_recibido.split(Constants.COLUMN_SEPARATOR)[5]);
                    //Comprobamos si la orden está asignada al usuario (operario) actual
                    if (row_recibido.split(Constants.COLUMN_SEPARATOR)[5].equals(Integer.toString(id))) {
                        //Si lo está, Insertamos la orden en el listado de nuevos
                        nuevos.add(row_recibido);
                    }
                } catch (Exception ex) {
                    System.out.println("Error parsing integer: " + ex.getMessage());
                }
            }
        }

        System.out.println("sincronizarLocalmente: nuevos");
        for (String s : nuevos) {
            System.out.println(s);
        }
        System.out.println("sincronizarLocalmente: modificados");
        for (String s : modificados) {
            System.out.println(s);
        }

        //Generamos las sentencias que vamos a ejecutar en BD para la sincronizacion
        String[] sentenciasInsert = generateSql("insert", nuevos);
        String[] sentenciasUpdate = generateSql("update", modificados);

        //Llamada a funcion para ejecutar sentencias SQL
        sincronizado = executeSql(sentenciasInsert) && executeSql(sentenciasUpdate);

        return sincronizado;
    }

    /*Función que genera todas las sentencias SQL que hay que ejecutar durante la 
     sincronización de datos*/
    private String[] generateSql(String action, ArrayList<String> ordenes) {
        String[] sentenciasSQL = new String[ordenes.size()];
        int cont = 0;
        String sql = "";

        //Recorremos todas las ordenes que nos llegan
        for (String orden : ordenes) {
            //Dividimos el String que representa la orden en un array. Cada 
            //columna de la orden en BD será una posición en el array
            String[] splittedOrder = orden.split(Constants.COLUMN_SEPARATOR);
            //Si estamos creando sentencias para insertar:
            if (action.equals("insert")) {
                //Generamos sentencia SQL para insertar la orden
                /*En principio si la orden es nueva solo existe en la BD 
                 local del jefe de taller, lo cual quiere decir que estamos 
                 insertando sobre la BD de un operario y por ello insertamos 
                 incluso el ID*/
                sql = "INSERT INTO " + OS_TABLE_NAME + " (id, input1, input2, input3, ultimaModificacion, asignadoA) "
                        + "VALUES (" + splittedOrder[0] + ", "//Empezamos en el 0 porque entra el ID tambien
                        + "'" + splittedOrder[1] + "', "
                        + "'" + splittedOrder[2] + "', "
                        + "'" + splittedOrder[3] + "', "
                        + "'" + splittedOrder[4] + "', "
                        + splittedOrder[5] + ")";
            }//Si estamos creando las sentencias para actualizar: 
            else if (action.equals("update")) {
                //Generamos sentencia SQL para realizar el update
                sql = "UPDATE " + OS_TABLE_NAME + " "
                        + "SET input1 = '" + splittedOrder[1] + "', "
                        + "input2 = '" + splittedOrder[2] + "', "
                        + "input3 = '" + splittedOrder[3] + "', "
                        + "ultimaModificacion = '" + splittedOrder[4] + "', "
                        + "asignadoA = " + splittedOrder[5] + " "
                        + "WHERE id = " + splittedOrder[0];
            }

            //Añadimos la sentencia SQL al array que devuelve la función
            sentenciasSQL[cont] = sql;
            //Sumamos 1 al contador que controla la posición del array en la que 
            //guardamos la orden
            cont++;
        }

        System.out.println("Sentencias SQL " + action + ": ");
        for (String s : sentenciasSQL) {
            System.out.println(s);
        }

        return sentenciasSQL;
    }

    /*Funcion cuyo objetivo es ejecutar las sentencias SQL generadas por la 
     función generateSql*/
    private boolean executeSql(String[] sentencias) {
        boolean executed = true;

        //Si hay por lo menos una sentencia
        if (sentencias.length > 0) {
            //Recorremos las sentencias a ejecutar
            for (String sentencia : sentencias) {
                System.out.println("Ejecutando sentencia:");
                System.out.println(sentencia);
                try (Statement s = DBConnection.instance.getConnection().createStatement()) {
                    //Las ejecutamos
                    s.execute(sentencia);
                } catch (SQLException ex) {
                    Logger.getLogger(OSPersistence.class.getName()).log(Level.SEVERE, null, ex);
                    executed = false;//Si salta el catch devolveremos false
                }
            }
        }
        return executed;
    }
    //    /*Función encargada de insertar las órdenes que faltan en la BD local durante 
    //     el proceso de sincronización.*/
    //    private boolean insertOrdenes(ArrayList<String> ordenes) {
    //        boolean insertadas = true;
    //        //Si hay por lo menos una orden:
    //        if (ordenes.size() > 0) {
    //            try {
    //                Statement s = DBConnection.instance.getConnection().createStatement();
    //                //TODO codificar función (ejecutar sentencias de generateSql("insert")
    //                String[] sentencias = generateSql("insert", ordenes);
    //
    //                for (String sentencia : sentencias) {
    //                    s.execute(sentencia);
    //                }
    //
    //            } catch (SQLException ex) {
    //                Logger.getLogger(OSPersistence.class.getName()).log(Level.SEVERE, null, ex);
    //                insertadas = false;
    //            }
    //        }
    //
    //        return insertadas;
    //    }
    //
    //    /*Función encargada de hacer el update de las órdenes de la BD local cuya fecha 
    //     de modificación sea posterior a la almacenada localmente */
    //    private boolean updateOrdenes(ArrayList<String> ordenes) {
    //        boolean modificadas = true;
    //        //Si hay por lo menos una orden:
    //        if (ordenes.size() > 0) {
    //            try {
    //                Statement s = DBConnection.instance.getConnection().createStatement();
    //                //TODO codificar función (ejecutar sentencias de generateSql("update")
    //            } catch (SQLException ex) {
    //                Logger.getLogger(OSPersistence.class.getName()).log(Level.SEVERE, null, ex);
    //                modificadas = false;
    //            }
    //        }
    //        return modificadas;
    //    }

    /*Funcion que tiene como objetivo convertir los datos brutos en un map 
     key (id) - valor (row entero (id incluido tambien) de la BD)*/
    private Map extraerIdOrdenes(String[] datos) {
        Map<String, String> ordenes = new HashMap();
        //Recorremos todas las rows y guardamos en el map su id->key, string completo->value
        for (String orden : datos) {
            ordenes.put(orden.split(Constants.COLUMN_SEPARATOR)[0], orden);
        }
        return ordenes;
    }

    /*Función que exporta los datos de la BD y los devuelve en un 
     byte[], preparándolo de este modo para ser transferido durante 
     la conexión UDP a través de socket*/
    public byte[] exportarDatos() {
        byte[] data = null;
        try {
            String rawData = "";
            //Meterlo todo en un string y separators:
            //Separadores-->  | columna       _ fila      \ tabla
            Statement s = DBConnection.instance.getConnection().createStatement();
            String sql = "SELECT * FROM " + OS_TABLE_NAME + " ORDER BY input3 DESC";
            ResultSet rs = s.executeQuery(sql);
            while (rs.next()) {
                //Recorremos columnas
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    rawData += rs.getString(i);
                    //Si no es la última columna, añadimos separador
                    if (i < rs.getMetaData().getColumnCount()) {
                        rawData += Constants.RAW_COLUMN_SEPARATOR;
                    }
                }
                //Añadimos separador de fila
                rawData += Constants.ROW_SEPARATOR;
            }
            //Lo metemos en un try por si salta exception que no pete
            try {
                rawData = rawData.substring(0, rawData.length() - 1);
            } catch (Exception ex) {
                rawData = "";
            }

            System.out.println("DATOS EXPORTADOS: \n" + rawData);

            //Convertimos el String a array de bytes
            data = rawData.getBytes();
        } catch (SQLException ex) {
            Logger.getLogger(OSPersistence.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }
}
