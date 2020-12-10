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

	/**
	 * Setup handler methods for different HTML elements specified in the config.
	 */
	attachHtmlElements() {
		this.setEventHandler("btnSpell", "click", this.spellCheck);
	}

	/**
	 * Copy spell checked content to clipboard
	 */
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

	/**
	 * Spell check the text entered by the user.
	 */
	spellCheck() {
		var isValid = this.validateInputs();
		if (isValid) {
			this.clearResults();
			this.setBusy(true);
			this.tokenizeAndSpellCheck();
		}
	}

	/**
	 * Tokenize the text provided by the user, then spell check
	 * each token in turn, displaying result for each token as
	 * soon as it becomes available.
	 */
	tokenizeAndSpellCheck() {
		this.invokeTokenizeService(
			this.tokenizeRequestData(),
			this.cbkTokenizeSuccess, this.cbkTokenizeFailure
		);
	}

	/**
	 * Clear the display area containing the spell checking results.
	 */
	clearResults() {
		this.elementForProp('divError').empty();
		this.elementForProp('divResults').empty();
	}

	/**
	 * Invoke the tokenization web service.
	 *
	 * The success callback should spell check every token returned by the
	 * web service.
	 */
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

	/**
	 * Invoke the spell check web service on a single word.
	 */
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

	/**
	 * Invoked when user clicks on the picklist providing the list of suggested
	 * corrections for a mis-spelled word.
	 */
	onClickOnCorrections(ev) {
			var target = $(ev.target);
			var divParent = target.closest('div');
			console.log('divParent: '+divParent.length+"; word= "+divParent.attr('word'));
			$('span',divParent).css('display','block');
	}

	/**
	 * Sets handlers on the picklists that provide suggested corrections for
	 * a single misspelled words.
	 */
	attachSingleWordCorrectionHandlers(divSingleWordCorrection) {
		var spellController = this;
		divSingleWordCorrection.on('mouseleave',function(ev){
			var target = $(ev.target);
			var divParent = target.closest('div');
			$('span',divParent).css('display','none');
			$('.selected',divParent).css('display','block');
			$('.additional input',divParent).val('');
		});
		divSingleWordCorrection.find('span.suggestion.selected')
			.on('click',this.onClickOnCorrections);
		divSingleWordCorrection.find('span.suggestion:not(.selected)')
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
				spellController.attachAllCorrectionsHandlers();
			});
		divSingleWordCorrection.find('span.additional input')
			.on('mouseleave',function(ev){
				var target = $(ev.target);
				var divParent = target.closest('div');
				//$('span',divParent).css('display','none');
				$('.selected',divParent).css('display','block');
				$('.additional input',divParent).val('');
			})
			.on('keyup',function(ev){
				console.log("-- attachSingleWordCorrectionHandlers: keyup on correction text input");
				if(ev.keyCode == 13) {
					console.log("-- attachSingleWordCorrectionHandlers: ENTER key was just typed");
					var target = $(ev.target);
					var divParent = target.closest('div');
					var newSuggestionValue = target.val().trim();
					if (newSuggestionValue != '') {
						$('span',divParent).css('display','none');
						$('span.selected',divParent).removeClass('selected');
						var newSuggestionElement = $('<span class="suggestion selected">'+newSuggestionValue+'</span>');
						newSuggestionElement.insertBefore($('.original',divParent));
						console.log("-- attachSingleWordCorrectionHandlers: invoking attachAllCorrectionsHandlers");
						spellController.attachAllCorrectionsHandlers();
						console.log("-- attachSingleWordCorrectionHandlers: DONE invoking attachAllCorrectionsHandlers");
						$('.additional input',divParent).val('');
						$('.selected',divParent).css('display','block');
					}
					else {

					}
				} else {
					console.log("-- attachSingleWordCorrectionHandlers: NON-ENTER key was just typed");
				}
				console.log("-- attachSingleWordCorrectionHandlers: EXITING");

			});
	}

	/**
	 * Sets handlers on the picklists that provide suggested corrections for
	 * the misspelled words.
	 */
	attachAllCorrectionsHandlers() {
		var spellController = this;
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
				spellController.attachAllCorrectionsHandlers();
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
						spellController.attachAllCorrectionsHandlers();
						$('.additional input',divParent).val('');
						$('.selected',divParent).css('display','block');
					}
					else {
						
					}
				}
			});
	}

	/**
	 * Make the div where spell checking results are displayed visible
	 */
	makeResultsSectionVisible() {
		var divChecked = this.elementForProp('divChecked');
		var divCheckedResults = divChecked.find('div#div-results');
		var divCheckedTitle = divChecked.find('div#title-and-copy');
		divCheckedTitle.css('display','block');
		divCheckedResults.css('display','block');
		divChecked.show();
	}

	/**
	 * Spell check all the tokens produced by the tokenize web service.
	 * Each token is spell checked one at a time to avoid overloading the
	 * web server.
	 */
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

			// Spell check the tokens
			this.spellCheckRemainingTokens();
		}
	}

	/**
	 * Invokes the spell check web service on all remaining tokens.
	 * Each token is spell checked one at a time to avoid overloading the
	 * web server.
	 */
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
		var spellController = this;

		// Don't spell check next token until we are done with the current one
		var readyForNextToken = function() {
			var ready = false;
			if (spellController.tokenBeingChecked == null) {
				console.log("-- spellCheckRemainingTokens.readyForNextToken: IS ready to check next token");
				ready = true;
			} else {
				console.log("-- spellCheckRemainingTokens.readyForNextToken: NOT ready to check next token");
				ready = false;
			}
			return ready;
		}

		var doRemaining = function() {
			spellController.spellCheckRemainingTokens();
		}
		new RunWhen().conditionMet(readyForNextToken, doRemaining, null, 100);
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

	/**
	 * Display the result of the spell checking web service for a token to be
	 * spell checked.
	 */
	cbkSpellWordSuccess(resp) {
		if (resp.errorMessage != null) {
			this.cbkSpellWordFailure(resp);
		} else {
			var corrections = resp.correction;
			for (var ii=0; ii < corrections.length; ii++) {
				var aCorr = corrections[ii];
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
		// this.attachAllCorrectionsHandlers();
		this.attachSingleWordCorrectionHandlers(appended)
		return;
	}

	cbkSpellWordFailure(resp) {
		if (! resp.hasOwnProperty("errorMessage")) {
			// Error condition comes from tomcat itself, not from our servlet
			resp.errorMessage =
				"Server generated a "+resp.status+" error:\n\n" +
				resp.responseText;
		}
		this.error(resp);
		this.displayToken(this.tokenBeingChecked);
	}

	spanify(text) {
		return '<span>'+text+'</span>';
	}

	/**
	 * Generate html for a picklist that provides suggested corrections for
	 * a misspelled word.
	 */
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

	/**
	 * Generate data for a word spell check service request
	 * @param word: Word to be spell checked
	 * @returns the request data
	 */
	spellWordRequestData(word) {
		var includePartials =
			this.elementForProp("chkIncludePartials").is(':checked')

		var request = {
			text: word,
			includePartiallyCorrect: includePartials
		};

		return JSON.stringify(request);
	}

	/**
	 * Generate data for a tokenize web request.
	 * @returns the data
	 */
	tokenizeRequestData() {
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

	/**
	 * Returns the div where the spell checking results are displayed.
	 * @returns The div
	 */
	divSpellCheckResults() {
		var divChecked = this.elementForProp('divChecked');
		var divCheckedResults = divChecked.find('div#div-results');
		return divCheckedResults;
	}

	/**
	 * Display a spell checked token.
	 * @param token: The token to display.
	 * @param correction: Result of a spell check web request. If null it means
	 *   that the token was not a word and should be displayed as is.
	 */
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
	}

	/**
	 * Run the spell checking web service on a single token.
	 * @param token
	 */
	spellCheckToken(token) {
		console.log("-- spellCheckToken: checking token '"+
			JSON.stringify(token)+"'");
		if (!token.isWord) {
			this.displayToken(token);
			this.tokenBeingChecked = null;
		} else {
			var word = token.text;
			var spellController = this;
			var cbkSuccess = function(resp) {
				spellController.cbkSpellWordSuccess(resp);
				spellController.tokenBeingChecked = null;
			}
			var cbkFailure = function(resp) {
				spellController.cbkSpellWordFailure(resp);
				spellController.tokenBeingChecked = null;
			}
			this.invokeSpellCheckWordService(
				this.spellWordRequestData(word),
				cbkSuccess, cbkFailure);
		}
	}
}

