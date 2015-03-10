
package es.uam.eps.bmi.search.indexing;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Bean para almacenar un índice parcial
 * @author Diego Castaño y Daniel Garnacho
 */
public class TemporalIndexDescriptor implements Comparable{
    private String termino;
    private final DataInputStream index;
    private int nPostings;
    private long tamBytesPostings;
    private long tamBytesPostingsNoEscritos;
    private final int idTemporal;
    
    public TemporalIndexDescriptor(DataInputStream index, int idTemporal){
        this.index = index;
        this.idTemporal = idTemporal;
    }
    
    //termino
    //file(datainputstream)
    //numero de postings
    //n bytes de postings
    
    /**
     * leer siguiente termino
     * @throws IOException 
     */
    public void readTermino() throws IOException{
        //lectura del termino
        this.termino = this.index.readUTF();
        //lectura del numero de postings
        this.nPostings = this.index.readInt();
        //lectura del tam en bytes
        this.tamBytesPostings = this.index.readLong();
        //tamBytesNoEscritos
        this.tamBytesPostingsNoEscritos = this.tamBytesPostings;
    }
    
    /**
     * Imprimir a disco un índice parcial
     * @param finalIndex
     * @throws IOException 
     */
    public void printPostingList(DataOutputStream finalIndex) throws IOException{
        int byteRead = 0;
        boolean flag = false;
        
        if(this.tamBytesPostingsNoEscritos > Integer.MAX_VALUE){
           this.tamBytesPostingsNoEscritos -=  Integer.MAX_VALUE;
           byteRead = Integer.MAX_VALUE;
           flag = true;
        }else{
            byteRead = (int)this.getTamBytesPostings();
        }
        
        byte [] buffer = new byte[byteRead];
        
        this.index.readFully(buffer, 0, byteRead);

        finalIndex.write(buffer, 0, byteRead);
        
        
        if(flag == true){
            this.printPostingList(finalIndex);
        }
        
    }
    
    /**
     * Compara el término de este índice parcial con el de otro
     * @param tid 
     * @return true o false
     */
    public boolean isEqualTerm(TemporalIndexDescriptor tid){
        return this.getTermino().equals(tid.getTermino());
    }
    
    /**
     * Comprueba si queda espacio disponible en este índice parcial
     * @return 
     */
    public boolean isAvailable(){
        try{
            return this.index.available() > 0;
        }catch(Exception e){
            return false;
        }
    }
    
    
    //escritura de postings list(copia de un fichero a otro n bytes de postings)
        //recibe el fichero final
    //es comparable
        //se compara primero el termino
        //se compara despues el id del fichero temporal

  

    /**
     * @return the nPostings
     */
    public int getnPostings() {
        return nPostings;
    }

    /**
     * @return the tamBytesPostings
     */
    public long getTamBytesPostings() {
        return tamBytesPostings;
    }

    /**
     * @return the termino
     */
    public String getTermino() {
        return termino;
    }

    @Override
    public int compareTo(Object o) {
        //suponemos que los dos objetos son de este tipo
        TemporalIndexDescriptor tid1 = this;
        TemporalIndexDescriptor tid2 = (TemporalIndexDescriptor)o;
        if(tid1.getTermino().equals(tid2.getTermino())){
            return tid1.idTemporal - tid2.idTemporal;
        }else{
            return tid1.getTermino().compareTo(tid2.getTermino());
        }
    }
    
    
}
