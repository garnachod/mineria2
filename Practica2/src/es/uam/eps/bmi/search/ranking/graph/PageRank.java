
package es.uam.eps.bmi.search.ranking.graph;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Métodos para calcular PageRanks dado un grafo de enlaces
 * @author Diego Castaño y Daniel Garnacho
 */
public class PageRank {
    
    // Número de enlaces salientes para cada identificador 
    private final HashMap<String, Integer> outlinkCount;
    
    // Lista de enlaces salientes en la colección para cada identificador
    private final HashMap<String, ArrayList<String>> outlinkList;
    
    // Valor de PageRank calculado para cada identificador de documento
    private final HashMap<String, Double> scores;
    
    /**
     * Constructor
     */
    public PageRank () {
        this.outlinkCount = new HashMap<>();
        this.outlinkList = new HashMap<>();
        this.scores = new HashMap<>();
    }
    
    /**
     * Devuelve el valor de PageRank (ya calculado) asociado al documento con 
     * el identificador dado. 
     * @param documentId Identificador de un documento
     * @return 
     */
    double getScoreOf(String documentId) {
        return this.scores.get(documentId);
    }
    
    /**
     * Calcula los valores de PageRank de los documentos a partir de un fichero 
     * de enlaces dado.
     * @param args 
     */
    public void loadLinks (String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            
            // Para cada línea
            while ((line = br.readLine()) != null) {
                String tokens[] = line.split(" ");
                if (tokens.length > 1) {
                    
                    // Guardar nº de outlinks
                    this.outlinkCount.put(tokens[0], Integer.parseInt(tokens[1]));

                    // Guardar enlaces si los hay                    
                    if (tokens.length > 2) {
                        ArrayList<String> links = new ArrayList<>();
                        for (int i = 2; i < tokens.length; i++) {
                            links.add(tokens[i]);
                        }    
                        this.outlinkList.put(tokens[0], links);
                    }
                }
            }
            
            // Calcula y almacena scores
            calculateScores();
            
            System.out.println(this.scores);
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PageRank.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PageRank.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Calcula y almacena los pageranks de todos los documentos cargados
     */
    private void calculateScores() {
        for (String docId : outlinkCount.keySet()) {
            scores.put(docId, calculateScore(docId));
        }
    }
    
    /**
     * Calcula el PageRank asociado a un documento mediante iteración Jacobiana
     * @param docId
     * @return Valor de PageRank
     */
    private double calculateScore (String docId) {
        
        double r = 0.15;
        int N = this.outlinkCount.size();
        double pagerank = 1 / N;
        int maxIterations = 40; // Ver http://www.webmasterworld.com/forum3/25867.htm
        
        if (this.outlinkCount.get(docId) > 0) {
            for (String link : this.outlinkList.get(docId)) {

            }   
        }
        
        return pagerank;
    }
    
    /*
    Test
    */
    public static void main (String args[]) {
        
        PageRank pr = new PageRank();
        String ruta = "C:\\Users\\diego.castaño\\Desktop\\grafo1.txt";
        pr.loadLinks(ruta);       
        
    }
    
    // POR HACER:
/*    
    También se recomienda que la clase guarde en disco (p.e. asociando al índice) 
    los valores de PageRank calculados, con el fin de que se pre-calculen 
    offline (en tiempo de indexado) y estén disponibles en el momento de 
    procesar consultas.
*/
    
/*
    Para probar y validar la implementación de PageRank realizada, se pide 
    desarrollar dos programas (métodos main) enlas siguientes clases: 
*/
    
    /*
    • Se recomienda, al menos inicialmente, llevar a cabo una implementación con la que los valores de PageRank
sumen 1, para ayudar a la validación de la misma. Posteriormente, si se desea, se pueden escalar (o no, a
criterio del estudiante) los cálculos omitiendo la división por el número total de páginas en el grafo.
    
    • Será necesario tratar los nodos sumidero tal como se ha explicado en las clases de teoría. Se recomienda
comprobar el correcto funcionamiento del algoritmo en un pequeño grafo con algún nodo sumidero. 
    */

}
