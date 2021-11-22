/*
 * Controller for the Settings page.
 */

class SettingsController extends IUToolsController {
    constructor(sConfig) {
        super(sConfig);
        var tracer = Debug.getTraceLogger('SettingsController.constructor');
        this.settingsDefault = {
            iuAlphabet: "SYLLABIC",
        }
        tracer.trace("upon exit, this=" + jsonStringifySafe(this));
    }

    attachHtmlElements() {
    }

    setting(name, value) {
        var tracer = Debug.getTraceLogger("SettingsController.setting");
        tracer.trace("name="+name+", value="+value);
        if (value != null) {
            // value != null --> SET the value
            tracer.trace("SETTING the cookie");
            new CookieManager().setCookie(name, value);
        } else {
            // value != null --> GET the value
            tracer.trace("GETTING the cookie");
            value = new CookieManager().getCookie(name);
            if (value == null || value === "") {
                tracer.trace("Cookie not set. Getting value from defaults")
                value = this.settingsDefault[name];
            }
        }
        tracer.trace("Returning value="+value)
        return value;
    }

    iuAlphabet(value) {
        var tracer = Debug.getTraceLogger("SettingsCotroller.iuAlphabet");
        var alphabet = this.setting('iuAlphabet', value);
        return alphabet;
    }
}
