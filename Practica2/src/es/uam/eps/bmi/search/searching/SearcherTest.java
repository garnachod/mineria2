
package es.uam.eps.bmi.search.searching;

import es.uam.eps.bmi.search.RelevanceUtils;
import es.uam.eps.bmi.search.ScoredTextDocument;
import es.uam.eps.bmi.search.indexing.AdvancedIndex;
import es.uam.eps.bmi.search.indexing.BasicIndex;
import es.uam.eps.bmi.search.indexing.Index;
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
        
        //IndexBuilder.main(args);
        
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

            // Se crean los 4 buscadores
            BooleanSearcher booleanAND = new BooleanSearcher(BooleanSearcher.Mode.AND);
            BooleanSearcher booleanOR = new BooleanSearcher(BooleanSearcher.Mode.OR);
            LiteralMatchingSearcher literal = new LiteralMatchingSearcher();
            TFIDFSearcher tfidf = new TFIDFSearcher();            
            
            // Almacenar consultas
            ArrayList<String> queries = RelevanceUtils.parseQueries(queriesPath);
            
            // Almacenar documentos relevantes para cada consulta
            ArrayList<ArrayList<String>> relevantFilenames = RelevanceUtils.parseRelevantFilenames(relevantDocsPath);
            
            // Para todas las combinaciones imprimir p@5 y p@10 promedio
            System.out.println("Combinación\tP@5\tP@10");
            
            // Genera basic index
            String basicPath = indexPath + "/basic"; 
            BasicIndex basic = new BasicIndex();
            basic.load(basicPath);
        
            // Basic Index
            System.out.println("Basic + Boolean AND\t" + getPrecisionResults(basic, booleanAND, queries, relevantFilenames));
            System.out.println("Basic + Boolean OR\t" + getPrecisionResults(basic, booleanOR, queries, relevantFilenames));
            System.out.println("Basic + Literal\t" + getPrecisionResults(basic, literal, queries, relevantFilenames));
            System.out.println("Basic + TFIDF\t" + getPrecisionResults(basic, tfidf, queries, relevantFilenames));
            
            // Free index
            basic = null;
            System.gc();
            
            // Genera stopword index
            String stopwordsList = "./src/stop-words.txt";
            String stopwordPath = indexPath + "/stopword"; 
            StopwordIndex stopword = new StopwordIndex(stopwordsList);
            stopword.load(stopwordPath);
            
            // Stopword Index
            System.out.println("Stopword + Boolean AND\t" + getPrecisionResults(stopword, booleanAND, queries, relevantFilenames));
            System.out.println("Stopword + Boolean OR\t" + getPrecisionResults(stopword, booleanOR, queries, relevantFilenames));
            System.out.println("Stopword + Literal\t" + getPrecisionResults(stopword, literal, queries, relevantFilenames));
            System.out.println("Stopword + TFIDF\t" + getPrecisionResults(stopword, tfidf, queries, relevantFilenames));
            
            // Free index
            stopword = null;
            System.gc();
            
            // Genera stem index
            String stemPath = indexPath + "/stem"; 
            StemIndex stem = new StemIndex();
            stem.load(stemPath);
            
            // Stem Index
            System.out.println("Stem + Boolean AND\t" + getPrecisionResults(stem, booleanAND, queries, relevantFilenames));
            System.out.println("Stem + Boolean OR\t" + getPrecisionResults(stem, booleanOR, queries, relevantFilenames));
            System.out.println("Stem + Literal\t" + getPrecisionResults(stem, literal, queries, relevantFilenames));
            System.out.println("Stem + TFIDF\t" + getPrecisionResults(stem, tfidf, queries, relevantFilenames));
            
            // Free index
            stem = null;
            System.gc();
            
            // Genera advanced index
            String advancedPath = indexPath + "/advanced"; 
            AdvancedIndex advanced = new AdvancedIndex(stopwordsList);
            advanced.load(advancedPath);
            
            // Advanced Index
            System.out.println("Advanced + Boolean AND\t" + getPrecisionResults(advanced, booleanAND, queries, relevantFilenames));
            System.out.println("Advanced + Boolean OR\t" + getPrecisionResults(advanced, booleanOR, queries, relevantFilenames));
            System.out.println("Advanced + Literal\t" + getPrecisionResults(advanced, literal, queries, relevantFilenames));
            System.out.println("Advanced + TFIDF\t" + getPrecisionResults(advanced, tfidf, queries, relevantFilenames));
            
            // Free index
            advanced = null;
            System.gc();
            
            
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(IndexBuilder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(IndexBuilder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(IndexBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static String getPrecisionResults (Index i, Searcher s, ArrayList<String> queries, ArrayList<ArrayList<String>> relevantFilenames) {
            
            // Calcular precisiones para cada consulta en Basic index + Boolean AND 
            s.build(i);
            RelevanceUtils.setIndex(i);
            
            ArrayList<Double> p5s = new ArrayList<>();
            ArrayList<Double> p10s = new ArrayList<>();
            
            for (int j = 0; j < queries.size(); j++) {
                List<ScoredTextDocument> results = s.search(queries.get(j));
                p5s.add(RelevanceUtils.calculatePrecision(results, relevantFilenames.get(j), 5));
                p10s.add(RelevanceUtils.calculatePrecision(results, relevantFilenames.get(j), 10));
            }
            
            Double averageP5 = Math.round(100.0 * RelevanceUtils.calculateAverage(p5s)) / 100.0;
            Double averageP10 = Math.round(100.0 * RelevanceUtils.calculateAverage(p10s)) / 100.0;
            return averageP5 + "\t" + averageP10;
    }
}
