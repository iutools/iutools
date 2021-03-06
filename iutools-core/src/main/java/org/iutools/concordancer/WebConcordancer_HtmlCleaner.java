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
        }
        return harvester;
    }

}
