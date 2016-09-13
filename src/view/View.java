/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import controller.Controller;

/**
 * Clase que agrupa todos los views (ventanas) de la aplicación de modo que al 
 * pasar una instancia de esta clase al constructor del controller, nos permite 
 * implementar el modelo MVC siendo el controller el encargado de responder a 
 * los eventos generados por la interacción del usuario con la vista y también 
 * de modificar el GUI según corresponda
 * @author agomez
 */
public class View {

    private Login login;
    private OSForm form;

    public View() {
        init();
    }

    public void init() {
        //Inicializamos login
        login = new Login();
        //Lo movemos al centro de la pantalla
        login.setLocationRelativeTo(null);
        
        //Inicializamos form
        form = new OSForm();
        //Por defecto ya sem ueve al centro de la pantalla por el custom init que se escribio en OSForm
        //form.setLocationRelativeTo(null);
    }

    public Login getLogin() {
        return login;
    }

    public OSForm getForm() {
        return form;
    }
    
    public void bindActionListener(Controller controller){
        
    }
    
    
}
