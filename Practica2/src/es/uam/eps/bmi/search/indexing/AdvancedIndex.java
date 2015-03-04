
package es.uam.eps.bmi.search.indexing;

import java.io.FileNotFoundException;
import java.util.ArrayList;


/**
 * @author Diego Castaño y Daniel Garnacho
 */
public class AdvancedIndex extends StemIndex{
    
    private StopwordIndex si;
    
    public AdvancedIndex (String stopwordsFilePath) throws FileNotFoundException {
        super();
        si = new StopwordIndex(stopwordsFilePath);
    }
    
    @Override
    protected ArrayList<String> removeNotAllowed(String terms[]) {
        return si.removeNotAllowed(terms);
    }

}
