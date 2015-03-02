/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search;

import es.uam.eps.bmi.search.indexing.BasicIndex;
import es.uam.eps.bmi.search.indexing.Posting;
import es.uam.eps.bmi.search.parsing.HTMLSimpleParser;
import es.uam.eps.bmi.search.parsing.TextParser;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author dani
 */
public class TestIndexProgramado {
    /*
     * Recibe dos argumentos: Ruta a docs.zip y ruta donde almacenar el índice
     */
    public static void main (String args[]) throws Exception {
        
        if (args.length != 2) {
            System.out.println("Recibe 2 argumentos: Ruta a docs.zip, ruta donde almacenar el índice y ruta donde se almacena la estadistica");
            return;
        }
        
        TextParser tp = new HTMLSimpleParser();
        BasicIndex indexer = new BasicIndex();
        System.out.println("Construyendo índice...");
        //indexer.build(args[0], args[1], tp);
        
        System.out.println("Leyendo índice...");
        indexer.load(args[1]);
        System.out.println("índice leido...");
        /*List<String> lista =indexer.getTerms();
        Collections.sort(lista);
        int i = lista.size()-2000;
        int limite = lista.size()-1000;
        for(;i<limite;i++){
            System.out.println(lista.get(i));
            i++;
            if(i>limite){
                break;
            }
        }
        List<Posting> listaP = indexer.getTermsPosting("zalagenas");

        for(Posting p:listaP){
            System.out.println(p);
        }*/
    }
}
