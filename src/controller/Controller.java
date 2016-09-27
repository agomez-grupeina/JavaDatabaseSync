/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import view.Login;
import javax.swing.SwingUtilities;
import persistence.DBConnection;
import networking.UDPClient;
import networking.UDPServer;
import persistence.OSPersistence;
import persistence.UserPersistence;
import view.OSForm;
import view.View;

/**
 *
 * @author agomez
 */
public class Controller implements ActionListener {

    //Instancias de clases necesarias para el funcionamiento de la aplicación
    private View view;
    private UserPersistence usersPers;
    private OSPersistence OSPers;

    public Controller(View view) {
        this.view = view;
        initApplication();
    }

    public void initApplication() {
        //Inicializamos la BD
        DBConnection.instance.init();

        //-----------------------PRUEBAS------------------------------




//        OSPersistence.exportarDatos();
//        System.exit(0);
        



        //-----------------------/PRUEBAS-----------------------------
        
        //Inicializamos clases de persistencia (que a su vez inicializan algunas tablas)
        usersPers = new UserPersistence();
        OSPers = new OSPersistence();

        //Seteamos el controller como action listener para los botones del view
        view.getLogin().bindActionListener(this);
        view.getForm().bindActionListener(this);

        //Mostramos la ventana de login
        view.getLogin().setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        switch (ae.getActionCommand()) {
            //Caso Ventana Login
            case "LOGIN":
                Login login = view.getLogin();
                OSForm form = view.getForm();
                //Si el inicio de sesion es correcto:
                if (usersPers.checkLogin(login.getUsername(), login.getPassword())) {
                    //Guardamos referencia en la clase Login al usuario logueado
                    login.setUsuarioActual(login.getUsername(), usersPers.isAdmin(login.getUsername()));

                    //Cerramos ventana de login
                    login.dispose();

                    //Poblamos el combo box de usuarios para el campo 
                    //asignadoA
                    form.fillAsignadoA(usersPers.getDatabaseUsers());
                    
                    //Mostramos ventana del formulario
                    form.setVisible(true);

                    //Inicializaciones según tipo de usuario que loguea: 
                    //(admin o operario)
                    Thread hilo_sincronizacion;
                    //Si el usuario es admin
                    if (login.isUsuarioActualAdmin()) {
                        //Arrancamos el servidor UDP
                        hilo_sincronizacion = new Thread(new UDPServer(OSPers));
                    } //Si el usuario no es admin
                    else {
                        //Arrancamos el cliente UDP
                        hilo_sincronizacion = new Thread(new UDPClient(OSPers));
                        //Eliminamos pestañas cuyo uso está reservado a admin
                        form.removeNoAdminTabs();
                    }
                    hilo_sincronizacion.start();
                }
                break;
            //Caso ventana OSForm
            case "CLOSE":
                System.out.println("Close button pressed!!");
                break;
            //Caso boton SAVE
            case "SAVE":
                System.out.println("Save button pressed!!");
                OSPers.saveOS(view.getForm().getInput1(), view.getForm().getInput2(), new Date(view.getForm().getInput3()));
                break;
        }
    }

}
