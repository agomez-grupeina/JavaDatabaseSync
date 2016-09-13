package persistence;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.DBConnection;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 *
 * @author agomez
 */
public class UserPersistence {

    public static final String USERS_TABLE_NAME = "usuarios";

    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    //adfmitnw2016
    private static final char[] DEFAULT_ADMIN_PLAIN_PASSWORD = {'a', 'd', 'f', 'm', 'i', 't', 'n', 'w', '2', '0', '1', '6'};

    public UserPersistence() {
        init();
    }

    private void init() {
        //Si la tabla usuarios se encuentra vacia significa que es el primer run de la app:
        if (tablaVacia()) {
            //Inicializamos tabla usuarios metiendo user admin
            initUserTables();
        }
    }

    //Inicializamos las tablas con los valores mínimos para que la 
    //aplicacion pueda funcionar
    private void initUserTables() {
        try {
            Statement s = DBConnection.instance.getConnection().createStatement();
            //Codificar plain password
            byte[] salt = createSalt();
            String hashedPassword = getHashedPassword(new String(DEFAULT_ADMIN_PLAIN_PASSWORD), salt);

            //Insertar usuario en BD
//            String sql = "INSERT INTO usuarios (username, password, salt, admin) VALUES "
//                    + "('" + DEFAULT_ADMIN_USERNAME + "', "
//                    + "'" + hashedPassword + "', "
//                    + "'" + new BASE64Encoder().encode(salt) + "', "
//                    + "1)";
            String sql = "INSERT INTO " + USERS_TABLE_NAME + " (username, password, salt, admin) VALUES "
                    + "('" + DEFAULT_ADMIN_USERNAME + "', "
                    + "'" + hashedPassword + "', "
                    + "'" + new BASE64Encoder().encode(salt) + "', "
                    + "1), "
                    + "('prueba', '" + hashedPassword + "', '" + new BASE64Encoder().encode(salt) + "', 0)";
            s.execute(sql);
            System.out.println("Usuario admin insertado correctamente.");
        } catch (SQLException ex) {
            Logger.getLogger(OSPersistence.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error init user tables");
        }
    }

    //Encriptamos el password
    private String getHashedPassword(String password, byte[] salt) {
        String generatedPassword = null;
        try {
            //SHA-512 la mas segura. Otros: SHA-128, SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt);
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    //Creamos un salt para el hash del password
    private byte[] createSalt() {
        SecureRandom sr;
        byte[] salt = null;
        try {
            sr = SecureRandom.getInstance("SHA1PRNG");
            salt = new byte[16];
            sr.nextBytes(salt);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(UserPersistence.class.getName()).log(Level.SEVERE, null, ex);
        }
        return salt;
    }

    //Funcion que comprueba si la tabla está vacía
    public boolean tablaVacia() {
        boolean vacia = true;
        try {
            Statement s = DBConnection.instance.getConnection().createStatement();
            String sql = "SELECT * FROM " + USERS_TABLE_NAME + " WHERE username LIKE '" + DEFAULT_ADMIN_USERNAME + "'";
            ResultSet rs = s.executeQuery(sql);
            if (rs.next()) {
                vacia = false;
            }

        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return vacia;
    }

    //Funcion para comprobar el correcto login del usuario
    public boolean checkLogin(String username, char[] password) {
        boolean correcto = false;
        try {
            Statement s = DBConnection.instance.getConnection().createStatement();
            String salt = null;
            String storedPassword = null;

            String sql = "SELECT * FROM " + USERS_TABLE_NAME + " WHERE username LIKE '" + username + "'";
            ResultSet rs = s.executeQuery(sql);
            if (rs.next()) {
                salt = rs.getString("salt");
                storedPassword = rs.getString("password");

                //Decodificamos en base 64 el valor de salt en base de datos, es decir, 
                //la inversa de la acción realizada para insertarlo en BD (si no 
                //se codificaba en base 64 había caracteres raros que no se podrian 
                //haber utilizado para comparar)
                byte[] saltBytes = new BASE64Decoder().decodeBuffer(salt);

                //Comparamos el valor del password hasheado en BD con el valor 
                //que obtenemos al hashear la contraseña introducida por el usuario
                if (storedPassword.equals(getHashedPassword(new String(password), saltBytes))) {
                    System.out.println("Usuario correcto!!!");
                    correcto = true;
                }

            }

        } catch (Exception ex) {
            Logger.getLogger(UserPersistence.class.getName()).log(Level.SEVERE, null, ex);
        }

        return correcto;
    }

    public boolean insertUser() {
        boolean insertado = false;

        return insertado;
    }

    /*Funcion utilizada una única vez al arrancar la aplicación para relenar 
    el atributo 'admin' de la clase Login mediante la funcion setUsuarioActual.
    Posteriormente, se utiliza el método de Login -> isUsuarioActualAdmin 
    para determinar si el usuario es admin o no (ello nos evita llamar a 
    esta misma función cada vez que queramos comprobarlo, lo que nos ahorra queries)*/
    public boolean isAdmin(String username) {
        boolean admin = false;
        try {
            Statement s = DBConnection.instance.getConnection().createStatement();
            String sql = "SELECT admin FROM " + USERS_TABLE_NAME + " WHERE username LIKE '" + username + "'";
            ResultSet rs = s.executeQuery(sql);
            //Si encontramos resultados para ese username
            if (rs.next()) {
                if (rs.getInt("admin") == 1) {
                    admin = true;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserPersistence.class.getName()).log(Level.SEVERE, null, ex);
        }
        return admin;
    }

    /*Extraemos los username de la BD para poblar el campo de OSForm de 
     AsignadoA.*/
    public String[] getDatabaseUsers() {
        List<String> users = new ArrayList<>();
        try {
            Statement s = DBConnection.instance.getConnection().createStatement();
            String sql = "SELECT username FROM " + USERS_TABLE_NAME;
            ResultSet rs = s.executeQuery(sql);
            while (rs.next()) {
                users.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserPersistence.class.getName()).log(Level.SEVERE, null, ex);

        }
        /* the JVM doesn't know how to blindly downcast Object[] (the result 
         of toArray()) to String[]. To let it know what your desired object 
         type is, you can pass a typed array into toArray(). The typed array 
         can be of any size (new String[1] is valid), but if it is too small 
         then the JVM will resize it on it's own*/
        return users.toArray(new String[0]);
    }

}
