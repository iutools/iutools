/*
 * Controller for the spell.html page.
 */

class SpellController extends IUToolsController {

	constructor(config) {
		super(config);
		// List of tokens that remain to be spell checked.
		// If null, it means we aren't in the process of checking tokens.
		this.tokensRemaining = null;

		// Token currently being spell checked. If null, means we are
		// not currently processing a token.
		this.tokenBeingChecked = null;

		// Set to false if we want to abort the current spell checking
		// task.
		this.abortCheck = false;

		this.MAX_WORDS = null;

        this.correctWordController = new ChooseCorrectionController(config);
    }

	/**
	 * Setup handler methods for different HTML elements specified in the config.
	 */
	attachHtmlElements() {
		this.setEventHandler("btnSpell", "click", this.spellCheck);
		this.setEventHandler("btnCancelSpell", "click", this.abortSpellCheck);
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
		var tracer = Debug.getTraceLogger("SpellController.getSpellCheckedText")
		var wholeTextElements = $('div#div-results').contents();
		var allText = '';
		var prevEltWasCorrection = false;
		wholeTextElements.each(function (index, item) {
			tracer.trace("item.textContent='"+item.textContent+"'");
            tracer.trace("prevEltWasCorrection="+prevEltWasCorrection);
			var text = "";
			var curr_elt_is_correction = false
			if ($(item).is('.corrections')) {
				tracer.trace("Item IS a correction");
				text = $(item).find('.selected').text();
                curr_elt_is_correction = true;
			} else  {
				tracer.trace("Item is NOT a correction");
				text = $(item).text();
			}


			if (prevEltWasCorrection && text === "\n") {
				// For some reason, we get a "\n" string after
				// each correction element. Skip those
				tracer.trace("Item is a spurious newline that follows a correction div. Skipping it.");
			} else {
                tracer.trace("Appending text for this item:'" + text + "'");
                allText += text;
                tracer.trace("allText='" + allText + "'");

                prevEltWasCorrection = curr_elt_is_correction
            }

		});
		return allText;
	}

	/**
	 * Spell check the text entered by the user.
	 */
	spellCheck() {
		var tracer = Debug.getTraceLogger("SpellController.spellCheck")
		var isValid = this.validateInputs();
		if (isValid) {
			this.clearResults();
			this.setBusy(true);
			this.tokenizeThenSpellCheck();
		}
	}

	abortSpellCheck() {
		this.abortCheck = true;
	}

	/**
	 * Tokenize the text provided by the user, then spell check
	 * each token in turn, displaying result for each token as
	 * soon as it becomes available.
	 */
	tokenizeThenSpellCheck() {
		var data = this.tokenizeRequestData();
        this.userActionStart("SPELL", 'srv2/tokenize', data,
            this.cbkTokenizeSuccess, this.cbkTokenizeFailure);
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
			url: 'srv2/tokenize',
			data: jsonRequestData,
			dataType: 'json',
			async: true,
			success: fctSuccess,
			error: fctFailure
		});

		return;
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
			url: 'srv2/spell',
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
				var target = $(ev.target);
				var divParent = target.closest('div');
				$('span',divParent).css('display','none');
				$('span.selected',divParent).removeClass('selected');
				target.addClass('selected');
				$('.additional input',divParent).val('');
				$('.selected',divParent).css('display','block');
				spellController.attachSingleWordCorrectionHandlers(divParent);
				return;
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
				if(ev.keyCode == 13) {
					var target = $(ev.target);
					var divParent = target.closest('div');
					var newSuggestionValue = target.val().trim();
					if (newSuggestionValue != '') {
						$('span',divParent).css('display','none');
						$('span.selected',divParent).removeClass('selected');
						var newSuggestionElement = $('<span class="suggestion selected">'+newSuggestionValue+'</span>');
						newSuggestionElement.insertBefore($('.original',divParent));
						spellController.attachSingleWordCorrectionHandlers(divParent);
						$('.additional input',divParent).val('');
						$('.selected',divParent).css('display','block');
					}
					else {

					}
				} else {
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
		var tracer = Debug.getTraceLogger("SpellChecker.cbkTokenizeSuccess");
		tracer.trace("resp="+JSON.stringify(resp));
		if (resp.errorMessage != null) {
			this.cbkTokenizeFailure(resp);
		} else {
			// Retrieve the tokens from the tokenize response
			var respTokens = resp['tokens'];
			tracer.trace("respTokens="+JSON.stringify(respTokens));
			this.tokensRemaining = [];
			for (var ii=0; ii < respTokens.length; ii++) {
				var respToken = respTokens[ii];
				var text = respToken.text;
				var isWord = respToken.isWord;
                this.tokensRemaining.push({text: text, isWord: isWord})
			}

			// Spell check the tokens
			this.spellCheckRemainingTokens(resp.taskID);
		}
	}

	/**
	 * Invokes the spell check web service on all remaining tokens.
	 * Each token is spell checked one at a time to avoid overloading the
	 * web server.
	 */
	spellCheckRemainingTokens(taskID) {
		var tracer = Debug.getTraceLogger("SpellController.spellCheckRemainingTokens")
		tracer.trace("taskID="+taskID+", this.tokensRemaining="+JSON.stringify(this.tokensRemaining));
		if (this.abortCheck) {
			this.clearRemainingWords();
		}

		if (this.tokensRemaining == null ||
			this.tokensRemaining.length == 0) {
			this.setBusy(false);
			var data = {
                'taskID': taskID,
                '_taskID': taskID,
            }
			this.userActionEnd("SPELL",data)

            return;
		}

		this.tokenBeingChecked = this.tokensRemaining.shift();
		tracer.trace("this.tokenBeingChecked="+JSON.stringify(this.tokenBeingChecked));
		this.checkTokenCorrectness(this.tokenBeingChecked, taskID);
		var spellController = this;

		// Don't spell check next token until we are done with the current one
		var readyForNextToken = function() {
			var ready = false;
			if (spellController.tokenBeingChecked == null) {
				ready = true;
			} else {
				ready = false;
			}
			return ready;
		}

		var doRemaining = function() {
            var dTracer = Debug.getTraceLogger("SpellController.spellCheckRemainingTokens.doRemaining")
            dTracer.trace("taskID="+taskID);
            spellController.spellCheckRemainingTokens(taskID);
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
		this.error(resp.errorMessage);
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
	cbkCheckWordCorrectnessSucces(resp) {
		if (resp.errorMessage != null) {
			this.cbkWordCorrectnessFailure(resp);
		} else {
			var corrections = resp.correction;
			for (var ii=0; ii < corrections.length; ii++) {
				var aCorr = corrections[ii];
				var htmlWord = this.htmlCheckedWord(aCorr);
                var divSpellCheckedWords = this.divSpellCheckResults();
                divSpellCheckedWords.append(htmlWord);
				// if (!aCorr.wasMispelled) {
				// 	var appended = divSpellCheckedWords.append(aCorr.orig);
				// } else {
				// 	this.appendSuggestionsPicklist(aCorr)
				// }
			}
		}
	}

	appendSuggestionsPicklist(correction) {
		var divSpellCheckedWords = this.divSpellCheckResults();
		var html = this.picklistFor(correction);
		var appended = divSpellCheckedWords.append(html);
		appended.css("display", "block")

		this.attachSingleWordCorrectionHandlers(appended)
		return;
	}

	cbkWordCorrectnessFailure(resp) {
		var tracer = Debug.getTraceLogger("SpellController.cbkSpellWordFailure")
		tracer.trace("resp="+JSON.stringify(resp));
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
		var origWord = corrResult.orig;
		var alternatives = corrResult.allSuggestions;
		alternatives.unshift(origWord);
		var picklistHtml = "<div class='corrections' word='"+corrResult.orig+"'>\n";
		picklistHtml += "<span class=\"suggestion original selected\">"+origWord+"</span>\n";
		var inputLength = origWord.length;
		for (var ii=0; ii < alternatives.length; ii++) {
			var anAlternative = alternatives[ii];
			picklistHtml += "<span class=\"suggestion";
			anAlternative = anAlternative.replaceAll("[", '<b>(');
			anAlternative = anAlternative.replaceAll("]", ')</b>');
			picklistHtml += "\">"+anAlternative+"</span>\n";
			if (anAlternative.length > inputLength)
				inputLength = anAlternative.length;
		}
		picklistHtml += "<span class=\"additional\">"+"<input type=\"text\" />"+"</span>\n";
		picklistHtml += "</div>\n";
		return picklistHtml;
	}
	
	setBusy(flag) {
		this.busy = flag;
		if (flag) {
			this.disableSpellButton();
			this.enableCancelButton();
			this.showSpinningWheel('divMessage', "Checking");
			this.error("");
			this.elementForProp('btnCopy').hide();
		} else {
			this.enableSpellButton();
			this.disableCancelButton();
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
	spellWordRequestData(word, taskID, suggestCorrections) {
        if (typeof suggestCorrections === 'undefined') {
            suggestCorrections = false;
        }

		var includePartials =
			this.elementForProp("chkIncludePartials").is(':checked')

		var request = {
			text: word,
            '_taskID': taskID,
			includePartiallyCorrect: includePartials,
            suggestCorrections: suggestCorrections
		};

		return JSON.stringify(request);
	}

	/**
	 * Generate data for a tokenize web request.
	 * @returns the data
	 */
	tokenizeRequestData() {
		var request = {
			text: this.elementForProp("txtToCheck").val(),
            maxWords: this.MAX_WORDS,
		};

		return JSON.stringify(request);
	}
	
	disableSpellButton() {
		this.elementForProp('btnSpell').attr("disabled", true);
	}
	
	enableSpellButton() {
		this.elementForProp('btnSpell').attr("disabled", false);
	}

	disableCancelButton() {
		var button = this.elementForProp('btnCancelSpell');
		button.attr("disabled", true);
		button.css('display', 'none')
	}

	enableCancelButton() {
		var button = this.elementForProp('btnCancelSpell');
		button.attr("disabled", false);
		button.css('display', 'block')
	}

	displayError(errMess) {
		if (errMess != null && !(errMess === "")) {
			errMess = "SpellController raised an error:\n" + errMess;
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
		var divCheckedResults = this.divSpellCheckResults();
		var html;
		var word = token.text;
		if (!token.isWord || correction == null || !correction.wasMisspelled) {
			// This token is either punctuation or a word that was correctly
			// spelled.
			html = word;
			html = html.replace(/\n/g, "<br/>\n");
			html = html.replace(/ /g, "&nbsp;");
		} else {
			html = this.picklistFor(correction);
		}
		divCheckedResults.append(html);
	}

	/**
	 * Run the spell checking web service on a single token.
	 * @param token
	 */
	checkTokenCorrectness(token, taskID) {
		var tracer = Debug.getTraceLogger("SpellController.spellCheckToken")
		tracer.trace("token="+JSON.stringify(token));
		if (!token.isWord) {
			this.displayToken(token);
			this.tokenBeingChecked = null;
		} else {
			var word = token.text;
			var spellController = this;
			var cbkSuccess = function (resp) {
				spellController.cbkCheckWordCorrectnessSucces(resp);
				spellController.tokenBeingChecked = null;
			}
			var cbkFailure = function (resp) {
				spellController.cbkWordCorrectnessFailure(resp);
				spellController.tokenBeingChecked = null;
			}
			var suggestCorrections = false;
			this.invokeSpellCheckWordService(
				this.spellWordRequestData(word, taskID),
				cbkSuccess, cbkFailure, suggestCorrections);
		}
	}

	clearRemainingWords() {
		this.tokensRemaining = null;
		this.tokenBeingChecked;
		this.abortCheck = false;
	}

    htmlCheckedWord(checkedWord) {
	    var tracer = Debug.getTraceLogger("SpellController.htmlCheckedWord");
	    tracer.trace("checkedWord="+JSON.stringify(checkedWord))
        var origWord = checkedWord.orig;
	    var html = origWord;
	    var origWordEscaped = origWord.replace("'", "\'");

        if (checkedWord.wasMispelled) {
            var tokenID = this.tokensRemaining.length;
        	html =
                // '<a name="corrected-word" class="corrected-word" onclick="alert(\'hi\')">' +
                '<a class="corrected-word" name="corrected-word-'+tokenID+'"'+
                ' onclick="spellController.openSuggestionsDialog(\''+origWordEscaped+'\', '+tokenID+')">'+
                html +
                '</a>';
        }

        tracer.trace("returnin html="+html);
        return html;
    }

    openSuggestionsDialog(word, tokenID) {
        // alert("SpellController.openSuggestionsDialog: word="+word+", tokenID="+tokenID);
	    this.correctWordController.display(word, tokenID);
    }
}

