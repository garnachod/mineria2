/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.bmi.search.parsing;

/**
 *
 * @author dani
 */
public class SimpleTokenizer {
    private String separators;
    
    public SimpleTokenizer(){
        //this.separators = ",| |\t|\n|<|>|(|)|_|\"|'|%|$|@|/";
        this.separators = ",| |\t|\n|<|>|_|\"|'|%|\\$|@|/|&|=|“|”|‘|’|•|　| |\\||–|—|:|;|●|≈|…|\\.|\\?|\\¿|»|«|©";
        this.separators += "|~|\\{|\\}|\\(|\\)|�|!|¡|\\[|\\]|-|0|1|2|3|4|5|6|7|8|9|\\+|\\*|\\#||`";
    }
    
    public String[] split(String text){
        return text.split(this.separators);
    }
    
}
