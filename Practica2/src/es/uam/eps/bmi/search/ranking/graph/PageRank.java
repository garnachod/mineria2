
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
    
    // Lista de enlaces entrantes en la colección para cada identificador
    private final HashMap<String, ArrayList<String>> inlinkList;
    
    // Valor de PageRank calculado para cada identificador de documento
    private final HashMap<String, Double> scores;
    
    private static final double r = 0.15;
    
    
    /**
     * Constructor
     */
    public PageRank () {
        this.outlinkCount = new HashMap<>();
        this.outlinkList = new HashMap<>();
        this.inlinkList = new HashMap<>();
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

                    // Guardar enlaces salientes si los hay                    
                    if (tokens.length > 2) {
                        ArrayList<String> links = new ArrayList<>();
                        for (int i = 2; i < tokens.length; i++) {
                            links.add(tokens[i]);
                        }    
                        this.outlinkList.put(tokens[0], links);
                    }
                }
           }
           
            // Calcula enlaces entrantes (inlink) a partir de los salientes
            calculateInlinkList();
            
            // Calcula y almacena scores
            calculateScores();
            
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PageRank.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PageRank.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Calcula enlaces entrantes (inlink) a partir de los salientes
     */
    private void calculateInlinkList() {
        // Para cada doc coger sus enlances salientes
            for (String docId : outlinkList.keySet()) {
                
                // Para cada documento destino agregar enlace entrante 
                for (String outlink : outlinkList.get(docId)) {
                    ArrayList<String> links = inlinkList.get(outlink);
                    if (links == null) {
                        links = new ArrayList<>();
                    }
                    links.add(docId);
                    inlinkList.put(outlink, links);
                }
            }
            System.out.println(inlinkList);
    }

    
    /**
     * Calcula y almacena los pageranks de todos los documentos cargados
     */
    private void calculateScores() {
        
        int maxIterations = 50; // Ver http://www.webmasterworld.com/forum3/25867.htm
        
        // Inicializar scores a 1/N
        initScores();
        
        System.out.println(this.scores);
            
        // Condición de convergencia (50 veces)
        for (int i = 0; i < maxIterations; i++) {
            
            for (String docId : scores.keySet()) {
                scores.put(docId, calculateScore(docId));
            }
            
            System.out.println(this.scores);
        }
    }
    
    /**
     * Inicializa tabla de scores a 1/N
     */
    private void initScores() {
        for (String docId : outlinkList.keySet()) {
            if (this.outlinkList.get(docId) != null) {
                scores.put(docId, 1/(double)this.outlinkList.size());
                for (String link : this.outlinkList.get(docId)) {
                    scores.put(link, 1/(double)this.outlinkList.size());
                }   
            }
        }
    }
    
    /**
     * Calcula el PageRank asociado a un documento mediante iteración Jacobiana
     * @param docId
     * @return Valor de PageRank
     */
    private double calculateScore (String docId) {
        
        double score;
        
        // Si tiene links entrantes
        if (this.inlinkList.get(docId) != null) {
            score = this.scores.get(docId);
            for (String link : this.inlinkList.get(docId)) {
                double outlinkNumber = (double)outlinkCount.get(link);
                if (outlinkNumber > 0.0) { 
                    score = score + ((1 - r) * (scores.get(link) / outlinkNumber));
                } else {
                    // SUMIDERO
                }
            }   
        } else {
            // No tiene links entrantes
            score = r / (double)scores.size();
        }
        
        return score;
    }
    
    /*
    Test
    */
    public static void main (String args[]) {
        
        PageRank pr = new PageRank();
        String ruta = "./src/grafo1.txt";
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
