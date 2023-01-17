    <!-- Form for searching for examples -->
    <form id="form-query" method="post" action="">
    <div>
        <table>
            <tr>
                <td>Canonical form: </td>
                <td><input id="inp-morpheme" name="inp-morpheme" style="width:20em;" placeholder="ex: gaq"></td>
            </tr>
            <tr>
                <td>Grammatical role: </td>
                <td><input id="inp-grammar" name="inp-grammar" style="width:20em;" placeholder="ex: verb"></td>
            </tr>
            <tr>
                <td>Meaning: </td>
                <td><input id="inp-meaning" name="inp-meaning" style="width:20em;" placeholder="ex: to fish"></td>
            </tr>
            <tr>
                <td>Max examples: </td>
                <td><input id="nb-examples" name="nb-examples" value="10" style="width:5em;"></td>
            </tr>

        </table>
<%--    Morpheme (or start of it):--%>
<%--    <input id="morpheme" name="morpheme" style="width:20em;" placeholder="ex: gaq">--%>
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
