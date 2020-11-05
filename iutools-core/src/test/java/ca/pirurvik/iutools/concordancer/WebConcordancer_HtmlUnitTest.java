package ca.pirurvik.iutools.concordancer;

public class WebConcordancer_HtmlUnitTest extends WebConcordancerTest {
    @Override
    protected WebConcordancer makeConcordancer() {
        WebConcordancer_HtmlUnit concordancer = new WebConcordancer_HtmlUnit();
        return concordancer;
    }
}
