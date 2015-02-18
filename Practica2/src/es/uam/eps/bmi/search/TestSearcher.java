
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
            
            // Almacenar consultas
            ArrayList<String> queries = parseQueries(args[1]);
            
            // Almacenar documentos relevantes para cada consulta
            ArrayList<ArrayList<String>> relevantFilenames = parseRelevantFilenames(args[2]);
            
            // Calcular precisiones para cada consulta
            ArrayList<Double> p5s = new ArrayList<>();
            ArrayList<Double> p10s = new ArrayList<>();
            for (int i = 0; i < queries.size(); i++) {
                List<ScoredTextDocument> results = ls.search(queries.get(i));
                p5s.add(calculatePrecision(results, relevantFilenames.get(i), 5));
                p10s.add(calculatePrecision(results, relevantFilenames.get(i), 10));
            }
            System.out.println("Id \t p@5\t p@10");
            DecimalFormat df = new DecimalFormat("#.##");
            for (int i = 0; i < queries.size(); i++) {
                System.out.println((i+1) + " \t" + df.format(p5s.get(i)) + "\t" + df.format(p10s.get(i)));
            }
        
    }
    
    /**
     * Recibe un nombre de fichero y devuelve una lista de consultas
     * 
     * @param file Fichero con la lista en formato id:consulta
     * @return ArrayList de consultas
     */
    private static ArrayList<String> parseQueries(String file) {
        ArrayList<String> queries = new ArrayList<>();
        
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            while (in.ready()) {
              String s = in.readLine();
              String tokens[] = s.split(":");
              queries.add(tokens[1]);
            }
            in.close();
        } catch (Exception e) {
             System.out.println("No se pudieron parsear las queries");
        }
        
        return queries;
    }

    /**
     * Devuelve un array de listas de nombres de documento relevantes
     * 
     * @param file Fichero con la lista de documentos relevantes en formato id\tdocumento1\tdocumento2...
     * @return ArrayList de ArrayList de nombres de fichero
     */
    private static ArrayList<ArrayList<String>> parseRelevantFilenames(String file) {
        
        ArrayList<ArrayList<String>> relevantFilenames = new ArrayList<>();
        
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            while (in.ready()) {
              String s = in.readLine();
              String tokens[] = s.split("\t");
              ArrayList<String> filenames = new ArrayList<>(Arrays.asList(tokens));
              filenames.remove(0);
              relevantFilenames.add(filenames);
            }
            in.close();
        } catch (Exception e) {
             System.out.println("No se pudieron parsear los documentos relevantes");
        }
        
        return relevantFilenames;
    }
    
    /**
     * Calcula la precisión P@n dados los resultados, documentos relevantes y n
     * 
     * @param results Lista de documentos ordenados de mayor a menor score
     * @param relevantFilenames Ficheros relevantes
     * @param n Número de resultados a tener en cuenta 
     * @return Precision P@n
     */
    private static double calculatePrecision(List<ScoredTextDocument> results, ArrayList<String> relevantFilenames, int n) {
        double precision = 0.0;
        try {
            for (int i = 0; i < n; i++) {
                String filename = li.getDocument(results.get(i).getDocId()).getName();
                String name = filename.split("\\.")[0];
                for (String relevantFilename : relevantFilenames){
                    if (relevantFilename.equals(name)) {
                        precision += 1.0;
                        break;
                    }
                }
            }
            precision = precision / (double)n;
        } catch (Exception e) {
        }
        return precision;
        
    }
}
