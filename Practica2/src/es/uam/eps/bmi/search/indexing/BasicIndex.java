
package es.uam.eps.bmi.search.indexing;

import es.uam.eps.bmi.search.TextDocument;
import es.uam.eps.bmi.search.parsing.TextParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 *
 * @author Diego Castaño y Daniel Garnacho
 */
public class BasicIndex implements Index{

    final long MAX_RAM = 50 * 1024; // Min bytes of free memory
    HashMap<String, List<Posting>> partialIndex;
    PriorityQueue<String> sortedTerms;
    ZipFile zip;
    
    @Override
    public void build(String inputCollectionPath, String outputIndexPath, TextParser textParser) {
        this.partialIndex = new HashMap<>();
        try{
            zip = new ZipFile(inputCollectionPath);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                this.analyzeDocument(entry, textParser);
            }
            
        }catch(Exception e){
        }
        
    }

    @Override
    public void load(String indexPath) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<String> getDocIds() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TextDocument getDocument(String docId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<String> getTerms() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Posting> getTermsPosting(String term) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getPath() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void analyzeDocument(ZipEntry entry, TextParser textParser) {
        
        // Si queda espacio en RAM
        if (Runtime.getRuntime().freeMemory() > MAX_RAM) {
            this.insertDocument(entry, textParser);
        } else { 
            // Imprimir índice temporal en disco
            this.saveIndex();
            this.analyzeDocument(entry, textParser);
        }
    }
    
    /**
     * Inserta un documento en el índice parcial
     * @param entry 
     */
    private void insertDocument(ZipEntry entry, TextParser textParser) {
          // Si es un fichero
            if (!entry.isDirectory()) {

                final String name = entry.getName();

                // Y es HTML
                if (name.endsWith(".html") || name.endsWith(".htm")) {

                    try {
                        // Tokenizar fichero
                        textParser.parse(name);
                        String html = this.getDocumentText(zip.getInputStream(entry));
                        
                        // Parsearlo
                        String text = textParser.parse(html);
                        
                        
                        // Contar frecuencias y posiciones (tabla hash<String, Posting>)
                    } catch (IOException ex) {
                        Logger.getLogger(BasicIndex.class.getName()).log(Level.SEVERE, null, ex);
                    }


                }
            }
    }

    /**
     * Write sorted postings to file
     */
    private void saveIndex() {
        
        // Print to binary file using sortedTerms heap
        while (!this.sortedTerms.isEmpty()) {
            String term = this.sortedTerms.remove();
            
            // Try to get postings list
            List<Posting> postings = partialIndex.get(term);
            
            // Print postings to file
            
            
        }
        
        // Free memory
        this.cleanIndex();
        
        
    }

    /**
     * Free memory
     */
    private void cleanIndex() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Devuelve el texto como una cadena leyendo de un inputstream
     * 
     * @param input Devuelve el texto como una cadena leyendo de un inputstream
     * @return Cadena con todo el contenido
     */
    private String getDocumentText(InputStream input) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(input));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();    
    }
    
}
