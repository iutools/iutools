    <!-- Form for searching for examples -->
    <form id="form-query" method="post" action="">
    <div>
    Max examples:
    <input id="nb-examples" name="nb-examples" value="10" style="width:5em;">
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

    </form>
