/*
 * Controller for the Word Correction dialog.
 */

class ChooseCorrectionController extends IUToolsController {

    constructor(corrConfig) {
        var tracer = Debug.getTraceLogger('CorrectWordController.constructor');
        tracer.trace("corrConfig=" + JSON.stringify(corrConfig));
        super(corrConfig);

        this.elementForProp("divChooseCorrectionDlg").draggable();
        tracer.trace("upon exit, this=" + JSON.stringify(this));
    }

    display(word, tokenID) {
        var tracer = Debug.getTraceLogger("ChooseCorrectionController.display");
        tracer.trace("word="+word+", tokenID="+tokenID);
        var html = this.htmlChooseCorrection(word, tokenID);
        this.divDialog().html(html);
        this.show();
    }

    htmlChooseCorrection(word, tokenID, suggestions) {
        if (typeof suggestions === 'undefined') {
            suggestions = [];
        }
        var html = "<h2>Correct word: <em>"+word+"</em></h2>\n\n";

        html += "<h3>Suggested spellings</h3>";
        if (suggestions.length == 0) {
            html += "No suggestions for this word";
        } else {
            for (var ii = 0; ii < suggestions.length; ii++) {
                html += suggestions[ii]+"<br/>\n";
            }
        }

        return html;
    }

    divDialog() {
        return this.elementForProp("divChooseCorrectionDlg");
    }

    show() {
        this.divDialog().show();
    }
}
