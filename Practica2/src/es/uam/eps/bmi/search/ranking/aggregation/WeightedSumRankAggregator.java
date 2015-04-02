/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.ranking.aggregation;

import es.uam.eps.bmi.search.ScoredTextDocument;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author dani y diegolin
 */
public class WeightedSumRankAggregator {
    
    public static List<ScoredTextDocument> minMax(List<List<ScoredTextDocument>> entrada){
        List<ScoredTextDocument> retorno = new ArrayList<>();
        //todos los buscadores tienen el mismo peso
        int nBuscadores = entrada.size();
        for(List<ScoredTextDocument> lstd : entrada){
            int tam = lstd.size();
            if(tam == 0){
               continue;
            }
            double min = lstd.get(tam - 1).getScore();
            double max = lstd.get(0).getScore();
            double maxMinusMin = max - min;
            
            for(int i = 0; i < tam; i++){
                ScoredTextDocument sc = lstd.get(i);
                double value = sc.getScore();
                sc.setScore(((value - min)/maxMinusMin)/nBuscadores);
                retorno.add(sc);
            }
        }
        //suma de los valores
        HashMap<String, ScoredTextDocument> hashDeSuma = new HashMap<>();
        for(ScoredTextDocument sc : retorno){
            if(hashDeSuma.containsKey(sc.getDocId())){
                ScoredTextDocument aux = hashDeSuma.get(sc.getDocId());
                double auxScore = aux.getScore();
                auxScore += sc.getScore();
                aux.setScore(auxScore);
            }else{
                hashDeSuma.put(sc.getDocId(), sc);
            }
        }
        //
        retorno = new ArrayList<>();
        for(String key:hashDeSuma.keySet()){
            retorno.add(hashDeSuma.get(key));
        }
        Collections.sort(retorno);
        return retorno;
    }
    
    public static List<ScoredTextDocument> sum(List<List<ScoredTextDocument>> entrada){
        List<ScoredTextDocument> retorno = new ArrayList<>();
        //todos los buscadores tienen el mismo peso
        int nBuscadores = entrada.size();
        for(List<ScoredTextDocument> lstd : entrada){
            int tam = lstd.size();
            double sum = 0;
            for(ScoredTextDocument doc :lstd){
                sum += doc.getScore();
            }
            for(int i = 0; i < tam; i++){
                ScoredTextDocument sc = lstd.get(i);
                double value = sc.getScore();
                sc.setScore((value / sum)/nBuscadores);
                retorno.add(sc);
            }
        }
        //suma de los valores
        HashMap<String, ScoredTextDocument> hashDeSuma = new HashMap<>();
        for(ScoredTextDocument sc : retorno){
            if(hashDeSuma.containsKey(sc.getDocId())){
                ScoredTextDocument aux = hashDeSuma.get(sc.getDocId());
                double auxScore = aux.getScore();
                auxScore += sc.getScore();
                aux.setScore(auxScore);
            }else{
                hashDeSuma.put(sc.getDocId(), sc);
            }
        }
        //
        retorno = new ArrayList<>();
        for(String key:hashDeSuma.keySet()){
            retorno.add(hashDeSuma.get(key));
        }
        Collections.sort(retorno);
        return retorno;
    }
    
}
