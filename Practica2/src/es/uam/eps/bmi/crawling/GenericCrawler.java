package es.uam.eps.bmi.crawling;

import es.uam.eps.bmi.search.ScoredTextDocument;
import es.uam.eps.bmi.search.TextDocument;
import es.uam.eps.bmi.search.indexing.BasicIndex;
import es.uam.eps.bmi.search.indexing.Index;
import es.uam.eps.bmi.search.indexing.IndexBuilder;
import es.uam.eps.bmi.search.parsing.HTMLSimpleParser;
import es.uam.eps.bmi.search.parsing.TextParser;
import es.uam.eps.bmi.search.ranking.graph.PageRank;
import es.uam.eps.bmi.search.ranking.graph.PageRankTest2;
import static es.uam.eps.bmi.search.ranking.graph.PageRankTest2.showResults;
import es.uam.eps.bmi.search.searching.InteractiveSearcher;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

/**
 * Realiza el proceso de crawling de un sitio web
 *
 * @author Diego Castaño y Daniel Garnacho
 */
public class GenericCrawler {
    
    
     static ZipFile collection;
    final static int topResults = 5;
    static Index index;
    private String startUrl = "";
    private ArrayList<String> allowedDomains;
    private int maxPages = 10;
    private String destinationFolder;
    private HashSet<String> documents;

    /**
     * Constructor
     * @param startUrl URL de partida
     * @param allowedDomains dominios permitidos
     * @param folderPath carpeta destino
     * @param maxPages max. de webs a descargar
     */
    public GenericCrawler(String startUrl, ArrayList<String> allowedDomains, String folderPath, int maxPages) {
        this.startUrl = startUrl;
        this.allowedDomains = allowedDomains;
        this.maxPages = maxPages;
        this.destinationFolder = folderPath;
        this.documents = new HashSet<>();
    }
    
    /**
     * Añade un dominio permitido
     * @param domain 
     */
    public void addAllowedDomain(String domain) {
        this.allowedDomains.add(domain);
    }
    
    /**
     * Cambia el max de paginas a bajar
     * @param n 
     */
    public void setMaxPages(int n) {
        this.maxPages = n;
    }
    
    /**
     * Cambia carpeta destino
     * @param folderPath 
     */
    public void setDestinationFolder(String folderPath) {
        this.destinationFolder = folderPath;
    }

    /**
     * Comenzar crawling
     */
    public void crawl() {
        LinkedList<String> links = new LinkedList<>();
        links.add(startUrl);

        // Guardar tabla link -> id
        HashMap<String, Integer> ids = new HashMap<>();

        // Guardar tabla link -> [outlinksReales]
        HashMap<String, ArrayList<String>> allowedOutlinks = new HashMap<>();

        // Guardar tabla link -> outlinkCount
        HashMap<String, Integer> outlinkCounts = new HashMap<>();

        // Mientras no se hayan descargado N páginas distintas
        while (documents.size() < this.maxPages && links.size() > 0) {
            String link = links.poll();
            documents.add(link);
            ids.put(link, documents.size());


            // Descargar página
            String text = downloadPage(link);

            // Guardar como html con un id
            savePage(text, documents.size());

            // Sacar sus outlinks
            ArrayList<String> outlinks = getOutlinks(text);

            // Añadir a la cola solo si son del dominio
            ArrayList<String> outlinksInCollection = new ArrayList<>();
            for (String outlink : outlinks) {

                for (String domain : allowedDomains) {
                    if (outlink.contains(domain)) {
                        links.add(outlink);
                        outlinksInCollection.add(outlink);
                    }
                }
            }

            allowedOutlinks.put(link, outlinksInCollection);
            outlinkCounts.put(link, outlinks.size());
        }


        // Generar grafo de hiperenlaces (una línea por nodo visitado)
        PrintWriter writer;
        try {
            File file = new File(this.destinationFolder + "grafo.txt");
            writer = new PrintWriter(file);

            for (String link : ids.keySet()) {
                int id = ids.get(link);
                int outlinkCount = outlinkCounts.get(link);
                String linea = id + " " + outlinkCount + " ";
                for (String allowedOutlink : allowedOutlinks.get(link)) {
                    if (allowedOutlink != null && ids.get(allowedOutlink) != null) {
                        linea += ids.get(allowedOutlink) + " ";
                    }
                }
                System.out.println(linea);
                writer.println(linea);
            }
            writer.close();
       } catch (FileNotFoundException ex) {
            Logger.getLogger(GenericCrawler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Guarda el texto dado como un documnento id.html
     * @param text
     * @param id 
     */
    private void savePage(String text, int id) {
        try {
            PrintStream out = new PrintStream(new FileOutputStream(this.destinationFolder + "/" + id + ".html"));
            out.print(text);
        } catch (Exception e) {
        }
    }
    
    /**
     * Descarga una web y devuelve su texto
     * @param link
     * @return 
     */
    private String downloadPage(String link) {
        System.out.println("Descargando " + link + "...");
        URL url;
        InputStream is = null;
        BufferedReader br;
        String line;
        String text = "";
        try {
            url = new URL(link);
            is = url.openStream();  // throws an IOException
            br = new BufferedReader(new InputStreamReader(is));

            while ((line = br.readLine()) != null) {
                text += line;
            }
        } catch (MalformedURLException mue) {
        } catch (IOException ioe) {
        } finally {
            try {
                if (is != null) {
                    is.close();
                }

            } catch (IOException ioe) {
                // nothing to see here
            }
        }
        return text;
    }
    
    /**
     * Saca una lista de links de un contenido html dado
     * @param text texto html
     */
    private ArrayList<String> getOutlinks(String text) {

        ArrayList<String> outlinks = new ArrayList<>();
        Document doc = (Document) Jsoup.parse(text);
        Elements links = doc.select("a[href]"); // a with href
        for (Element link : links) {
            outlinks.add(link.absUrl("href"));
        }

        return outlinks;
    }
    
    /**
     * Añade un fichero a un zip
     * @param id
     * @param fileName
     * @param zos
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public static void addToZipFile(int id, String fileName, ZipOutputStream zos) throws FileNotFoundException, IOException {

        System.out.println("Writing " + id + ".html to zip file");

        File file = new File(fileName);
        try (FileInputStream fis = new FileInputStream(file)) {
            ZipEntry zipEntry = new ZipEntry(id + ".html");
            zos.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zos.write(bytes, 0, length);
            }

            zos.closeEntry();
            fis.close();
        }

    }

    public static void main(String args[]) throws ParserConfigurationException, SAXException, IOException {
        
             
        String settingsXML = "./src/index-settings.xml";
        System.err.println("Falta un argumento: Ruta del fichero XML de configuración.");
        System.err.println("Usando XML por defecto: " + settingsXML);
        
        File fXmlFile = new File(settingsXML);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        org.w3c.dom.Document xml = dBuilder.parse(fXmlFile);
        xml.getDocumentElement().normalize();

        String collectionPath = xml.getElementsByTagName("collection-folder").item(0).getTextContent();
        String indexPath = xml.getElementsByTagName("index-folder").item(0).getTextContent();
        String linksPath = xml.getElementsByTagName("links-doc").item(0).getTextContent();

        // Crear y configurar crawler
        ArrayList<String> domains = new ArrayList<>();
        domains.add("wired.com");
        GenericCrawler gc = new GenericCrawler("http://www.wired.com/", domains, collectionPath.split("docs.zip")[0], 100);
        
        // Empezar el crawling (Generar grafo de hiperenlances)...
        gc.crawl();

        // Hacer zip los html
        final File f = new File(collectionPath);
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f))) {
            int i = 1;
            for (String document : gc.documents) {
                addToZipFile(i, collectionPath.split("docs.zip")[0] + i + ".html", out);
                i++;
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GenericCrawler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GenericCrawler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Indexar documentos
        IndexBuilder.main(args);
        
        // Mostrar docs con mejor pagerank
        PageRankTest2.main(args);
        
    }
    
}
