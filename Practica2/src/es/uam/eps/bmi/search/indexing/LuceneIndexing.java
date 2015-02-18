
package es.uam.eps.bmi.search.indexing;

import es.uam.eps.bmi.search.TextDocument;
import es.uam.eps.bmi.search.parsing.HTMLSimpleParser;
import es.uam.eps.bmi.search.parsing.TextParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

/**
 * @author Diego Castaño y Daniel Garnacho
 */
public class LuceneIndexing implements Index {
    
    // Indice en RAM
    String path = "";
    IndexReader ireader = null;
    
    /**
     * Construye un índice a partir de una colección de documentos de texto plano.
     * Usando Lucene
     * 
     * @param inputCollectionPath ruta de la carpeta en la que se encuentran los documentos a indexar
     * @param outputIndexPath la ruta de la carpeta en la que almacenar el índice creado
     * @param textParser parser de texto que procesará el texto de los documentos para su indexación
     */
    @Override
    public void build(String inputCollectionPath, String outputIndexPath, TextParser textParser) {
        
        try {
            
            Directory dir = FSDirectory.open(new File(outputIndexPath));
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_31);
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_31, analyzer);
            ZipFile zip = new ZipFile(inputCollectionPath);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            
            // Crear o reemplazar un indice 
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            iwc.setRAMBufferSizeMB(512.0);
            IndexWriter writer = new IndexWriter(dir, iwc);

            while (entries.hasMoreElements()) {
                
                ZipEntry entry = entries.nextElement();
                
                // Si es un fichero
                if (!entry.isDirectory()) {
                    final String name = entry.getName();
                    
                    // Y es HTML
                    if (name.endsWith(".html") || name.endsWith(".htm")) {
                        
                        // Leer su contenido
                        String html = this.getDocumentText(zip.getInputStream(entry));
                        
                        // Parsearlo
                        String text = textParser.parse(html);
                        
                        // Añadirlo al índice
                        Document doc = new Document();
                        
                        // Nombre (almacenar)
                        Field nameField = new Field("name", name, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
                        doc.add(nameField);
                        
                        // Contenido (no almacenar)
                        Field textField = new Field("content", text, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS);
                        doc.add(textField);
                        
                        writer.addDocument(doc);
                    }
                }
            }
            writer.close();

           
        } catch (Exception e) {
            System.out.println("No se pudo acceder a la colección \"" + inputCollectionPath + "\"");
        }
    }
    /**
     * Cargará en RAM (parcial o completamente) un índice creado previamente,
     * y que se encuentra almacenado en la carpeta cuya ruta se pasa como argumento de entrada. 
     * Usando Lucene
     * 
     * @param indexPath ruta del indice creado previamente
     */
    @Override
    public void load(String indexPath) {
        try {
            Directory directory = new SimpleFSDirectory(new File(indexPath));
            this.ireader = IndexReader.open(directory);
            this.path = indexPath;
        } catch (IOException ex) {
             System.out.println("No se pudo acceder al índice \"" + indexPath + "\"");
        }
    }
    /**
     * Devuelve los identificadores de los documentos indexados
     * 
     * @return  identificadores de los documentos indexados
     */
    @Override
    public List<String> getDocIds() {
        int count = ireader.numDocs();        
        ArrayList<String> idList = new ArrayList<>();
        for (int docID = 0; docID < count; docID++) {
            String id = docID + "";
            idList.add(id);
        }
        return idList;
    }

    /**
     *  Devuelve el documento del identificador dado
     * 
     * @param docId identificador del documento
     * @return documento dado el identificador
     */
    @Override
    public TextDocument getDocument(String docId) {
        try {
            String name = ireader.document(Integer.parseInt(docId)).get("name");
            return new TextDocument(docId, name);
        } catch (IOException ex) {
            System.out.println("Id no válido");
        }
        return null;
    }
    /**
     * Devuelve la lista de términos extraídos de los documentos indexados
     * 
     * @return lista de términos extraídos
     */
    @Override
    public List<String> getTerms() {
        ArrayList<String> termList = new ArrayList<>();
        try {
            
            TermEnum terms = ireader.terms();
            
            while (terms.next()) {
                termList.add(terms.term().text());
            }
            return termList;
        } catch (IOException ex) {
            Logger.getLogger(LuceneIndexing.class.getName()).log(Level.SEVERE, null, ex);
        }
        return termList;
    }
    
    /**
     * Devuelve los postings de un término dado
     * 
     * @param term Termino a buscar para devolver sus postings
     * @return lista de postings de un termino
     */
    @Override
    public List<Posting> getTermsPosting(String term) {
        ArrayList<Posting> postingList = new ArrayList<>();
        try {
           
            TermDocs termDocs = ireader.termDocs(new Term("content", term));
            TermPositions termPositions = ireader.termPositions(new Term("content", term));
            //si se usa seek termDocs se borra
            //termDocs.seek(new Term(term));
           
            while(termDocs.next()) {
               
                int docId = termDocs.doc();
                int freq = termDocs.freq();
                ArrayList<Long> positions = new ArrayList<>();
                while (termPositions.next()) {
                    positions.add((long)termPositions.nextPosition());
                }
                Posting p = new Posting(docId + "", freq, positions);
                postingList.add(p);
            }
            return postingList;
        } catch (IOException ex) {
            Logger.getLogger(LuceneIndexing.class.getName()).log(Level.SEVERE, null, ex);
        }
        return postingList;
    }
    
    
    @Override
    public String getPath() {
        return this.path;
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
    
    /*
     * Recibe dos argumentos: Ruta a docs.zip y ruta donde almacenar el índice
     */
    public static void main (String args[]) {
        
        if (args.length != 2) {
            System.out.println("Recibe dos argumentos: Ruta a docs.zip y ruta donde almacenar el índice");
            return;
        }

        LuceneIndexing li = new LuceneIndexing();
        TextParser tp = new HTMLSimpleParser();
        System.out.println("Construyendo índice...");
        li.build(args[0], args[1], tp);
  
        System.out.println("Cargando índice en RAM...");
        li.load(args[1]);
        
        System.out.println("Lista de ids: ");
        System.out.println(li.getDocIds());
        
        System.out.println("Lista de TextDocuments: ");
        for (String id : li.getDocIds()) {
            TextDocument document = li.getDocument(id);
            System.out.println(document);
        }
        
        System.out.println("Lista de terminos: ");
        for (String term: li.getTerms()) {
            System.out.println("Posting para termino " + term);
            for (Posting p : li.getTermsPosting(term)) {
                System.out.println(p.getDocId() + " " + p.getTermFrequency() + " " + p.getTermPositions());
            }
        }

    }

}
