package ca.pirurvik.iutools.concordancer;

public class WebConcordancer_NoJS extends WebConcordancer {
    @Override
    public boolean canFollowLanguageLink() {
        return false;
    }
}
