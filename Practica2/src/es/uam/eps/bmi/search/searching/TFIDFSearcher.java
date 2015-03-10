
package es.uam.eps.bmi.search.searching;

import es.uam.eps.bmi.search.ScoredTextDocument;
import es.uam.eps.bmi.search.indexing.Index;
import es.uam.eps.bmi.search.indexing.Posting;
import es.uam.eps.bmi.search.parsing.SimpleNormalizer;
import es.uam.eps.bmi.search.parsing.SimpleTokenizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.PriorityQueue;

/**
 * Buscador vectorial
 * @author Diego Castaño y Daniel Garnacho
 */
public class TFIDFSearcher implements Searcher {
    Index index;
    
    @Override
    public void build(Index index) {
        this.index = index;
    }

    @Override
    public List<ScoredTextDocument> search(String query) {
        // Tokenizar consulta
        SimpleTokenizer st = new SimpleTokenizer();
        String[] tokens = st.split(query);
        String[] terms = SimpleTokenizer.deleteRepeated(tokens);
        for (int i = 0; i < terms.length; i++) {
            terms[i] = SimpleNormalizer.normalize(terms[i]);
        }
        // Sacar listas de postings de cada term
        ArrayList<String> terminosFinal = SimpleNormalizer.removeNotAllowed(terms);
        
        List<ScoredTextDocument> listaDocs = new ArrayList<>();
        PriorityQueue<MergePostings> postingsHeap = new PriorityQueue<>();
        
        // Sacar listas de postings de cada term
        for (String term : terminosFinal) {
            ArrayList<Posting> termPostings = new ArrayList(index.getTermPostings(term));
            if(!termPostings.isEmpty()){
                ListIterator<Posting> listIterator = termPostings.listIterator();
                MergePostings merge = new MergePostings(listIterator, termPostings.size());
                postingsHeap.add(merge);
            }
        }
        
        double totalDocs = this.index.getNDocsIndex();
        
        //mientras que no se hayan terminado todas las listas
        while(!postingsHeap.isEmpty()){
            ArrayList<MergePostings> listaAuxiliarInsertar = new ArrayList<>();
            MergePostings primero = postingsHeap.poll();
            listaAuxiliarInsertar.add(primero);
            Posting auxPosting = primero.getPosting();
            double tf = 1.0 + this.logBase2(auxPosting.getTermFrequency());
            double idf = this.logBase2(totalDocs/primero.getNTotalDocs());
            double tf_idfAux = 0;
            tf_idfAux += tf*idf;
            while(!postingsHeap.isEmpty()){
                MergePostings otro = postingsHeap.poll();
                //System.out.println("UNO:" + primero.getDocID() + " DOS:"+ otro.getDocID());
                if(primero.equals(otro)){
                    //System.out.println("equals");
                    auxPosting = otro.getPosting();
                    tf = 1.0 + this.logBase2(auxPosting.getTermFrequency());
                    idf = this.logBase2(totalDocs/otro.getNTotalDocs());
                    tf_idfAux += tf*idf;
                    listaAuxiliarInsertar.add(otro);
                }else{
                    postingsHeap.add(otro);
                    break;
                }
            }
            String docid = primero.getDocID();
            double nomalizedtf_idf = tf_idfAux/(this.index.getBytesDocument(docid)/1024.0);
            ScoredTextDocument scored = new ScoredTextDocument(docid, nomalizedtf_idf);
            listaDocs.add(scored);
            for(MergePostings mp: listaAuxiliarInsertar){
                if(mp.hasNext()){
                    mp.avanzaPuntero();
                    postingsHeap.add(mp);
                }
            }
        }
        Collections.sort(listaDocs);
        return listaDocs;
    }
    private double logBase2(double x)
    {
        return Math.log(x) / Math.log(2);
    }
    
    /**
    * Solicita al usuario una consulta, y muestra por pantalla
    * los top 5 resultados de la consulta imprimiendo el título y parte del
    * contenido
    *
    * @param args Ruta del fichero de config
    */
    public static void main (String[] args) {
        InteractiveSearcher.main(args, new TFIDFSearcher());
    }
}
