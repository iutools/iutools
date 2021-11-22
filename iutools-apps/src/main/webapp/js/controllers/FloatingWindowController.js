/** Use this class to manage a div as a floating dialog */

class FloatingWindowController extends WidgetController {
    constructor(config) {
        var tracer = Debug.getTraceLogger("FloatingWindowController.constructor");
        tracer.trace("config=" + jsonStringifySafe(config, undefined, 2));
        super(config);

        this._winbox = null;
        this.x = 50;
        this.y = 50;
        this.width = null;
        this.height = null;
        this.prevViewportWidth = null;
        this.prevViewportHeight = null;
        this.top = null;
        this.right = null;
        this.bottom = null;
        this.left = null;
        this.mounted = null;

        this.hidden = false;

        this.hide();
    }

    attachHtmlElements() {
    }

    winbox() {
        var tracer = Debug.getTraceLogger("FloatingWindowController.winbox")
        if (this._winbox == null) {
            tracer.trace("generating a new winbox window")
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

            var dimensions = this.responsiveDimensions();

            this._winbox =
                new WinBox("Looking up word...", {
                    title: "Looking up word...",
                    mount: this.mounted,
                    html: html,
                    x: this.x,
                    y: this.y,
                    width: dimensions.width,
                    height: dimensions.height,
                    onclose: onCloseHandler,
                });

            if (this.config.hasOwnProperty('onNewWindow')) {
                this.config.onNewWindow();
            }


            // Delete the winbox maximize buttons because they behave in a
            // non-standard way. Basically, when you maximize a window, it
            // takes on the whole size of the browser instead of restoring
            // itself to the dimensions it was at before minimisation.
            //
            tracer.trace("Removing the maximize buttons");
            this.deleteMaximizeButtons();

            if (this.hidden) {
                this._winbox.hide();
            }
        }
        return this._winbox;
    }

    responsiveDimensions() {
        var tracer = Debug.getTraceLogger("FloatingWindowController.responsiveDimensions");
        var width = this.width;
        var height = this.height;
        var x = 100;
        var y = 100;

        var viewportWidth = window.innerWidth;
        var viewportHeight = window.innerHeight;
        tracer.trace(
            "Previous viewportWidth="+this.prevViewportWidth+", viewportHeight="+this.prevViewportHeight+"\n"+
            "Actual viewportWidth="+viewportWidth+", viewportHeight="+viewportHeight
        );


        if (width == null || viewportWidth != this.prevViewportWidth) {
            // Set the window width if it has not been set yet, or
            // the viewport width has changed
            tracer.trace("Reassessing width");
            if (viewportWidth <= 600) {
                width = "350";
                tracer.trace("width: < 600");
            } else if (viewportWidth < 800) {
                width = 400;
                tracer.trace("width:[600,800]");
            } else if (viewportWidth < 1000) {
                width = 500;
                tracer.trace("width:[800,1000]");
            } else {
                width = 800;
                tracer.trace("width: > 1000");
            }
        } else {
            tracer.trace("Keeping previous width:"+this.width);
        }

        if (height == null || viewportHeight != this.prevViewportHeight) {
            // Set the window height if it has not been set yet, or
            // the viewport height has changed
            tracer.trace("Reassessing height");
            if (viewportHeight <= 600) {
                tracer.trace("height < 600");
                height = "350";
            } else if (viewportHeight < 800) {
                tracer.trace("height < 800");
                height = 600;
            } else if (viewportHeight < 1000) {
                tracer.trace("height < 1000");
                height = 800;
            } else {
                tracer.trace("height > 1000");
                height = 800;
            }
        } else {
            tracer.trace("Keeping previous height:"+this.height);
        }

        this.prevViewportHeight = viewportHeight;
        this.prevViewportWidth = viewportWidth

        var dimensions = {width: width, height: height};

        tracer.trace("Returning dimensions="+JSON.stringify(dimensions));

        return dimensions;
    }

    deleteMaximizeButtons() {
        var tracer = Debug.getTraceLogger("FloatingWindowController.deleteMaximizeButtons")
        var maxButtons = $(".wb-max");
        tracer.trace("Initial # of maximize buttons: "+maxButtons.length);
        for (var ii=0; ii < maxButtons.length; ii++) {
            maxButtons.get(ii).remove();
        }
        tracer.trace("After deletion, # of maximize buttons: "+$(".wb-max").length);
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
        this.hidden = true;
        if (this._winbox != null) {
            this._winbox.hide();
        }
        return this;
    }

    show() {
        var tracer = Debug.getTraceLogger("FloatingWindowController.show");
        this.hidden = false;
        if (this._winbox != null) {
            this.makeMountedElementVisible();
            this._winbox.show();
        }
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