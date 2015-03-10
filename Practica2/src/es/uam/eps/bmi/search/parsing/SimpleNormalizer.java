
package es.uam.eps.bmi.search.parsing;

import java.util.ArrayList;


/**
 * Métodos para normalizar texto a indexar/buscar
 * @author Diego Castaño y Daniel Garnacho
 */
public class SimpleNormalizer {
    public static String normalize(String str){
        str = str.toLowerCase();
        return str;
    }
    public static ArrayList<String> removeNotAllowed(String [] array){
        ArrayList<String> lista = new ArrayList<>();
        for(int i = 0; i < array.length; i++){
            if(array[i].length() < 3){
                //no insertar
            }else{
                String normal = array[i].replace(" ", "");
                lista.add(array[i]);
            }
        }
        return lista;
    }
    
}
