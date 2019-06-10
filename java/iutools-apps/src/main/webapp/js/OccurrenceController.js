/*
 * Controller for the search.html page.
 */

class OccurrenceController extends WidgetController {

	constructor(config) {
		super(config);
		this.totalHits = 0;
		this.attachHtmlElements();
		
		this.alreadyShownHits = [];
	} 
	
	// Setup handler methods for different HTML elements specified in the config.
	attachHtmlElements() {
		this.setEventHandler("btnGet", "click", this.onGet);
		this.setEventHandler("iconizer", "click", this.iconizeDivExampleWord);
		this.setEventHandler("divIconizedExampleWord", "click", this.deiconizeDivExampleWord);
		this.onReturnKey("morpheme", this.onGet);
	}
	
	attachHtmlElementsOthers() {
		$(".word-example",document).on("click",this.onWordSelect);
	}
	
	onGet() {
		var divExampleWord = this.elementForProp("divExampleWord");
		$('#contents',divExampleWord).html('').parent().hide();
		this.elementForProp('exampleWord').val('');
		var isValid = this.validateQueryInput();
		if (isValid) {
			this.clearResults();
			this.setBusy(true);
			this.invokeSearchService(this.getSearchRequestData(), 
					this.successCallback, this.failureCallback)
		}
	}
	
	onWordSelect(event) {
		var element = event.target;
		console.log("word: "+$(element).text());
		// TODO: decide where the results will be displayed (in hidden div?)
//		this.setBusy(true);
		this.elementForProp('exampleWord').val($(element).text());
		var divExampleWord = this.elementForProp("divExampleWord");
		$('#contents',divExampleWord).html('');
		var divIconizedWordExample = this.elementForProp("divIconizedExampleWord");
		divIconizedWordExample.hide();
		divExampleWord.show();
		this.showSpinningWheelInWordExample();
		this.invokeSearchService(this.getSearchRequestData(),
				this.successExampleWordCallback, this.failureExampleWordCallback)
	}
	
	iconizeDivExampleWord() {
		var divExampleWord = this.elementForProp("divExampleWord");
		divExampleWord.hide();
		var divIconizedWordExample = this.elementForProp("divIconizedExampleWord");
		divIconizedWordExample.show();
	}
	
	deiconizeDivExampleWord() {
		console.log('deiconize example word div');
		var divExampleWord = this.elementForProp("divExampleWord");
		divExampleWord.show();
		var divIconizedWordExample = this.elementForProp("divIconizedExampleWord");
		divIconizedWordExample.hide();
	}
	
	clearResults() {
		this.elementForProp('divError').empty();
		this.elementForProp('divResults').empty();
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
		
//			console.log("jsonRequestData= "+JSON.stringify(jsonRequestData));
			$.ajax({
				method: 'POST',
				url: 'srv/occurrences',
				data: jsonRequestData,
				dataType: 'json',
				async: true,
		        success: fctSuccess,
		        error: fctFailure
			});
	}
	
	validateQueryInput() {
		var isValid = true;
		var query = this.elementForProp("morpheme").val();
		if (query == null || query === "") {
			isValid = false;
			this.error("You need to enter something in the morpheme field");
		}
		return isValid;
	}

	successCallback(resp) {
		if (resp.errorMessage != null) {
			this.failureCallback(resp);
		} else {
//			console.log('successCallback --- resp.totalHits='+resp.totalHits);
			//this.setTotalHits(resp.totalHits);
			//this.totalHits = resp.totalHits;
			this.setResults(resp);	
			//this.generatePagesButtons(resp.totalHits);
		}
		this.setBusy(false);
	}
	
	successExampleWordCallback(resp) {
		if (resp.errorMessage != null) {
			this.failureCallback(resp);
		} else {
//			console.log('successCallback --- resp.totalHits='+resp.totalHits);
			this.setExampleWordResults(resp);	
		}
//		this.setBusy(false);
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
	
	failureExampleWordCallback(resp) {
		if (! resp.hasOwnProperty("errorMessage")) {
			// Error condition comes from tomcat itself, not from our servlet
			resp.errorMessage = 
				"Server generated a "+resp.status+" error:\n\n" +
				resp.responseText;
		}				
		this.error(resp.errorMessage);
		this.hideSpinningWheelInWordExample();
	}
	
	
	setBusy(flag) {
		if (flag) {
			//this.setTotalHits(null);
			this.disableSearchButton();	
			this.showSpinningWheel();
			this.error("");
		} else {
			this.enableSearchButton();		
			this.hideSpinningWheel();
		}
	}
	
	
	getSearchRequestData() {
		var wordPattern = this.elementForProp("morpheme").val().trim();
		if (wordPattern=='')
			wordPattern = null;
		var exampleWord = this.elementForProp("exampleWord").val();
		if (exampleWord=='')
			exampleWord = null;

		var request = {
				wordPattern: wordPattern,
				exampleWord: exampleWord
		};
		
		var jsonInputs = JSON.stringify(request);
		
		return jsonInputs;
	}
	
	disableSearchButton() {
		this.elementForProp('btnGet').attr("disabled", true);
	}
	
	enableSearchButton() {
		this.elementForProp('btnGet').attr("disabled", false);

	}
	
	showSpinningWheel() {
		var divMessage = this.elementForProp('divMessage');
		divMessage.empty();
		divMessage.append("<img src=\"ajax-loader.gif\"> Searching ...")
		divMessage.css('display');
	}
	
	hideSpinningWheel() {
		var divMessage = this.elementForProp('divMessage');
		divMessage.empty();
		divMessage.css('display');
	}

	error(err) {
		this.elementForProp('divError').html(err);
		this.elementForProp('divError').show();	 
		//this.setTotalHits(0);
	}
	
	setQuery(query) {
		this.elementForProp("morpheme").val(query);
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
		
		var res = results.matchingWords;
		var keys = Object.keys(res);
		var html = 'The input is the nominal form of '+keys.length+' morpheme'+
			(keys.length==1?'':'s')+': ';
		html += '<div id="list-of-morphemes">';
		html += '<ul>';
		for (var ires=0; ires<keys.length; ires++) {
			var key = keys[ires];
			var meaning = res[key].meaning;
			html += '<li><a href="#'+key+'">'+key+'</a>&nbsp;&nbsp;&nbsp;&ndash;&nbsp;&nbsp;&nbsp;'+meaning+'</li>'
		}
		html += '</ul>';
		html += '</div>';
		
		
		for (var ires=0; ires<keys.length; ires++) {
			var key = keys[ires];
			var meaning = res[key].meaning;
			var words = res[key].words;
			var wordFreqs = res[key].wordFrequencies;
			var wordsFreqsArray = new Array(wordFreqs.length);
			for (var iwf=0; iwf<wordFreqs.length; iwf++) {
				wordsFreqsArray[iwf] = '<a class="word-example" id="word-example-'+words[iwf]+'">'+words[iwf]+'</a>'+
										'('+wordFreqs[iwf]+')';
			}
			html += '<div class="morpheme-details">';
			html += '<a name="'+key+'"></a>'+'<strong>'+key+'</strong>&nbsp;&nbsp;&nbsp;&ndash;&nbsp;&nbsp;&nbsp;'+meaning+
				'<div style="margin:5px 80px 15px 15px;">'+wordsFreqsArray.join(';&nbsp;&nbsp;&nbsp; ')+'</div>';
			html += '</div>';
		}
		divResults.append(html);
		
		var thisController = this;
		var anchorsWords = document.querySelectorAll('.word-example');
	    for (var ipn=0; ipn<anchorsWords.length; ipn++) {
	    	anchorsWords[ipn].addEventListener(
		    		  'click', function(ev) {
		    				thisController.onWordSelect(ev);
		    		  });
	    }
				
		divResults.show();
	}
	
	setExampleWordResults(results) {
		console.log("results: "+JSON.stringify(results));
		var divExampleWord = this.elementForProp("divExampleWord");
		this.hideSpinningWheelInWordExample();
		var gist = results.exampleWord.gist;
		console.log('gist= '+JSON.stringify(gist));
		$('#word',divExampleWord).html('Example word: '+'<strong>'+gist.word+'</strong>');
		var wordComponents = gist.wordComponents;
		console.log("wordComponents= "+JSON.stringify(wordComponents));
		var html = '<table class="gist"><tr><th>Morpheme</th><th>Meaning</th></tr>';
		for (var iwc=0; iwc<wordComponents.length; iwc++) {
			var component = wordComponents[iwc];
			html += '<tr><td>'+component.fst+'</td><td>'+component.snd+'</td></tr>'
		}
		html += '</table>';
		var alignments = results.exampleWord.alignments;
		html += '<table class="alignments"><th>Inuktitut</th><th>English</th></tr>';
		for (var ial=0; ial<Math.min(30,alignments.length); ial++) {
			var alignmentParts = alignments[ial].split(":: ");
			var sentences = alignmentParts[1].split("@----@");
			var inuktitutSentence = sentences[0].replace(gist.word,'<strong>'+gist.word+'</strong>').replace(/\.{5,}/,'...');
			var englishSentence = sentences[1].replace(/\.{5,}/,'...').trim();
			html += '<tr><td>'+inuktitutSentence+'</td><td>'+englishSentence+'</td></tr>';
		}
		html += '</table>';
		$('#contents',divExampleWord).html(html);
	}

	showSpinningWheelInWordExample() {
		var divExampleWord = this.elementForProp("divExampleWord");
		$('#word',divExampleWord).html("<img src=\"ajax-loader.gif\"> Searching ...");
	}
	
	hideSpinningWheelInWordExample() {
		var divExampleWord = this.elementForProp("divExampleWord");
		$('#word',divExampleWord).empty();
	}


	/*generatePagesButtons(nbHits) {
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
				' value="'+(ip+1)+'"/>';
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
		    				thisSearchController.currentPage = pageNumberOfButton;
		    				thisSearchController.searchFromCurrentPage();
		    		  });
	    }
	}*/
	
	onTest() {
		this.invokeTestService({}, 
				this.testSuccessCallback, this.testFailureCallback);
	}

	invokeTestService(jsonRequestData, _successCbk, _failureCbk) {
			var controller = this;
			var fctSuccess = 
					function(resp) {
						_successCbk.call(controller, resp);
					};
			var fctFailure = 
					function(resp) {
						_failureCbk.call(controller, resp);
					};
		
			$.ajax({
				method: 'POST',
				url: 'srv/hello',
				data: jsonRequestData,
				dataType: 'json',
				async: true,
		        success: fctSuccess,
		        error: fctFailure
			});
	}
	
	testSuccessCallback(resp) {
		var element = this.elementForProp("divTestResponse");
		element.empty();
		element.html(resp.message);
	}
	    
	testFailureCallback(resp) {
		var element = this.elementForProp("divTestResponse");
		element.empty();
		element.html("Server returned error, resp="+JSON.stringify(resp));
	}
	

}
