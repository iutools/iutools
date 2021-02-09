
    Today's date: <%= (new java.util.Date()).toLocaleString()%>
    </p>

    Enter your request:
    <input id="txt-query-words" name="query-words" type="text" value="" />
    <button id="btn-search" type="button" name="btn-search" value="Search">Search</button>
    <p></p>
    <div id="div-message" class="div-message"></div>
    <div id="div-error" class="div-error"></div>
    <div id="div-total-hits" class="div-total-hits"></div>
	<div id="div-search-results"></div>

    <div id="links-to-pages">
      <div id="links">
        <button id="previous-page"
            type="button"
            name="page-number" value="***">Previous</button>&nbsp;
        <div id="page-numbers"></div>&nbsp;
        <button id="next-page"
            type="button"
            name="page-number" value="***">Next</button>
       </div>
      </div>

    <!--</form>-->

    <div id="hits">
        <div id="nb-hits"><span id="nb-hits"></span> result<span id="plural">s</span><hr></div>
        <div id="page-hits"></div>
    </div>

