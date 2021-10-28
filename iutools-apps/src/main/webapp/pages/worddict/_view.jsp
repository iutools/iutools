
    <div>
    <input id="txt-word-query" name="txt-word-query" style="width:20em;" placeholder="word or portion of word">
    <br/>
    Search in:
    <select id="sel-language">
   		<option value="iu" selected="">Inuktitut</option>
   		<option value="en">English</option>
   	</select>
    </div>


    <button id="btn-search-word" type="button" name="btn-search-word">Search</button>

    <!-- Areas where to put the results, as well as status and error messages -->
    <div id="div-message" class="div-message"></div>
    <p/>
    <div id="div-error" class="div-error"></div>
    <div id="div-results" class="div-results"></div>

    <!-- Floating div where we put the dictionary entry for a word -->
    <div class="div-floating-dlg" id="div-wordentry">
        <div id="div-wordentry-message"></div>
    	<div class="div-floating-dlg-titlebar" id="div-wordentry-word"></div>
        <div id="div-wordentry-iconizer" title="Minimize"><img src="imgs/minimize.png" ></div>
        <div class="div-floating-dlg-contents" id="div-wordentry-contents"></div>
    </div>
    <div id="div-wordentry-iconized" title="Maximize"><img src="imgs/maximize.png" height=24 ></div>
