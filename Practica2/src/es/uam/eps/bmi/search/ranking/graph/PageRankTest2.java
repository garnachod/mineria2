/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.ranking.graph;

import es.uam.eps.bmi.search.ScoredTextDocument;
import es.uam.eps.bmi.search.TextDocument;
import es.uam.eps.bmi.search.indexing.BasicIndex;
import es.uam.eps.bmi.search.indexing.Index;
import es.uam.eps.bmi.search.parsing.HTMLSimpleParser;
import es.uam.eps.bmi.search.searching.InteractiveSearcher;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Diego Castaño y Daniel Garnacho
 */
public class PageRankTest2 {
    static ZipFile collection;
    final static int topResults = 5;
    static Index index;
    
    public static void main (String args[]) throws ParserConfigurationException, SAXException, IOException {
       
        
        String settingsXML = "./src/index-settings.xml";
        System.err.println("Falta un argumento: Ruta del fichero XML de configuración.");
        System.err.println("Usando XML por defecto: " + settingsXML);
        
        File fXmlFile = new File(settingsXML);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document xml = dBuilder.parse(fXmlFile);
        xml.getDocumentElement().normalize();

        String collectionPath = xml.getElementsByTagName("collection-folder").item(0).getTextContent();
        String indexPath = xml.getElementsByTagName("index-folder").item(0).getTextContent();
        String linksPath = xml.getElementsByTagName("links-doc").item(0).getTextContent();

        // Almacena colección para después mostrar contenido de los documentos
        collection = new ZipFile(collectionPath);
        
        System.out.println("Creando basic index...");
        String basicPath = indexPath + "/basic";
        index = new BasicIndex();
        index.load(basicPath);
       
        PageRank pr = new PageRank();
        pr.setVerbose(false);
        
        
        System.out.println("Resolviendo grafo");
        String ruta = linksPath;
        pr.loadLinks(ruta);
        System.out.println();
        showResults(pr.getTopN(topResults));
       
        
    }
    
    /**
     * Muestra el top de resultados imprimiendo el título y parte del contenido
     *
     * @param docs
     */
    private static void showResults(List<ScoredTextDocument> docs) {
        System.out.println("");
        
        if (docs.isEmpty()) {
            System.out.println("No se encontraron documentos relevantes para esa consulta.");
            return;
        } else {
            System.out.println("Mostrando los primeros " + topResults + " resultados de un total de " + docs.size() + ":\n");
        }
        
        int i = 0;
        for (ScoredTextDocument doc : docs) {
            //los documentos en pagerank no son docid
            String docid = index.nameDocToDocId(doc.getDocId());
            TextDocument document = index.getDocument(docid);
            System.out.println((i + 1) + ". " + document.getName());
            System.out.println(getSummary(document.getName()));
            System.out.println("");
            if (i == (topResults - 1)) {
                break;
            } else {
                i++;
            }
        }
    }

    /**
     * Saca un resumen del contenido de un documento
     * @param docName Nombre del documento
     * @return Resumen
     */
    private static String getSummary(String docName) {
        InputStream stream = null;
        HTMLSimpleParser parser = new HTMLSimpleParser();
        String summary = "";
        int length = 500;
        try {
            ZipEntry entry = collection.getEntry(docName);
            stream = collection.getInputStream(entry);
            String html = getStringFromInputStream(stream);
            String content = parser.parse(html);
            
            // Si hay poco contenido, cogerlo todo
            if ( content.length() < length) {
                summary = content;
            // Si hay un poco más, coger primeros n caracteres
            } else if ( content.length() < length * 3){
                summary = content.substring(0, length-1);
            // Si hay suficiente, coger contenido del medio
            } else {
                summary = content.substring(length-1, (length-1)*2);
            }
            
        } catch (IOException ex) {
            Logger.getLogger(InteractiveSearcher.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                stream.close();
            } catch (IOException ex) {
                Logger.getLogger(InteractiveSearcher.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return summary;
    }
    
    /**
     * Lee todo el contenido de un inputStream a cadena
     * @param is InputStream
     * @return Cadena
     */
    private static String getStringFromInputStream(InputStream is) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
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
