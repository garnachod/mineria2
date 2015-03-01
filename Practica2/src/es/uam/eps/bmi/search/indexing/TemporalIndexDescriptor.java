/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.indexing;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Comparator;

/**
 *
 * @author dani
 */
public class TemporalIndexDescriptor implements Comparator{
    private String termino;
    private final DataInputStream index;
    private int nPostings;
    private long tamBytesPostings;
    private final int idTemporal;
    
    public TemporalIndexDescriptor(DataInputStream index, int idTemporal){
        this.index = index;
        this.idTemporal = idTemporal;
    }
    //termino
    //file(datainputstream)
    //numero de postings
    //n bytes de postings
    
    //leer siguiente termino
    public void readTermino() throws IOException{
        //lectura del termino
        this.termino = this.index.readUTF();
        //lectura del numero de postings
        this.nPostings = this.index.readInt();
        //lectura del tam en bytes
        this.tamBytesPostings = this.index.readLong();
    }
    
    
    public void printPostingList(DataOutputStream finalIndex) throws IOException{
        int byteRead = 0;
        boolean flag = false;
        
        if(this.getTamBytesPostings() > Integer.MAX_VALUE){
           this.tamBytesPostings -=  Integer.MAX_VALUE;
           byteRead = Integer.MAX_VALUE;
           flag = true;
        }else{
            byteRead = (int)this.getTamBytesPostings();
        }
        
        byte [] buffer = new byte[byteRead];
        this.index.read(buffer, 0, byteRead);
        finalIndex.write(buffer);
        
        if(flag == true){
            this.printPostingList(finalIndex);
        }
        
    }
    
    public boolean isEqualTerm(TemporalIndexDescriptor tid){
        return this.getTermino().equals(tid.getTermino());
    }
    
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

    @Override
    public int compare(Object o1, Object o2) {
        //suponemos que los dos objetos son de este tipo
        TemporalIndexDescriptor tid1 = (TemporalIndexDescriptor)o1;
        TemporalIndexDescriptor tid2 = (TemporalIndexDescriptor)o2;
        if(tid1.getTermino().equals(tid2.getTermino())){
            return tid1.idTemporal - tid2.idTemporal;
        }else{
            return tid1.getTermino().compareTo(tid2.getTermino());
        }
    }

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
    
    
}
