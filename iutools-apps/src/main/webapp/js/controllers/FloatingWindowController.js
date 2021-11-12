/** Use this class to manage a div as a floating dialog */

class FloatingWindowController extends WidgetController {
    constructor(config) {
        var tracer = Debug.getTraceLogger("FloatingWindowController.constructor");
        tracer.trace("config="+jsonStringifySafe(config,undefined,2));
        super(config);

        this._winbox = null;
        this.width = null;
        this.height = null;
        this.top = null;
        this.right = null;
        this.bottom = null;
        this.left = null;

        this.hide();
    }

    attachHtmlElements() {
    }

    winbox() {
        if (this._winbox == null) {
            var controller = this;
            var onCloseHandler = function (force) {
                controller.rememberSizeAndPosition()
                controller._winbox = null;
                return false;
            }
            if (this.x == null) {
                this._winbox =
                    new WinBox("Looking up word...", {
                        title: "Looking up word...",
                        onclose: onCloseHandler,
                    });
            } else {
                this._winbox =
                    new WinBox("Looking up word...", {
                        title: "Looking up word...",
                        x: this.x,
                        y: this.y,
                        width: this.width,
                        height: this.height,
                        onclose: onCloseHandler,
                    });
            }
            this._winbox.body.innerHTML = "";
        }
        return this._winbox;
    }

    rememberSizeAndPosition() {
        if (this._winbox != null) {
            this.x = this._winbox.x;
            this.y = this._winbox.y;
            this.width = this._winbox.width;
            this.height = this._winbox.height;
        }
    }


    hide() {
        this.winbox().hide();
        return this;
    }

    show() {
        var tracer = Debug.getTraceLogger("FloatingWindowController.show");
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
        var tracer = Debug.getTraceLogger("FloatingWindowController.setTitle");
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