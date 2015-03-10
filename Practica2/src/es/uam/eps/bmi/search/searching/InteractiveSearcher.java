package es.uam.eps.bmi.search.searching;

import es.uam.eps.bmi.search.ScoredTextDocument;
import es.uam.eps.bmi.search.TextDocument;
import es.uam.eps.bmi.search.indexing.BasicIndex;
import es.uam.eps.bmi.search.indexing.Index;
import es.uam.eps.bmi.search.indexing.IndexBuilder;
import es.uam.eps.bmi.search.parsing.HTMLSimpleParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.lucene.util.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Contiene main genérico para probar todos los buscadores
 * @author Diego Castaño y Daniel Garnacho
 */
public class InteractiveSearcher {

    /**
     * Número de resultados a mostrar del top
     */
    final static int topResults = 5;
    static Index index;
    static ZipFile collection;

    /**
     * Main genérico: Solicita al usuario una consulta, y muestra por pantalla
     * los top 5 resultados de la consulta imprimiendo el título y parte del
     * contenido
     *
     * @param args Ruta del fichero de config
     * @param s Buscador a utilizar
     */
    public static void main(String args[], Searcher s) {

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
            Document xml = dBuilder.parse(fXmlFile);
            xml.getDocumentElement().normalize();

            String collectionPath = xml.getElementsByTagName("collection-folder").item(0).getTextContent();
            String indexPath = xml.getElementsByTagName("index-folder").item(0).getTextContent();

            // Almacena colección para después mostrar contenido de los documentos
            collection = new ZipFile(collectionPath);

            // Genera basic index
            System.out.println("Creando basic index...");
            String basicPath = indexPath + "/basic";
            File basicFolder = new File(basicPath);
            basicFolder.mkdir();
            index = new BasicIndex();
            index.build(collectionPath, basicPath, parser);
            index.load(basicPath);

            // Lo carga en el buscador
            s.build(index);

            // Solicita consulta de forma iterativa
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                System.out.println("Introduce una consulta o \"s\" para salir:");
                String query = br.readLine();
                if (query.equals("s")) {
                    return;
                }
                showResults(s.search(query));
            }

        } catch (ParserConfigurationException ex) {
            Logger.getLogger(IndexBuilder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(IndexBuilder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(IndexBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
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
            TextDocument document = index.getDocument(doc.getDocId());
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
