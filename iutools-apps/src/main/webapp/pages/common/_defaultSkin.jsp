<%--<h1>START: /pages/common/_defaultSkin.jsp</h1>--%>

<%--<b>/pages/common/_defaultSkin.jsp</b> Received:--%>
<%--<ul>--%>
<%--    <li>param.pageName='${param.pageName}'</li>--%>
<%--    <li>request.getParameter("pageName")=<%= request.getParameter("pageName") %>--%>
<%--    <li>param.pageTitle='${param.pageTitle}'</li>--%>
<%--    <li>request.getParameter("pageTitle")=<%= request.getParameter("pageTitle") %>--%>
<%--    </li>--%>
<%--</ul>--%>

<!-- START: "Skin" of the page -->
<!-- Modify this file to change the IUToools skin -->

<div id="header" class="header">
  <div id="header_inner">
		<a id="header_title" href="index.html">
			<span>INUKTITOOLS:</span> <span>APPS</span> <span>FOR</span> <span>THE</span> <span>INUKTITUT</span> <span>LANGUAGE</span>
		</a>
	</div>
</div>

<nav id="main_nav">
  <button class="menu-toggle"><span></span><span></span><span></span></button>
  <ul id="main_nav_menu">
    <li id="home_link"><a href="index.html">Home</a></li>
    <li id="feedback_link"><a target="#iutools_feeback" href="mailto:alaindesilets0@gmail.com;contact@inuktitutcomputing.ca?subject=Inuktitut Tools Feedback">Send Feedback</a></li>
    <li id="other_tools">
      <button id="mnu-other-tools" class="drop-menu-toggle">Other Inuktut Tools</button>
      <ul class="drop-menu">
        <li><a href="worddict.jsp">Inuktitut-English Dictionary</a></li>
        <li><a href="spell.jsp">Spell Checker</a></li>
        <li><a href="search.jsp">Web Search Engine</a></li>
        <li><a href="morpheme_dictionary.jsp">Morpheme Dictionary</a></li>
        <li><a href="gisttext.jsp">Reading Assistant</a></li>
        <li><a href="http://inuktitutcomputing.ca/Transcoder/index.php">Inuktitut Computing Transcoder</a></li>
      </ul>
    </li>
    <li id="alphabet">
        <button id="mnu-alphabet" class="drop-menu-toggle">SYLLABIC</button>
        <ul class="drop-menu">
            <li><a onclick="mainNavController.selectAlphabet('ROMAN')">Roman</a></li>
            <li><a onclick="mainNavController.selectAlphabet('SYLLABIC')">Syllabic</a></li>
        </ul>
    </li>
  </ul>
</nav>

<div id="page_title">
<h1>${param.pageTitle}</h1>
<p><em>${param.pageUsage}</em></p>
</div>

<!--
   Setup the "main" part for this type of page
-->
<main>
<jsp:include page="/pages/${param.pageName}/_view.jsp" />
</main>

<div id="footer" class="footer">
  In collaboration with:
</div>
<div id="sponsors">
  <a href="https://nrc.canada.ca/en" target="_blank"><img src="imgs/NRC-ID_138x138.jpg" alt="logo of National Research Council Canada - Conseil national de recherches Canada"></a>
  <a href="https://www.pirurvik.ca/" target="_blank"><img src="imgs/Pirurvik_logo_2.jpg" alt="logo of pirurvik"></a>
</div>

<!-- END: "Skin" of the page -->
