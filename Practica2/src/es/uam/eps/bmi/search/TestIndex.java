
package es.uam.eps.bmi.search;

import es.uam.eps.bmi.search.indexing.LuceneIndexing;
import es.uam.eps.bmi.search.indexing.Posting;
import es.uam.eps.bmi.search.parsing.HTMLSimpleParser;
import es.uam.eps.bmi.search.parsing.TextParser;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Diego Castaño y Daniel Garnacho
 */
public class TestIndex {
    /*
     * Recibe dos argumentos: Ruta a docs.zip y ruta donde almacenar el índice
     */
    public static void main (String args[]) throws Exception {
        
        if (args.length != 3) {
            System.out.println("Recibe 3 argumentos: Ruta a docs.zip, ruta donde almacenar el índice y ruta donde se almacena la estadistica");
            return;
        }

        LuceneIndexing li = new LuceneIndexing();
        TextParser tp = new HTMLSimpleParser();
        System.out.println("Construyendo índice...");
        li.build(args[0], args[1], tp);
  
        System.out.println("Cargando índice en RAM...");
        li.load(args[1]);
        
        FileWriter fw = new FileWriter(args[2]);
        
        ArrayList<FrecObject> listaFrecuencias = new ArrayList<>();
        for (String term: li.getTerms()) {
            int nDocs = 0;
            int termFrequency = 0;
            for (Posting p : li.getTermsPosting(term)) {
                termFrequency += p.getTermFrequency();
                nDocs++;
            }
            listaFrecuencias.add(new FrecObject(term,termFrequency,nDocs));
        }
        Collections.sort(listaFrecuencias, new FrecObject());
        for(FrecObject fo : listaFrecuencias){
            fw.write(fo.getPalabla() + ";" + fo.getFrecuencia() + ";" + fo.getnFicheros() + "\n");
        }
   }
    
}




