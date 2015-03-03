package es.uam.eps.bmi.search.indexing;

import es.uam.eps.bmi.search.indexing.stemmer.SnowballStemmer;
import es.uam.eps.bmi.search.indexing.stemmer.ext.englishStemmer;

public class StemIndex extends BasicIndex {
    SnowballStemmer stemmer;

    public StemIndex() {
        this.stemmer = (SnowballStemmer)new englishStemmer();
    }

    @Override
    protected String normalize(String term) {
        String stemTerm = super.normalize(term);
        stemmer.setCurrent(term);
	stemmer.stem();
	return stemmer.getCurrent();
    }
}
