
package es.uam.eps.bmi.search.searching;

import es.uam.eps.bmi.search.ScoredTextDocument;
import es.uam.eps.bmi.search.indexing.Index;
import es.uam.eps.bmi.search.indexing.Posting;
import es.uam.eps.bmi.search.parsing.SimpleNormalizer;
import es.uam.eps.bmi.search.parsing.SimpleTokenizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.PriorityQueue;

 /**
 *
 * @author Diego Castaño y Daniel Garnacho
 */
public class BooleanSearcher implements Searcher {
    
    Index index;
    
    /**
     * Modos de búsqueda
     */
    public enum Mode { OR, AND };
    private Mode currentMode;
    
    /**
     * Constructor
     * @param mode 
     */
    public BooleanSearcher (Mode mode) {
        this.currentMode = mode;
    }
        
    /**
     * Carga un índice ya creado
     * @param index se presupone index.load(...)
     */
    @Override
    public void build(Index index) {
        this.index = index;
    }
    
    /**
     * Realiza una búsqueda en modo AND
     * @param terms Términos a buscar
     * @return Lista de documentos que contienen todos los términos válidos
     */
    private List<ScoredTextDocument> searchAND(String[] terms) {
        
        ArrayList<Posting> resultPostings;
        ArrayList<ScoredTextDocument> results = new ArrayList<>();
        PriorityQueue<Posting> postingsHeap = new PriorityQueue<>();
        HashMap<String, ArrayList<Posting>> postingLists = new HashMap<>();
        HashMap<String, Integer> positions = new HashMap<>();
        
        // Sacar listas de postings de cada term
        for (String term : SimpleNormalizer.removeNotAllowed(terms)) {
            List<Posting> termPostings = index.getTermPostings(term);
            if (termPostings.isEmpty()) {
                return new ArrayList<>();
            }
            postingLists.put(term, (ArrayList<Posting>) termPostings);
        }
        
        // Intersectar las listas 2 a 2 (match)
        HashSet<Posting> match = new HashSet<>(postingLists.get(postingLists.keySet().iterator().next()));
        for (String term: postingLists.keySet()) {
            if (!match.isEmpty()) {
                match.retainAll(postingLists.get(term));
            }
        }
        resultPostings = new ArrayList<>(match);
        for (Posting p : resultPostings) {
            results.add(new ScoredTextDocument(p.getDocId(), 1));
        }
        return results;
    }
    
     /**
     * Realiza una búsqueda en modo OR
     * @param terms Términos a buscar
     * @return Lista de documentos que contienen alguno de los términos válidos
     */
    private List<ScoredTextDocument> searchOR(String[] terms) {
        List<ScoredTextDocument> listaDocs = new ArrayList<>();
        PriorityQueue<MergePostings> postingsHeap = new PriorityQueue<>();
        
        // Sacar listas de postings de cada term
        for (String term : SimpleNormalizer.removeNotAllowed(terms)) {
            ArrayList<Posting> termPostings = new ArrayList(index.getTermPostings(term));
            if(!termPostings.isEmpty()){
                ListIterator<Posting> listIterator = termPostings.listIterator();
                MergePostings merge = new MergePostings(listIterator, termPostings.size());
                postingsHeap.add(merge);
            }
        }
        
        //mientras que no se hayan terminado todas las listas
        while(!postingsHeap.isEmpty()){
            MergePostings primero = postingsHeap.poll();
            while(!postingsHeap.isEmpty()){
                MergePostings otro = postingsHeap.poll();
                if(primero.equals(otro)){
                    if(otro.hasNext()){
                        otro.avanzaPuntero();
                        postingsHeap.add(otro);
                    }
                }else{
                    postingsHeap.add(otro);
                    break;
                }
            }
            String docid = primero.getDocID();
            ScoredTextDocument scored = new ScoredTextDocument(docid, 1.0);
            listaDocs.add(scored);
            if(primero.hasNext()){
                primero.avanzaPuntero();
                postingsHeap.add(primero);
            }
        }
        
        return listaDocs;
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
        
        
        if (currentMode.equals(Mode.AND)) {
            return this.searchAND(terms);
        } else {
            return this.searchOR(terms);
        }   
    }
}
