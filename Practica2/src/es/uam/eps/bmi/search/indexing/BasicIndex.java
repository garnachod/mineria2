
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

    
 /**
 * Índice básico
 * @author Diego Castaño y Daniel Garnacho
 */
public class BasicIndex implements Index{

    private long maxIndexLong;
    protected long estimatedIndexSize;
    protected final long biggestDocumentSize = 10000000;
    protected SimpleTokenizer tokenizer;
    protected HashMap<String, List<Posting>> partialIndex;
    protected PriorityQueue<String> sortedTerms;
    private ArrayList<String> ficherosTemporales;
    protected String outputIndexPath;
    protected ZipFile zip;
    protected DataInputStream indexFile = null;
    private HashMap<String, InfoDocumentoIndex> indexedIDtoFile;
    
    //String el termino y Long la posicion desde el principio del fichero
    //de la lista de postings que lo determina
    protected HashMap<String, Integer> indexRAMBusqueda;
    
    private long nDocumentosProcesados = 0;

    /**
     * Construir e inicializar
     */
    public BasicIndex(){
        this.tokenizer = new SimpleTokenizer();
        this.partialIndex = new HashMap<>();
        this.sortedTerms = new PriorityQueue<>();
        this.ficherosTemporales = new ArrayList<>();
        this.outputIndexPath = "";
        this.maxIndexLong = (long) (Runtime.getRuntime().maxMemory() * 0.1);
        this.estimatedIndexSize = 0;
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
                dos.writeLong(entry.getSize());
                //guardar aqui el tamaño en bytes del doc entry.getSize() para normalizar
                
                contFiles++;
                
            }
            this.saveIndexFinal(outputIndexPath+"\\indexed.data");
            dos.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }

    @Override
    public void load(String indexPath) {
        this.outputIndexPath = indexPath;
        this.indexedIDtoFile = new HashMap<>();
        try{
            // Load idFileToName
            String nombreFichero = indexPath + "\\idFileToName.data";
            DataInputStream dis = new DataInputStream(new FileInputStream(nombreFichero));
            
            while(dis.available() > 0){
                String docID = dis.readUTF();
                InfoDocumentoIndex info = new InfoDocumentoIndex(dis.readUTF(), dis.readLong());
                indexedIDtoFile.put(docID, info);
                this.nDocumentosProcesados++;
            }
            dis.close();
            
            // Load index in RAM
            this.indexRAMBusqueda = new HashMap<>();
            nombreFichero = indexPath + "\\indexed.data";
            FileInputStream file = new FileInputStream(nombreFichero);
            FileChannel fileChannel = file.getChannel();
            dis = new DataInputStream(file);
            long contPosition = 0;
            
            while(dis.available() > 0){
                // Leemos el termino y lo insertamos en la Hash
                int nPostings = 0;
                String termino = "";
                long tamPostings = 0;
                try{
                    termino = dis.readUTF();
                    
                    // Insercion en la hash
                    if (this.indexRAMBusqueda.containsKey(termino)){
                        System.out.println("Indice mal formado");
                        return;
                    }
                    
                    this.indexRAMBusqueda.put(termino, new Integer((int)fileChannel.position()));
                    
                    nPostings = dis.readInt();
                    tamPostings = dis.readLong();
                    dis.skipBytes((int)tamPostings);
                    
                }catch(Exception e){
                    System.out.println("termino:"+ termino +"npostings:" +nPostings+ "tamPostings" +tamPostings+ "pos"+ contPosition);
                    return;
                }
            }
            dis.close();
            
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getDocIds() {
        return new ArrayList<>(this.indexedIDtoFile.keySet());
    }

    @Override
    public TextDocument getDocument(String docId) {
        return new TextDocument(docId, this.indexedIDtoFile.get(docId).getNombreCompleto());
    }
    
    public Long getNDocsIndex(){
        return this.nDocumentosProcesados;
    }
    
    public Long getBytesDocument(String docId){
        return this.indexedIDtoFile.get(docId).getTamBytes();
    }

    @Override
    public List<String> getTerms() {
        return new ArrayList<>(this.indexRAMBusqueda.keySet());
    }

    @Override
    public List<Posting> getTermPostings(String term){
        try {
            if(this.indexRAMBusqueda.containsKey(term)){
                if(this.indexFile == null){
                    String nombreFichero = this.outputIndexPath + "\\indexed.data";
                    this.indexFile = new DataInputStream(new BufferedInputStream(new FileInputStream(nombreFichero)));
                }
                List<Posting> listaRetorno = Posting.readListPostingsByPos(this.indexFile, this.indexRAMBusqueda.get(term));
                this.indexFile.close();
                this.indexFile = null;
                return listaRetorno;
            }else{
                return new ArrayList<Posting>();
            }
            
        } catch (IOException ex) {
            
            ex.printStackTrace();
        }
        return new ArrayList<Posting>();
    }

    @Override
    public String getPath() {
        return this.outputIndexPath;
    }

    /**
     * Intenta analizar un documento comprimido
     * @param entry
     * @param textParser
     * @param docId 
     */
    private void analyzeDocument(ZipEntry entry, TextParser textParser, String docId) {
        // Si queda espacio en RAM
        if (this.hasFreeSpace()) {
            //System.out.println(Runtime.getRuntime().freeMemory());
            this.insertDocument(entry, textParser, docId);
        } else {
            try {
                this.saveIndexTemporal(this.getNameIndexTemporal());
                this.analyzeDocument(entry, textParser, docId);
            } catch (Exception ex) {
                Logger.getLogger(BasicIndex.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private boolean hasFreeSpace() {
        //System.out.println("El indice mide " + this.estimatedIndexSize + "/" + this.maxIndexLong);
        return ((this.estimatedIndexSize +  biggestDocumentSize ) < this.maxIndexLong);
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
                        
                        String html = this.getDocumentText(zip.getInputStream(entry));
                        // Parsearlo
                        String text = textParser.parse(html);
                        
                        // Tokenizar fichero
                        String [] terms = this.tokenizer.split(text);
                        //normalizar
                        for(int i = 0; i< terms.length; i++){
                            terms[i] = this.normalize(terms[i]);
                        }
                        ArrayList<String> termsList = this.removeNotAllowed(terms);
                        
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
                            if(this.partialIndex.containsKey(term)){
                                this.partialIndex.get(term).add(fileIndex.get(term));
                            }else{
                                List<Posting> listaPost = new ArrayList<>();
                                listaPost.add(fileIndex.get(term));
                                this.partialIndex.put(term, listaPost);
                                this.sortedTerms.add(term);
                             }
                            this.estimatedIndexSize += fileIndex.get(term).getBinarySizeBytes();
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
        System.out.println("Número de terminos en el índice parcial: " + this.sortedTerms.size());
        while (!this.sortedTerms.isEmpty()) {
            String term = this.sortedTerms.poll();
         
            List<Posting> postings = this.partialIndex.get(term);
            
            // impresión del termino
            dos.writeUTF(term);
            
            // impresion del tamaño en numero de elementos de la lista de postings
            dos.writeInt(postings.size());
            
            // get tam en bytes
            long bytesLenght = 0;
            for(Posting post:postings){
                bytesLenght += post.getBinarySizeBytes();
            }
            dos.writeLong(bytesLenght);
           
            // TODO: Imprimir en bytes el tamaño tambien para poder hacer saltos en el indice
            for(Posting post:postings){
                post.printBinary(dos);
            }
        }
        dos.close();
        // add file to lista files
        this.ficherosTemporales.add(nombreFichero);
        // Free memory
        this.cleanIndex();
    }
    
    /**
     * Hace merge de todos los índices parciales y lo guarda a disco.
     * @param nombreFichero
     * @throws Exception 
     */
    private void saveIndexFinal(String nombreFichero) throws Exception {
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(nombreFichero)));
        
        if(!this.sortedTerms.isEmpty()){
            this.saveIndexTemporal(this.getNameIndexTemporal());
        }
        
        PriorityQueue<TemporalIndexDescriptor> sortedTID = new PriorityQueue<>();
        System.out.println("Creando heap...");
        int i = 1;
        for(String nombreITemp : this.ficherosTemporales){
            DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(nombreITemp)));
            TemporalIndexDescriptor tid = new TemporalIndexDescriptor(dis, i);
            tid.readTermino();
            sortedTID.add(tid);
            i++;
        }
         //se coge del heap el ultimo se avanza y se inserta en el heap
            //se cogen del heap hasta que el termino no sea igual, se insertan en otro heap
            //finalidad:
                //sumar posting list en numero y en bytes
            //se insertan en orden las posting list
            //se lee el siguente termino, se inserta en el heap principal
        //lo he llamado TemporalIndexDescriptor
        while(!sortedTID.isEmpty()){
            
            ArrayList<TemporalIndexDescriptor> temporalTIDList = new ArrayList<>();
            TemporalIndexDescriptor primero = sortedTID.poll();
            temporalTIDList.add(primero);
            
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
        
        dos.close();
        
        // Borrar indices temporales
        this.removeTemp();
    }
    
    private String getNameIndexTemporal(){
        return this.outputIndexPath + "\\_auxIndex" + this.ficherosTemporales.size() + ".data";
    }

    /**
     * Free memory
     */
    private void cleanIndex() {
        this.partialIndex = new HashMap<>();
        this.sortedTerms = new PriorityQueue<>();
        this.estimatedIndexSize = 0;
    }
    
    /**
     * Devuelve el texto como una cadena leyendo de un inputstream
     * 
     * @param input Devuelve el texto como una cadena leyendo de un inputstream
     * @return Cadena con todo el contenido
     */
    protected String getDocumentText(InputStream input) {
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
    
    /**
     * Elimina índices temporales
     */
    private void removeTemp() {
        System.gc();
        for (String temp: this.ficherosTemporales) {
            File f = new File(temp);
            f.delete();
        }
    }
    
    /**
     * Elimina términos no permitidos
     * @param terms array inicial de terminos
     * @return términos permitidos
     */
    protected ArrayList<String> removeNotAllowed(String terms[]) {
         return SimpleNormalizer.removeNotAllowed(terms);
    }

    /**
     * Normaliza términos (minúsculas, etc)
     * @param term término a normalizar
     * @return término normalizado
     */
    protected String normalize(String term) {
        return SimpleNormalizer.normalize(term);
    }
    
    /**
     * Bean interno para asociar un documento a su representación en disco
     */
    private class InfoDocumentoIndex{
        private String nombreCompleto;
        private long tamBytes;
        
        public InfoDocumentoIndex(String nombreCompleto, long tamBytes){
            this.nombreCompleto = nombreCompleto;
            this.tamBytes = tamBytes; 
        }
        
        public String getNombreCompleto(){
            return this.nombreCompleto;
        }
        
        public long getTamBytes(){
            return this.tamBytes;
        }
    }
}
