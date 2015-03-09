
package es.uam.eps.bmi.search;

/**
 * @author Diego Castaño y Daniel Garnacho
 */
public class ScoredTextDocument implements Comparable {
    String docId;
    double score;
    
    /**
     * Constructor
     * @param docId
     * @param score 
     */
    public ScoredTextDocument (String docId, double score) {
        this.docId = docId;
        this.score = score;
    }
    
    public String getDocId() {
        return docId;
    }
    
    public double getScore() {
        return score;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof ScoredTextDocument) {
            return (int) ((this.score * 100.0) - ((((ScoredTextDocument)o).score)*100.0));
        }
        return Integer.MAX_VALUE;
    }
    
}
