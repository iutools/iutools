/*
 * Controller for the Word Correction dialog.
 */

class ChooseCorrectionController extends IUToolsController {

    constructor(corrConfig) {
        var tracer = Debug.getTraceLogger('ChooseCorrectionController.constructor');
        tracer.trace("corrConfig=" + jsonStringifySafe(corrConfig));

        var chooseCorrConfig = Object.assign({}, corrConfig);
        chooseCorrConfig.divError =
        super(corrConfig);


        this.windowController = new FloatingWindowController(
            {
                html:
                    "<div class=\"div-floating-dlg\" id=\"div-choose-correction-dlg\" style=\"visibility:visible\">\n" +
                    "  <div class='div-error' id='div-choose-correction-message'></div>\n"+
                    "  <div class=\"div-floating-dlg-contents\">\n" +
                    "    <div id=\"div-choose-correction-message\" class='div-message' style=\"display: none;\"></div>\n" +
                    "    <div id='div-choose-correction-form'>\n"+
                    "      <input id=\"txt-finalized-correction\" type=\"text\" value=\"\" style=\"\">\n" +
                    "      <button id=\"btn-choose-correction-apply\" style=\"\">Apply</button>\n" +
                    "      <button id=\"btn-choose-correction-cancel\" style=\"\">Cancel</button>\n" +
                    "    </div>\n" +
                    "    <div id=\"div-choose-correction-suggestions\"></div>\n" +
                    "  </div>\n" +
                    "  <div id=\"div-choose-correction-error\"></div>\n" +
                    "</div>\n",
                divMessage: 'div-choose-correction-message',
                divError: "div-choose-correction-message",
            }
        )

        this.idOfWordBeingCorrected = null;
        this.busy = false;

        tracer.trace("upon exit, this=" + jsonStringifySafe(this));
    }

    attachHtmlElements() {
        this.setEventHandler("btnChooseCorrection_ApplyCorrection", "click", this.applyCorrection);
        this.setEventHandler("btnChooseCorrection_CancelCorrection", "click", this.cancelCorrection);
    }

    hide() {
        this.windowController.hide();
    }

    show(word) {
        var tracer = Debug.getTraceLogger("ChooseCorrectionController.show");
        tracer.trace("invoked");
        this.clear();
        this.diplayClickedWord(word)
        this.setBusy(true);
        this.windowController.show();
    }

    clear() {
        this.windowController.setTitle("Looking up suggestions...");
        this.txtCorrection().val("");
        this.divSuggestions().html("");
        this.divChooseCorrectionForm().hide();
    }

    displayClickedWord(word) {
        var tracer = Debug.getTraceLogger("ChooseCorrectionController.diplayClickedWord");
        tracer.trace("invoked");
        this.windowController.show();
        this.windowController.setTitle(word)
        this.windowController.showSpinningWheel(
            "Looking up suggestions for: "+word);
        this.hideEditCorrectionForm();
    }

    hideEditCorrectionForm() {
        this.divChooseCorrectionForm().hide();
    }

    showEditCorrectionForm() {
        this.divChooseCorrectionForm().show();
    }

    setBusy(flag) {
        var tracer = Debug.getTraceLogger("ChooseCorrectionController.setBusy");
        tracer.trace("this.config.divMessage="+this.config.divMessage);
        this.busy = flag;
        if (flag) {
            this.windowController.showSpinningWheel("Looking for suggestions");
            this.windowController.this.error("");
        } else {
            this.windowController.hideSpinningWheel();
        }

        return;
    }


    display(checkedWordID) {
        var tracer = Debug.getTraceLogger("ChooseCorrectionController.display");
        tracer.trace("checkedWordID="+checkedWordID);
        this.idOfWordBeingCorrected = checkedWordID
        var word = this.wordBeingCorrected();
        tracer.trace("word="+word);

        this.displayClickedWord(word);

        var controller = this;
        var cbkSuccess = function(resp) {
            // controller.suggestCorrectionsSuccess.call(controller, resp);
            controller.suggestCorrectionsSuccess(resp);
        };
        var cbkFailure = function(resp) {
            // controller.suggestCorrectionsFailure.call(controller, resp);
            controller.suggestCorrectionsFailure(resp);
        };

        var request = {
            text: word,
            suggestCorrections: true,
            includePartiallyCorrect: this.includePartialCorrections(),
        };

        new SpellService().invokeSpellCheckWordService(
            request, cbkSuccess, cbkFailure)
    }

    suggestCorrectionsSuccess(resp) {
        var tracer = Debug.getTraceLogger("ChooseCorrectionController.suggestCorrectionsSuccess");
        tracer.trace("this.idOfWordBeingCorrected="+this.idOfWordBeingCorrected+", resp="+jsonStringifySafe(resp));
        if (resp.errorMessage != null) {
            this.suggestCorrectionsFailure(resp);
        } else {
            var html = this.htmlChooseCorrection(resp);
            if (html != null) {
                // html == null means that:
                //
                // - the user clicked on a word while the server was still
                //   processing
                // - this response is the one for the FIST word that was clicked
                //
                // Therefore we should ignore that response because the user
                // is clearly not interested in the first word anymore
                //
                this.divSuggestions().html(html);
                this.setBusy(false);
                this.divChooseCorrectionForm().show();
            }
            tracer.trace("DONE");
        }
    }

    suggestCorrectionsFailure(resp) {
        var tracer = Debug.getTraceLogger("ChooseCorrectionController.suggestCorrectionsFailure");
        tracer.trace("resp="+jsonStringifySafe(resp));
        if (! resp.hasOwnProperty("errorMessage")) {
            // Error condition comes from tomcat itself, not from our servlet
            resp.errorMessage =
                "Server generated a "+resp.status+" error:\n\n" +
                resp.responseText;
        }
        this.error(resp.errorMessage);
    }

    htmlChooseCorrection(resp) {
        var tracer = Debug.getTraceLogger("ChooseCorrectionController.htmlChooseCorrection");
        tracer.trace("resp="+jsonStringifySafe(resp));
        var html = null;
        var correction = resp.correction;
        var suggestions = correction.allSuggestions;
        if (correction != null && suggestions != null) {
            var word = correction.orig;
            // Note: If word !== this.wordBeingCorrected(), it means that
            //
            // - the user clicked on a word while the server was still
            //   computing the suggestions for a previously clicked word
            //
            // - the server response we are handlign in this method corresponds
            //   FIST word that was clicked
            //
            // Therefore we should ignore that response because the user
            // is clearly not interested in the first word anymore
            //
            var wordBeingCorrected = this.wordBeingCorrected();
            if (word != null && word === wordBeingCorrected) {
                html = "";
                if (suggestions.length == 0) {
                    html += "No suggestions for this word";
                } else {
                    for (var ii = 0; ii < suggestions.length; ii++) {
                        html += this.htmlSuggestion(suggestions[ii], ii);
                    }
                }
            }
        }
        return html;
    }

    htmlSuggestion(suggestion, suggIndex) {
        var eltName = "suggestion-"+suggIndex;
        var html =
            '</u><a class="spell-suggestion" id="suggestion-'+suggIndex+'" onclick="chooseCorrectionController.onClickSuggestion(\''+eltName+'\')">'+suggestion+'</a><br/>\n';
        return html;
    }

    onClickSuggestion(suggEltID) {
        var tracer = Debug.getTraceLogger("ChooseCorrectionController.onClickSuggestion");
        var suggestion = $("#"+suggEltID).text();
        tracer.trace("this="+(typeof this)+": "+jsonStringifySafe(this));
        tracer.trace("suggEltID="+suggEltID+", suggestion="+suggestion+", this.idOfWordBeingCorrected="+this.idOfWordBeingCorrected);
        this.txtCorrection().val(suggestion);
    }

    applyCorrection() {
        var tracer = Debug.getTraceLogger("ChooseCorrectionController.applyCorrection");
        tracer.trace("this="+jsonStringifySafe(this)+", $(this)="+jsonStringifySafe($(this)));
        var correction = this.txtCorrection().val();
        var idOfWordBeingCorrected = this.idOfWordBeingCorrected;
        tracer.trace("correction="+correction+", idOfWordBeingCorrected="+idOfWordBeingCorrected);
        $('.corrected-word').each(
            function(index, wordElt) {
                var wordID = $(this).attr('id');
                tracer.trace("Looking at index="+index+", $(this)="+$(this)+", wordID='"+wordID+"'");
                if (wordID === idOfWordBeingCorrected) {
                    tracer.trace("Changing text of the word");
                    $(this).text(correction);
                } else {

                }
            }
        );
        this.idOfWordBeingCorrected = null;
        this.clear();
        this.hide();
    }

    cancelCorrection() {
        this.idOfWordBeingCorrected = null;
        this.clear();
        this.hide();
    }

    divDialog() {
        return this.elementForProp("divChooseCorrectionDlg");
    }

    divTitle() {
        return this.elementForProp("divChooseCorrectionTitle");
    }

    divChooseCorrectionForm() {
        return $("#div-choose-correction-form");
    }

    divSuggestions() {
        var tracer = Debug.getTraceLogger("ChooseCorrectionController.divSuggestions");
        var div = this.elementForProp("divChooseCorrectionSuggestions");
        tracer.trace("Returning div="+div);
        return div;
    }

    txtCorrection() {
        return this.elementForProp("txtChooseCorrection_FinalCorrection");
    }

    btnApplyCorrection() {
        return this.elementForProp("btnChooseCorrection_ApplyCorrection");
    }

    btnCancelCorrection() {
        return this.elementForProp("btnChooseCorrection_CancelCorrection");
    }

    includePartialCorrections() {
        var included =
            this.elementForProp("chkIncludePartials").is(":checked");
        return included;
    }

    wordBeingCorrected() {
        var word = null;
        var parts = this.parseIDOfWordBeingCorrected();
        if (parts != null) {
            word = parts[0];
        }
        var parts = this.idOfWordBeingCorrected.split("_");
        return word;
    }

    parseIDOfWordBeingCorrected() {
        var parts = null;
        if (this.idOfWordBeingCorrected != null) {
            parts = this.idOfWordBeingCorrected.split("_");
            if (parts.length > 2) {
                var word = parts.slice(0, -1).join("");
                var tokenID = parts.slice(-1);
                parts = [word, tokenID];
            }
        }
        return parts;
    }
}
