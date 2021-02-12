    <h1 style="text-align:left;">Gist text
    </h1>

    <!--<form id="form-query" method="post" action="">-->

    <!-- Form for submitting text or url to gist -->
    <div>
	<b>Text to gist:</b>
	<p/>


    <textarea id="txt-url-or-text" name="txt-to-check" rows=20 cols=80></textarea>
    <p></p>
    <button id="btn-gisttext">Gist text</button>

    <!-- Areas where to put the results, as well as status and error messages -->
    <div id="div-message" class="div-message"></div>
    <div id="div-error" class="div-error"></div>
    <div id="div-results" class="div-results"></div>
    <div id="div-gist-text-results" class="div-gist-text-results"></div>

    <!-- Floating div where we put the gist of the example word -->
    <div id="div-gist" class="div-gist">
    	<div id="div-gist-message"></div>
    	<div id="div-gist-word"></div>
    	<div id="div-gist-iconizer" title="Minimize"><img src="imgs/minimize.png" ></div>
    	<div id="div-gist-contents"></div>
    </div>
    <div id="div-gist-iconized" title="Maximize"><img src="imgs/maximize.png" height=24 ></div>
