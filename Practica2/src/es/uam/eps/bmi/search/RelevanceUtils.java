
package es.uam.eps.bmi.search;

import es.uam.eps.bmi.search.indexing.Index;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author Diego Castaño y Daniel Garnacho
 */
public class RelevanceUtils {
    
    private static Index index;
    
    /**
     * Configura la clase para trabajar con un índice
     * @param i 
     */
    public static void setIndex (Index i) {
        index = i;
    }
    
    /**
     * Recibe un nombre de fichero y devuelve una lista de consultas
     * 
     * @param file Fichero con la lista en formato id:consulta
     * @return ArrayList de consultas
     */
     public  static ArrayList<String> parseQueries(String file) {
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
     public  static ArrayList<ArrayList<String>> parseRelevantFilenames(String file) {
        
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
     public static double calculatePrecision(List<ScoredTextDocument> results, ArrayList<String> relevantFilenames, int n) {
        double precision = 0.0;
        try {
            for (int i = 0; i < n; i++) {
                String filename = index.getDocument(results.get(i).getDocId()).getName();
                String name = filename.split("\\.")[0];
                for (String relevantFilename : relevantFilenames){
                    //System.out.println(" dado " + name + " esperado: " + relevantFilename);
                    if (relevantFilename.equals(name)) {
                        precision += 1.0;
                        break;
                    }
                }
            }
            //System.out.println(precision);
            precision = precision / (double)n;
        } catch (Exception e) {
        }
        return precision;
        
    }
     
     /**
      * Calcula el promedio de una lista de números
      */
     public static Double calculateAverage (List<Double> list) {
        double sum = 0.0;
        for(double d : list) {
            sum += d;
        }
        return (sum / (double)list.size());
     }

}
