/*
 * Conseil national de recherche Canada 2006/
 * National Research Council Canada 2006
 * 
 * Cr�� le / Created on Sep 12, 2006
 * par / by Benoit Farley
 * 
 */
package ca.inuktitutcomputing.fonts.ngrams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Ngram {

    public ArrayList fontNgrams;
    
    public Ngram() {
        fontNgrams = new ArrayList();
    }
    
    public void add(int ngrammesTrouves, int ngrammesRetenus, String font, float freqAverage, int ngrammes,String[] pasTrouves) {
        fontNgrams.add(new FontNgram(ngrammesTrouves,ngrammesRetenus,font,freqAverage,ngrammes,pasTrouves));
    }
    
    public List sort() {
        FontNgram [] fngs  = (FontNgram[])fontNgrams.toArray(new FontNgram[]{});
        Arrays.sort(fngs);
        return Arrays.asList(fngs);
    }
    
    public class FontNgram implements Comparable {
        
        //public int ngrammesTrouves;
        public int ngramsFound;
        public int ngrammesRetenus;
        public String font;
        public float freqAverage;
        //public int ngrammes;
        public int ngrams;
        public float freq;
        //public String [] pasTrouves;
        public String [] notFound;
        
        public FontNgram(int ngrammesTrouves, int ngrammesRetenus, String font, float freqAverage,int ngrammes,String[] pasTrouves) {
            this.ngramsFound = ngrammesTrouves;
            this.ngrammesRetenus = ngrammesRetenus;
            this.font = font;
            this.freqAverage = freqAverage;
            this.ngrams = ngrammes;
            this.notFound = pasTrouves;
            freq = (float)ngrammesTrouves / (float)ngrammes * 100;
        }

        public int compareTo(Object arg0) {
            FontNgram arg = (FontNgram)arg0;
             if (freq > arg.freq)
                return -1;
            else if (freq < arg.freq)
                return 1;
            else
                return 0;
        }
    }
}
