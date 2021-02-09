/*
 * Controller for the search.html page.
 */

class SearchController extends WidgetController {

    constructor(config) {
        super(config);
        this.hitsPerPage = 10;
        this.totalHits = 0;
        this.currHitsPageNum = 0;
        var prevPage = this.initialPage();
        this.query = prevPage.query;
        this.allHits = [];
    }

    initialPage(query) {
        var initialPage = {
            'query': query,
            'pageNum': 0,
            'hasNext': true
        };
        return initialPage;
    }

    // Setup handler methods for different HTML elements specified in the config.
    attachHtmlElements() {
        this.setEventHandler("btnSearch", "click", this.onSearch);
        this.setEventHandler("prevPage", "click", this.onSearchPrev);
        this.setEventHandler("nextPage", "click", this.onSearchNext);
        this.onReturnKey("txtQuery", this.onSearch);
    }

    onSearch() {
        Debug.getTraceLogger("SearchController.onSearch").trace("Invoked");
        // this.expandQueryThenSearch();
    }

    showHitsPage(pageNum) {
        var divResults = this.elementForProp("divResults");
        divResults.empty();

        var results = this.hitsForPage(pageNum);

        for (var ii = 0; ii < results.length; ii++) {
            var aHit = results[ii];
            console.log('aHit.title= '+aHit.title);
            var hitHtml =
                "<div id=\"hit"+ii+"\" class=\"hitDiv\">\n" +
                "  <div id=\"hitTitle\" class=\"hitTitle\">"+
                "    <a href=\""+aHit.url+"\" target=\"_blank\">"+aHit.title+"</a>"+"</div>\n" +
                "  <div id=\"hitURL\" class=\"hitURL\">"+
                "    <a href=\""+aHit.url+"\" target=\"_blank\">"+aHit.url+"</a>"+"</div>\n" +
                "  <div id=\"hitSnippet\" class=\"hitSnippet\">"+aHit.snippet+"</div>\n" +
                "<div>"
            ;
            var aHitDiv = $.parseHTML(hitHtml);
            divResults.append(aHitDiv);

            this.generatePagesButtons(this.totalHits);
        }

    }

    hitsForPage(pageNum) {
        var startIndex = pageNum * this.hitsPerPage;
        var endIndex = startIndex + this.hitsPerPage - 1;
        var hits = this.allHits.slice(startIndex, endIndex+1);

        console.log("** SearchController.hitsForPage: pageNum="+pageNum+", startIndex="+startIndex+", endIndex="+endIndex)
        console.log("** SearchController.hitsForPage: return #hits="+hits.length);

        return hits;
    }

    onSearchPrev() {
        if (this.currHitsPageNum > 0) {
            this.currHitsPageNum--;
        }
        this.showHitsPage(this.currHitsPageNum);
    }

    onSearchNext() {
        var nbPages = Math.ceil(this.totalHits / this.hitsPerPage);
        if (this.currHitsPageNum < nbPages - 1) {
            this.currHitsPageNum++;
        }
        this.showHitsPage(this.currHitsPageNum);
    }


    expandQueryThenSearch() {
        this.currHitsPageNum = 0;
        var isValid = this.validateQueryInput();
        if (isValid) {
            var divMessage = this.elementForProp("divMessage"); divMessage.html("retrieveAllHitsFromService---");
            this.setBusy(true);
            this.clearResults();
            var data = this.getSearchRequestData();
            this.invokeExpandQueryService(data,
                this.expandQuerySuccessCallback, this.expandQueryFailureCallback)
        }
    }

    clearResults() {
        this.elementForProp('divError').empty();
        this.elementForProp('divTotalHits').empty();
        this.elementForProp('divResults').empty();
        this.elementForProp('divPageNumbers').empty();
        this.elementForProp("prevPage").css("visibility", "hidden");
        this.elementForProp("nextPage").css("visibility", "hidden");
    }

    invokeExpandQueryService(jsonRequestData, _successCbk, _failureCbk) {
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
            url: 'srv/relatedwords',
            data: jsonRequestData,
            dataType: 'json',
            async: true,
            success: fctSuccess,
            error: fctFailure
        });
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
            this.setQuery(resp.expandedQuery);
            // this.setTotalHits(resp.totalHits);
            // this.totalHits = resp.totalHits;
            // this.allHits = resp.hits;
            // this.showHitsPage(0);
        }
        this.setBusy(false);
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


    setBusy(flag) {
        this.busy = flag;
        if (flag) {
            this.setTotalHits(null);
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
            word: this.elementForProp("txtQuery").val()
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
        this.setTotalHits(0);
    }

    setQuery(query) {
        this.elementForProp("txtQuery").val(query);
    }

    setTotalHits(totalHits) {
        var totalHitsText = "";
        if (totalHits > 0) {
            totalHitsText = "Found approximately "+totalHits+" hits";
        } else if (totalHits == 0) {
            totalHitsText = "No hits found";
        }
        this.elementForProp('divTotalHits').text(totalHitsText);
    }

    showOrHidePrevNextButtons(nbHits) {
        if (this.currHitsPageNum == 0) {
            this.elementForProp("prevPage").css("visibility", "hidden");
        } else {
            this.elementForProp("prevPage").css("visibility", "visible");
        }
        if (this.currHitsPageNum == 9) {
            this.elementForProp("nextPage").css("visibility", "hidden");
        } else {
            this.elementForProp("nextPage").css("visibility", "visible");
        }
    }

    generatePagesButtons(nbHits) {
        var divPageNumbers = this.elementForProp('divPageNumbers');
        divPageNumbers.empty();
        var nbPages = Math.ceil(nbHits / this.hitsPerPage);
        var more = false;
        if (nbPages > 10) {
            nbPages = 10;
            more = true;
        }
        for (var ip=0; ip<nbPages; ip++) {
            var pageLink = '<input class="page-number"' +
                ' type="button" '+
                ' name="'+'page-number'+(ip+1)+'" '+
                ' value="'+(ip)+'"/>';
            divPageNumbers.append(pageLink);
            if (ip != nbPages-1)
                divPageNumbers.append('&nbsp;&nbsp;');
        }
        if (more) divPageNumbers.append(" and more...");

        divPageNumbers.css('display','inline');
        $("#links-to-pages").css("display", "block");

        divPageNumbers.show();

        var thisSearchController = this;
        var inputsPageNumber = document.querySelectorAll('.page-number');
        for (var ipn=0; ipn<inputsPageNumber.length; ipn++) {
            inputsPageNumber[ipn].addEventListener(
                'click', function(ev) {
                    var el = ev.target;
                    var pageNumberOfButton = el.value;
                    thisSearchController.currHitsPageNum = pageNumberOfButton;
                    thisSearchController.showHitsPage(pageNumberOfButton);
                });
        }

        // Disable/enable the Previous and Next buttons depending on whether
        // we are on first/middle/last page of hits
        //
        this.showOrHidePrevNextButtons(nbHits);

        // Highlight the page button that corresponds to the current page of
        // hits.
        //
        $(".page-number").removeClass('current-page');
        $(".page-number[value='"+this.currHitsPageNum+"']").addClass('current-page');
    }
}