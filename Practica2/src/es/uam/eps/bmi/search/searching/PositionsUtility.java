/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.searching;

import java.util.List;

/**
 *
 * @author dani y diegolin
 */
public class PositionsUtility implements Comparable{
    private List<Long> termPositions;
    private int position;
    private int longitud;
    
    public PositionsUtility(List<Long> termPositions){
        this.termPositions = termPositions;
        this.position = 0;
        this.longitud = this.termPositions.size();
    }
    
    long getPosition(){
        return this.termPositions.get(this.position);
    }
    
    long nextSinAvanzar(){
        if(this.hasNext()){
           return this.termPositions.get(this.position + 1);
        }
        
        return Long.MAX_VALUE;
    }
    
    void avanzaPuntero(){
        this.position += 1;
    }
    
    boolean hasNext(){
        return (this.position + 1) < this.getLongitud();
    }

    @Override
    public int compareTo(Object o) {
        PositionsUtility aux = (PositionsUtility) o;
        return (int)(this.getPosition() - aux.getPosition());
    }

    /**
     * @return the longitud
     */
    public int getLongitud() {
        return longitud;
    }
}
