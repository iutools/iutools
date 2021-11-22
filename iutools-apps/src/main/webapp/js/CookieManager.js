class CookieManager {

    constructor() {
        this.second = 1000;
        this.minute = 60*this.second;
        this.hour = 60*this.minute;
        this.day = 24*this.hour;
        this.week = 7*this.day;
        this.month = 30*this.day;
        this.year = 365*this.day;
    }

    getCookie(cname) {
        var tracer = Debug.getTraceLogger('CookieManager.getCookie');
        var cvalue = $.cookie(cname);
        tracer.trace("For cname="+cname+", returning cvalue="+cvalue)
        return cvalue;
    }

    setCookie(cname, cvalue, expiresIn) {
        var tracer = Debug.getTraceLogger("CookieManager.setCookie");
        tracer.trace("cname="+cname+", cvalue="+cvalue+", expiresIn="+expiresIn);
        var cookie = cname + "=" + cvalue + ";path=/";
        var exp = this.expiryTime(expiresIn);
        tracer.trace("Setting cookie with exp='"+exp+"'");
        $.cookie(cname, cvalue, { expires: exp });
    }

    expiryTime(expStr) {
        var tracer = Debug.getTraceLogger("CookieManager.expiryTime");
        tracer.trace("expStr="+expStr);

        var numAndUnit = expStr.match(/(\d+)([smhdwMy])/);
        tracer.trace("numAndUnit="+JSON.stringify(numAndUnit));
        var num = parseInt(numAndUnit[1], 10);
        var unit = numAndUnit[2];
        var additional = 0;
        var unitNum = this.second;
        if (unit === 's') {
            unitNum = this.second;
        } else if (unit === 'm') {
            unitNum = this.minute;
        } else if (unit === 'h') {
            unitNum = this.hour;
        } else if (unit === 'd') {
            unitNum = this.day;
        } else if (unit === 'w') {
            unitNum = this.week;
        } else if (unit === 'M') {
            unitNum = this.month;
        } else if (unit === 'y') {
            unitNum = this.year;
        }
        additional = num * unitNum;
        tracer.trace("num="+num+", unit="+unit+", additional="+additional);

        var date = new Date();
        date.setTime(date.getTime() + (additional));

        return date;

    }

    displayCookieConsent() {
        var cookieManager = this;
        window.addEventListener("load", function () {
            const cookieConsent = cookieManager.getCookie('cookieconsent_status');

            // Initialise cookie consent banner
            window.cookieconsent.initialise({
                "palette": {
                    "popup": {
                        "background": "#efefef",
                        "text": "#404040"
                    },
                    "button": {
                        "background": "#8ec760",
                        "text": "#ffffff"
                    }
                },
                "type": "informational",
                "content": {
                    "dismiss": "Got it",
                    "href": "help.jsp?topic=about_cookies"
                },
            })
        });
    }
}