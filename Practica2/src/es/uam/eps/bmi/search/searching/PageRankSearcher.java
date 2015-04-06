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
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.PriorityQueue;

/**
 *
 * @author dani
 */
public class PageRankSearcher implements Searcher{
    private Index index;
    /**
     * Carga un índice ya creado
     * @param index se presupone index.load(...)
     */
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
        //ejecucion de una or
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
            //System.out.println(this.index.getPageRankDocId(docid));
            ScoredTextDocument scored = new ScoredTextDocument(docid, this.index.getPageRankDocId(docid));
            listaDocs.add(scored);
            if(primero.hasNext()){
                primero.avanzaPuntero();
                postingsHeap.add(primero);
            }
        }
        Collections.sort(listaDocs);
        return listaDocs;
    }
    public static void main (String[] args) {
        InteractiveSearcher.main(args, new PageRankSearcher());
    }
}
