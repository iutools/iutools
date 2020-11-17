package ca.pirurvik.iutools.concordancer;

public class WebConcordancer_HtmlCleanerTest extends WebConcordancerTest {
    @Override
    protected WebConcordancer makeConcordancer() {
        WebConcordancer_HtmlCleaner concordancer = new WebConcordancer_HtmlCleaner();
        return concordancer;
    }
}
