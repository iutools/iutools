package ca.pirurvik.iutools.bin;

import ca.nrc.data.harvesting.BingSearchEngine;
import ca.nrc.data.harvesting.SearchEngine;

import java.io.IOException;

public class ListWebSitePages {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            usage();
        }

        String domain = args[0];

        new ListWebSitePages().run(domain);
    }

    private void run(String domain) throws Exception {
        BingSearchEngine searchEngine = new BingSearchEngine();
    }

    protected static void usage() {
        System.out.println("Usage: ListWebSitePages domain");
        System.exit(0);
    }
}
