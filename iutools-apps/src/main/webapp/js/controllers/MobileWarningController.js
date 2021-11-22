/* This controller issues a warning if the site is accessed through a
   mobile device.

   The warning is issued only once every N days
 */

class MobileWarningController extends IUToolsController {
    possiblyWarnAgainstMobile() {
        var tracer = Debug.getTraceLogger("MobileWarningController.possiblyWarnAgainstMobile");
        if (new PlatformDetector().isMobile()) {
            tracer.trace("is mobile!");
            var cookieManager = new CookieManager();
            tracer.trace("after CookieManager");
            var cookieName = 'mobile_warning_issued';
            tracer.trace("before getCookie");
            var alreadyIssued = cookieManager.getCookie(cookieName);
            var blah = cookieManager.getCookie("blah");
            tracer.trace("alreadyIssued='"+alreadyIssued+"'"+", blah'"+blah+"'");
            if (alreadyIssued == null || alreadyIssued === "") {
                alert(
                    "Warning: You seem to be accessing InuktiTools through a mobile device.\n\n"+
                    "The apps currently do not run well on such devices. "+
                    "If you experience problems we recommend that you access the site through a laptop or desktop computer.\n\n"+
                    "We apologize for the inconvenience and hope to fix these problems soon.");
                cookieManager.setCookie(cookieName, true, '1s')
            }

        }
    }
}