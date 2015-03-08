
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
    private int posTermino = 0;

    public MergePostings(ListIterator<Posting> listIterator, int nTotal){
        this.listIterator = listIterator;
        this.p = this.listIterator.next();
        this.nTotal = nTotal;
    }
    public MergePostings(ListIterator<Posting> listIterator){
        this.listIterator = listIterator;
        this.p = this.listIterator.next();
        this.nTotal = 0;//no relevante
    }

    public void setPosTermino(int posTermino){
        this.posTermino = posTermino;
    }
    @Override
    public int compareTo(Object o) {
        Posting post = ((MergePostings)o).getPosting();
        int comparado = this.p.compareTo(post);
        if(comparado == 0){
            return this.posTermino - ((MergePostings)o).posTermino;
        }
        return comparado;
    }
    public Posting getPosting(){
        return this.p;
    }
    public int posTermino(){
        return this.posTermino;
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
