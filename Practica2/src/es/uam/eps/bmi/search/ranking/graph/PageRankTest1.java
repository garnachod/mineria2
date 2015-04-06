
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
       
        System.out.println("Resolviendo grafo 1");
        PageRank pr = new PageRank();
        pr.setVerbose(true);
        
        String ruta = "./src/grafo1.txt";
        pr.loadLinks(ruta);    
        /*
        System.out.println("Resolviendo grafo 2");
        ruta = "./src/grafo2.txt";
        pr.loadLinks(ruta);    
        
        
        System.out.println("Resolviendo grafo 1K");
        ruta = "./src/links_1K.txt";
        pr.loadLinks(ruta);
        
        System.out.println("Resolviendo grafo 10K");
        ruta = "./src/links_10K.txt";
        pr.loadLinks(ruta); 

        System.out.println("Resolviendo grafo 100K");
        ruta = "./src/links_100K.txt";
        pr.loadLinks(ruta);    
        */
    }
}
