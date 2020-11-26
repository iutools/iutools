package ca.pirurvik.iutools.concordancer;

import ca.nrc.data.harvesting.PageHarvester;
import ca.nrc.data.harvesting.PageHarvester_HtmlCleaner;

public class WebConcordancer_HtmlCleaner extends WebConcordancer {

    public WebConcordancer_HtmlCleaner(AlignOptions... options) {
        super(options);
    }

    protected PageHarvester getHarvester() {
        if (harvester == null) {
            harvester = new PageHarvester_HtmlCleaner();
            if (filterMainContent) {
                harvester.setHarvestFullText(true);
            }
        }
        return harvester;
    }

}
