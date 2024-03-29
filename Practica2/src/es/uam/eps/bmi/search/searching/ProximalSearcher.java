
package es.uam.eps.bmi.search.searching;

import es.uam.eps.bmi.search.ScoredTextDocument;
import es.uam.eps.bmi.search.indexing.Index;
import es.uam.eps.bmi.search.indexing.Posting;
import es.uam.eps.bmi.search.parsing.SimpleNormalizer;
import es.uam.eps.bmi.search.parsing.SimpleTokenizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.PriorityQueue;


/**
 * Buscador proximal
 * 
 * 1)And de las palabras
 * 2)formula vista en clase optimizada usando heaps:
 *    2.1)Se busca la menor posicion y la mayor que contiene a todas las listas,
 *        se busca el mayor y se avanzan todas las demás hasta que no sean mayores
 *        que el mayor, asi encontramos el menor y solo ese menor.
 * @author Diego Castaño y Daniel Garnacho
 */
public class ProximalSearcher implements Searcher{
    Index index;
    
    @Override
    public void build(Index index) {
        this.index = index;
    }

    @Override
    public List<ScoredTextDocument> search(String query) {
        // Tokenizar consulta
        SimpleTokenizer st = new SimpleTokenizer();
        String[] tokens = st.split(query);
        
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = SimpleNormalizer.normalize(tokens[i]);
        }
        // Sacar listas de postings de cada term
        ArrayList<String> terminosFinal = SimpleNormalizer.removeNotAllowed(tokens);
        List<ScoredTextDocument> listaDocs = new ArrayList<>();
        PriorityQueue<MergePostings> postingsHeap = new PriorityQueue<>();
        
        // Sacar listas de postings de cada term
        int j = 0;
        for (String term : terminosFinal) {
            ArrayList<Posting> termPostings = new ArrayList(index.getTermPostings(term));
            if(!termPostings.isEmpty()){
                ListIterator<Posting> listIterator = termPostings.listIterator();
                MergePostings merge = new MergePostings(listIterator);
                merge.setPosTermino(j);
                postingsHeap.add(merge);
                
            }else{
                return new ArrayList<>();
            }
            j++;
        }
        
        while(!postingsHeap.isEmpty()){
            MergePostings primero = postingsHeap.poll();
            ArrayList<MergePostings> listMerges = new ArrayList<>();
            listMerges.add(primero);
            boolean flagMismoDoc = true;
            while(!postingsHeap.isEmpty()){
                MergePostings otro = postingsHeap.poll();
                if(primero.equals(otro)){
                    listMerges.add(otro);
                }else{
                    flagMismoDoc = false;
                    postingsHeap.add(otro);
                    break;
                }
            }
            
            if(flagMismoDoc == true && listMerges.size() != terminosFinal.size()){
                //condicion de parada 2
                //el heap ya no contiene todas las listas
                break;
            }
            
            if(flagMismoDoc == true){
                PriorityQueue<PositionsUtility> positionHeap = new PriorityQueue<>();
                //heap, insertamos las posiciones con su lista en un objeto creado a proposito
                for(MergePostings mp:listMerges){
                    PositionsUtility posUtil = new PositionsUtility(mp.getPosting().getTermPositions());
                    positionHeap.add(posUtil);
                }
                double score = 0.0;
                while(!positionHeap.isEmpty()){
                    //cogemos el menor, el primero del heap
                    ArrayList<PositionsUtility> listaPosAux = new ArrayList<>();
                    PositionsUtility menor = positionHeap.poll();
                    listaPosAux.add(menor);
                    PositionsUtility ultimoSacado = null;
                    PositionsUtility maximo = null;
                    

                    //cogemos todos los demás buscando el mayor
                    while(!positionHeap.isEmpty()){
                        ultimoSacado = positionHeap.poll();
                        if(positionHeap.size()>=1){
                            listaPosAux.add(ultimoSacado);
                        }
                    }
                    if(ultimoSacado == null){
                        score = menor.getLongitud();
                    }else{
                        maximo = ultimoSacado;
                        long maximoNumero = maximo.getPosition();
                        boolean flagContinua = true;
                        //recorremos todos los que no sean el mayor mirando si son menores ->
                        //      que el mayor sus siguente elementos si no lo son avanzamos el puntero.
                        while(flagContinua){
                            flagContinua = false;
                            for(int i = 0; i < listaPosAux.size(); i++){
                                if(listaPosAux.get(i).nextSinAvanzar() < maximoNumero){
                                    flagContinua = true;
                                    listaPosAux.get(i).avanzaPuntero();
                                }
                            }
                        }
                        for(PositionsUtility pu:listaPosAux){
                            positionHeap.add(pu);
                        }
                        menor = positionHeap.poll();
                        //ya tenemos a  y  b
                        long bMenosA = maximoNumero - menor.getPosition();
                        score += 1.0/ (double)bMenosA;
                        //insertamos en el heap con todos sin avanzar excepto el menor
                        //si no se puede avanzar se termina
                        if(menor.hasNext()){
                            menor.avanzaPuntero();
                            positionHeap.add(menor);
                            positionHeap.add(maximo);
                        }else{
                            positionHeap.clear();
                        }
                    }
                }
                
                String docid = primero.getDocID();
                //score = score /(1.0 + this.logBase2(this.index.getBytesDocument(docid)/1024.0));
                ScoredTextDocument scored = new ScoredTextDocument(docid, score);
                listaDocs.add(scored);
                
            }
            //insertamos los docs en el heap de nuevo
            //esto no
            for(MergePostings mp: listMerges){
                if(mp.hasNext()){
                    mp.avanzaPuntero();
                    postingsHeap.add(mp);
                }
                
            }
            
        }
        Collections.sort(listaDocs);
        return listaDocs;
        
    }
    
    /**
     * Logaritmo en base 2
     * @param x
     * @return 
     */
    private double logBase2(double x)
    {
        return Math.log(x) / Math.log(2);
    }
    /**
    * Solicita al usuario una consulta, y muestra por pantalla
    * los top 5 resultados de la consulta imprimiendo el título y parte del
    * contenido
    *
    * @param args Ruta del fichero de config
    */
    public static void main (String[] args) {
        InteractiveSearcher.main(args, new ProximalSearcher());
    }
}
