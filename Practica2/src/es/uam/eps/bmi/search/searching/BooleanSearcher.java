
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
import java.util.PriorityQueue;

 /**
 *
 * @author Diego Castaño y Daniel Garnacho
 */
public class BooleanSearcher implements Searcher {
    Index index;

    private List<ScoredTextDocument> searchAND(String[] terms) {
        
        ArrayList<Posting> resultPostings;
        ArrayList<ScoredTextDocument> results = new ArrayList<>();
        PriorityQueue<Posting> postingsHeap = new PriorityQueue<>();
        HashMap<String, ArrayList<Posting>> postingLists = new HashMap<>();
        HashMap<String, Integer> positions = new HashMap<>();
        
        // Sacar listas de postings de cada term
        for (String term : SimpleNormalizer.removeNotAllowed(terms)) {
            List<Posting> termPostings = index.getTermPostings(term);
            if (termPostings == null) {
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

    private List<ScoredTextDocument> searchOR(String[] terms) {
        return new ArrayList<>();
    }

    public enum Mode { OR, AND };
    private Mode currentMode;
    
    public BooleanSearcher (Mode mode) {
        this.currentMode = mode;
    }
        
    /**
     * @param index SE SUPONE YA CARGADO (load) 
     */
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
        
        if (currentMode.equals(Mode.AND)) {
            return this.searchAND(terms);
        } else {
            return this.searchOR(terms);
        }   
    }
    
}