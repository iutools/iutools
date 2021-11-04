/*
 * Controller for a floating dialog
 */

class FloatingDialogController extends WidgetController {

    constructor(config) {
        var tracer = Debug.getTraceLogger('FloatingDialogController.constructor');
        super(config);
        tracer.trace("upon exit, this="+JSON.stringify(this));
    }

    show() {
        var titlebarID = this.elementForProp("div-titlebar").id;
        var divDialog = this.elementForProp("div-dialog");
        divDialog.draggable(
            {handle: "#"+titlebarID}
        );

        $("#div-wordentry")
            .draggable(
                {
                    handle: ".div-floating-dlg-titlebar",
                    containment: "window"
                })


    }

}