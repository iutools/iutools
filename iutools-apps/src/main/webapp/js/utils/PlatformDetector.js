/* Use this class to detect the OS and browser */

class PlatformDetector {
    isMobile() {
        var tracer = Debug.getTraceLogger("PlatformDetector.isMobile");
        tracer.trace("navigator.userAgent="+navigator.userAgent);
        var mobile = false;
        var regexp = /(Android|webOS|iPhone|iPad|iPod|BlackBerry|Windows Phone)/i;
        if (navigator.userAgent.search(regexp) >= 0) {
            mobile = true;
        }
        tracer.trace("Returning mobile="+mobile);
        return mobile;
    }

}