/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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

    public MergePostings(ListIterator<Posting> listIterator, int nTotal){
        this.listIterator = listIterator;
        this.p = this.listIterator.next();
        this.nTotal = nTotal;
    }

    @Override
    public int compareTo(Object o) {
        Posting post = ((MergePostings)o).getPosting();
        return this.p.compareTo(post);
    }
    public Posting getPosting(){
        return this.p;
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
