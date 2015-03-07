
package es.uam.eps.bmi.search.searching;

import es.uam.eps.bmi.search.indexing.Posting;
import java.util.ListIterator;

/**
 *
 * @author dani
 */
public class MergePostings implements Comparable{
    private ListIterator<Posting> listIterator;
    private Posting p;
    private int nTotal;
    private String termino;

    public MergePostings(ListIterator<Posting> listIterator, int nTotal){
        this.listIterator = listIterator;
        this.p = this.listIterator.next();
        this.nTotal = nTotal;
        this.termino = null;
    }
    public MergePostings(ListIterator<Posting> listIterator, String termino){
        this.listIterator = listIterator;
        this.p = this.listIterator.next();
        this.nTotal = 0;//no relevante
        this.termino = termino;
    }

    @Override
    public int compareTo(Object o) {
        Posting post = ((MergePostings)o).getPosting();
        int comparado = this.p.compareTo(post);
        if(comparado == 0){
            if(this.termino == null){
                return 0;
            }
            return this.termino.compareTo(((MergePostings)o).getTermino());
        }
        return comparado;
    }
    public Posting getPosting(){
        return this.p;
    }
    public String getTermino(){
        return this.termino;
    }

    public void avanzaPuntero(){
        this.p = this.listIterator.next();
    }

    public boolean hasNext(){
        return this.listIterator.hasNext();
    }

    @Override
    public boolean equals(Object o) {
        if (o.getClass() != this.getClass()) {
            return false;
        }
        return this.p.equals(((MergePostings)o).getPosting());
    }

    public String getDocID(){
        return this.p.getDocId();
    }
    
    public int getNTotalDocs(){
        return this.nTotal;
    }
}
