/*
 * Controller for the spell.html page.
 */

class SpellController extends WidgetController {

	constructor(config) {
		super(config);
		// List of tokens that remain to be spell checked.
		// If null, it means we aren't in the process of checking tokens.
		this.tokensRemaining = null;

		// Token currently being spell checked. If null, means we are
		// not currently processing a token.
		this.tokenBeingChecked = null;
	}

	// Setup handler methods for different HTML elements specified in the config.
	attachHtmlElements() {
		this.setEventHandler("btnSpell", "click", this.spellCheck);
	}

	copyToClipboard() {
		// Create new element
		var el = document.createElement('textarea');
		// Set value (string to be copied)
		el.value = this.getSpellCheckedText();
		// Set non-editable to avoid focus and move outside of view
		el.setAttribute('readonly', '');
		el.style = {position: 'absolute', left: '-9999px'};
		document.body.appendChild(el);
		// Select text inside element
		el.select();
		// Copy text to clipboard
		document.execCommand('copy');
		// Remove temporary element
		document.body.removeChild(el);
		window.getSelection().removeAllRanges();
	}

	getSpellCheckedText() {
		var wholeTextElements = $('div#div-results').contents();
		var allText = '';
		wholeTextElements.each(function (index, item) {
			var text = "";
			if ($(item).is('.corrections')) {
				text = $(item).find('.selected').text();
				console.log('item.select text= "' + text + '"');
			} else if ($(item).is('span')) {
				text = $(item).text();
				console.log('item text= "' + text + '"');
			}
			allText += text;
			console.log('allText= "' + allText + '"');
		});
		return allText;
	}

	spellCheck() {
		var isValid = this.validateInputs();
		if (isValid) {
			this.clearResults();
			this.setBusy(true);
			this.tokenizeAndSpellCheck();
		}
	}

	tokenizeAndSpellCheck() {
		this.invokeTokenizeService(
			this.getTokenizeRequestData(),
			this.cbkTokenizeSuccess, this.cbkTokenizeFailure
		);
	}

	clearResults() {
		this.elementForProp('divError').empty();
		this.elementForProp('divResults').empty();
	}

	// invokeSpellServiceOnWholeText(jsonRequestData, _successCbk, _failureCbk) {
	// 	var controller = this;
	// 	var fctSuccess =
	// 		function (resp) {
	// 			_successCbk.call(controller, resp);
	// 		};
	// 	var fctFailure =
	// 		function (resp) {
	// 			_failureCbk.call(controller, resp);
	// 		};
	//
	// 	$.ajax({
	// 		type: 'POST',
	// 		url: 'srv/spell',
	// 		data: jsonRequestData,
	// 		dataType: 'json',
	// 		async: true,
	// 		success: fctSuccess,
	// 		error: fctFailure
	// 	});
	// }

	invokeSpellCheckWordService(jsonRequestData, _successCbk, _failureCbk) {
		var controller = this;
		var fctSuccess =
			function (resp) {
				_successCbk.call(controller, resp);
			};
		var fctFailure =
			function (resp) {
				_failureCbk.call(controller, resp);
			};

		$.ajax({
			type: 'POST',
			url: 'srv/spell',
			data: jsonRequestData,
			dataType: 'json',
			// We run each word synchronously, otherwise the server
			// may receive thouasands of concurrent requests for a
			// very long document.
			//
			async: true,
			success: fctSuccess,
			error: fctFailure
		});

	}

	invokeTokenizeService(jsonRequestData, _successCbk, _failureCbk) {
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
			type: 'POST',
			url: 'srv/tokenize',
			data: jsonRequestData,
			dataType: 'json',
			async: true,
			success: fctSuccess,
			error: fctFailure
		});
	}

	validateInputs() {
		var isValid = true;
		var toSpell = this.elementForProp("txtToCheck").val();
		if (toSpell == null || toSpell === "") {
			isValid = false;
			this.error("You need to enter some text to spell check");
		}
		return isValid;
	}
	
	onClickOnCorrections(ev) {
			var target = $(ev.target);
			var divParent = target.closest('div');
			console.log('divParent: '+divParent.length+"; word= "+divParent.attr('word'));
			$('span',divParent).css('display','block');
	}

	setCorrectionsHandlers() {
		$(document).find('div.corrections').on('mouseleave',function(ev){
			var target = $(ev.target);
			var divParent = target.closest('div');
			$('span',divParent).css('display','none');
			$('.selected',divParent).css('display','block');
			$('.additional input',divParent).val('');
			});
		$(document).find('span.suggestion.selected')
			.on('click',this.onClickOnCorrections);
		$(document).find('span.suggestion:not(.selected)')
			.on('mouseover',function(ev){
				$(ev.target).css({'color':'red'});
				})
			.on('mouseleave',function(ev){
				$(ev.target).css('color','black')
				})
			.on('click',function(ev){
				console.log('click');
				var target = $(ev.target);
				var divParent = target.closest('div');
				$('span',divParent).css('display','none');
				$('span.selected',divParent).removeClass('selected');
				target.addClass('selected');
				$('.additional input',divParent).val('');
				$('.selected',divParent).css('display','block');
				console.log('out of click');
				spellController.setCorrectionsHandlers();
				});
		$(document).find('span.additional input')
			.on('mouseleave',function(ev){
				var target = $(ev.target);
				var divParent = target.closest('div');
				//$('span',divParent).css('display','none');
				$('.selected',divParent).css('display','block');
				$('.additional input',divParent).val('');
				})
			.on('keyup',function(ev){
				if(ev.keyCode == 13) {
					var target = $(ev.target);
					var divParent = target.closest('div');
					var newSuggestionValue = target.val().trim();
					if (newSuggestionValue != '') {
						$('span',divParent).css('display','none');
						$('span.selected',divParent).removeClass('selected');
						var newSuggestionElement = $('<span class="suggestion selected">'+newSuggestionValue+'error(</span>');
						newSuggestionElement.insertBefore($('.original',divParent));
						spellController.setCorrectionsHandlers();
						$('.additional input',divParent).val('');
						$('.selected',divParent).css('display','block');
					}
					else {
						
					}
				}
			});
	}

	makeResultsSectionVisible() {
		var divChecked = this.elementForProp('divChecked');
		var divCheckedResults = divChecked.find('div#div-results');
		var divCheckedTitle = divChecked.find('div#title-and-copy');
		divCheckedTitle.css('display','block');
		divCheckedResults.css('display','block');
		divChecked.show();
	}

	cbkTokenizeSuccess(resp) {
		// console.log("-- SpellController.cbkTokenizeSuccess: got resp="+
		// 	  JSON.stringify(resp));
		if (resp.errorMessage != null) {
			this.cbkTokenizeFailure(resp);
		} else {
			// Retrieve the tokens from the tokenize response
			var respTokens = resp['tokens'];
			this.tokensRemaining = [];
			for (var ii=0; ii < respTokens.length; ii++) {
				var respToken = respTokens[ii];
				this.tokensRemaining.push({text: respToken.first, isWord:respToken.second})
			}
			this.spellCheckRemainingTokens();

			// // Invoke the Spell service on each word
			// //
			// for (ii=0; ii < tokens.length; ii++) {
			// 	var aToken = tokens[ii];
			// 	var word = aToken.text;
			// 	if (!aToken.isWord) {
			// 		this.displayToken(aToken);
			// 	} else {
			// 		this.invokeSpellCheckWordService(
			// 			this.getSpellWordRequestData(word),
			// 			this.cbkSpellWordSuccess, this.cbkSpellWordFailure);
			// 	}
			// }

			// this.setCorrectionsHandlers();

			// this.setBusy(false);
		}
	}

	spellCheckRemainingTokens() {
		console.log("-- spellCheckRemainingTokens: "+
			this.tokensRemaining.length+" tokens left");
		if (this.tokensRemaining.length == 0) {
			console.log("-- spellCheckRemainingTokens: No more tokens left. EXITING");
			this.setBusy(false);
			return;
		}

		this.tokenBeingChecked = this.tokensRemaining.shift();
		console.log("-- spellCheckRemainingTokens: checking token '"+
			JSON.stringify(this.tokenBeingChecked)+"'");
		this.spellCheckToken(this.tokenBeingChecked);
		var spellChecker = this;
		var readyForNextWord = function() {
			var ready = false;
			if (spellChecker.tokenBeingChecked == null) {
				console.log("-- spellCheckRemainingTokens.readyForNextWord: IS ready to check next token");
				ready = true;
			} else {
				console.log("-- spellCheckRemainingTokens.readyForNextWord: NOT ready to check next token");
				ready = false;
			}
			return ready;
		}
		var doRemaining = function() {
			spellChecker.spellCheckRemainingTokens();
		}
		new RunWhen().conditionMet(readyForNextWord, doRemaining, null, 100);
	}

	cbkTokenizeFailure(resp) {
		if (! resp.hasOwnProperty("errorMessage")) {
			// Error condition comes from tomcat itself, not from our servlet
			resp.errorMessage =
				"Server generated a "+resp.status+" error:\n\n" +
				resp.responseText;
		}
		this.error(resp);
		this.setBusy(false);
	}

	// cbkSpellSuccess(resp) {
	// 	if (resp.errorMessage != null) {
	// 		this.cbkSpellFailure(resp);
	// 	} else {
	// 		var divChecked = this.elementForProp('divChecked');
	// 		var divCheckedResults = divChecked.find('div#div-results');
	// 		var divCheckedTitle = divChecked.find('div#title-and-copy');
	// 		// var btnCopy = this.elementForProp('btnCopy');
	// 		divCheckedTitle.css('display','block');
	// 		divCheckedResults.css('display','block');
	// 		for (var ii=0; ii < resp.correction.length; ii++) {
	// 			var corrResult = resp.correction[ii];
	// 			var wordOutput = ""
	// 			if (! corrResult.wasMispelled) {
	// 				wordOutput = this.spanify(corrResult.orig)
	// 			} else {
	// 				wordOutput = this.picklistFor(corrResult);
	// 			}
	// 			divCheckedResults.append(wordOutput);
	// 		}
	// 		spellController.setCorrectionsHandlers();
	// 	}
	//
	// 	// btnCopy.show();
	// 	// this.setEventHandler("btnCopy", "click", this.copyToClipboard);
	//
	// 	this.setBusy(false);
	// }

	cbkSpellFailure(resp) {
		if (! resp.hasOwnProperty("errorMessage")) {
			// Error condition comes from tomcat itself, not from our servlet
			resp.errorMessage = 
				"Server generated a "+resp.status+" error:\n\n" +
				resp.responseText;
		}				
		this.error(resp);
		this.setBusy(false);
	}

	cbkSpellWordSuccess(resp) {
		if (resp.errorMessage != null) {
			this.cbkSpellWordFailure(resp);
		} else {
			// var divChecked = this.elementForProp("divCheckedNew");
			// var divCheckedResults = divChecked.find("div#div-results-new");
			var corrections = resp.correction;
			for (var ii=0; ii < corrections.length; ii++) {
				var aCorr = corrections[ii];
				// // console.log("-- cbkSpellWordSuccess: aCorr=" + JSON.stringify(aCorr));
				// var html = this.htmlWordToCheck(aCorr.orig, aCorr);
				// var eltPattern = '[class="token"][data-word="' + aCorr.orig + '"]';
				// var matchingTokens = $(eltPattern);
				// if (aCorr.wasMispelled) {
				// 	matchingTokens.empty();
				// 	// matchingTokens.append(this.picklistFor(aCorr));
				// 	matchingTokens.append(" >>"+aCorr.orig+"<< ");
				// }
				// this.labelWordsAsSpellChecked(aCorr.orig);
				var divSpellCheckedWords = this.divSpellCheckResults();
				if (!aCorr.wasMispelled) {
					var appended = divSpellCheckedWords.append(aCorr.orig);
				} else {
					this.appendSuggestionsPicklist(aCorr)
				}
			}
		}
	}

	appendSuggestionsPicklist(correction) {
		var divSpellCheckedWords = this.divSpellCheckResults();
		var html = this.picklistFor(correction);
		var appended = divSpellCheckedWords.append(html);
		appended.css("display", "block")

		// TODO-2020-12-09: This will set the handlers on ALL the picklists
		//   that have been created so far, including some for which the handlers
		//   have already been set.
		//
		// It would be more efficient to just set the handlers on the last
		//   picklist that was added
		//
		this.setCorrectionsHandlers();
		return;
	}

	eltsForWordToCheck(word) {
		var eltPattern = '[class="token"][data-word="' + word + '"]';
		return $(eltPattern);
	}

	cbkSpellWordFailure(resp) {
		if (! resp.hasOwnProperty("errorMessage")) {
			// Error condition comes from tomcat itself, not from our servlet
			resp.errorMessage =
				"Server generated a "+resp.status+" error:\n\n" +
				resp.responseText;
		}
		this.error(resp);
		this.setBusy(false);
	}

	spanify(text) {
		return '<span>'+text+'</span>';
	}

	labelWordsAsSpellChecked(word) {
		var wordSpans = this.eltsForWordToCheck(word);
		wordSpans.removeAttr("style");
	}

	picklistFor(corrResult) {
		console.log("corrResult= "+JSON.stringify(corrResult));
		var origWord = corrResult.orig;
		var alternatives = corrResult.allSuggestions;
		var picklistHtml = "<div class='corrections' word='"+corrResult.orig+"'>\n";
		picklistHtml += "<span class=\"suggestion original selected\">"+origWord+"</span>\n";
		var inputLength = origWord.length;
		for (var ii=0; ii < alternatives.length; ii++) {
			var anAlternative = alternatives[ii];
			picklistHtml += "<span class=\"suggestion";
			picklistHtml += "\">"+anAlternative+"</span>\n";
			if (anAlternative.length > inputLength)
				inputLength = anAlternative.length;
		}
		picklistHtml += "<span class=\"additional\">"+"<input type=\"text\" />"+"</span>\n";
		picklistHtml += "</div>\n";
		return picklistHtml;
	}
	
	setBusy(flag) {
		console.log('setBusy: '+flag);
		this.busy = flag;
		if (flag) {
			this.disableSpellButton();	
			this.showSpinningWheel('divMessage', "Checking");
			this.error("");
			this.elementForProp('btnCopy').hide();
		} else {
			this.enableSpellButton();
			this.hideSpinningWheel('divMessage');
			var btnCopy = this.elementForProp('btnCopy');
			btnCopy.show();
			this.setEventHandler("btnCopy", "click", this.copyToClipboard);
		}

		return;
	}
	
	getSpellWholeTextRequestData() {
		
		var includePartials = 
			this.elementForProp("chkIncludePartials").is(':checked')
		
		var request = {
				text: this.elementForProp("txtToCheck").val(),
				includePartiallyCorrect: includePartials
		};
		
		return JSON.stringify(request);
	}

	getSpellWordRequestData(word) {
		var includePartials =
			this.elementForProp("chkIncludePartials").is(':checked')

		var request = {
			text: word,
			includePartiallyCorrect: includePartials
		};

		return JSON.stringify(request);
	}

	getTokenizeRequestData() {

		var request = {
			textOrUrl: this.elementForProp("txtToCheck").val(),
		};

		return JSON.stringify(request);
	}
	
	disableSpellButton() {
		this.elementForProp('btnSpell').attr("disabled", true);
	}
	
	enableSpellButton() {
		this.elementForProp('btnSpell').attr("disabled", false);
	}

	displayError(errMess) {
		if (errMess != null && !(errMess === "")) {
			errMess = "SpellController raised an error:\n" + errMess;
			console.log(errMess);
			this.elementForProp('divError').html(errMess);
			this.elementForProp('divError').show();
		} else {
			this.elementForProp('divError').empty();
		}

	}
	
	getCheckedText() {
		var divChecked = this.elementForProp('divChecked');

		return divChecked.text();
	}

	// clearResults() {
	// 	var divCheckedResults = this.divSpellCheckResults();
	// 	divCheckedResults.empty();
	// 	divCheckedResults.css('display','block');
	// }

	divSpellCheckResults() {
		var divChecked = this.elementForProp('divChecked');
		var divCheckedResults = divChecked.find('div#div-results');
		return divCheckedResults;
	}

	displayToken(token, correction) {
		console.log("-- displayToken: displaying token '"+
			JSON.stringify(token)+"'");

		var divCheckedResults = this.divSpellCheckResults();
		var html;
		var word = token.text;
		if (!token.isWord || !correction.wasMisspelled) {
			// This token is either punctuation or a word that was correctly
			// spelled.
			html = word;
		} else {
			html = this.picklistFor(correction);
		}
		divCheckedResults.append(html);

		// TODO-2020-12-09: Should only set the handler on the last
		//   element in divCheckedResults.
		//   Note: We want to set the handler NOW because we want the user
		//   to be able to manipulate the suggestions picklist right away.
		// spellController.setCorrectionsHandlers();
	}


	displayTokens(tokens) {
		// var divChecked = this.elementForProp('divChecked');
		// var divCheckedResults = divChecked.find('div#div-results');
		// var words = [];
		// for (var ii=0; ii < tokens.length; ii++) {
		// 	var aToken = tokens[ii];
		// 	var aTokenText = aToken.text;
		// 	var wordOutput = null;
		// 	if (aToken.isWord) {
		// 		words.push(aTokenText);
		// 		wordOutput = this.htmlWordToCheck(aTokenText);
		// 	} else {
		// 		wordOutput = aTokenText;
		// 	}
		// 	divCheckedResults.append(wordOutput);
		// }
		// divCheckedResults.show();
		// spellController.setCorrectionsHandlers();
		//
		// return words;
	}

	htmlWordToCheck(text, correction) {
		var html = null;
		// For words, render them as a <span>
		html = "<span class=\"token\" data-is-word=true";
		html +=" data-word=\""+text+"\"";
		if (correction == null) {
			// If we haven't received a correction, it means we are just
			// displaying the words that will be eventually be spell checked.
			// Make they be greyed out so we know they have not yet been process
			//
			html += " style=\"color:grey\"";
		}
		html += ">"+text+"</span>";

		return html;
	}

	spellCheckToken(token) {
		console.log("-- spellCheckToken: checking token '"+
			JSON.stringify(token)+"'");
		if (!token.isWord) {
			this.displayToken(token);
			this.tokenBeingChecked = null;
		} else {
			var word = token.text;
			var spellChecker = this;
			var cbkSuccess = function(resp) {
				spellChecker.cbkSpellWordSuccess(resp);
				spellChecker.tokenBeingChecked = null;
			}
			var cbkFailure = function(resp) {
				spellChecker.cbkSpellWordFailure(resp);
				spellChecker.tokenBeingChecked = null;
			}
			this.invokeSpellCheckWordService(
				this.getSpellWordRequestData(word),
				cbkSuccess, cbkFailure);
		}
	}
}

