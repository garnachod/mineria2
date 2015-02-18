package es.uam.eps.bmi.search.indexing;

import java.util.ArrayList;
import java.util.List;

public class Posting {
    
    private final String docId;
    private final int termFrequency;
    private final List<Long> termPositions;
    
    /**
     * Constructor
     * @param docId
     * @param termFrequency
     * @param termPositions 
     */
    public Posting (String docId, int termFrequency, List<Long> termPositions) {
        this.docId = docId;
        this.termFrequency = termFrequency;
        this.termPositions = new ArrayList<>(termPositions); // OJO: Menos óptimo
    }

    /**
     * @return the docId
     */
    public String getDocId() {
        return docId;
    }

    /**
     * @return the termFrequency
     */
    public int getTermFrequency() {
        return termFrequency;
    }

    /**
     * @return the termPositions
     */
    public List<Long> getTermPositions() {
        return termPositions;
    }
    
    
}
