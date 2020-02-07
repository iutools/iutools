/*
 * Controller for the search.html page.
 */

class SearchControllerNew extends WidgetController {
	
	constructor(config) {
		super(config);
		this.hitsPerPage = 10;
		this.pagesCache = {};
		this.cacheHitsPage(0, this.hitsPage(0));
		this.prevPageNum = 0
		this.totalHits = 0;
	}
	
	initializeHitsCache() {
		this.cacheHitsPage(0, this.initialPage());
		this.alreadyShownHits = [];		
	}
	
	
	initialPage(query) {
		var initialPage = {
			'query': query,
			'pageNum': 0,
			'hasNext': true,
			'resp': null
		};	
		return initialPage;
	}
	
	currPageNum() {
		if (typeof this.prevPageNum === 'string' || this.prevPageNum instanceof String) {
			this.prevPageNum = parseInt(this.prevPageNum, 10);
		}
		return this.prevPageNum + 1;
	}
	
	incrCurrPageNum() {
		if (this.prevPageNum < this.maxPages() - 1)
			this.prevPageNum++;
	}
	
	decrCurrPageNum() {
		if (this.prevPageNum > 0) {
			this.prevPageNum--;
		}
	}
	
	hitsPage(pageNum, resp, hasNext) {
		var query = this.elementForProp("txtQuery").val();
				
		var page = {
				'query': query,
				'pageNum': pageNum,
				'hasNext': hasNext,
				'resp': resp
		}
		return page;
	}
	
	cacheNewHitsPage(resp) {
		var newPageNum = Object.keys(this.pagesCache).length;
		this.incrCurrPageNum();
		var currPage = this.hitsPage(newPageNum, resp);
		this.cacheHitsPage(newPageNum, currPage);
	}
	
	uncacheCurrPageServiceResponse() {
		var resp = null;
		if (this.currPageNum() in this.pagesCache) {
			resp = this.pagesCache[this.currPageNum()];
		}
		return resp;
	}
	
	cacheHitsPage(pageNum, page) {
		this.pagesCache[pageNum] = page;
	}	
	
	uncacheHitsPage(pageNum) {
		var page = null;
		if (pageNum in this.pagesCache) {
			page = this.pagesCache[pageNum];
		}
		return page;
	}
	
	maxPages() {
		var nbPages = Math.ceil(this.totalHits / this.hitsPerPage);
		return nbPages;
	}
	
	// Setup handler methods for different HTML elements specified in the config.
	attachHtmlElements() {
		this.setEventHandler("btnSearch", "click", this.onSearch);
		this.setEventHandler("prevPage", "click", this.onSearchPrev);
		this.setEventHandler("nextPage", "click", this.onSearchNext);
		this.onReturnKey("txtQuery", this.onSearch);
	}
	
	onSearch() {
		console.log("-- SearchControllerNew.onSearch: invoked");
		this.initializeHitsCache();
		this.searchFromCurrentPage();
	}
	
	onSearchPrev() {
		this.decrCurrPageNum();
		this.searchFromCurrentPage();
	}

	onSearchNext() {
		this.incrCurrPageNum();
		this.searchFromCurrentPage();
	}

	searchFromCurrentPage() {
		var cachedResp = this.uncacheCurrPageServiceResponse();
		if (cachedResp != null) {
			// We have a cache entry for this page of hits
			this.successCallback(cachedResp);
		} else {
			// Set the previous page's cache entry so it knows
			// it has a next element.
			//
			this.uncacheHitsPage(this.currPageNum()-1).hasNext = true;
			
			var isValid = this.validateQueryInput();
			if (isValid) {
				this.setBusy(true);
				this.clearResults();
				this.invokeSearchService(this.getSearchRequestData(), 
						this.successCallback, this.failureCallback)
			}
		}
	}
	
	clearResults() {
		this.elementForProp('divError').empty();
		this.elementForProp('divTotalHits').empty();
		this.elementForProp('divResults').empty();
		this.elementForProp('divPageNumbers').empty();
	}
	
	invokeSearchService(jsonRequestData, _successCbk, _failureCbk) {
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
					
			var request = {
					method: 'POST',
					url: 'srv/search',
					data: jsonRequestData,
					dataType: 'json',
					async: true,
			        success: fctSuccess,
			        error: fctFailure
				};	
		
			$.ajax(request);
			
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

	successCallback(resp) {
		console.log('resp= '+JSON.stringify(resp));
		if (resp.errorMessage != null) {
			this.failureCallback(resp);
		} else {
			this.cacheNewHitsPage(resp);
			this.setQuery(resp.expandedQuery);
			this.setTotalHits(resp.totalHits);
			this.totalHits = resp.totalHits;
			this.setResults(resp.hits);	
			this.generatePagesButtons(resp.totalHits);
			$(".page-number").removeClass('current-page');
			
			$(".page-number[value='"+(this.currPageNum()-1)+"']").addClass('current-page');
		}
		this.setBusy(false);
	}

	failureCallback(resp) {
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
		var prevPage = this.uncacheHitsPage(this.currPageNum()-1)
		
		var prevPageNoResp = JSON.parse(JSON.stringify(prevPage));
		delete prevPageNoResp.resp;
		prevPageNoResp.query = this.elementForProp("txtQuery").val();
		
		var request = {
			prevPage: prevPageNoResp
		}
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
			totalHitsText = "Found "+totalHits+" hits";
		} else if (totalHits == 0) {
			totalHitsText = "No hits found";
		}
		this.elementForProp('divTotalHits').text(totalHitsText);
	}
	
	setResults(results) {
		var jsonResults = JSON.stringify(results);
		var divResults = this.elementForProp("divResults");
		
		divResults.empty();
		
		for (var ii = 0; ii < results.length; ii++) {
			var aHit = results[ii];
			console.log('aHit.title= '+aHit.title);
			this.alreadyShownHits.push(aHit.url);
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
	    }
				
		divResults.show();
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
		    				thisSearchController.prevPage.pageNum = pageNumberOfButton;
		    				thisSearchController.searchFromCurrentPage();
		    		  });
	    }
	}
}
