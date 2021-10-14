# Manual test plan for Inuktut apps

## Word Dict

Basic scenario
- Here is a list of steps that will be applied in different scenarios.
- Search for a QUERY_WORD in a specific QUERY_LANGUAGE
- Check that the entry for that QUERY_WORD is displayed automatically
- In some scenarios we expect the QUERY_WORD to be the only hit in the list if so,
  make sure that the hit list only contains the QUERY_WORD
- But in most scenarios, we expect the list of hits to contain other words besides 
  the QUERY_WORD. In those cases, check that:
  - the number of hits is approximately what the specific scenario expects
  - every additional hit is a superstring of the QUERY_WORD   
- Inspect the entry for the QUERY_WORD
  - Title bar of the Word Entry window shows the QUERY_WORD in both latin and syllabics.
    - The first script should be the script used to enter the QUERY_WORD.
      In other words, if we entered the QUERY_WORD in latin, then the word should
      appear as latin/syllabics. Otherwise, it should appear as syllabics/latin.
  - Everything else (Translations, Related words, Examples) displayed in the 
    script used to enter the QUERY_WORD, except of course for text that is in English.
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
    - Check that there are about 17 hits
    - Translations include 'clams' and 'divers'

Syllabic query
- Do the steps described in the 'basic scenario' with the following 'specifics':
  - QUERY_WORD: 'ᐊᒻᒨᒪᔪᖅ'
  - QUERY_LANGUAGE: Leave it at Inuktitut 
  - Check that there are about 17 hits
  - Translations include 'clams' and 'divers'

English query
- Do the steps described in the 'basic scenario' with English word 'housing', 
  with the following 'specifics':
  - QUERY_WORD: 'housing'
  - QUERY_LANGUAGE: Change it to English
  - At the moment, it's 'normal' that the list of hits only shows the
    word 'housing'

Query that returns no results
- Search for ninuksuk
- Make sure no Console error
- Make sure system says no hits were found
- Make sure the Word Info window is not visible at all (not even minimized)
- Nothing else to check for that 

Very long word
- Search for aanniaqarnanngittulirijimmarik
- Make sure that the word and its Syllabics transcoding are properly displayed 
and do not overlap with the test that is supposed to be below it

Search with Enter vs Button
- Submit search by either
  - Typing Enter
  - Clicking the Search button

Progress wheel etc.
- Submit a search and check that
  - progress wheel is displayed
  - search button is disabled
  - when search is done, wheel disappears and butto is re-enabled
  
Min-max-Drag word entry window
- Make sure you can minimimize, maximize and drag the word entry window
- Minimize the word entry, then:
  - Click on on a word and make sure it maximises itself and the correct word is 
    displayed 
     
     
Word Entry window hiding/showing
- Reload the Word Dictionary page
   - Make sure the Word Entry window is NOT showing
- Search for word 'inuksuk'
   - Make sure the Word Entry window is NOW showing and displays the entry for 
     'inuksuk'.
- Search for a word that does not return any results like 'blahblahblah' 
   - Make sure the Word Entry window is NOT showing
- Search again for 'inuksuk' to make sure the WordEntry window is shown
- Search for a query that returns some hits but is not itself a word, ex: 'iglum'
  - Make sure that some hits are displayed, but that the Word Entry window is NOT SHOWN   
      
## Gister

Syllabic text
- Copy and paste some Syllabic text from https://www.gov.nu.ca/iu
- Hit [Gist Text]
- Check that 
  - Text has been Romanized
  - Hover the mouse over a word:
    - It shows that it's clickable
    - Clicking on that IU word produces a Gist with:
      - Morphological analysis
      - Parallel sentences
  - Hover the mouse over a NON-word (ex: period, comma, space):
    - It does NOT show that it's clickable
    - Clicking on that NON-word does NOT produce a Gist

Romanized text
- Copy and paste some syllabics text from https://www.gov.nu.ca/iu
- Translitarate it to roman and paste it into the Gister form
- Hit [Gist Text]
- Check that 
  - Text _stayed_ Romanized
  - Hover the mouse over a word:
    - It shows that it's clickable
    - Clicking on that IU word opens a Dictionary Entry for that word
  - Hover the mouse over a NON-word (ex: period, comma, space):
    - It does NOT show that it's clickable
    - Clicking on that NON-word does NOT open a Word Entry
      - NOTE: In this particular scenario, if the the original text contained 
        an English word whose characters are all valid IU Latin chars (ex: 'main')
        then this word may be clickable eventhough it is not IU.

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
- Check that the text has been romanized
- Check that all Inuktitut romanized words are clickable...
  - But the English word 'main' is NOT clickable     
     
IU url (Happy Path)
- Enter url https://www.gov.nu.ca/iu
- Check that
  - IU sentences on the left, En on the right
  - IU Text has been Romanized
  - Hover the mouse over a word:
    - It shows that it's clickable
    - Clicking on that IU word opens a Dictionary Entry for that word
  - Hover the mouse over a NON-word (ex: period, comma, space):
    - It does NOT show that it's clickable
    - Clicking on that NON-word does NOT produce a Gist

En url (Happy Path)
- Enter https://www.gov.nu.ca/honourable-joe-savikataaq-4
- Check that
  - IU sentences on the left, En on the right
  - IU Text has been Romanized
  - Hover the mouse over a word:
    - It shows that it's clickable
    - Clicking on that IU word opens a Dictionary Entry for that word
  - Hover the mouse over a NON-word (ex: period, comma, space):
    - It does NOT show that it's clickable
    - Clicking on that NON-word does NOT open a Dictionary Entry for that word

     
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

      Unable to download Inuktitut content for the page

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



## Search Engine

Search for word in SYLLABIC -- Happy Path
- Enter ᐅᒃᐱᕐᓂᖅ (= religion) in the query text box, then click [Search] 
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

Search for word in LATIN - Happy Path
- Enter ukpirniq (= religion) in the search box, then click [Search] button
- Check that this displays the results of a Google search for a list of alternatives, surrounded 
  parens and spearated by ORs. As of Oct 2021, the list of alternatives was:

     (ᐅᒃᐱᕐᓂᖅ OR ᐅᑉᐱᕈᓱᑉᐳᖓ OR ᐅᑉᐱᕈᓱᑦᑐᖓ OR ᐅᑉᐱᕈᓱᒃᑲᒪ OR ᐅᑉᐱᕆᔭᕋ OR ᐅᒃᐱᕈᓱᒃᐳᒍᑦ)
     
  The specific alternatives used may change over time, but you should at least 
  make sure that they start with the same 3-4 chars as the input word.
- Copy the query that was sent to Google and tranlisterate it to Latin. 
- Click on the Back button to go back to the IUTools Web Search page and check
  that the original query has been replaced by transliterated Google query that 
  that you generated above. 

  
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
  
Search by pressing Enter key vs clicking Search
- Enter a search word in the text box, then press Enter key.
  - Make sure the search is launched as expected
- Do the same but this time launch the search by pressing the _Search_ button  
  

  
## Spell Checker

Spell check SYLLABIC -- Happy Path
- Enter some text in SYLLABIC, some of which are badly spelled, others not.
- Hit _Spell Check_ button
- Words from input text should be displayed one at a time, with misspelled 
  words underlined. 
- Make sure that the corrected text, as well as all the suggested spellings
    are  displayed in SYLLABIC
- Make sure that all the words that are mis-spelled are labeled as such and that:
  - The FIRST suggestion is the original misspelled word
  - The list shows the correct spelling in the list, ideally in SECOND place     
- Click on a mis-spelled word and CHOOSE an alternate spelling
  - Make sure that the chosen spelling is now displayed
- Click on a mis-spelled word and TYPE an alternate spelling
  - Make sure that the typed spelling is now displayed

Spell check LATIN -- Happy Path
- Enter some text in LATIN, some of which are badly spelled, others not. 
- Hit _Spell Check_ button
- Words from input text should be displayed one at a time, with misspelled 
  words underlined. 
- Make sure that the corrected text, as well as all the suggested spellings
    are  displayed in LATIN
- Make sure that all the words that are mis-spelled are labeled as such and that:
  - The FIRST suggestion is the original misspelled word
  - The list shows the correct spelling in the list, ideally in SECOND place     
- Click on a mis-spelled word and CHOOSE an alternate spelling
  - Make sure that the chosen spelling is now displayed
- Click on a mis-spelled word and TYPE an alternate spelling
  - Make sure that the typed spelling is now displayed

Concurency testing and Interruption
- When you hit _Spell Check_
  - Check that the Progress Wheel is displayed and that the _Spell Check__ 
    is deactivated until the very last word has been displayed.
  - While the checker is still working on some of the words, try click on a 
    misspelled word and doing each of the followgin:
    - Click on a mis-spelled word and CHOOSE an alternate spelling
        - Make sure that the chosen spelling is now displayed
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
- Try it both ways
  - Make sure that the partial correction is included or not 
  - Note: Even when _Include partial corrections_ is checked, SOME words may 
    not have partial corrections. So if you click on a word and don't see one, 
    try other words.   
    
Spell check text that is too large
- Try to spell check a text with > 500 words
- Check that the system prints an error message that says you have to split the
  text into smaller chunks. 
    
## Morpheme Dictionary

Happy path
- Enter morpheme 'tut', then click on Search
  - Should see 4  morphemes that match 'tut'
  - For each morpheme, check that we display
    - human-readable description (ex: verb to verb suffix)
    - Definition (ex: "To hit or land on something")
    - List of example words sorted in DECREASING order of frequency
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

  
 
## Feedback link

- Click Feedback, type a message and make sure it gets sent      
 
 ## Action logging
 
 - For each of the actions mentioned below, make sure that the action is logged 
   properly in the tomcat log, i.e.
   - The action is logged once and only once
   - The action data provided by the log line is accurate
     
 - Actions to test:
   - Word Dictionary
     - Search for a word (_action=WORD_SEARCH, data=lang,word_)
     - Click on a word (_action=WORD_ENTRY, data=lang,word_)
   - Morpheme Examples
     - Do a search for a morpheme (_action=MORPHEME_DICT, data=corpusName,nbExamples,wordPattern_)
       - By typing ENTER and by clicking button
     - Click on an example word (_action=WORD_ENTRY, data=lang,word__)
   - Gister
     - Gist some text (_action=GIST_TEXT, data:type=text,totalWords)
     - Gist a URL (_action=GIST_TEXT, data:type=url)
     - Click on a word to display its word entry (_action=WORD_ENTRY, data=lang,word_)
   - Spell Checker
     - Spellcheck some text (_action=SPELL, data=totalWords)
   - Web Search
     - Search for a word (_action=SEARCH_WEB, data=origQuery)
     
 