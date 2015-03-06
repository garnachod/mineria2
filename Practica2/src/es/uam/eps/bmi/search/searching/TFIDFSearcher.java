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
 * @author Diego Castaño y Daniel Garnacho
 */
public class TFIDFSearcher implements Searcher {
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
                MergePostings merge = new MergePostings(listIterator, termPostings.size());
                postingsHeap.add(merge);
            }
        }
        
        long totalDocs = this.index.getNDocsIndex();
        
        //mientras que no se hayan terminado todas las listas
        while(!postingsHeap.isEmpty()){
            MergePostings primero = postingsHeap.poll();
            Posting auxPosting = primero.getPosting();
            double tf = 1 + this.logBase2(auxPosting.getTermFrequency());
            double idf = this.logBase2(totalDocs/primero.getNTotalDocs());
            double tf_idfAux = 0;
            tf_idfAux += tf*idf;
            while(!postingsHeap.isEmpty()){
                MergePostings otro = postingsHeap.poll();
                if(primero.equals(otro)){
                    auxPosting = otro.getPosting();
                    tf = 1 + this.logBase2(auxPosting.getTermFrequency());
                    idf = this.logBase2(totalDocs/primero.getNTotalDocs());
                    tf_idfAux += tf*idf;
                    
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
            double nomalizedtf_idf = tf_idfAux/this.index.getBytesDocument(docid);
            ScoredTextDocument scored = new ScoredTextDocument(docid, nomalizedtf_idf);
            listaDocs.add(scored);
            if(primero.hasNext()){
                primero.avanzaPuntero();
                postingsHeap.add(primero);
            }
        }
        
        return listaDocs;
    }
    private double logBase2(double x)
    {
        return Math.log(x) / Math.log(2);
    }
}
