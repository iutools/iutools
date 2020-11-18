package ca.pirurvik.iutools.concordancer;

import ca.nrc.data.harvesting.PageHarvester;
import ca.nrc.data.harvesting.PageHarvester_HtmlCleaner;

public class WebConcordancer_HtmlCleaner extends WebConcordancer {

    protected PageHarvester getHarvester() {
        if (harvester == null) {
            harvester = new PageHarvester_HtmlCleaner();
            harvester.setHarvestFullText(true);
        }
        return harvester;
    }

}
