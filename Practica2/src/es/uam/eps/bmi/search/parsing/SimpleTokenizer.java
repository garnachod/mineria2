/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.parsing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author dani
 */
public class SimpleTokenizer {
    private String separators;
    
    public SimpleTokenizer(){
        //this.separators = ",| |\t|\n|<|>|(|)|_|\"|'|%|$|@|/";
        this.separators = ",| |\t|\n|<|>|_|\"|'|%|\\$|@|/|&|=|“|”|‘|’|•|　| |\\||–|—|:|;|●|≈|…|\\.|\\?|\\¿|»|«|©||^|：|《|》|。";
        this.separators += "|~|\\{|\\}|\\(|\\)|�|க|ு|!|¡|\\[|\\]|-|0|1|2|3|4|5|6|7|8|9|\\+|\\*|\\#||`| |\\\\|||||||||，|";
        this.separators += "|^||||►|♥|▪|─|█|®";
    }
    
    public String[] split(String text){
        return text.split(this.separators);
    }
    
    public static String[] deleteRepeated (String[] tokens) {
        String[] terms;
        Set<String> set = new HashSet<>();    
        set.addAll(Arrays.asList(tokens));
        return (String[]) set.toArray();
    }
}
