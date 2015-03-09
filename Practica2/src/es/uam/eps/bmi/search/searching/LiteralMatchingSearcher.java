
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
 * @author Diego Castaño y Daniel Garnacho
 */
public class LiteralMatchingSearcher implements Searcher{
    Index index;
    
    
    public LiteralMatchingSearcher(){
        
    }
    
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
        System.out.println(terminosFinal);
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
                //ahora hay que recorrer cada posting list a la vez
                MergePostings primerMP = listMerges.get(0);
                //iterador de las posiciones del primer posting
                ListIterator<Long> listIteratorP = primerMP.getPosting().getTermPositions().listIterator();
                //generamos un array de lista de posiciones
                ArrayList<List<Long>> listaDeListasDePos = new ArrayList<>();
                for(int i = 1; i < listMerges.size(); i++){
                    listaDeListasDePos.add(listMerges.get(i).getPosting().getTermPositions());
                }

                double tf = 0;
                int nRepeticionesBueno = 0;
                //idf no tiene sentido porque siempre vamos a multiplicar por el mismo valor
                while(listIteratorP.hasNext()){
                    long next = listIteratorP.next();
                    int i = 1;
                    boolean flagBuenDoc = true;
                    for(List<Long> listaDePos : listaDeListasDePos){
                        if(listaDePos.contains(next + i)){
                            flagBuenDoc = true;
                        }else{
                            flagBuenDoc = false;
                        }
                        i++;
                    }
                    if(flagBuenDoc = true){
                        nRepeticionesBueno++;
                    }
                }
                if(nRepeticionesBueno > 0){
                    String docid = primero.getDocID();
                    tf = 1 + this.logBase2(nRepeticionesBueno);
                    double tf_idf = tf/this.index.getBytesDocument(docid);
                    ScoredTextDocument scored = new ScoredTextDocument(docid, tf_idf);
                    listaDocs.add(scored);
                }
            }
            //insertamos los docs en el heap de nuevo
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
