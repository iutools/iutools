<%@ page contentType="text/html;charset=UTF-8" %>
<% String IUTOOLS_JS_VERSION=(new java.util.Date()).toLocaleString(); %>

<!--
For some reason, including a DOCTYPE html tag messes up the floating winbox
windows
-->
<%--<!DOCTYPE html>--%>

<html lang="en">

<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>InuktiTools: ${param.pageTitle}</title>
    <link rel="stylesheet" href="./css/styles.css?v2">
    <link rel="stylesheet" href="./css/design-styles.css">
    <%--    Possibly override default styles with custom ones--%>
    <%if(null != application.getResource("/css/custom-styles.css")){%>
    <link rel="stylesheet" href="./css/custom-styles.css">
    <%}%>

    <link rel="preconnect" href="https://fonts.gstatic.com">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:ital,wght@0,300;0,400;0,500;0,600;1,300;1,400;1,500;1,600&display=swap" rel="stylesheet">
    <script src="./js/vendors/jquery/jquery-3.3.1.min.js"></script>
    <script src="./js/vendors/jquery/jquery-ui.min.js" type="text/javascript"></script>
    <script src="./js/vendors/jquery/jquery.cookie.js" type="text/javascript"></script>
    <link rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">

    <!-- winbox stuff -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/10.7.2/styles/railscasts.min.css">
    <script src="./js/vendors/winbox/winbox.bundle.js"></script>

    <!-- cookieconsent stuff -->
    <link rel="stylesheet" type="text/css"
          href="./js/vendors/cookieconsent/cookieconsent.min.css"/>
    <script src="./js/vendors/cookieconsent/cookieconsent.min.js"
            data-cfasync="false"></script>
</head>

<body>

<%--Use the custom "skin" file if it exists.--%>
<%--Otherwise use the default skin.--%>
<%if(null != application.getResource("/pages/common/_customSkin.jsp")){%>
<jsp:include page="/pages/common/_customSkin.jsp" >
    <jsp:param name="pageTitle" value="${param.pageTitle}" />
    <jsp:param name="pageName" value="${param.pageName}" />
    <jsp:param name="pageUsage" value="${param.pageUsage}" />
</jsp:include>

<%}else{%>
<jsp:include page="/pages/common/_defaultSkin.jsp" >
    <jsp:param name="pageTitle" value="${param.pageTitle}" />
    <jsp:param name="pageName" value="${param.pageName}" />
    <jsp:param name="pageUsage" value="${param.pageUsage}" />
</jsp:include>
<%}%>

<!-- Cookie consent stuff -->
<script src="./js/CookieManager.js"></script>
<script>
    new CookieManager().displayCookieConsent();
</script>

<!-- To ensure that the browser remembers the old breakpoints after page
reloading -->
<script>debugger;</script>

<!--
   Load scripts that are common to all page types
-->
<script src="./js/navscript.js"></script>
<script src="./js/vendors/log4javascript.js"></script>
<script src="./js/utils/jsonStringifySafe.js?version=<%= IUTOOLS_JS_VERSION %>"></script>
<script src="./js/debug/Debug.js?version=<%= IUTOOLS_JS_VERSION %>"></script>
<script src="./js/debug/DebugConfig.js?version=<%= IUTOOLS_JS_VERSION %>"></script>
<script src="./js/utils/PlatformDetector.js?version=<%= IUTOOLS_JS_VERSION %>"></script>
<script src="./js/utils/CSSUtils.js?version=<%= IUTOOLS_JS_VERSION %>"></script>
<script src="./js/controllers/RunWhen.js?version=<%= IUTOOLS_JS_VERSION %>"></script>
<script src="./js/controllers/WidgetController.js?version=<%= IUTOOLS_JS_VERSION %>"></script>
<script src="./js/controllers/FloatingWindowController.js?version=<%= IUTOOLS_JS_VERSION %>"></script>

<script src="./js/iutools_config.js?version=<%= IUTOOLS_JS_VERSION %>"></script>

<!-- This one must be before any other controller -->
<script src="./js/controllers/IUToolsController.js?version=<%= IUTOOLS_JS_VERSION %>"></script>

<script src="./js/controllers/MobileWarningController.js?version=<%= IUTOOLS_JS_VERSION %>"></script>
<script src="./js/controllers/settings/SettingsController.js"></script>
<script src="./js/controllers/navigation/MainNavController.js"></script>
<script src="./js/controllers/worddict/WordEntryData.js?version=<%= IUTOOLS_JS_VERSION %>"></script>
<script src="./js/controllers/worddict/WordEntryController.js?version=<%= IUTOOLS_JS_VERSION %>"></script>

<script>
    var mainNavController = new MainNavController();
    new MobileWarningController().possiblyWarnAgainstMobile();
</script>

<!-- Include the code that creates and configures the controller for this page -->

<!-- DISABLE THIS BIT UNTIL THE PageTemplateControler IS FULLY IMPLEMENTED
<script src="../../js/controllers/common/MainContainerController.js?version=<%= IUTOOLS_JS_VERSION %>></script>
<script>
    var pageTemplateController =
        new PageTemplateControlLer({"linkFeedback", "feedback_link"});
</script>
-->

<jsp:include page="/pages/${param.pageName}/_controller.jsp" />

</body>

</html>