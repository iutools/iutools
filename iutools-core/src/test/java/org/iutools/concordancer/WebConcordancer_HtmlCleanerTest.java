package org.iutools.concordancer;

import static org.iutools.concordancer.WebConcordancer.AlignOptions;

public class WebConcordancer_HtmlCleanerTest extends WebConcordancerTest {
    @Override
    protected WebConcordancer makeConcordancer(AlignOptions... options) {
        WebConcordancer_HtmlCleaner concordancer =
            new WebConcordancer_HtmlCleaner(options);
        return concordancer;
    }
}
