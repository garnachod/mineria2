/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.searching;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author dani
 */
public class AggregatorTest {
    public static void main (String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("0) Cancelar");
        System.out.println("1) TFIDFSearcher");
        System.out.println("2) ExactMatchingSearcher->Literal");
        System.out.println("3) ProximalSearcher");
        System.out.println("4) TFIDFSearcher + PageRank ");
        System.out.println("5) ExactMatchingSearcher + PageRank  ");
        System.out.println("6) TFIDFSearcher + ProximalSearcher + PageRank ");
        System.out.println();
        System.out.println("Introduce una opción:");
        String query = br.readLine();
        switch(query){
            case "1":
                InteractiveSearcher.main(args, new TFIDFSearcher());
                break;
            case "2":
                InteractiveSearcher.main(args, new LiteralMatchingSearcher());
                break;
            case "3":
                InteractiveSearcher.main(args, new ProximalSearcher());
                break;
            case "4":
                InteractiveSearcher.main(args, new TFIDFSearcher(), new PageRankSearcher());
                break;
            case "5":
                InteractiveSearcher.main(args, new LiteralMatchingSearcher(), new PageRankSearcher());
                break;
            case "6":
                InteractiveSearcher.main(args,new TFIDFSearcher(), new ProximalSearcher() , new PageRankSearcher());
                break;
            default:
                return;
        }
        
    }
    
}
