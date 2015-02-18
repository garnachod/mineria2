
package es.uam.eps.bmi.search.searching;

import es.uam.eps.bmi.search.ScoredTextDocument;
import es.uam.eps.bmi.search.indexing.Index;
import java.util.List;

/**
 * @author Diego Castaño y Daniel Garnacho
 */
public interface Searcher {
    
    /**
     * Crea el buscador a partir del índice pasado como argumento de entrada
     * @param index índice con el que crear el buscador
     */
    void build (Index index);
    
    /**
     * Que devolverá un ranking (ordenado por score decreciente) de documentos, 
     * resultantes de ejecutar una consultada dada sobre el índice del buscador.
     * 
     * @param query Consulta a ejecutar
     * @return ranking (ordenado por score decreciente) de documentos
     */
    List<ScoredTextDocument> search (String query);
}
