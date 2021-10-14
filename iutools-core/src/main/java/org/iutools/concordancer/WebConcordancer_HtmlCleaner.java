package org.iutools.concordancer;

import ca.nrc.data.harvesting.PageHarvester;
import ca.nrc.data.harvesting.PageHarvester_HtmlCleaner;

public class WebConcordancer_HtmlCleaner extends WebConcordancer {

    public WebConcordancer_HtmlCleaner(AlignOptions... options) {
        super(options);
    }

    protected PageHarvester getHarvester() {
        if (harvester == null) {
            harvester = new PageHarvester_HtmlCleaner();
            // Many of the servers in Nunavut are slow to respond
            harvester.setConnectionTimeoutSecs(15);
            harvester.setMaxTries(1);
        }
        return harvester;
    }

}
