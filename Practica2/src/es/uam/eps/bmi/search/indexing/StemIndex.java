package es.uam.eps.bmi.search.indexing;

import es.uam.eps.bmi.search.indexing.stemmer.SnowballStemmer;
import es.uam.eps.bmi.search.indexing.stemmer.ext.englishStemmer;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

 /**
 *
 * @author Diego Castaño y Daniel Garnacho
 */
public class StemIndex extends BasicIndex {
    SnowballStemmer stemmer;

    public StemIndex() {
        this.stemmer = (SnowballStemmer)new englishStemmer();
    }

    @Override
    protected String normalize(String term) {
        stemmer.setCurrent(super.normalize(term));
	stemmer.stem();
	return stemmer.getCurrent();
    }
    
    @Override
    public List<Posting> getTermPostings(String term){
        try {
            stemmer.setCurrent(term);
            stemmer.stem();
            if(this.indexRAMBusqueda.containsKey(stemmer.getCurrent())){
                if(this.indexFile == null){
                    String nombreFichero = this.outputIndexPath + "\\indexed.data";
                    this.indexFile = new DataInputStream(new BufferedInputStream(new FileInputStream(nombreFichero)));
                }
                List<Posting> listaRetorno = Posting.readListPostingsByPos(this.indexFile, this.indexRAMBusqueda.get(term));
                this.indexFile.close();
                this.indexFile = null;
                return listaRetorno;
            }else{
                return null;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
