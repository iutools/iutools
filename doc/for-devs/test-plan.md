# Manual test plan for Inuktut apps

## IMPORTANT: Run all apps with ?debug=1

That way if a server exception is raised, you will see the stack trace.
Otherwise, you will only see a "generic" message along the lines of:

   The server encountered an error

## Word Dict

Basic scenario
- Here is a list of steps that will be applied in different scenarios.
- Search for a QUERY_WORD in a specific QUERY_LANGUAGE, with a speicific 
  PREFERRED_SCRIPT (top right menu)
- Check that the entry for that QUERY_WORD is displayed automatically
- In some scenarios we expect the QUERY_WORD to be the only hit in the list if so,
  make sure that the hit list only contains the QUERY_WORD
- But in most scenarios, we expect the list of hits to contain other words besides 
  the QUERY_WORD. In those cases, check that:
  - the number of hits is approximately what the specific scenario expects
  - every additional hit is a superstring of the QUERY_WORD   
- Inspect the entry for the QUERY_WORD
  - Title bar of the Word Entry window shows the QUERY_WORD in both latin and syllabics.
    - The first script should be the PREFERRED script and the second one should 
      be the other script.
  - Everything else is displayed in the PREFERRED script, 
    except for:
    - text that is in English.
    - Inuktitut text for the bilingual examples (which at the moment are always in latin)
  - There is a decomp displayed (unless the specific scenario expects the word 
    to be undecomposable)
  - There are some translations displyayed, including some that are relevant for 
    that specific scenario.
    - The translations are in other language than the QUERY_LANGUAGE
  - There are some related words
  - There are some bilingual Examples of use
    - The QUERY_LANGUAGE appears on the left and the other langauge on the right
    - The names of the languages shown in the table header correspond are set 
      accordingly.
    - Highlighting is ok on both sides
- Click on one of the other words in the hit list
  - Check its word entry
- Click on one of the Related words and make sure the content of 
  the Word Entry window changes accordingly 
- Minimimize the word entry window
- Maximize the word entry window
  - Make sure word displayed is like before
- Move the word entry window around

Latin query:
- Do the steps described in the 'basic scenario' with following specifics
    - QUERY_WORD: 'ammuumajuq'
    - QUERY_LANGUAGE: Leave it at Inuktitut 
    - PREFERRED_SCRIPT: Roman
    - Check that there are about 17 hits
    - Translations include 'clams' and 'divers'

Latin query - Capitalized words:
- Do the steps described in the 'basic scenario' with following specifics
    - QUERY_WORD: 'Nunavut'
    - QUERY_LANGUAGE: Leave it at Inuktitut 
    - PREFERRED_SCRIPT: Roman
    - Check that there are about 26 hits
    - Make sure the word entry is OK

Syllabic query
- Do the steps described in the 'basic scenario' with the following 'specifics':
  - QUERY_WORD: 'ᐊᒻᒨᒪᔪᖅ'
  - QUERY_LANGUAGE: Leave it at Inuktitut 
  - PREFERRED_SCRIPT: SYLLABICS
  - Check that there are about 17 hits
  - Translations include 'clams' and 'divers'

English query - Single word
- Do the steps described in the 'basic scenario' with English word 'housing', 
  with the following 'specifics':
  - QUERY_WORD: 'development'
  - QUERY_LANGUAGE: Change it to English
  - PREFERRED_SCRIPT: Roman
  - At the moment, it's 'normal' that the list of hits only shows the
    word 'development'
    
English query - Multi word
- Do the steps described in the 'basic scenario' with English word 'healthcare coverage', 
  with the following 'specifics':
  - QUERY_WORD: 'healthcare coverage'
  - QUERY_LANGUAGE: Change it to English
  - PREFERRED_SCRIPT: Roman
  - At the moment, it's 'normal' that the list of hits only shows the
    expression 'healthcare coverage'

Out-of-hansard valid IU word
- Search for a word that is a valid IU word but is not in the Hansard
- ex: surusilaalirijikkut
- Make sure that the dictionary displays a word entry for it

Invalid IU word that does NOT appear in the Hansard
- Search for a word that is NOT a valid IU word
- ex: ninuksuk
- The dictionary should say 0 hits found
  
Invalid IU word that DOES appear in the Hansard
- ex: ???
- The dictioanry should still display an entry that says:
-- No decomp (possibly misspelled?)
-- Lists the examples found in the Hansard
  
Very long word
- Search for aanniaqarnanngittulirijikkunnik
- Make sure that the word and its Syllabics transcoding are properly displayed 
and do not overlap with the text that is supposed to be below it

Change preferred script (INUTKTITUT query)
- ROMAN-to-ROMAN
  - Set preferred script to ROMAN
  - Search for ammuumajuq, then check that:
      - Title shows word as ROMAN/SYLL
      - Related words, Alignments shown in ROMAN
- ROMAN-to-SYLL
  - Set preferred script to SYLL
  - Search for ammuumajuq, then check that:
      - Title shows word as SYLL/ROMAN
      - Related words, Alignments shown in SYLL
- SYLL-to-SYLL
  - Set preferred script to SYLL
  - Search for ᐊᒻᒨᒪᔪᖅ, then check that:
      - Title shows word as SYLL/ROMAN
      - Related words, Alignments shown in SYLL
- SYLL-to-ROMAN
  - Set preferred script to ROMAN
  - Search for ᐊᒻᒨᒪᔪᖅ, then check that:
      - Title shows word as SYLL/ROMAN
      - Related words, Alignments shown in ROMAN


Change preferred script (ENGLISH query)
- Set langauge to English
- ENGLISH-ROMAN
  - Set preferred script to ROMAN
  - Search for 'housing', then check that:
      - Title stayed ROMAN (cause it's English)
      - Translations and Alignments shown in ROMAN
- ENGLISH-SYLL
  - Set preferred script to SYLL
  - Search for 'housing', then check that:
    - Title stayed ROMAN (cause it's English)
    - Translations and Alignments shown in SYLL
  

Search with Enter vs Button
- Submit search by either
  - Typing Enter
  - Clicking the Search button

Progress wheel etc.
- Submit a search and check that
  - progress wheel is displayed in the Search form
  - search button is disabled
  - when search is done, wheel disappears and butto is re-enabled
- Click on a word
  - Make sure the progress wheel is displayed in the Word Entry window
  
Min-max-Drag word entry window
- Make sure you can minimimize, maximize and drag the word entry window
  (Note: To drag the window, you must drag its title bar)
- Minimize the word entry, then:
  - Click on on a word and make sure it maximises itself and the correct word is 
    displayed 
     
Copy and paste content from the Word Entry dialog
- Copy and paste one of the Related words
- Copy and paste one of the Translations     
     
Word Entry window hiding/showing/closing
- Reload the Word Dictionary page
   - Make sure the Word Entry window is NOT showing
- Search for word 'inuksuk'
   - Make sure the Word Entry window is NOW showing and displays the entry for 
     'inuksuk'.
- Leaving the word entry opened, search for a word that does not return any 
  results like 'blahblahblah' 
   - Make sure the Word Entry window is NOT showing
- Search again for 'inuksuk' to make sure the WordEntry window is shown
- Search for a query that returns some hits but is not itself a word, ex: 'iglum'
  - Make sure that some hits are displayed, but that the Word Entry window is NOT SHOWN
- Close the word info windows
  - Search for word inuksuk and make sure that the info for the word is correctly 
    displayed   
    
Word Dict Help
- Search for a word and open its word entry
- Click on the _info_ link at the top right of that window
- Test all the links on that help page to make sure they lead to the right place    
    
## Spell Checker

Spell check LATIN -- Happy Path
- Enter some text in LATIN, some of which are badly spelled, others not. 
- Hit _Spell Check_ button
- Words from input text should be displayed one at a time, with misspelled 
  words underlined. 
- Click on an underlined word and make sure that:
    - The corrected text, as well as all the suggested spellings are  displayed 
      in LATIN
    - The list shows the correct spelling in the list, ideally in FIRST place
- Test different ways of APPLYING a suggestion     
    - Click on a mis-spelled word, CHOOSE an alternate spelling and hit Apply
      - Make sure that the chosen spelling is now displayed, and that the dialog
        box disappeared.
    - Click on a mis-spelled word, CHOOSE an alternate spelling, MODIFY IT and 
      hit Apply
      - Make sure that the modified spelling is now displayed, and that the 
        dialogbox disappeared.
    - Click on a mis-spelled word, TYPE an alternate spelling from scratch and hit Apply
      - Make sure that the typed spelling is now displayed, and that the dialog
        box disappeared.
- Test different ways of CANCELING a suggestion     
    - Click on a mis-spelled word, CHOOSE an alternate spelling and hit Cancel
      - Make sure that the displayed spelling has not changed, and that the dialog
        box disappeared.
    - Click on a mis-spelled word, CHOOSE an alternate spelling, MODIFY IT and 
      hit Cancel
      - Make sure that the displayed spelling has not changed, and that the dialog
        box disappeared.
    - Click on a mis-spelled word, TYPE an alternate spelling from scratch and hit Cancel
      - Make sure that the displayed spelling has not changed, and that the dialog
        box disappeared.


Spell check SYLLABIC -- Happy Path
- Enter some text in SYLLABIC, some of which are badly spelled, others not. 
- Hit _Spell Check_ button
- Words from input text should be displayed one at a time, with misspelled 
  words underlined. 
- Click on an underlined word and make sure that:
    - The corrected text, as well as all the suggested spellings are  displayed 
    in SYLLABIC
    - The FIRST suggestion is the original misspelled word
    - The list shows the correct spelling in the list, ideally in SECOND place
- Test different ways of APPLYING a suggestion     
    - Click on a mis-spelled word, CHOOSE an alternate spelling and hit Apply
      - Make sure that the chosen spelling is now displayed, and that the dialog
        box disappeared.
    - Click on a mis-spelled word, CHOOSE an alternate spelling, MODIFY IT and 
      hit Apply
      - Make sure that the modified spelling is now displayed, and that the dialog
        box disappeared.
    - Click on a mis-spelled word, TYPE an alternate spelling from scratch and hit Apply
      - Make sure that the typed spelling is now displayed, and that the dialog
        box disappeared.
- Test different ways of CANCELING a suggestion     
    - Click on a mis-spelled word, CHOOSE an alternate spelling and hit Cancel
      - Make sure that the displayed spelling has not changed, and that the dialog
        box disappeared.
    - Click on a mis-spelled word, CHOOSE an alternate spelling, MODIFY IT and 
      hit Cancel
      - Make sure that the displayed spelling has not changed, and that the dialog
        box disappeared.
    - Click on a mis-spelled word, TYPE an alternate spelling from scratch and hit Cancel
      - Make sure that the displayed spelling has not changed, and that the dialog
        box disappeared.

- Prefered script
  - When spellchecking text, we ALWAYS want the spellchecked result to be in the 
    same script as the input, no matter what the preferred script is
    - LATIN text + LATIN preferred --> LATIN result
    - SYLL text + LATIN preferred --> SYLL result
    - LATIN text + SYLL preferred --> LATIN result
    - SYLL text + SYLL preferred --> SYLL result

- Choose correction for different words
    - Spell check text: 'nunavvvut iglu inuksssuk'
    - Click on 'nunavvvut' and Apply the correct spelling
    - Click on 'inuksssuk' and make sure that:
        - The suggestions displayed are for that words AND ONLY THAT WORD (in 
          particular, no suggestions for 'nunavvvut')
        - Apply the correct spelling
        - Make sure that
            - The correct spelling for 'inuksuk' is now displayed instead of 
              'inuksssuk'
            - The spelling for nunavut stayed the same. 

- Multiple occurences of same mistake
    - Spell check the following text: 'nunavvvut nunavvvut'
    - Click on first word and apply the correct spelling
    - Check that the spelling of the first word was changed, but not for the 
      second one.

Concurency testing and Interruption
- When you hit _Spell Check_
  - Check that the Progress Wheel is displayed and that the _Spell Check__ 
    is deactivated until the very last word has been displayed.
  - While the checker is still working on some of the words, try click on a 
    misspelled word and doing each of the followgin:
    - Click on a mis-spelled word and CHOOSE an alternate spelling
        - Make sure that the chosen spelling is now displayed
    - Click on a mis-spelled word, CHOOSE a spelling and MODIFY it
        - Make sure that the modified spelling is now displayed
    - Click on a mis-spelled word and TYPE an alternate spelling
        - Make sure that the typed spelling is now displayed
  - While the checker is still working on some of the words:
    - Hit the Stop button
      - Make sure progress wheel has disappeared and that the app has stopped 
        checking words
    - Copy paste some new text to be spelled and hit _Spell Check_
       - Make sure the new spell checking task is started correctly

Progress Wheel
- When you hit Spell button, make sure that the progress wheel is displayed
    
 Spell check numbers
 - Spell check a number like: 2019
 - Make sure that it's NOT labelled as being mis-spelled
   
 Copy to clipboard -- LATIN
 - Spell check some LATIN text
   - Make sure that the text to be checked includes:
     - Some words that are separated by commas, single spaces, double spaces, 
       single newline and blank line
 - Click on copy to clipboard and paste into a text editor
 - Make sure that:
   - The pasted text corresponds to the text displayed in the web app
   - In particular, the separators between the words are as show in the input box for the
     text to be spelled
   - No extra spaces were inserted between words
   - No extra newlines were 
- Click on two of the mis-spelled words, and change their spellings
  - One by selecting from the sugggestions, the other by typing a new correction
  - Click on copy to clipboard and paste into a text editor
  - Copy text to text editor, and make sure that the changed spellings are the 
    ones that appear in the editor
    
Check with and without _Include partial corrections_
- Spell check nunavvvvut
  - With and without partial correction
    - WITH: the following entry should be included nunav[vv]ut
    - WITHOUT: It should NOT appear
    
Spell check text that is too large
- Try to spell check a text with > 500 words
- Check that the system prints an error message that says you have to split the
  text into smaller chunks. 
  
Choose corrrection after closing suggestions box
- Click on a bad word
- Click on the X to close the suggestions window
- Click on the bad word again and make sure you can select and apply a suggestion
          
Click on two misspelled words in close succession
- Spell check 'nunavvvut inuksssuk'
- Click on nunavvvut, then move and resize the choose correction box so it 
  does not hide the two clickable words
- Click on nunavvvut again, AND IMMEDIATLY AFTER click on inuksssuk (i.e. before 
  the system has time to finish displaying the resuts for nunavvvut)
- Click on one  of the suggestions and make sure that
  - inuksssuk was changed as expected
  - nunavvvut stayed the same
  was 
  corrected   
            
## Search Engine
Search for word in LATIN - Happy Path
- Enter ammuumajuq (= clam) in the search box, then click [Search] button
- Check that this displays the results of a Google search for a list of alternatives, surrounded 
  parens and spearated by ORs. As of Oct 2021, the list of alternatives was:

    (ᐊᒻᒨᒪᔪᖅ OR ᐊᒻᒨᒪᔪᕐᓂᐊᕐᑏᑦ OR ᐊᒻᒨᒪᔪᖅᓯᐅᖅᑐᑎᒃ OR ᐊᒻᒨᒪᔪᖅᑕᕐᓂᕐᒧᑦ OR ᐊᒻᒨᒪᔪᖅᑕᖅᑏᑦ OR ᐊᒻᒨᒪᔪᕐᓂᐊᕐᓂᕐᒧᑦ)
     
  The specific alternatives used may change over time, but you should at least 
  make sure that they start with the same 3-4 chars as the input word.
- Copy the query that was sent to Google and tranlisterate it to Latin. 
- Click on the Back button to go back to the IUTools Web Search page and check
  that the original query has been replaced by transliterated Google query that 
  that you generated above. 


Search for word in SYLLABIC -- Happy Path
- Enter ᐊᒻᒨᒪᔪᖅ (= clam) in the query text box, then click [Search] 
  button.
- Check that this displays the results of a Google search for a list of alternatives, surrounded 
  parens and spearated by ORs. As of Oct 2021, the list of alternatives was:

     (ᐅᒃᐱᕐᓂᖅ OR ᐅᑉᐱᕈᓱᑉᐳᖓ OR ᐅᑉᐱᕈᓱᑦᑐᖓ OR ᐅᑉᐱᕈᓱᒃᑲᒪ OR ᐅᑉᐱᕆᔭᕋ OR ᐅᒃᐱᕈᓱᒃᐳᒍᑦ)
     
  The specific alternatives used may change over time, but you should at least 
  make sure that they start with the same 3-4 chars as the input word. 
- Copy the query that was sent to Google and paste it somewhere for future 
  reference          
- Click on the Back button to go back to the IUTools Web Search page and check
  that the original query has been replaced by the expended query that was 
  sent to Google (which you pasted above). 


  
Search using an already expanded query
 Enter ᐅᒃᐱᕐᓂᖅ (= religion) in the query text box, then click [Search] 
  button.
- Check that this displays the results of a Google search for a list of alternatives, surrounded 
  parens and spearated by ORs. As of Oct 2021, the list of alternatives was:

     (ᐅᒃᐱᕐᓂᖅ OR ᐅᑉᐱᕈᓱᑉᐳᖓ OR ᐅᑉᐱᕈᓱᑦᑐᖓ OR ᐅᑉᐱᕈᓱᒃᑲᒪ OR ᐅᑉᐱᕆᔭᕋ OR ᐅᒃᐱᕈᓱᒃᐳᒍᑦ)
  
  The specific alternatives used may change over time, but that does not matter
  for this test case.
- Copy the query that was sent to Google and paste it somewhere for future 
  reference        
- Click on the Back button to go back to the IUTools Web Search page and check
  that the original query has been replaced by that pasted Google query. 
- Click on Search again and make sure that
  - The Google page uses the same expanded query as before (i.e. it is the same 
    as the original Google query that you pasted above). 
  - Click on the back button and check again that the query in the IU Web Search 
    app is the same as the original Google Query that you pasted above.  
  
Search word with spaces at start or end
- Search for a word that has spaces at the start, end or both
- Make sure the query is being expanded  
  
Search by pressing Enter key vs clicking Search
- Enter a search word in the text box, then press Enter key.
  - Make sure the search is launched as expected
- Do the same but this time launch the search by pressing the _Search_ button  
    
## Morpheme Dictionary

Happy path
- Enter morpheme 'tut', then click on Search
  - Should see 5  morphemes that match 'tut'
  - Morphemes should be order by:
    - Those with most word examples first
    - Those whose morpheme ID is shorter in case of tie
  - For each morpheme, check that we display
    - human-readable description (ex: verb to verb suffix)
    - Definition (ex: "To hit or land on something")
    - List of example words sorted in DECREASING order of frequency
      - Note: in some cases, it may say 'No examples found for this morpheme'
      - Click on an example word and make sure its dictionary entry is displayed
  
Submit form with Enter key
- Enter morpheme 'tut', then PRESS ENTER
  - Form should be submitted 
  
Word Entry window hiding/showing
- Reload the Morpheme Dictionary page
   - Make sure the Word Entry window is NOT showing
- Search for morpheme 'tut'
   - Make sure the Word Entry window is NOT showing
- Click on an example word
   - Make sure the Word Entry IS now showing
- Search for the morpheme 'gaq'
   - Make sure the Word Entry window is NOT showing
- Click on an example word
   - Make sure the Word Entry IS now showing and that it shows the newly 
     clicked word as opposed to the previous one
- Reload the Morpheme Dictionary page     
   - Make sure the Word Entry window is NOT showing

Change max number of examples
- Search for tut
- Make sure that the number of examples provided for the first morpheme is equal 
  to the default max number of example (i.e. 10 as of Oct 202)
- Redo the search with max number of examples set to twice as much
  - Check that the number of examples is now that number
- Do the same thing this time setting the max number to 5 
          
    
## Reading Assistant

Syllabic text
- Copy and paste some Syllabic text from https://www.gov.nu.ca/iu
- Hit [Assist Reading]
- Check that 
  - Text is displayed in the PREFERRED SCRIPT
  - Hover the mouse over a word:
    - It shows that it's clickable
    - Clicking on that IU word opens the dictionary entry for that IU word, and 
      that everything in the entry is in LATIN 
  - Hover the mouse over a NON-word (ex: period, comma, space):
    - It does NOT show that it's clickable
    - Clicking on that NON-word does NOT produce a Gist
- Change the Preffered script and click [Assist Reading] again
  - Check that the text is now diplayed in the new PREFERRED script

Romanized text
- Copy some syllabics text from https://www.gov.nu.ca/iu
- Translitarate it to roman and paste it into the Gister form
- Hit [Assist Reading]
- Check that 
  - Text is displayed in the current PREFERRED script
  - Hover the mouse over a word:
    - It shows that it's clickable
    - Clicking on that IU word opens the dictionary entry for that IU word, and 
      that everything in the entry is in LATIN 
  - Hover the mouse over a NON-word (ex: period, comma, space):
    - It does NOT show that it's clickable
    - Clicking on that NON-word does NOT open a Word Entry
      - NOTE: In this particular scenario, if the the original text contained 
        an English word whose characters are all valid IU Latin chars (ex: 'main')
        then this word may be clickable eventhough it is not IU.
- Change the Preffered script and click [Assist Reading] again
  - Check that the text is now diplayed in the new PREFERRED script

Word that does not decompose
- Enter text: 'ᓴᕕᑲᑖᖅ' (This is a proper noun)
- Click on the romanized word: 'savikataaq'
- Make sure that the Gist does not crash and that the Dictionary Entry for the word says:
  - Word could not be decomposed
  
Text that contains spaces and newlines
- Gist text that contains some newlines and extra spaces
- Make sure that the extra spaces and newlines are preserved in the gist checke output  
  - AND that the browser is still able to do automatic line wrapping
    i.e. you don't have very long lines that correspond to each paragraph.
     
Syllabic text that contains some English word with only IU latin chars
- Enter some syllabics text and add the word 'main' in it (note: all characters 
in that word are valid Latin IU chars)
- Gist that text
- Check that the text is displayed in the PREFERRED script
- Check that all Inuktitut romanized words are clickable...
  - But the English word 'main' is NOT clickable     
     
Text that contains an INVALID out-of-corpus word
- Enter 'ninuksuk' and Assist Reading
- Click on the word ninuksuk in the gist
- Make sure that the word entry shows empty info in all sections 

Text that contains a VALID out-of-corpus word
- Enter '???' and Assist Reading
- Click on the word ??? in the gist
- Make sure that the word entry concludes and says 'No entry found for this word' 
     
IU url (Happy Path)
- Enter url https://www.gov.nu.ca/iu
- Check that
  - IU sentences on the left, En on the right
  - IU Text displayed in the current PREFERRED script
  - Hover the mouse over a word:
    - It shows that it's clickable
    - Clicking on that IU word opens a Dictionary Entry for that word
  - Hover the mouse over a NON-word (ex: period, comma, space):
    - It does NOT show that it's clickable
    - Clicking on that NON-word does NOT produce a Gist
- Change the PREFERRED script and hit [Assist Reading] again
  - Check that the IU text is now displayed in the new preferred script

En url (Happy Path)
- Enter https://www.gov.nu.ca/honourable-joe-savikataaq-4
- Check that
  - IU sentences on the left, En on the right
  - IU Text displayed in the current PREFERRED script
  - Hover the mouse over a word:
    - It shows that it's clickable
    - Clicking on that IU word opens a Dictionary Entry for that word
  - Hover the mouse over a NON-word (ex: period, comma, space):
    - It does NOT show that it's clickable
    - Clicking on that NON-word does NOT open a Dictionary Entry for that word
- Change the PREFERRED script and hit [Assist Reading] again
  - Check that the IU text is now displayed in the new preferred script

     
Undownloadable pages
- For each of the following situations, make sure the system does not crash and 
  displays a message saying the page could not be downloaded
  - URL on a server that exists but where the page itself does not exist
    - https://www.gov.nu.ca/blahblah
  - URL on a server that does not exist
    - https://www.asdfadsf.com/
  - URL on existing server that returns page not found
    - https://www.pipsnacks.com/404

Pages whose Inuktitut content cannot be downloaded
- Gist the following url: http://travelnunavut.ca/
  - This is a page in English that has an Inuktitut link, but that link 
    leads to an English page. Hence, the system is unable to acquire the 
    Inuktitut content for the page
- Check that the page displays the following error message:

      The requested page is not in Inuktitut and it does not seem to provide an Inuktitut translation

  - IMPORTANT: If the error message says 

      Unable to download the input page
      
    Then it means that the English page itself timed out. This is differrent 
    from the error condition we are trying to test in this case, and it is one 
    that can happen intermitently.
    
    When this happens, try the search again until you get the first error 
    message above.

Word Entry window hiding/showing
- Reload the Gister text
   - Make sure the Word Entry window is NOT showing
- Gist some text
   - Make sure the Word Entry window is NOT showing
- Click on a word
   - Make sure the Word Entry IS now showing
- Gist the text again
   - Make sure the Word Entry window is NOT showing
- Click on a word
   - Make sure the Word Entry IS now showing and it display the newly clicked
     word as opposed to the previous one
- Reload the Gister page     
   - Make sure the Word Entry window is NOT showing
   
- Gist in ROMAN vs SYLL
  - Copy and paste some ROMAN inuktitut
      - Set preferred script to ROMAN
      - Gist and chekc that:
        - Content is in ROMAN
        - When you click on a word, word entry is in ROMAN
      - Set preferred script to SYLL
      - Gist and chekc that:
        - Content is in SYLL
        - When you click on a word, word entry is in SYLL
  - Copy and paste some SYLLABICS inuktitut
      - Set preferred script to ROMAN
      - Gist and chekc that:
        - Content is in ROMAN
        - When you click on a word, word entry is in ROMAN
      - Set preferred script to SYLL
      - Gist and chekc that:
        - Content is in SYLL
        - When you click on a word, word entry is in SYLL
  - Gist URL: http://www.gov.nu.ca/
      - Set preferred script to ROMAN
      - Gist and chekc that:
        - Content is in ROMAN
        - When you click on a word, word entry is in ROMAN
      - Set preferred script to SYLL
      - Gist and chekc that:
        - Content is in SYLL
        - When you click on a word, word entry is in SYLL
 
## Feedback link

- Click Feedback, type a message and make sure it gets sent      
 
 ## Action logging
 
 Take a sheet of paper and write down the following categories:

    // Searching a word with the Inuktitut-English Dictioary
    DICTIONARY_SEARCH
    
    // Looking up a word, whether it be from the Inuktitut-English Dictioary or
    // any other InuktiTools app
    WORD_LOOKUP

    // Spell check some Inuktitut text
    SPELL

    // Do a web search using the InuktiTools web search app
    SEARCH_WEB

    // Lookup a morpheme in the InuktiTools Morpheme Dictionary app
    MORPHEME_SEARCH

    // Launching the InuktiTools Reading Assistant app on either some text
    // or URL
    GIST_TEXT
   
 - Next, clear the tomcat applications log and start using the various 
   InuktiTools apps
   - Whenever you perform an action that corresponds to one of the above 
     categories, put a tick mark beside it so you keep track of how many 
     times you did that action. 
     - NOTE: When you do a word dict SEARCH and it produces some results,
       it also counts as  WORD_LOOKUP (because it automatically displayce 
       the dict entry for the first hit)
   - Make sure you do each of the actions more than once.
- Once you have done all the actions more than once, run the cli command

     iutools_cli analyze_log --log-file "path-to-your-tomcat-apps-logfile"

- This will output a summary of all the actions and endpoints that were sent to 
  the InuktiTools server. 
  - Items whose names are uppercased (ex: "DICTIONARY_SEARCH") 
    correspond to end-user actions, and those that are lowercased (ex: "gist/preparecontent")
    correspond to endpoints that were fired as a result of user actions.
  - For the purpose of this test, you only need to worry about the ACTIONS
  - Just make sure that the frequency that is mentioned in the summary report 
    for a given action corresponds to the number of tick marks you made for 
    that action.
        
     
 ## Test on 'small screen'
 
 Open a browser window, then
 - Make sure it is NOT maximised
 - Is made as small (width and height wise) as possible.
 - Test that
   - All the menus etc display correctly
   - Click on each of the menu and make sure that the page looks "good"
 - Try a basic, "happy path" scenario for each app and make sure there are no
   issues with displaying results on that small of a screen
 
 
 ## Cookie consent
 
 - Delete all cookies on localhost
   - With Chrome, click in the 'i' icon to the left of the URL, then Cookies and 
     Remove 
 - Load any page (except possibly the home page)
 - Check that a banner appears at bottom notifying you that the site uses cookies.
 - Click on the 'Read more' link and check that it brings you to an IUTools 
   help page describing our use of cookies
 - DON'T click the 'Got it' link just yet
   - Reload the page and make sure the cookies banner is displayed there
   - Click on 'Got it' link and make sure the banner is NOT there anymore
   - Load all other IUTools pages and make sure the banner is not displayed.