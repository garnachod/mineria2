
package es.uam.eps.bmi.search;

import java.util.Comparator;

/**
 * @author Diego Castaño y Daniel Garnacho
 */
public class FrecObject implements Comparator<FrecObject>{
    private String palabla;
    private int frecuencia;
    private int nFicheros;
    
    public FrecObject(){
        
    }
    
    public FrecObject(String palabra, int frecuencia, int nFicheros){
        this.palabla = palabra;
        this.frecuencia = frecuencia;
        this.nFicheros = nFicheros;
    }

    @Override
    public int compare(FrecObject o1, FrecObject o2) {
        return o2.getFrecuencia() - o1.getFrecuencia();
    }

    /**
     * @return the palabla
     */
    public String getPalabla() {
        return palabla;
    }

    /**
     * @return the frecuencia
     */
    public int getFrecuencia() {
        return frecuencia;
    }

    /**
     * @return the nFicheros
     */
    public int getnFicheros() {
        return nFicheros;
    }
}
