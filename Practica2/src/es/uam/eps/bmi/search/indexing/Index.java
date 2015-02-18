
package es.uam.eps.bmi.search.indexing;

import es.uam.eps.bmi.search.TextDocument;
import es.uam.eps.bmi.search.parsing.TextParser;
import java.util.List;

/**
 * @author Diego Castaño y Daniel Garnacho
 */
public interface Index {
    
    /**
     * Construye un índice a partir de una colección de documentos de texto plano.
     * 
     * @param inputCollectionPath ruta de la carpeta en la que se encuentran los documentos a indexar
     * @param outputIndexPath la ruta de la carpeta en la que almacenar el índice creado
     * @param textParser parser de texto que procesará el texto de los documentos para su indexación
     */
    void build (String inputCollectionPath, String outputIndexPath, TextParser textParser);
    
    /**
     * Cargará en RAM (parcial o completamente) un índice creado previamente,
     * y que se encuentra almacenado en la carpeta cuya ruta se pasa como argumento de entrada. 
     * 
     * @param indexPath ruta del indice creado previamente
     */
    void load (String indexPath);
    
    /**
     * Devuelve los identificadores de los documentos indexados
     * 
     * @return  identificadores de los documentos indexados
     */
    List<String> getDocIds();
    
    /**
     *  Devuelve el documento del identificador dado
     * 
     * @param docId identificador del documento
     * @return documento dado el identificador
     */
    TextDocument getDocument (String docId);
    
    /**
     * Devuelve la lista de términos extraídos de los documentos indexados
     * 
     * @return lista de términos extraídos
     */
    List<String> getTerms();
    
    /**
     * Devuelve los postings de un término dado
     * 
     * @param term Termino a buscar para devolver sus postings
     * @return lista de postings de un termino
     */
    List<Posting> getTermsPosting (String term);
    
    /**
     * Devuelve dirección del índice creado
     */
    public String getPath();
}
