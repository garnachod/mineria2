
package es.uam.eps.bmi.search.searching;

import es.uam.eps.bmi.search.ScoredTextDocument;
import es.uam.eps.bmi.search.indexing.Index;
import es.uam.eps.bmi.search.indexing.Posting;
import es.uam.eps.bmi.search.parsing.SimpleNormalizer;
import es.uam.eps.bmi.search.parsing.SimpleTokenizer;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.PriorityQueue;

 /**
 * Buscador booleano. Permite modos AND y OR
 * @author Diego Castaño y Daniel Garnacho
 */
public class BooleanSearcher implements Searcher {
    
    Index index;
    
    /**
     * Modos de búsqueda
     */
    public enum Mode { OR, AND };
    private Mode currentMode;
    
    /**
     * Constructor
     * @param mode 
     */
    public BooleanSearcher (Mode mode) {
        this.currentMode = mode;
    }
        
    /**
     * Carga un índice ya creado
     * @param index se presupone index.load(...)
     */
    @Override
    public void build(Index index) {
        this.index = index;
    }
    
    /**
     * Realiza una búsqueda en modo AND, optimizando el uso de procesador y memoria
     * la busqueda total 100k pasa de 20 minutos a 2
     * @param terms Términos a buscar
     * @return Lista de documentos que contienen todos los términos válidos
     */
    private List<ScoredTextDocument> searchAND(String[] terms) {
        // Sacar listas de postings de cada term
        ArrayList<String> terminosFinal = SimpleNormalizer.removeNotAllowed(terms);
        List<ScoredTextDocument> listaDocs = new ArrayList<>();
        PriorityQueue<MergePostings> postingsHeap = new PriorityQueue<>();
        
        // Sacar listas de postings de cada term
        for (String term : terminosFinal) {
            ArrayList<Posting> termPostings = new ArrayList(index.getTermPostings(term));
            if(!termPostings.isEmpty()){
                ListIterator<Posting> listIterator = termPostings.listIterator();
                MergePostings merge = new MergePostings(listIterator);
                postingsHeap.add(merge);
            }else{
                return new ArrayList<>();
            }
        }
        while(!postingsHeap.isEmpty()){
            MergePostings primero = postingsHeap.poll();
            Posting auxPosting = primero.getPosting();
            ArrayList<MergePostings> listMerges = new ArrayList<>();
            listMerges.add(primero);
            boolean flagMismoDoc = true;
            while(!postingsHeap.isEmpty()){
                MergePostings otro = postingsHeap.poll();
                
                if(primero.equals(otro)){
                    listMerges.add(otro);
                }else{
                    flagMismoDoc = false;
                    postingsHeap.add(otro);
                    break;
                }
            }
            
            if(flagMismoDoc == true && listMerges.size() != terminosFinal.size()){
                //condicion de parada 2
                //el heap ya no contiene todas las listas
                break;
            }
            
            if(flagMismoDoc == true){
                MergePostings primerMP = listMerges.get(0);
                listaDocs.add(new ScoredTextDocument(primerMP.getDocID(), 1));
            }
            //insertamos los docs en el heap de nuevo
            for(MergePostings mp: listMerges){
                if(mp.hasNext()){
                    mp.avanzaPuntero();
                    postingsHeap.add(mp);
                }
                
            }
        }
        
        
        return listaDocs;
    }
    
    
     /**
     * Realiza una búsqueda en modo OR
     * @param terms Términos a buscar
     * @return Lista de documentos que contienen alguno de los términos válidos
     */
    private List<ScoredTextDocument> searchOR(String[] terms) {
        List<ScoredTextDocument> listaDocs = new ArrayList<>();
        PriorityQueue<MergePostings> postingsHeap = new PriorityQueue<>();
        
        // Sacar listas de postings de cada term
        for (String term : SimpleNormalizer.removeNotAllowed(terms)) {
            ArrayList<Posting> termPostings = new ArrayList(index.getTermPostings(term));
            if(!termPostings.isEmpty()){
                ListIterator<Posting> listIterator = termPostings.listIterator();
                MergePostings merge = new MergePostings(listIterator, termPostings.size());
                postingsHeap.add(merge);
            }
        }
        
        //mientras que no se hayan terminado todas las listas
        while(!postingsHeap.isEmpty()){
            MergePostings primero = postingsHeap.poll();
            while(!postingsHeap.isEmpty()){
                MergePostings otro = postingsHeap.poll();

                if(primero.equals(otro)){
                    if(otro.hasNext()){
                        otro.avanzaPuntero();
                        postingsHeap.add(otro);
                    }
                }else{
                    postingsHeap.add(otro);
                    break;
                }
            }
            String docid = primero.getDocID();
            ScoredTextDocument scored = new ScoredTextDocument(docid, 1.0);
            listaDocs.add(scored);
            if(primero.hasNext()){
                primero.avanzaPuntero();
                postingsHeap.add(primero);
            }
        }
        return listaDocs;
    }


    @Override
    public List<ScoredTextDocument> search(String query) {
        
        // Tokenizar consulta
        SimpleTokenizer st = new SimpleTokenizer();
        String[] tokens = st.split(query);
        String[] terms = SimpleTokenizer.deleteRepeated(tokens);
        for (int i = 0; i < terms.length; i++) {
            terms[i] = SimpleNormalizer.normalize(terms[i]);
        }
        
        
        if (currentMode.equals(Mode.AND)) {
            //return this.searchAND(terms);
            return this.searchAND(terms);
        } else {
            return this.searchOR(terms);
        }   
    }
    
    /**
    * Solicita al usuario una consulta, y muestra por pantalla
    * los top 5 resultados de la consulta imprimiendo el título y parte del
    * contenido
    *
    * @param args Ruta del fichero de config
    */
    public static void main (String[] args) {
        InteractiveSearcher.main(args, new BooleanSearcher(Mode.AND));
    }
}


