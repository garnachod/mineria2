
package es.uam.eps.bmi.search.parsing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Métodos para obtener tokens de cadenas de texto
 * @author Diego Castaño y Daniel Garnacho
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
        Set<String> set = new HashSet<>();
        set.addAll(Arrays.asList(tokens));
        String[] terms = new String[set.size()];
        int i = 0;
        for (Object o : set.toArray()) {
        terms[i] = (String)o;
        i++;
        }
        return terms;
    }
}
