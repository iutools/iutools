package ca.pirurvik.iutools.concordancer;

import ca.nrc.datastructure.Pair;
import com.gargoylesoftware.htmlunit.WebClient;

import javax.print.Doc;
import java.io.IOException;
import java.net.URL;

public class WebConcordancer_HtmlUnit extends WebConcordancer {

    @Override
    protected StepOutcome harvestOtherLangPage_UnknownSites(
        DocAlignment alignment, String lang, String otherLang) throws WebConcordancerException {
        URL pageURL = alignment.getPageURL(lang);

        WebClient browser = new WebClient();
        try {
            browser.getPage(pageURL);
        } catch (IOException e) {
            throw new WebConcordancerException("Could not fetch content of page in lang="+lang+": "+pageURL, e);
        }
        return StepOutcome.FAILURE;
    }

    public static void main(String[] args) throws Exception {
        WebConcordancer_HtmlUnit concordancer = new WebConcordancer_HtmlUnit();
        URL url = new URL("https://www.gov.nu.ca/sports-and-recreation");
        DocAlignment alignment = concordancer.alignPage(url, new String[] {"en", "iu"});
        return;
    }
}
