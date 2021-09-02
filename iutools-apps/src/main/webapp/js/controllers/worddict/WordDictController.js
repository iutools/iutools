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
            this.logOnServer("DICTIONARY_LOOKUP", inputs);
            this.invokeDictionaryService(inputs,
                this.searchSuccessCallback, this.searchFailureCallback)
        }
    }

    acquireInputs() {
        var query = this.elementForProp("txtQuery").val();
        var inputs =
            {
                "word": query
            };

        var inputsJson = JSON.stringify(inputs);
        return inputsJson;
    }

    invokeDictionaryService(jsonRequestData, _successCbk, _failureCbk) {
        var tracer = Debug.getTraceLogger("WordDictController.invokeDictionaryService");
        tracer.trace("invoked with jsonRequestData="+JSON.stringify(jsonRequestData));

        var controller = this;
        var fctSuccess =
            function(resp) {
                _successCbk.call(controller, resp);
            };
        var fctFailure =
            function(resp) {
                _failureCbk.call(controller, resp);
            };

        // this line is for development only, allowing to present results without calling Bing.
        //var jsonResp = this.mockSrvSearch();fctSuccess(jsonResp);

        $.ajax({
            method: 'POST',
            url: 'srv2/worddict',
            data: jsonRequestData,
            dataType: 'json',
            async: true,
            success: fctSuccess,
            error: fctFailure
        });
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
        this.wordEntryController.dictionaryLookup(iuWord);
    }
}
