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
                });
            var id = this._winbox.id;
            this._winbox.body.innerHTML = "";
        }
        return this._winbox;
    }

    attachHtmlElements() {
    }

    hide() {
        this.winbox().hide();
        return this;
    }

    show() {
        this.winbox().show();
        return this;
    }

    minimize() {
        this.winbox().minimize();
    }

    maximize() {
        this.winbox().maximize();
    }

    setTitle(title) {
        this.winbox().setTitle(title);
        return this;
    }

    setBody(html) {
        this.winbox().body.innerHTML = html;
        return this;
    }

    divMessage() {
        var div = $("#div-"+this.winboxID()+"-message");
        return div;
    }

    showSpinningWheel(message) {
        if (message == null) message = "Processing request";
        var tracer = Debug.getTraceLogger("FloatingWindowController.showSpinningWheel");

        var body = this.body();
        var html = body.innerHTML;
        message =
            "<div class='div-message' id='"+this.divMessageID()+"'>"+
            "<img src='ajax-loader.gif'>"+
            message+"...</div>";
        tracer.trace("message="+message+", orig html="+html);
        if (html.includes("ajax-loader.gif")) {
            html.replace(/<div class='div-message'.*?<\/div>/, message);
        } else {
            html = message + "\n" + html;
        }
        body.innerHTML = html;
    }

    hideSpinningWheel() {
        this.divMessage().hide();

        return;
    }

    winboxID() {
        var _id = this.winbox().id;
        return _id;
    }

    divMessageID() {
        var id = "div-"+this.winboxID()+"-message";
        return id;
    }

    body() {
        return this.winbox().body;
    }
}