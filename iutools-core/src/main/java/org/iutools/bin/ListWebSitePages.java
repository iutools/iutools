package org.iutools.bin;

import ca.nrc.data.harvesting.BingSearchEngine;
import ca.nrc.data.harvesting.SearchEngine;
import ca.nrc.data.harvesting.SearchResults;

import static ca.nrc.data.harvesting.SearchEngine.Query;

public class ListWebSitePages {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            usage();
        }

        String domain = args[0];
        Integer maxHits = Integer.parseInt(args[1]);

        new ListWebSitePages().run(domain, maxHits);
    }

    private void run(String domain, Integer maxHits) throws Exception {
        BingSearchEngine searchEngine = new BingSearchEngine();
        Query query =
            new Query("-PDF")
            .setSite("gov.nu.ca")
            .setMaxHits(maxHits)
            ;
        SearchResults hits = searchEngine.search(query);
        System.out.println("Found "+hits.estTotalHits+" hits");
        int hitCount = 0;
        for (SearchEngine.Hit aHit: hits.retrievedHits) {
            hitCount++;
            System.out.println("#"+hitCount+": "+aHit.url);
        }



        return;
    }

    protected static void usage() {
        System.out.println("Usage: ListWebSitePages domain maxHits");
        System.exit(0);
    }
}
