
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

        if (!(o instanceof ScoredTextDocument)) {
            return Integer.MAX_VALUE;
        }
        
        int scoreA = (int)this.score * 1000;
        int scoreB = (int)((ScoredTextDocument)o).score * 1000;
        
        if (scoreA == scoreB) {
            return 0;
        }
        
        if (scoreA > scoreB) {
            return 1;
        }
        
        return -1;
    }
    
}
