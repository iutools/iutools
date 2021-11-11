/** Use this class to manage a div as a floating dialog */

class FloatingWindowController extends WidgetController {
    constructor(config) {
        var tracer = Debug.getTraceLogger("FloatingWindowController.constructor");
        tracer.trace("config="+jsonStringifySafe(config,undefined,2));
        super(config);

        this._winbox = null;


        this.hide();
    }

    winbox() {
        if (this._winbox == null) {
            this._winbox =
                new WinBox("Looking up word...", {
                    title: "Looking up word...",
                    html: "",
                });
        }
        return this._winbox;
    }

    attachHtmlElements() {
    }

    hide() {
        this.winbox().hide();
    }

    show() {
        this.winbox().show();
    }

    setTitle(title) {
        this.winbox().setTitle(title);
    }
}