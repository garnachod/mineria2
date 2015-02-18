
package es.uam.eps.bmi.search.parsing;

import org.jsoup.Jsoup;

/**
 * @author Diego Castaño y Daniel Garnacho
 */
public class HTMLSimpleParser implements TextParser {

    @Override
    public String parse(String text) {
        return Jsoup.parse(text).text();
    }
    
}
