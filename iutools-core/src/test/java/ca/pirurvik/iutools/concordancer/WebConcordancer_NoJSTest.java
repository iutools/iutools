package ca.pirurvik.iutools.concordancer;

public class WebConcordancer_NoJSTest extends WebConcordancerTest {
    @Override
    protected WebConcordancer makeConcordancer() {
        WebConcordancer_NoJS concordancer = new WebConcordancer_NoJS();
        return concordancer;
    }
}
