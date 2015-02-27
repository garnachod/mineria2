package es.uam.eps.bmi.search.indexing;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Posting {
    
    private String docId;
    private int termFrequency;
    private final List<Long> termPositions;
    
    /**
     * Constructor
     * @param docId
     * @param termFrequency
     * @param termPositions
     */
    public Posting (String docId, int termFrequency, List<Long> termPositions) {
        this.docId = docId;
        this.termFrequency = termFrequency;
        this.termPositions = new ArrayList<>(termPositions); // OJO: Menos óptimo
    }
    /**
     * Constructor
     * @param docId
     * @param termFrequency
     */
    public Posting (String docId, int termFrequency) {
        this.docId = docId;
        this.termFrequency = termFrequency;
        this.termPositions = new ArrayList<>();
    }

    /**
     * @return the docId
     */
    public String getDocId() {
        return docId;
    }

    /**
     * @return the termFrequency
     */
    public int getTermFrequency() {
        return termFrequency;
    }

    /**
     * @return the termPositions
     */
    public List<Long> getTermPositions() {
        return termPositions;
    }
    
    /**
     * Add a position to the list
     * @param pos 
     */
    public void addTermPosition (Long pos) {
        this.termPositions.add(pos);
        this.termFrequency++;
    }
    
    public void printBinary(DataOutputStream stream) throws IOException{
        //output = new BufferedOutputStream(new FileOutputStream(aOutputFileName));
        //output.write(aInput);
        //docId
        stream.writeChars(docId);
        stream.writeInt(this.termFrequency);
        for(Long tP: this.termPositions){
            stream.writeFloat(tP.floatValue());
        }
    }
    public int getBinarySizeBytes(){
        int sum = 0;
        sum += this.docId.getBytes().length;
        sum += Integer.SIZE/8;
        sum += (Long.SIZE/8) * this.termFrequency;
        return sum;
    }
    public void readBinary(DataInputStream stream) throws IOException{
        this.docId = stream.readUTF();
        this.termFrequency = stream.readInt();
        for(int i = 0; i < this.termFrequency; i++){
            this.termPositions.add(stream.readLong());
        }
        
    }
}
