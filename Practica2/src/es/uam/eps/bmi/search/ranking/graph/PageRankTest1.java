
package es.uam.eps.bmi.search.ranking.graph;

/**
 * Pograma para probar cálculo de PageRank con grafos 1 y 2
 * @author Diego Castaño y Daniel Garnacho
 */
public class PageRankTest1 {
     /**
     *  Main para probar cálculo de PageRank con grafos 1 y 2
     */
    public static void main (String args[]) {
        PageRank pr = new PageRank();
        String ruta = "./src/grafo1.txt";
        pr.loadLinks(ruta);    
        pr = new PageRank();
        ruta = "./src/grafo2.txt";
        pr.loadLinks(ruta);    
    }
}
