
package es.uam.eps.bmi.search.searching;

import es.uam.eps.bmi.search.ScoredTextDocument;
import es.uam.eps.bmi.search.indexing.Index;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;


/**
 * @author Diego Castaño y Daniel Garnacho
 */
public class LuceneSearcher implements Searcher {
    // Inspeccionar el índice
    private IndexReader ireader = null;
    private QueryParser qParser = null;
    private IndexSearcher isearcher = null;
    private int topDocs = 1000;
    
    /**
     * Crea el buscador a partir del índice pasado como argumento de entrada
     * @param index índice con el que crear el buscador
     */
    @Override
    public void build(Index index) {
        
        try{
            
            //apertura del indice
            Directory directory = new SimpleFSDirectory(new File(index.getPath()));
            this.ireader = IndexReader.open(directory);
            this.isearcher = new IndexSearcher(this.ireader);
            //query parser, parsea igual que el creador de indices.
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_31);
            this.qParser = new QueryParser(Version.LUCENE_31, "content", analyzer);
            
        }catch(Exception e){
             Logger.getLogger(LuceneSearcher.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * Que devolverá un ranking (ordenado por score decreciente) de documentos, 
     * resultantes de ejecutar una consultada dada sobre el índice del buscador.
     * 
     * @param query Consulta a ejecutar
     * @return ranking (ordenado por score decreciente) de documentos
     */
    @Override
    public List<ScoredTextDocument> search(String query) {
        ArrayList<ScoredTextDocument> scoredTDList = new ArrayList<>();
        try{
            Query q = this.qParser.parse(query);
            
            ScoreDoc result[] = this.isearcher.search(q, this.topDocs).scoreDocs;
            
            for(int i = 0; i < result.length; i++){
                //hacer un cambio entre identificador de documento 
                //a String del documento
                ScoredTextDocument sctd = new ScoredTextDocument(result[i].doc+"" , result[i].score);
                scoredTDList.add(sctd);
            }
            
        }catch(Exception e){
            Logger.getLogger(LuceneSearcher.class.getName()).log(Level.SEVERE, null, e);
        }
        return scoredTDList;
    }
    
}
