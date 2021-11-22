class CookieManager {
    getCookie(cname) {
        var b = document.cookie.match('(^|[^;]+)\\s*' + cname + '\\s*=\\s*([^;]+)');
        return b ? b.pop() : '';
    }

    setCookie(cname, cvalue, expireTime) {
        var tracer = Debug.getTraceLogger("CookieManager.setCookie");
        tracer.trace("cname="+cname+", cvalue="+cvalue+", expireTime="+expireTime);
        var cookie = cname + "=" + cvalue + ";path=/";
        if (expireTime != null) {
            cookie += "; "+this.expiryTime(expireTime);
        }
        tracer.trace("setting cookie='"+cookie+"'");
        document.cookie = cookie;
    }

    expiryTime(expStr) {
        var tracer = Debug.getTraceLogger("CookieManager.expiryTime");
        tracer.trace("expStr="+expStr);

        var nowDate = new Date();
        var nowTime = nowDate.getTime();
        var numAndUnit = expStr.match(/(\d+)([smhdm])/);
        tracer.trace("numAndUnit="+JSON.stringify(numAndUnit));
        var num = parseInt(numAndUnit[1], 10);
        var unit = numAndUnit[2];
        var additional = 0;
        if (unit === 's') {
            additional = num;
        } else if (unit === 'h') {
            additional = 60*num;
        } else if (unit === 'h') {
            additional = 60*60*num;
        } else if (unit === 'd') {
            additional = 24*60*60*num;
        } else if (unit === 'w') {
            additional = 7*24*60*60*num;
        } else if (unit === 'm') {
            additional = 30*24*60*60*num;
        } else if (unit === 'y') {
            additional = 365*24*60*60*num;
        }
        var expTime = nowTime + additional;
        var exp = nowDate.setTime(expTime).toUTCString();
        tracer.trace("num="+num+", unit="+unit+", additional="+additional+", nowTime="+nowTime+", expTime="+expTime+", returning exp="+exp);
        return exp;
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