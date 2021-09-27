/*
 * Controller for the search.html page.
 */

class SearchController extends IUToolsController {

    constructor(config) {
        super(config);
    }

    // Setup handler methods for different HTML elements specified in the config.
    attachHtmlElements() {
        this.setEventHandler("btnSearch", "click", this.onSearch);
        this.onReturnKey("txtQuery", this.onSearch);
    }

    onSearch() {
        Debug.getTraceLogger("SearchController.onSearch").trace("Invoked");
        this.expandQueryThenSearch();
    }

    expandQueryThenSearch() {
        var isValid = this.validateQueryInput();
        if (isValid) {
            var divMessage = this.elementForProp("divMessage"); divMessage.html("retrieveAllHitsFromService---");
            this.setBusy(true);
            this.clearResults();
            var data = this.getSearchRequestData();
            if (!this.isDuplicateEvent("expandQueryThenSearch", data)) {
                this.invokeExpandQueryService(data,
                    this.expandQuerySuccessCallback, this.expandQueryFailureCallback)
            }
        }
    }

    clearResults() {
        this.elementForProp('divError').empty();
        this.elementForProp('divResults').empty();
    }

    invokeExpandQueryService(jsonRequestData, _successCbk, _failureCbk) {
        var tracer = Debug.getTraceLogger("SearchController.invokeExpandQueryService");
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
        this.userActionStart("SEARCH_WEB", 'srv2/search/expandquery',
            jsonRequestData, fctSuccess, fctFailure)
    }

    validateQueryInput() {
        var isValid = true;
        var query = this.elementForProp("txtQuery").val();
        if (query == null || query === "") {
            isValid = false;
            this.error("You need to enter something in the query field");
        }
        return isValid;
    }

    expandQuerySuccessCallback(resp) {
        var tracer = Debug.getTraceLogger("SearchController.expandQuerySuccessCallback");
        tracer.trace('resp= '+JSON.stringify(resp));
        if (resp.errorMessage != null) {
            this.expandQueryFailureCallback(resp);
        } else {
            var expandedQuery = resp.expandedQuery;
            this.setQuery(expandedQuery);
            this.launchGoogleSearch(resp.expandedQuerySyll);
        }
        this.setBusy(false);
        this.userActionEnd("SEARCH_WEB", resp);
    }

    expandQueryFailureCallback(resp) {
        if (! resp.hasOwnProperty("errorMessage")) {
            // Error condition comes from tomcat itself, not from our servlet
            resp.errorMessage =
                "Server generated a "+resp.status+" error:\n\n" +
                resp.responseText;
        }
        this.error(resp.errorMessage);
        this.setBusy(false);
    }

    relatedWordsResp2ExpandedQuery(resp) {
        var words = [];
        var relatedWordsInfo = resp.relatedWords;
        for (var ii=0; ii < relatedWordsInfo.length; ii++) {
            words.push(relatedWordsInfo[ii].word)
        }
        var query = '('+words.join(' OR ')+')';

        return query;
    }

    launchGoogleSearch(query) {
        var url = "https://www.google.com/search?q="+query;
        window.open(url, "_self");
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

    getSearchRequestData() {
        var request = {
            origQuery: this.elementForProp("txtQuery").val()
        };

        var jsonInputs = JSON.stringify(request);

        return jsonInputs;
    }

    disableSearchButton() {
        this.elementForProp('btnSearch').attr("disabled", true);
    }

    enableSearchButton() {
        this.elementForProp('btnSearch').attr("disabled", false);
    }


    error(err) {
        this.elementForProp('divError').html(err);
        this.elementForProp('divError').show();
    }

    setQuery(query) {
        this.elementForProp("txtQuery").val(query);
    }
}