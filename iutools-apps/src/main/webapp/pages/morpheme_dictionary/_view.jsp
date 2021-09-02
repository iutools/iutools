	<b>In corpus: </b>
    <select id="corpus">
   		<option value="Hansard-1999-2002" selected>Nunavut Hansard 1999-2002</option>
   		<option value="Hansard-1999-2018">Nunavut Hansard 1999-2018</option>
   		<option value="CBC">CBC transcripts</option>
   	</select>

    <!-- Form for searching for examples -->
    <form id="form-query" method="post" action="">
    <div>
    Max examples:
    <input id="nb-examples" name="nb-examples" value="20" style="width:5em;">
    </div><br>
    <div>
    Morpheme (or start of it):
    <input id="morpheme" name="morpheme" style="width:20em;" placeholder="ex: gaq">
    </div>
    <button id="btn-occ" type="button" name="btn-occ" value="Occurrences">Search</button>
    <input type="hidden" id="example-word" name="example-word" value="">
    <input type="hidden" id="corpus-name" name="corpus-name" value="">
    <p></p>

    <!-- Areas where we put the results as well as status and error messages -->
    <div id="div-message" class="div-message"></div>
    <div id="div-error" class="div-error"></div>
    <div id="div-results" class="div-results"></div>

    <!-- Floating div where we put the gist of the example word -->
    <div id="div-gist" class="div-gist">
    	<div id="div-wordentry-message"></div>
    	<div id="div-wordentry-word"></div>
    	<div id="div-gist-iconizer" title="Minimize"><img src="imgs/minimize.png" ></div>
    	<div id="div-wordentry-contents"></div>
    </div>
    <div id="div-gist-iconized" title="Maximize"><img src="imgs/maximize.png" height=24 ></div>


    </form>
