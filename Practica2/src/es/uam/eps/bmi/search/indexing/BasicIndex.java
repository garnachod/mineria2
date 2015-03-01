
package es.uam.eps.bmi.search.indexing;

import es.uam.eps.bmi.search.TextDocument;
import es.uam.eps.bmi.search.parsing.SimpleNormalizer;
import es.uam.eps.bmi.search.parsing.SimpleTokenizer;
import es.uam.eps.bmi.search.parsing.TextParser;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
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
    private SimpleTokenizer tokenizer;
    private HashMap<String, List<Posting>> partialIndex;
    private PriorityQueue<String> sortedTerms;
    private ArrayList<String> ficherosTemporales;
    private String outputIndexPath;
    private ZipFile zip;
    private long contadorFilesProc;
    //read
    private HashMap<String, String> indexedIDtoFile;
    //String el termino y Long la posicion desde el principio del fichero
    //de la lista de postings que lo determina
    private HashMap<String, Long> indexRAMBusqueda;
    
    public BasicIndex(){
        this.tokenizer = new SimpleTokenizer();
        this.partialIndex = new HashMap<>();
        this.sortedTerms = new PriorityQueue<>();
        this.ficherosTemporales = new ArrayList<>();
        this.outputIndexPath = "";
        this.contadorFilesProc= 0;
    }
    
    @Override
    public void build(String inputCollectionPath, String outputIndexPath, TextParser textParser) {
       
        this.partialIndex = new HashMap<>();
        this.outputIndexPath = outputIndexPath;
        long contFiles = 0;
        try{
            String nombreFicheroIndexNameFiles = outputIndexPath + "\\idFileToName.data";
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(nombreFicheroIndexNameFiles)));
        
            zip = new ZipFile(inputCollectionPath);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String idFile = contFiles+"";
                this.analyzeDocument(entry, textParser, idFile);
                dos.writeUTF(idFile);
                dos.writeUTF(entry.getName());
                
                contFiles++;
                this.contadorFilesProc++;
                //return;
                //System.out.println(contFiles);
            }
            this.saveIndexFinal(outputIndexPath+"\\indexed.data");
            dos.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }

    @Override
    public void load(String indexPath) {
        this.indexedIDtoFile = new HashMap<>();
        try{
            //load idfile to name
            String nombreFichero = indexPath + "\\idFileToName.data";
            DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(nombreFichero)));
            
            while(dis.available() > 0){
                indexedIDtoFile.put(dis.readUTF(), dis.readUTF());
            }
            dis.close();
            //load index ram
            this.indexRAMBusqueda = new HashMap<>();
            nombreFichero = indexPath + "\\indexed.data";
            dis = new DataInputStream(new BufferedInputStream(new FileInputStream(nombreFichero)));
            long contPosition = 0;
            int intSize = Integer.SIZE/8;
            int longSize = Long.SIZE/8;
            while(dis.available() > 0){
                //leemos el termino y lo insertamos en la Hash
                int nPostings = 0;
                String termino = "";
                long tamPostings = 0;
                try{
                    termino = dis.readUTF();
                    //System.out.println(termino);
                    int offset = termino.getBytes("UTF-8").length+2;
                    contPosition += offset;
                        //insercion en la hash
                    this.indexRAMBusqueda.put(termino, new Long(contPosition));
                    nPostings = dis.readInt();
                    tamPostings = dis.readLong();
                    //a lo mejor comprobar que este float no ocupa mas que un int
                    //es poco probable
                    dis.skipBytes((int)tamPostings);
                    //dis.skip(tamPostings);
                    contPosition += intSize + longSize + tamPostings;
                    //System.out.println("npostings:" +nPostings+ "tamPostings" +tamPostings+ "pos"+ contPosition);
                }catch(Exception e){
                    System.out.println("termino:"+ termino +"npostings:" +nPostings+ "tamPostings" +tamPostings+ "pos"+ contPosition);
                    return;
                }
            }
            
        }catch(Exception e){
            e.printStackTrace();
        }
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

    private void analyzeDocument(ZipEntry entry, TextParser textParser, String docId) {
        
        // Si queda espacio en RAM
        if (Runtime.getRuntime().freeMemory() > MAX_RAM && this.contadorFilesProc < 1000) {
            //System.out.println(Runtime.getRuntime().freeMemory());
            this.insertDocument(entry, textParser, docId);
        } else { 
            // Imprimir índice temporal en disco
            try{
                this.contadorFilesProc = 0;
                this.saveIndexTemporal(this.getNameIndexTemporal());
                this.analyzeDocument(entry, textParser, docId);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Inserta un documento en el índice parcial
     * @param entry 
     */
    private void insertDocument(ZipEntry entry, TextParser textParser, String docId) {
          // Si es un fichero
            if (!entry.isDirectory()) {

                final String name = entry.getName();

                // Y es HTML
                if (name.endsWith(".html") || name.endsWith(".htm")) {

                    try {
                        
                        //textParser.parse(name);
                        String html = this.getDocumentText(zip.getInputStream(entry));
                        // Parsearlo
                        String text = textParser.parse(html);
                        //System.out.println(text);
                        // Tokenizar fichero
                        String [] terms = this.tokenizer.split(text);
                        //normalizar
                        for(int i = 0; i< terms.length; i++){
                            terms[i] = SimpleNormalizer.normalize(terms[i]);
                            //System.out.println(terms[i]);
                        }
                        ArrayList<String> termsList = SimpleNormalizer.removeNotAllowed(terms);
                        //tabla hash del fichero
                        HashMap<String, Posting> fileIndex = new HashMap<>();
                        //primero generamos las repeticiones del fichero
                        int i = 0;
                        for(String s:termsList){
                            //String s = terms[i];
                            if(fileIndex.containsKey(s)){
                                fileIndex.get(s).addTermPosition((long)i);
                            }else{
                                //si no está en el hash se crea una entrada
                                Posting postFile = new Posting(docId, 0);
                                postFile.addTermPosition((long)i);
                                fileIndex.put(s, postFile);
                            }
                            i++;
                        }
                        //insertamos el indice del fichero en el indice parcial
                        for(String term:fileIndex.keySet()){
                            if(this.partialIndex.containsKey(term)){
                                this.partialIndex.get(term).add(fileIndex.get(term));
                            }else{
                                List<Posting> listaPost = new ArrayList<>();
                                listaPost.add(fileIndex.get(term));
                                this.partialIndex.put(term, listaPost);
                                this.sortedTerms.add(term);
                            }
                        }
                        
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
    private void saveIndexTemporal(String nombreFichero) throws Exception {
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(nombreFichero)));
        //boolean debug = true;
        System.out.println("numero de terminos: " + this.sortedTerms.size());
        while (!this.sortedTerms.isEmpty()) {
            String term = this.sortedTerms.poll();
            /*if(debug == true){
                System.out.println(term);
            }*/
            List<Posting> postings = this.partialIndex.get(term);
            //impresión del termino
            dos.writeUTF(term);
            //impresion del tamaño en numero de elementos de la lista de postings
            dos.writeInt(postings.size());
            /*if(debug == true){
                System.out.println(postings.size());
            }*/
            //get tam en bytes
            long bytesLenght = 0;
            for(Posting post:postings){
                bytesLenght += post.getBinarySizeBytes();
            }
            dos.writeLong(bytesLenght);
            /*if(debug == true){
                System.out.println(bytesLenght);
            }*/
            //debug = false;
            //estoy pensando en imprimir en bytes el tamaño tambien para poder hacer saltos en el indice
            for(Posting post:postings){
                post.printBinary(dos);
            }
        }
        dos.close();
        //add file to lista files
        this.ficherosTemporales.add(nombreFichero);
        // Free memory
        this.cleanIndex();
    }
    private void saveIndexFinal(String nombreFichero) throws Exception {
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(nombreFichero)));
        
        if(!this.sortedTerms.isEmpty()){
            this.saveIndexTemporal(this.getNameIndexTemporal());
        }
        PriorityQueue<TemporalIndexDescriptor> sortedTID = new PriorityQueue<>();
        System.out.println("creando cola de prioridad");
        int i = 1;
        for(String nombreITemp : this.ficherosTemporales){
            DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(nombreITemp)));
            TemporalIndexDescriptor tid = new TemporalIndexDescriptor(dis, i);
            tid.readTermino();
            sortedTID.add(tid);
            i++;
        }
        //boolean debug = true;
        while(!sortedTID.isEmpty()){
            //System.out.println("iterando");
            ArrayList<TemporalIndexDescriptor> temporalTIDList = new ArrayList<>();
            TemporalIndexDescriptor primero = sortedTID.poll();
            temporalTIDList.add(primero);
            //System.out.println(primero.getTermino() +" tam:" +primero.getTamBytesPostings());
            /*if(primero.getTermino().equals("about")){
                System.out.println(primero.getTermino());
                System.out.println(primero.getnPostings());
                System.out.println(primero.getTamBytesPostings());
            }*/
            //System.out.println(primero.getTermino());
            /*if(debug == true){
                System.out.println(primero.getTermino());
                System.out.println(primero.getnPostings());
                System.out.println(primero.getTamBytesPostings());
            }*/
            
            int totalNPostings = primero.getnPostings();
            long totalBytes = primero.getTamBytesPostings();
            //busca hasta que el termino sea distinto
            while(!sortedTID.isEmpty()){
                TemporalIndexDescriptor otro = sortedTID.poll();
                if(primero.isEqualTerm(otro)){
                    temporalTIDList.add(otro);
                    totalNPostings += otro.getnPostings();
                    totalBytes += otro.getTamBytesPostings();
                }else{
                    sortedTID.add(otro);
                    break;
                }
            }
            //imprimir el termino y la suma total
            dos.writeUTF(primero.getTermino());
            dos.writeInt(totalNPostings);
            dos.writeLong(totalBytes);
            //imprimir las listas de postings en orden
            for(TemporalIndexDescriptor tid : temporalTIDList){
                tid.printPostingList(dos);
                if(tid.isAvailable()){
                    tid.readTermino();
                    sortedTID.add(tid);
                }
            }
        }
        
        //se coge del heap el ultimo se avanza y se inserta en el heap
            //se cogen del heap hasta que el termino no sea igual, se insertan en otro heap
            //finalidad:
                //sumar posting list en numero y en bytes
            //se insertan en orden las posting list
            //se lee el siguente termino, se inserta en el heap principal
        //lo he llamado TemporalIndexDescriptor
        dos.close();
    }
    
    private String getNameIndexTemporal(){
        return this.outputIndexPath + "/_auxIndex" + this.ficherosTemporales.size() + ".data";
    }

    /**
     * Free memory
     */
    private void cleanIndex() {
        this.partialIndex = new HashMap<>();
        this.sortedTerms = new PriorityQueue<>();
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
