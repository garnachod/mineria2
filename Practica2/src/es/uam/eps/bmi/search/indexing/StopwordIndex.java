
package es.uam.eps.bmi.search.indexing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author Diego Castaño y Daniel Garnacho
 */
public class StopwordIndex extends BasicIndex {
    
    ArrayList<String> stopwords;
    
    public StopwordIndex (String stopwordsFilePath) throws FileNotFoundException {
        super();
        this.stopwords = new ArrayList<>();
        Scanner s = new Scanner(new File(stopwordsFilePath));
        while (s.hasNext()){
            stopwords.add(s.next());
        }
        s.close();
    }
    
    @Override
    protected ArrayList<String> removeNotAllowed(String terms[]) {
        // Filtrar stopwords
        ArrayList<String> termsList = new ArrayList<>();
        
        for (String term: super.removeNotAllowed(terms)) {
            if (!stopwords.contains(term)) {
                termsList.add(term);
            }
        }
        
        return termsList;
    }


}
