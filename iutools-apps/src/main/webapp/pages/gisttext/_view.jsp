    <!-- Form for submitting text or url to gist -->
    <div>
	<b>Text to gist:</b>
	<p/>


    <textarea id="txt-url-or-text" name="txt-to-check" rows=20 cols=80></textarea>
    <p></p>
    <button id="btn-gisttext">Gist text</button>

    <!-- Areas where to put the results, as well as status and error messages -->
    <div id="div-message" class="div-message"></div>
    <p/>
    <div id="div-error" class="div-error"></div>
    <div id="div-results" class="div-results"></div>
    <div id="div-wordentry-text-results" class="div-wordentry-text-results"></div>

    <!-- Floating div where we put the dictionary entry for an example word -->
    <div id="div-wordentry" class="div-wordentry">
    	<div id="div-wordentry-message"></div>
    	<div id="div-wordentry-word"></div>
    	<div id="div-wordentry-iconizer" title="Minimize"><img src="imgs/minimize.png" ></div>
    	<div id="div-wordentry-contents"></div>
    </div>
    <div id="div-wordentry-iconized" title="Maximize"><img src="imgs/maximize.png" height=24 ></div>
