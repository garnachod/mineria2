
package es.uam.eps.bmi.search.ranking.graph;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Métodos para calcular PageRanks dado un grafo de enlaces
 * @author Diego Castaño y Daniel Garnacho
 */
public class PageRank {
    
    // Flag para imprimit iteraciones
    private boolean verbose = false;
    
    // Número de enlaces salientes para cada identificador 
    private HashMap<String, Integer> outlinkCount;
    
    // Lista de enlaces salientes en la colección para cada identificador
    private HashMap<String, ArrayList<String>> outlinkList;
    
    // Lista de enlaces entrantes en la colección para cada identificador
    private HashMap<String, ArrayList<String>> inlinkList;
    
    // Valor de PageRank calculado para cada identificador de documento
    private HashMap<String, Double> scores;
    
    // Tabla temporal para el calculo interativo de Pageranks
    private HashMap<String, Double> tempScores;
    
    // Factor de teleport
    private static final double r = 0.15;
    
    // Número total de links distintos
    private static int N = 0;
    
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
     * 
     * @param v 
     */
    public void setVerbose(Boolean v) {
        verbose = v;
    }
    
    /**
     * Devuelve el valor de PageRank (ya calculado) asociado al documento con 
     * el identificador dado. 
     * @param documentId Identificador de un documento
     * @return 
     */
    public double getScoreOf(String documentId) {
        if(this.scores.containsKey(documentId)){
            return this.scores.get(documentId);
        }
        return 0;
    }
    
    /**
     * Calcula los valores de PageRank de los documentos a partir de un fichero 
     * de enlaces dado.
     * @param filename Ruta del fichero
     */
    public void loadLinks (String filename) {
        
        // Descartar links anteriores si los hiubiera
        this.outlinkCount = new HashMap<>();
        this.outlinkList = new HashMap<>();
        this.inlinkList = new HashMap<>();
        this.scores = new HashMap<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            HashSet set = new HashSet();
            String line;
            
            // Para cada línea
            while ((line = br.readLine()) != null) {
                String tokens[] = line.split(" ");
                if (tokens.length > 1) {
                    
                    // Guardar nº de outlinks
                    this.outlinkCount.put(tokens[0], Integer.parseInt(tokens[1]));
                    
                    // Contar link origen
                    set.add(tokens[0]);
                    
                    // Guardar enlaces salientes si los hay   
                    ArrayList<String> links = new ArrayList<>();
                    for (int i = 2; i < tokens.length; i++) {
                        links.add(tokens[i]);
                        // Contar links destino
                        set.add(tokens[i]);
                    }    
                    this.outlinkList.put(tokens[0], links);
                    
                }
            }
            
            N = set.size();
           
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
    }

    
    /**
     * Calcula y almacena los pageranks de todos los documentos cargados
     */
    private void calculateScores() {
        
        int maxIterations = 50; // Ver http://www.webmasterworld.com/forum3/25867.htm
        
        // Inicializar scores a 1/N
        initScores();
        
        // Imprimir pageranks iniciales
        if (verbose) {
            System.out.println("PageRanks iniciales: " + this.scores);
        }
        
        // Condición de convergencia (50 veces)
        for (int i = 0; i < maxIterations; i++) {
            
            // Actualizar todos los pageranks
            updateScores();
            
            // Imprimir pageranks
            if (verbose) {
                System.out.println("PageRanks en iteración " + (i + 1) + ": " + this.scores);
            }
        }
        
        // Imprimir suma de scores
        if (verbose) {
            double sum = 0;
            for (String docId : scores.keySet()) {
                sum += scores.get(docId);
            }
            System.out.println("La suma de los PageRank es " + sum);
        }
    }
    
    /**
     * Inicializa tabla de scores a 1/N
     */
    private void initScores() {
        for (String docId : outlinkCount.keySet()) {
            scores.put(docId, 1/(double)N);
            if (this.outlinkList.get(docId) != null) {
                for (String link : this.outlinkList.get(docId)) {
                    scores.put(link, 1/(double)N);
                }   
            }
        }
    }
    
    /**
     * Actualiza los scores usando tempScores como tabla intermedia
     */
    private void updateScores() {
        
        tempScores = new HashMap<>();
        
        // Inicializa scores temporales a r/N
        for (String docId : scores.keySet()) {
            tempScores.put(docId, r / (double)N);
        }
        
        // Actualiza scores parciales
        for (String docId : tempScores.keySet()) {
            tempScores.put(docId, calculateScore(docId));
        }
        
        // Calcula sumatorio de scores parciales para controlar sumideros
        double tempSum = 0;
        for (String docId: tempScores.keySet()) {
            tempSum += tempScores.get(docId);
        }
        
        // Copia scores parciales actualizados
        for (String docId: tempScores.keySet()) {
            double finalScore = tempScores.get(docId) + ((1 - tempSum) / N);
            scores.put(docId, finalScore);
        }
        
    }

    /**
     * Calcula el PageRank asociado a un documento mediante iteración Jacobiana
     * @param docId
     * @return Valor de PageRank
     */
    private double calculateScore (String docId) {
        
        double score = this.tempScores.get(docId);
        
        // Si tiene links entrantes
        if (this.inlinkList.get(docId) != null) {
            for (String link : this.inlinkList.get(docId)) {
                score += ((1 - r) * (scores.get(link) / (double)outlinkCount.get(link)));
            }               
        }
        
        return score;
    }
    //se presupone generado
    public void toFile(String nombreFichero){
        try {
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(nombreFichero)));
            for(String key:this.scores.keySet()){
                dos.writeUTF(key);
                dos.writeDouble(this.scores.get(key));
            }
            dos.close();
        } catch (Exception ex) {
            Logger.getLogger(PageRank.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    //carga de un fichero
    public void fromFile(String nombreFichero){
        try {
            DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(nombreFichero)));
            this.scores = new HashMap<>();
            while(dis.available() > 0){
                String key = dis.readUTF();
                double pagerank = dis.readDouble();
                this.scores.put(key, pagerank);
            }
            dis.close();
        } catch (Exception ex) {
            Logger.getLogger(PageRank.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // POR HACER:
/*    
    También se recomienda que la clase guarde en disco (p.e. asociando al índice) 
    los valores de PageRank calculados, con el fin de que se pre-calculen 
    offline (en tiempo de indexado) y estén disponibles en el momento de 
    procesar consultas.
*/
}
