
package es.uam.eps.bmi.search.searching;

import es.uam.eps.bmi.search.ScoredTextDocument;
import es.uam.eps.bmi.search.indexing.Index;
import es.uam.eps.bmi.search.indexing.Posting;
import es.uam.eps.bmi.search.parsing.SimpleNormalizer;
import es.uam.eps.bmi.search.parsing.SimpleTokenizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

 /**
 *
 * @author Diego Castaño y Daniel Garnacho
 */
public class BooleanSearcher implements Searcher {
    Index index;

    private List<ScoredTextDocument> searchAND(String[] terms) {
        PriorityQueue<Posting> postingsHeap = new PriorityQueue<>();
        HashMap<String, ArrayList<Posting>> postingsListList = new HashMap<>();
        HashMap<String, Integer> positions = new HashMap<>();
        
        // Sacar lista de listas de postings
        for (String term : terms) {
            List<Posting> termPostings = index.getTermPostings(term);
        
            if (termPostings == null) {
                return new ArrayList<>();
            }
            
            postingsListList.put(term, (ArrayList<Posting>) termPostings);
        }
        
        // Inicializar punteros
        for (String term : terms) {
            positions.put(term, 0);
        }
        
        // Merge
        boolean flagFin = false;
        while (!flagFin) {
            for (String term : terms) {
                postingsHeap.add(postingsListList.get(term).get(positions.get(term)));
                if (positions.get(term) >= postingsListList.get(term).size()) {
                    flagFin=true;
                }
            }
        }
        
        return new ArrayList<>();
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
