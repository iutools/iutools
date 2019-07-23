/*
 * Controller for the search.html page.
 */

class OccurrenceController extends WidgetController {

	constructor(config) {
		super(config);
		this.totalHits = 0;
		this.alreadyShownHits = [];
	} 
	
	// Setup handler methods for different HTML elements specified in the config.
	attachHtmlElements() {
		this.setEventHandler("btnGet", "click", this.onGet);
		this.setEventHandler("iconizer", "click", this.iconizeDivExampleWord);
		this.setEventHandler("divIconizedExampleWord", "click", this.deiconizeDivExampleWord);
		this.onReturnKey("inpMorpheme", this.onGet);
	}
	
	onGet() {
		var divExampleWord = this.elementForProp("divExampleWord");
		$('#contents',divExampleWord).html('').parent().hide();
		this.elementForProp('inpExampleWord').val('');
		var isValid = this.validateQueryInput();
		if (isValid) {
			this.clearResults();
			this.setGetBusy(true);
			var requestData = this.getSearchRequestData();
			console.log('requestData= '+JSON.stringify(requestData));
			this.invokeGetService(requestData, 
					this.successGetCallback, this.failureGetCallback);
		}
	}
	
	onWordSelect(elementID) {
		var element = $('#'+elementID);
		console.log("word: "+$(element).text());
		occurrenceController.elementForProp("inpExampleWord").val($(element).text());
		occurrenceController.setWordExampleBusy(true);
		var divExampleWord = occurrenceController.elementForProp("divExampleWord");
		$('#contents',divExampleWord).html('');
		var divWordInExample = occurrenceController.elementForProp("divWordInExample");
		divWordInExample.html('');
		var divIconizedWordExample = occurrenceController.elementForProp("divIconizedExampleWord");
		divIconizedWordExample.hide();
		divExampleWord.show();
		occurrenceController.showSpinningWheel("divMessageInExample","Searching");
		occurrenceController.invokeExampleWordService(occurrenceController.getSearchRequestData(),
				occurrenceController.successExampleWordCallback, occurrenceController.failureExampleWordCallback);
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
	
	invokeGetService(jsonRequestData, _successCbk, _failureCbk) {
		this.invokeSearchService(jsonRequestData, _successCbk, _failureCbk);
	}
	
	invokeExampleWordService(jsonRequestData, _successCbk, _failureCbk) {
		this.invokeSearchService(jsonRequestData, _successCbk, _failureCbk);
	}
	
	invokeSearchService(jsonRequestData, _successCbk, _failureCbk) {
			var tracer = new Tracer('OccurenceController.invokeSearchService', true);
			tracer.trace("_successCbk="+_successCbk+", jsonRequestData="+JSON.stringify(jsonRequestData));
			this.busy = true;
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
				url: 'srv/occurrences',
				data: jsonRequestData,
				dataType: 'json',
				async: true,
		        success: fctSuccess,
		        error: fctFailure
			});
			
			tracer.trace('OccurenceController.invokeSearchService', "exited");
	}
	
	validateQueryInput() {
		var isValid = true;
		var query = this.elementForProp("inpMorpheme").val();
		if (query == null || query === "") {
			isValid = false;
			this.error("You need to enter something in the morpheme field");
		}
		return isValid;
	}

	successGetCallback(resp) {
		var tracer = new Tracer('OccurenceController.successGetCallback', true);
		tracer.trace("resp="+JSON.stringify(resp));
		
		if (resp.errorMessage != null) {
			this.failureGetCallback(resp);
		} else {
			this.setGetResults(resp);	
		}
		this.setGetBusy(false);
		tracer.trace("exited");
	}
	
	successExampleWordCallback(resp) {
		var tracer = new Tracer('OccurenceController.successExampleWordCallback', true);
		tracer.trace("resp="+JSON.stringify(resp));		
		if (resp.errorMessage != null) {
			this.failureExampleWordCallback(resp);
		} else {
			this.setExampleWordResults(resp);	
		}
		this.setWordExampleBusy(false);
	}

	failureGetCallback(resp) {
		if (! resp.hasOwnProperty("errorMessage")) {
			// Error condition comes from tomcat itself, not from our servlet
			resp.errorMessage = 
				"Server generated a "+resp.status+" error:\n\n" +
				resp.responseText;
		}				
		this.error(resp.errorMessage);
		this.setGetBusy(false);
	}
	
	failureExampleWordCallback(resp) {
		if (! resp.hasOwnProperty("errorMessage")) {
			// Error condition comes from tomcat itself, not from our servlet
			resp.errorMessage = 
				"Server generated a "+resp.status+" error:\n\n" +
				resp.responseText;
		}				
		this.error(resp.errorMessage);
		this.hideSpinningWheel("divMessageInExample");
	}
	
	
	setGetBusy(flag) {
		this.busy = flag;
		if (flag) {
			this.disableSearchButton();	
			this.showSpinningWheel('divMessage','Searching');
			this.error("");
		} else {
			this.enableSearchButton();		
			this.hideSpinningWheel('divMessage');
		}
	}
	
	setWordExampleBusy(flag) {
		this.busy = flag;		
		if (flag) {
			this.showSpinningWheel('divMessageInExample','Searching');
		} else {
			this.hideSpinningWheel('divMessageInExample');
		}
	}
	
	
	getSearchRequestData() {
		var tracer = new Tracer('OccurenceController.getSearchRequestData');
		var wordPattern = this.elementForProp("inpMorpheme").val().trim();
		if (wordPattern=='')
			wordPattern = null;
		var exampleWord = this.elementForProp("inpExampleWord").val();
		if (exampleWord=='')
			exampleWord = null;
		var corpusName = this.elementForProp("inpCorpusName").val().trim();
		if (corpusName=='')
			corpusName = null;

		var request = {
				wordPattern: wordPattern,
				exampleWord: exampleWord,
				corpusName: corpusName
		};
		
		var jsonInputs = JSON.stringify(request);;
		
		tracer.trace("returning jsonInputs="+jsonInputs);
		return jsonInputs;
	}
	
	enableSearchButton() {
		this.elementForProp('btnGet').attr("disabled", false);
	}
	
	disableSearchButton() {
		this.elementForProp('btnGet').attr("disabled", true);
	}
	
//	enableGetWordExample() {
//		var thisController = this;
//		var anchorsWords = document.querySelectorAll('.word-example');
//	    for (var ipn=0; ipn<anchorsWords.length; ipn++) {
//	    	anchorsWords[ipn].addEventListener(
//		    		  'click', thisController.onWordSelect);
//	    }
//	}
//
//	disableGetWordExample() {
//		var thisController = this;
//		var anchorsWords = document.querySelectorAll('.word-example');
//	    for (var ipn=0; ipn<anchorsWords.length; ipn++) {
//	    	anchorsWords[ipn].removeEventListener(
//		    		  'click', thisController.onWordSelect);
//	    }
//	}
	

	error(err) {
		this.elementForProp('divError').html(err);
		this.elementForProp('divError').show();	 
	}
	

	setGetResults(results) {
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
				wordsFreqsArray[iwf] = 
					'<a class="word-example" id="word-example-'+words[iwf]+'"' +
					' onclick="occurrenceController.onWordSelect(\''+'word-example-'+words[iwf]+'\')"'+
					'>'+words[iwf]+'</a>'+'('+wordFreqs[iwf]+')';
			}
			html += '<div class="morpheme-details">';
			html += '<a name="'+key+'"></a>'+'<strong>'+key+'</strong>&nbsp;&nbsp;&nbsp;&ndash;&nbsp;&nbsp;&nbsp;'+meaning+
				'<div style="margin:5px 80px 15px 15px;">'+wordsFreqsArray.join(';&nbsp;&nbsp;&nbsp; ')+'</div>';
			html += '</div>';
		}
		divResults.append(html);
		
		var thisController = this;
		
		new RunWhen().domReady(function() {
				divResults.show();
		});
	}
	
	attachListenersToExampleWords(controller) {
		var anchorsWords = document.querySelectorAll('.word-example');
	    for (var ipn=0; ipn<anchorsWords.length; ipn++) {
	    	anchorsWords[ipn].addEventListener(
		    		  'click', 
		    		  controller.onWordSelect
		    		  );
	    }
	}
	
		
	setExampleWordResults(results) {
		var divExampleWord = this.elementForProp("divExampleWord");
		this.hideSpinningWheel("divMessageInExample");
		var gist = results.exampleWord.gist;
		console.log('gist= '+JSON.stringify(gist));
		this.elementForProp("divWordInExample").html('Example word: '+'<strong>'+gist.word+'</strong>');
		var wordComponents = gist.wordComponents;
		console.log("wordComponents= "+JSON.stringify(wordComponents));
		var html = '<table id="tbl-gist" class="gist"><tr><th>Morpheme</th><th>Meaning</th></tr>';
		for (var iwc=0; iwc<wordComponents.length; iwc++) {
			var component = wordComponents[iwc];
			html += '<tr><td>'+component.fst+'</td><td>'+component.snd+'</td></tr>'
		}
		html += '</table>';
		var alignments = results.exampleWord.alignments;
		html += '<table id="tbl-alignments" class="alignments"><th>Inuktitut</th><th>English</th></tr>';
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

	
	// ---------------------- Test Section ------------------------------ //

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
