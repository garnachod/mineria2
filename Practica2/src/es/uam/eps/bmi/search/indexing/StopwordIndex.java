/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.indexing;

import es.uam.eps.bmi.search.parsing.SimpleNormalizer;
import es.uam.eps.bmi.search.parsing.TextParser;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

/**
 *
 * @author Diego Castaño y Daniel Garnacho
 */
public class StopwordIndex extends BasicIndex {
    
    
    /**
     * Inserta un documento en el índice parcial
     * @param entry 
     */
    @Override
    protected void insertDocument(ZipEntry entry, TextParser textParser, String docId) {
          // Si es un fichero
            if (!entry.isDirectory()) {

                final String name = entry.getName();

                // Y es HTML
                if (name.endsWith(".html") || name.endsWith(".htm")) {

                    try {
                        
                        String html = super.getDocumentText(super.zip.getInputStream(entry));
                        // Parsearlo
                        String text = textParser.parse(html);
                        
                        // Tokenizar fichero
                        String [] terms = super.tokenizer.split(text);
                        //normalizar
                        for(int i = 0; i< terms.length; i++){
                            terms[i] = SimpleNormalizer.normalize(terms[i]);
                        }
                        ArrayList<String> termsList = SimpleNormalizer.removeNotAllowed(terms);
                        
                        //tabla hash del fichero
                        HashMap<String, Posting> fileIndex = new HashMap<>();
                        
                        //primero generamos las repeticiones del fichero
                        int i = 0;
                        for(String s:termsList){
                            if(fileIndex.containsKey(s)){
                                fileIndex.get(s).addTermPosition((long)i);
                            }else{
                                // si no está en el hash se crea una entrada
                                Posting postFile = new Posting(docId, 0);
                                postFile.addTermPosition((long)i);
                                fileIndex.put(s, postFile);
                            }
                            i++;
                        }
                        // insertamos el indice del fichero en el indice parcial
                        for(String term:fileIndex.keySet()){
                            if(super.partialIndex.containsKey(term)){
                                super.partialIndex.get(term).add(fileIndex.get(term));
                            }else{
                                List<Posting> listaPost = new ArrayList<>();
                                listaPost.add(fileIndex.get(term));
                                super.partialIndex.put(term, listaPost);
                                super.sortedTerms.add(term);
                             }
                            super.estimatedIndexSize += fileIndex.get(term).getBinarySizeBytes();
                        }
                        
                        // Contar frecuencias y posiciones (tabla hash<String, Posting>)
                    } catch (IOException ex) {
                        Logger.getLogger(BasicIndex.class.getName()).log(Level.SEVERE, null, ex);
                    }


                }
            }
    }

}
