
package es.uam.eps.bmi.search.indexing;


import es.uam.eps.bmi.search.parsing.HTMLSimpleParser;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * @author Diego Castaño y Daniel Garnacho
 */
public class IndexBuilder {
    
    /**
     * Genera índices de 4 tipos posibles dado un fichero de config.
     * @param args Ruta del fichero de config
     */
    public static void main (String args[]) {
        
        HTMLSimpleParser parser = new HTMLSimpleParser();
        
        try {        
            
            File fXmlFile = new File("./src/index-settings.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
     
            String collectionPath = doc.getElementsByTagName("collection-folder").item(0).getTextContent();
            String indexPath = doc.getElementsByTagName("index-folder").item(0).getTextContent();
            
            
            // Genera basic index
            System.out.println("Creando basic index");
            String basicPath = indexPath + "/basic"; 
            File basicFolder = new File(basicPath);
            basicFolder.mkdir();
            BasicIndex basic = new BasicIndex();
            basic.build(collectionPath, basicPath, parser);
            basic.load(basicPath);
            
            // Genera stopword index
            System.out.println("Creando stopwords index");
            String stopwordsList = "./src/stop-words.txt";
            String stopwordPath = indexPath + "/stopword"; 
            File stopwordFolder = new File(stopwordPath);
            stopwordFolder.mkdir();
            StopwordIndex stopword = new StopwordIndex(stopwordsList);
            stopword.build(collectionPath, stopwordPath, parser);
            stopword.load(stopwordPath);
            
            // Genera stem index
            System.out.println("Creando stem index");
            String stemPath = indexPath + "/stem"; 
            File stemFolder = new File(stemPath);
            stemFolder.mkdir();
            StemIndex stem = new StemIndex();
            stem.build(collectionPath, basicPath, parser);
            stem.load(basicPath);
            
            // Genera advanced index
            System.out.println("Creando advanced index");
            
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(IndexBuilder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(IndexBuilder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(IndexBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
