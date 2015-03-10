
package es.uam.eps.bmi.search.searching;

import es.uam.eps.bmi.search.indexing.Posting;
import java.util.ListIterator;

/**
 * MergePostings encarga de unificar los postings y la lista de estos
 * para poder usar Heaps de una manera eficiente.
 * 
 * @author Diego Castaño y Daniel Garnacho
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
    /**
     * Inicializa la posicion del termino en la consulta, para la busqueda literal
     * @param posTermino 
     */
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
    /**
     * Retorna el posting recogido de la lista de postings interna
     * @return 
     */
    public Posting getPosting(){
        return this.p;
    }
    public int getPosTermino(){
        return this.posTermino;
    }
    /**
     * Avanza en la lista de elementos, guarda el estado en un posting temporal
     * que se va a poder consultar
     */
    public void avanzaPuntero(){
        this.p = this.listIterator.next();
    }
    /**
     * Comprueba si quedan elementos en la lista de postings
     * @return true si quedan elementos, false en caso contrario
     */
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
    /**
     * retorna el tamaño total de la lista de postings, por sencillez se pasa 
     * por parametros en el creador
     * @return tamaño total de la lista de postings
     */
    public int getNTotalDocs(){
        return this.nTotal;
    }
}
