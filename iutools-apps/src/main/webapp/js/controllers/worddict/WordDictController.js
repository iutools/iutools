/*
 * Controller for Dictionary dialog.
 */

class WordDictController extends IUToolsController {
    constructor(wdConfig) {
        var tracer = Debug.getTraceLogger('WordDicController.constructor');
        tracer.trace("wdConfig=" + JSON.stringify(wdConfig));
        super(wdConfig);

        this.wordEntryController = new WordEntryController(wdConfig);
        this.wordEntryController.hideIconisationControls();
        tracer.trace("upon exit, this=" + JSON.stringify(this));
    }

    // Setup handler methods for different HTML elements specified in the config.
    attachHtmlElements() {
        this.setEventHandler("btnSearch", "click", this.onSearch);
        this.onReturnKey("txtQuery", this.onSearch);
    }

    onSearch() {
        var tracer = Debug.getTraceLogger("WordDictController.onSearch")
        var inputs = this.acquireInputs();
        tracer.trace("Searching inputs="+JSON.stringify(inputs));
        if (!this.isDuplicateEvent("onSearch", inputs)) {
            this.clearHits();
            this.setBusy(true);
            this.invokeDictionarySearchService(inputs,
                this.searchSuccessCallback, this.searchFailureCallback)
        }
    }

    acquireInputs() {
        var query = this.queryWord();
        var lang = this.queryLang();
        var inputs =
            {
                "word": query,
                "lang": lang,
            };

        var inputsJson = JSON.stringify(inputs);
        return inputsJson;
    }

    invokeDictionarySearchService(jsonRequestData, _successCbk, _failureCbk) {
        var tracer = Debug.getTraceLogger("WordDictController.invokeDictionaryService");
        jsonRequestData = this.asJsonString(jsonRequestData);
        tracer.trace("invoked with jsonRequestData="+jsonRequestData);
        this.userActionStart("DICTIONARY_SEARCH", 'srv2/worddict',
            jsonRequestData, _successCbk, _failureCbk);
    }

    searchSuccessCallback(resp) {
        var tracer = Debug.getTraceLogger("WordDictController.searchSuccessCallback");
        tracer.trace('resp= '+JSON.stringify(resp));
        if (resp.errorMessage != null) {
            this.searchFailureCallback(resp);
        } else {
            this.displayLookupResult(resp);
        }
        this.setBusy(false);
    }


    searchFailureCallback(resp) {
        if (! resp.hasOwnProperty("errorMessage")) {
            // Error condition comes from tomcat itself, not from our servlet
            resp.errorMessage =
                "Server generated a "+resp.status+" error:\n\n" +
                resp.responseText;
        }
        this.error(resp.errorMessage);
        this.setBusy(false);
    }

    setBusy(flag) {
        this.busy = flag;
        if (flag) {
            this.disableSearchButton();
            this.showSpinningWheel('divMessage', "Searching");
            this.error("");
        } else {
            this.enableSearchButton();
            this.hideSpinningWheel('divMessage');
        }
    }

    disableSearchButton() {
        this.elementForProp('btnSearch').attr("disabled", true);
    }

    enableSearchButton() {
        this.elementForProp('btnSearch').attr("disabled", false);
    }

    displayLookupResult(resp) {
        var tracer = Debug.getTraceLogger("WordDictController.displayLookupResult");
        tracer.trace('resp= '+JSON.stringify(resp));
        var html = this.htmlHits(resp);

        var queryWordEntry = resp.queryWordEntry.word;
        if (queryWordEntry != null) {
            if (queryWordEntry === this.queryWord()) {
                // There is a word that matched the query exactly.
                // Display its entry.
                tracer.trace("Displaying the exact match entry");
                this.wordEntryController.dictionaryLookup(
                    resp.queryWordEntry.word, this.queryLang());
            }
        }

        var divHits = this.elementForProp("divSearchResults");
        divHits.empty();
        divHits.append(html);

        this.attachListenersToIUWords();
    }

    htmlHits(resp) {
        var html = "<h3>"+resp.totalWords+" words found</h3>\n\n";
        var totalLeft = resp.totalWords - resp.matchingWords.length;
        if (totalLeft > 0) {
            html += "<h4>First "+resp.matchingWords.length+" hits below</h4><br/>\n";
        }

        for (var ii=0; ii < resp.matchingWords.length; ii++) {
            if (ii > 0) {
                html += "<br/>\n";
            }
            var word = resp.matchingWords[ii];
            html +=
                '<a class="iu-word">'+
                word+"</a>";
        }


        return html;
    }

    attachListenersToIUWords() {
        var tracer = Debug.getTraceLogger('WordDicController.attachListenersToIUWords');
        var anchorWords = $(document).find('.iu-word');
        for (var ipn = 0; ipn < anchorWords.length; ipn++) {
            tracer.trace("Attaching lister to word: "+JSON.stringify(anchorWords[ipn]));
            this.setEventHandler(anchorWords.eq(ipn), "click", this.onClickWord);
        }
    }

    onClickWord(evt) {
        var tracer = Debug.getTraceLogger('WordDicController.onClickWord');
        tracer.trace("invoked, evt="+JSON.stringify(evt));
        tracer.trace("this="+JSON.stringify(this));
        tracer.trace("this.wordDictController="+JSON.stringify(this.wordDictController));
        tracer.trace("this.wordEntryController="+JSON.stringify(this.wordEntryController));

        var element = evt.target;
        var iuWord = $(element).text();
        var lang = this.queryLang();
        this.wordEntryController.dictionaryLookup(iuWord, lang);
    }

    queryLang() {
        var lang = this.elementForProp("selLanguage").val();
        return lang;
    }

    queryWord() {
        return this.elementForProp("txtQuery").val();
    }

    clearHits() {
        this.elementForProp("divSearchResults").empty();
    }
}
