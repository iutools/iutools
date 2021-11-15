/** Use this class to manage a div as a floating dialog */

class FloatingWindowController extends WidgetController {
    constructor(config) {
        var tracer = Debug.getTraceLogger("FloatingWindowController.constructor");
        tracer.trace("config=" + jsonStringifySafe(config, undefined, 2));
        super(config);

        this._winbox = null;
        this.width = null;
        this.height = null;
        this.top = null;
        this.right = null;
        this.bottom = null;
        this.left = null;
        this.mounted = null;

        this.hide();
    }

    attachHtmlElements() {
    }

    winbox() {
        var tracer = Debug.getTraceLogger("FloatingWindowController.winbox")
        if (this._winbox == null) {
            var controller = this;
            var onCloseHandler = function (force) {
                controller.rememberSizeAndPosition()
                controller._winbox = null;
                return false;
            }
            if (this.config.hasOwnProperty("mount")) {
                this.mounted =
                    document.getElementById(this.config.mount)
                    .cloneNode(true);
                tracer.trace("mounted.id=" + this.mounted.id);
            }
            var html = null;
            if (this.config.hasOwnProperty('html')) {
                html = this.config.html;
            }
            tracer.trace("mounted=" + this.mounted+", html="+html);
            this._winbox =
                new WinBox("Looking up word...", {
                    title: "Looking up word...",
                    mount: this.mounted,
                    html: html,
                    x: this.x,
                    y: this.y,
                    width: this.width,
                    height: this.height,
                    onclose: onCloseHandler,
                });

            // this._winbox.body.innerHTML = "";
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
        this.makeMountedElementVisible();
        this.winbox().show();
        return this;
    }

    makeMountedElementVisible() {
        console.log("-- makeMountedElementVisible: invoked");
        if (this.mounted != null) {
            this.mounted.style.display = "block";
        }
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
        this.showSpinningWheel()
        return div;
    }

    showSpinningWheel(message) {
        var divID = "#div-choose-correction-message";
        super.showSpinningWheel(divID, message);
    }

    hideSpinningWheel() {
        var divID = "#div-choose-correction-message";
        super.hideSpinningWheel(divID);
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