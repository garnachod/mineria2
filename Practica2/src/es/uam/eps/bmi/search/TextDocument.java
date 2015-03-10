
package es.uam.eps.bmi.search;

import java.util.Objects;

/**
 * Bean para representar un documento a indexar
 * @author Diego Castaño y Daniel Garnacho
 */
public class TextDocument {
    String id;
    String name;
    
    /**
     * Constructor
     * @param id
     * @param name 
     */
    public TextDocument (String id, String name) {
        this.id = id;
        this.name = name;
    }
    
    /**
     * Get document id
     * @return Document id
     */
    public String getId() {
        return id;
    }
    
    /**
     * Get document name
     * @return Document name
     */
    public String getName() {
        return name;
    }
    
    @Override
    public boolean equals (Object object) {
        if (!(object instanceof TextDocument)) {
            return false;
        }
        return id.equals(((TextDocument)object).getId());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.id);
        return hash;
    }
    
    @Override
    public String toString(){
        return this.name;
    }
    
}
