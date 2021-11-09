/** Use this class to manage a div as a floating dialog */

class FloatingDialogController extends WidgetController {
    constructor(config) {
        var tracer = Debug.getTraceLogger("FloatingDialogController.constructor");
        tracer.trace("config="+JSON.stringify(config,undefined,2));
        super(config);
        this.initIcons();
    }

    validateConfig() {
        this.validateProps(
            [
                ["divDialog", "div"],
                ["divTitlebar", "div"],
                ["divMinimize", "div"],
                ["divMaximize", "div"],
            ]
        )
    }

    attachHtmlElements() {
    }

    initIcons() {
        var tracer = Debug.getTraceLogger("FloatingDialogController.initIcons");


        this.divMinimize()
            .append('<img src="'+this.config.minIconURL+
                '" height="'+this.config.iconsHeight+'">');
        this.divMaximize()
            .append('<img src="'+this.config.maxIconURL+
                '" height="'+this.config.iconsHeight+'">');
        this.setEventHandler(
            this.divMinimize(), "click", this.minimize);
        this.setEventHandler(
            this.divMaximize(), "click", this.maximize);
    }

    divDialog() {
        var div = this.elementForProp("divDialog");
        return div;
    }

    divTitlebar() {
        var div = this.elementForProp("divTitlebar");
        return div;
    }

    divMinimize() {
        var div = this.elementForProp("divMinimize");
        return div;
    }

    divMinimizeID() {
        return config.divMinimize;
    }

    divMaximize() {
        var div = this.elementForProp("divMaximize");
        return div;
    }

    divMaximizeID() {
        return config.divMaximize;
    }

    show(minimized) {
        var tracer = Debug.getTraceLogger("FloatingDialogController.show");
        tracer.trace("invoked");
        if (!minimized) {
            maximize();
        } else {
            minimize();
        }
    }

    maximize() {
        var tracer = Debug.getTraceLogger("FloatingDialogController.maximize");
        tracer.trace("invoked");
        this.divDialog().show();
        this.elementForProp("divMinimize").show();
        this.elementForProp("divMaximize").hide();
        this.makeDraggable();
    }

    minimize() {
        var tracer = Debug.getTraceLogger("FloatingDialogController.minimize");
        tracer.trace("invoked");
        this.divDialog().hide();

        // var dlgOffset = this.divDialog().offset();
        // var winHeight = $(window).height();
        // var winWidth = $(window).width();
        // var margTop = winHeight - dlgOffset.top;
        // var margLeft = winWidth - dlgOffset.left;
        // this.divDialog().css({"left": dlgOffset.left, "top": dlgOffset.top}).animate({height: 0, width: 0, marginTop: margTop, marginLeft: margLeft, opacity: 0}, 250);

        this.elementForProp("divMinimize").hide();
        this.elementForProp("divMaximize").show();
    }

    makeDraggable() {

    }
}