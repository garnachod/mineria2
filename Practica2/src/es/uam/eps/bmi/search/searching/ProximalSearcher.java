
package es.uam.eps.bmi.search.searching;

import es.uam.eps.bmi.search.ScoredTextDocument;
import es.uam.eps.bmi.search.indexing.Index;
import es.uam.eps.bmi.search.indexing.Posting;
import es.uam.eps.bmi.search.parsing.SimpleNormalizer;
import es.uam.eps.bmi.search.parsing.SimpleTokenizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.PriorityQueue;


/**
 * Buscador proximal
 * @author Diego Castaño y Daniel Garnacho
 */
public class ProximalSearcher implements Searcher{
    Index index;
    
    @Override
    public void build(Index index) {
        this.index = index;
    }

    @Override
    public List<ScoredTextDocument> search(String query) {
        // Tokenizar consulta
        SimpleTokenizer st = new SimpleTokenizer();
        String[] tokens = st.split(query);
        
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = SimpleNormalizer.normalize(tokens[i]);
        }
        // Sacar listas de postings de cada term
        ArrayList<String> terminosFinal = SimpleNormalizer.removeNotAllowed(tokens);
        List<ScoredTextDocument> listaDocs = new ArrayList<>();
        PriorityQueue<MergePostings> postingsHeap = new PriorityQueue<>();
        
        // Sacar listas de postings de cada term
        int j = 0;
        for (String term : terminosFinal) {
            ArrayList<Posting> termPostings = new ArrayList(index.getTermPostings(term));
            if(!termPostings.isEmpty()){
                ListIterator<Posting> listIterator = termPostings.listIterator();
                MergePostings merge = new MergePostings(listIterator);
                merge.setPosTermino(j);
                postingsHeap.add(merge);
                
            }else{
                return new ArrayList<>();
            }
            j++;
        }
        
        while(!postingsHeap.isEmpty()){
            MergePostings primero = postingsHeap.poll();
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
                //ahora hay que recorrer cada posting list a la vez
                MergePostings primerMP = listMerges.get(0);
                //iterador de las posiciones del primer posting
                ListIterator<Long> listIteratorP = primerMP.getPosting().getTermPositions().listIterator();
                //generamos un array de lista de posiciones
                //no me gusta esto, un heap?
                ArrayList<List<Long>> listaDeListasDePos = new ArrayList<>();
                for(int i = 1; i < listMerges.size(); i++){
                    listaDeListasDePos.add(listMerges.get(i).getPosting().getTermPositions());
                }
                //
                //heap, insertamos las posiciones con su lista en un objeto creado a proposito
                //cogemos el menor, el primero del heap
                //cogemos todos los demás buscando el mayor
                //recorremos todos los que no sean el mayor mirando si son menores ->
                //      que el mayor sus siguente elementos si no lo son avanzamos el puntero.
                //en este paso seguimos buscando el menor
                //ya tenemos a  y  b
                //insertamos en el heap con todos sin avanzar excepto el menor
                
                           
                double score = 0.0;
                //TO DO
                String docid = primero.getDocID();
                ScoredTextDocument scored = new ScoredTextDocument(docid, score);
                listaDocs.add(scored);

            }
            //insertamos los docs en el heap de nuevo
            //esto no
            for(MergePostings mp: listMerges){
                if(mp.hasNext()){
                    mp.avanzaPuntero();
                    postingsHeap.add(mp);
                }
                
            }
            
        }
        Collections.sort(listaDocs);
        return listaDocs;
        
    }
    
    /**
     * Logaritmo en base 2
     * @param x
     * @return 
     */
    private double logBase2(double x)
    {
        return Math.log(x) / Math.log(2);
    }
    /**
    * Solicita al usuario una consulta, y muestra por pantalla
    * los top 5 resultados de la consulta imprimiendo el título y parte del
    * contenido
    *
    * @param args Ruta del fichero de config
    */
    public static void main (String[] args) {
        InteractiveSearcher.main(args, new LiteralMatchingSearcher());
    }
}
