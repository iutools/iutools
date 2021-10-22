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
        alert("SpellController.openSuggestionsDialog: word="+word+", tokenID="+tokenID);
    }
}
