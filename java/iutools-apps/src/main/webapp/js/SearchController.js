/*
 * Controller for the search.html page.
 */

class SearchController extends WidgetController {
	
	constructor(config) {
		super(config);
		this.hitsPerPage = 10;
		this.totalHits = 0;
		this.prevPage = this.initialPage();
		this.prevPageNum = -1;
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
		this.retrieveAllHitsFromService();
		this.showHitsPage(0);
	}
	
	showHitsPage(pageNum) {
		var divResults = this.elementForProp("divResults");		
		divResults.empty();
		
		var results = hitsForPage(pageNum);
		
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
		}
		
	}
	
	hitsForPage(pageNum) {
		var startIndex = pageNum * this.hitsPerPage;
		var endIndex = startIndex + this.hitsPerPage;
		var hits = this.allHits.slice(startIndex, endIndex+1);
		
		return hits;
	}
	
	onSearchPrev() {
		if (this.prevPage.pageNum > 0)
			this.prevPage.pageNum--;
		this.retrieveAllHitsFromService();
	}

	onSearchNext() {
		var nbPages = Math.ceil(this.totalHits / this.hitsPerPage);
		if (this.prevPage.pageNum < nbPages - 1)
			this.prevPage.pageNum++;
		this.retrieveAllHitsFromService();
	}
	

	retrieveAllHitsFromService() {
		var isValid = this.validateQueryInput();
		if (isValid) {
			var divMessage = this.elementForProp("divMessage"); divMessage.html("retrieveAllHitsFromService---");
			this.setBusy(true);
			this.clearResults();
			var data = this.getSearchRequestData();
			this.invokeSearchService(data, 
					this.successCallback, this.failureCallback)
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
		
			$.ajax({
				method: 'POST',
				url: 'srv/search',
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

	successCallback(resp) {
		console.log('resp= '+JSON.stringify(resp));
		if (resp.errorMessage != null) {
			this.failureCallback(resp);
		} else {
			this.setQuery(resp.expandedQuery);
			this.setTotalHits(resp.totalHits);
			this.totalHits = resp.totalHits;
			this.allHits = resp.hits;
			this.setResults(resp.hits);	
			this.generatePagesButtons(resp.totalHits);
			$(".page-number").removeClass('current-page');
			$(".page-number[value='"+this.prevPage.pageNum+"']").addClass('current-page');
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
		var request = {
				prevPage: this.prevPage
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
//			this.alreadyShownHits.push(aHit.url);
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
		    				thisSearchController.retrieveAllHitsFromService();
		    		  });
	    }
	}
}
