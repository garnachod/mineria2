
package es.uam.eps.bmi.search;

import es.uam.eps.bmi.search.indexing.LuceneIndexing;
import es.uam.eps.bmi.search.searching.LuceneSearcher;
import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author Diego Castaño y Daniel Garnacho
 */
public class TestSearcher {
    
    private static LuceneIndexing li = null;
    
    /**
     * Recibe 3 argumentos: , lista de queries y lista de docs relevantes
     * @param args 
     */
    public static void main (String args[]) {
        
            if (args.length != 3) {
                System.out.println("Recibe 3 argumentos: Indice, lista de queries y lista de docs relevantes");
                return;
            }

            // Cargar index
            System.out.println("Cargando índice en RAM...");
            li = new LuceneIndexing();
            li.load(args[0]);
            
            // Buscar en él
            LuceneSearcher ls = new LuceneSearcher();
            ls.build(li);
            
            // Configurar RelevanceUtils
            RelevanceUtils.setIndex(li);
            
            // Almacenar consultas
            ArrayList<String> queries = RelevanceUtils.parseQueries(args[1]);
            
            // Almacenar documentos relevantes para cada consulta
            ArrayList<ArrayList<String>> relevantFilenames = RelevanceUtils.parseRelevantFilenames(args[2]);
            
            // Calcular precisiones para cada consulta
            ArrayList<Double> p5s = new ArrayList<>();
            ArrayList<Double> p10s = new ArrayList<>();
            for (int i = 0; i < queries.size(); i++) {
                List<ScoredTextDocument> results = ls.search(queries.get(i));
                p5s.add(RelevanceUtils.calculatePrecision(results, relevantFilenames.get(i), 5));
                p10s.add(RelevanceUtils.calculatePrecision(results, relevantFilenames.get(i), 10));
            }
            System.out.println("Id \t p@5\t p@10");
            DecimalFormat df = new DecimalFormat("#.##");
            for (int i = 0; i < queries.size(); i++) {
                System.out.println((i+1) + " \t" + df.format(p5s.get(i)) + "\t" + df.format(p10s.get(i)));
            }
    }
}
