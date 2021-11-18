/*
 * Controller for the Settings page.
 */

class SettingsController extends IUToolsController {
    constructor(sConfig) {
        super(sConfig);
        var tracer = Debug.getTraceLogger('SettingsController.constructor');
        this.settingsDefault = {
            iuAlphabet: "SYLLABICS",
        }
        tracer.trace("upon exit, this=" + jsonStringifySafe(this));
    }

    attachHtmlElements() {
    }

    setting(name, value) {
        if (value != null) {
            // value != null --> SET the value
            new CookieManager().setCookie(name, value);
        } else {
            // value != null --> GET the value
            value = new CookieManager().getCookie(name);
            if (value == null || value === "") {
                value = this.settingsDefault[name];
            }
        }
        return value;
    }

    iuAlphabet(value) {
        return this.setting('iuAlphabet', value);
    }
}
