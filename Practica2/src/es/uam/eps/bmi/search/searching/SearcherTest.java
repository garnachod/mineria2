
package es.uam.eps.bmi.search.searching;

import es.uam.eps.bmi.search.RelevanceUtils;
import es.uam.eps.bmi.search.ScoredTextDocument;
import es.uam.eps.bmi.search.indexing.AdvancedIndex;
import es.uam.eps.bmi.search.indexing.BasicIndex;
import es.uam.eps.bmi.search.indexing.IndexBuilder;
import es.uam.eps.bmi.search.indexing.StemIndex;
import es.uam.eps.bmi.search.indexing.StopwordIndex;
import es.uam.eps.bmi.search.parsing.HTMLSimpleParser;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


/**
 * @author Diego Castaño y Daniel Garnacho
 */
public class SearcherTest {

    /**
     * Genera índices de 4 tipos posibles dado un fichero de config.
     * Crea 4 buscadores: Booleano-AND, Booleano-OR, Literal y TDIDF
     * @param args Ruta del fichero de config
     */
    public static void main (String args[]) {
        
        HTMLSimpleParser parser = new HTMLSimpleParser();
        String settingsXML;
        
        if (args.length < 1) {
            settingsXML = "./src/index-settings.xml";
            System.err.println("Falta un argumento: Ruta del fichero XML de configuración.");
            System.err.println("Usando XML por defecto: " + settingsXML);
        } else {
            settingsXML = args[0];
        }
        
        try {        
            
            File fXmlFile = new File(settingsXML);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
     
            String collectionPath = doc.getElementsByTagName("collection-folder").item(0).getTextContent();
            String indexPath = doc.getElementsByTagName("index-folder").item(0).getTextContent();
            
            // Se presupone que el fichero de queries y docs relevantes está en la misma carpeta que la colección 
            String queriesPath = collectionPath.replaceAll("docs.zip", "queries.txt");
            String relevantDocsPath = collectionPath.replaceAll("docs.zip", "relevance.txt");
                    
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
            String advancedPath = indexPath + "/advanced"; 
            File advancedFolder = new File(advancedPath);
            advancedFolder.mkdir();
            AdvancedIndex advanced = new AdvancedIndex(stopwordsList);
            advanced.build(collectionPath, advancedPath, parser);
            advanced.load(advancedPath);
            
            // Se crean los 4 buscadores
            BooleanSearcher booleanAND = new BooleanSearcher(BooleanSearcher.Mode.AND);
            BooleanSearcher booleanOR = new BooleanSearcher(BooleanSearcher.Mode.OR);
            LiteralMatchingSearcher literal = new LiteralMatchingSearcher();
            TFIDFSearcher tfidf = new TFIDFSearcher();            
            
            // Almacenar consultas
            ArrayList<String> queries = RelevanceUtils.parseQueries(queriesPath);
            
            // Almacenar documentos relevantes para cada consulta
            ArrayList<ArrayList<String>> relevantFilenames = RelevanceUtils.parseRelevantFilenames(relevantDocsPath);
            
            // TODO: Para todas las combinaciones:
            
            // Calcular precisiones para cada consulta en Basic index + Boolean AND 
            booleanAND.build(basic);
            RelevanceUtils.setIndex(basic);
            
            ArrayList<Double> p5s = new ArrayList<>();
            ArrayList<Double> p10s = new ArrayList<>();
            
            for (int i = 0; i < queries.size(); i++) {
                List<ScoredTextDocument> results = booleanAND.search(queries.get(i));
                p5s.add(RelevanceUtils.calculatePrecision(results, relevantFilenames.get(i), 5));
                p10s.add(RelevanceUtils.calculatePrecision(results, relevantFilenames.get(i), 10));
            }
            
            double averageP5 = RelevanceUtils.calculateAverage(p5s);
            double averageP10 = RelevanceUtils.calculateAverage(p10s);
            
            System.out.println("Basic Index\t" + averageP5 + "\t" + averageP10);
            
             
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(IndexBuilder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(IndexBuilder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(IndexBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
