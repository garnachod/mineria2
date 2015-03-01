/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.parsing;

import java.util.ArrayList;

/**
 *
 * @author dani
 */
public class SimpleNormalizer {
    public static String normalize(String str){
        str = str.toLowerCase();
        return str;
    }
    public static ArrayList<String> removeNotAllowed(String [] array){
        ArrayList<String> lista = new ArrayList<>();
        for(int i = 0; i < array.length; i++){
            if(array[i].length() <= 3){
                //no insertar
            }else{
                String normal = array[i].replace(" ", "");
                lista.add(array[i]);
            }
        }
        return lista;
    }
}
