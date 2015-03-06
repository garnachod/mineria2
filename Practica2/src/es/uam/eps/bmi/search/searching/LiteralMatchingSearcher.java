/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
 *
 * @author dani
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
        String[] terms = SimpleTokenizer.deleteRepeated(tokens);
        for (int i = 0; i < terms.length; i++) {
            terms[i] = SimpleNormalizer.normalize(terms[i]);
        }
        // Sacar listas de postings de cada term
        ArrayList<String> terminosFinal = SimpleNormalizer.removeNotAllowed(terms);
        List<ScoredTextDocument> listaDocs = new ArrayList<>();
        PriorityQueue<MergePostings> postingsHeap = new PriorityQueue<>();
        
        // Sacar listas de postings de cada term
        for (String term : terminosFinal) {
            ArrayList<Posting> termPostings = new ArrayList(index.getTermPostings(term));
            if(!termPostings.isEmpty()){
                ListIterator<Posting> listIterator = termPostings.listIterator();
                MergePostings merge = new MergePostings(listIterator, term);
                postingsHeap.add(merge);
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
                    if(otro.hasNext()){
                        otro.avanzaPuntero();
                        postingsHeap.add(otro);
                    }
                    break;
                }
            }
            
            if(flagMismoDoc == true && listMerges.size() != terminosFinal.size()){
                //condicion de parada 2
                //el heap ya no contiene todas las listas
                break;
            }
            //ahora hay que recorrer cada posting list a la vez
            MergePostings primerMP = listMerges.get(0);
            //iterador de las posiciones del primer posting
            ListIterator<Long> listIteratorP = primerMP.getPosting().getTermPositions().listIterator();
            //generamos un array de lista de posiciones
            ArrayList<List<Long>> listaDeListasDePos = new ArrayList<>();
            for(int i = 1; i < listMerges.size(); i++){
                listaDeListasDePos.add(listMerges.get(i).getPosting().getTermPositions());
            }
            
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
                    String docid = primero.getDocID();
                    ScoredTextDocument scored = new ScoredTextDocument(docid, 1.0);
                    listaDocs.add(scored);
                    break;
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
        return listaDocs;
        
    }
}