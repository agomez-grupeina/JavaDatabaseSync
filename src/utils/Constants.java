/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

/**
 *
 * @author agomez
 */
public class Constants {
    //UDP conexiones
    public static final int BUFFER_SIZE = 2048;
    public static final int PORT = 4445;
    
    //OSPersistence separadores
    /* is a control character in regex. So, to make regex parser understand 
    that you mean the literal |, you need to pass \| to the regex parser. But 
    \ is a control character in Java string literals. So, to make Java compiler 
    understand that you want to pass \| to the regex parser, you need to pass 
    "\\|" to the String#split()*/
    public static final String COLUMN_SEPARATOR = "\\|";//    "|"
    public static final char RAW_COLUMN_SEPARATOR = '|';
    public static final String ROW_SEPARATOR = "_";//       "_"
    public static final String TABLE_SEPARATOR = "\\";//    "\"
}
