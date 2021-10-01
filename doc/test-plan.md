# Manual test plan for Inuktut apps


## Word Dict

Basic scenario
- Search for 'ammuumajuq'
- Check that the entry for 'nunavut' is displayed autoamtically
- Check that other words are listed in the hits list 
  - Typically 25 of them
- Inspect the entry for 'ammuumajuq'
  - Word displayed in latin first, syll second
  - Everything else displayed in roman
    - Translations, Related words, Examples
  - There is a decomp displayed
  - There are some translations displyayed, including 'divers' and 'clams'
  - There are some related words
  - There are some Examples
    - The Inuktitut is on the left, English on the right
    - Highlighting is ok on both sides
- Click on one of the other words in the hit list
  - Check its word entry
- Click on one of the Related words and make sure the content of 
  the Word Entry window changes accordingly 
- Minimimize the word entry window
- Maximize the word entry window
  - Make sure word displayed is like before
- Move the word entry window around

Syllabic query
- Search for ᐊᒻᒨᒪᔪᖅᓯᐅᖅᑐᑎᒃ
- Make sure that the entry for that word is automatically opened
- Check the entry
  - Do the same checks as for the basic scenario

English query
- search for housing, with 'English' for the language picklist
- Make sure that the entry for that word is automatically opened
- Note: At the moment, it's 'normal' that the list of hits only shows the
  word 'housing'
- Check the entry
  - Do the same checks as for the basic scenario

Search for a string that returns no results
- Search for ninuksuk
- Make sure no Console error
- Make sure system says no hits were found

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
    - Clicking on that IU word produces a Gist with:
      - Morphological analysis
      - Parallel sentences
  - Hover the mouse over a NON-word (ex: period, comma, space):
    - It does NOT show that it's clickable
    - Clicking on that NON-word does NOT produce a Gist

Word that does not decompose
- Enter text: 'ᓴᕕᑲᑖᖅ' (This is a proper noun)
- Click on the romanized word: 'savikataaq'
- Make sure that the Gist does not crash and that it says:
  - Word could not be decomposed
  - No sentences found for this word
  
Text that contains spaces and newlines
- Gist text that contains some newlines and extra spaces
- Make sure that the extra spaces and newlines are preserved in the gist checke output  
  - AND that the browser is still able to do automatic line wrapping
    i.e. you don't have very long lines that correspond to each paragraph.
     
IU url (Happy Path)
- Enter url https://www.gov.nu.ca/iu
- Check that
  - IU sentences on the left, En on the right
  - IU Text has been Romanized
  - Hover the mouse over a word:
    - It shows that it's clickable
    - Clicking on that IU word produces a Gist with:
      - Morphological analysis
      - Parallel sentenecs
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
    - Clicking on that IU word produces a Gist with:
      - Morphological analysis
      - Parallel sentenecs
  - Hover the mouse over a NON-word (ex: period, comma, space):
    - It does NOT show that it's clickable
    - Clicking on that NON-word does NOT produce a Gist

Undownloadable pages
- For each of the following situations, make sure the system does not crash and 
  displays a message saying the page could not be downloaded
  - IU page whose En page cannot be determined
  - EN page whose IU page cannot be determined
  - URL on a server that does not exist
    - https://www.gov.nu.ca/blahblah
  - URL on existing server that returns page not found
    - https://www.pipsnacks.com/404


## Search Engine

Search for word in SYLLABIC -- Happy Path
- Enter ᐅᒃᐱᕐᓂᖅ (= religion) in the query text box, then click [Search] 
  button.
- Check that the search term is replaced by this:

     (ᐅᑉᐱᕐᓂᕐᒥᒃ OR ᐅᑉᐱᕐᓂᖅ OR ᐅᑉᐱᕐᓂᖏᑦ OR ᐅᑉᐱᕐᓂᐅᕗᖅ OR ᐅᑉᐱᓂᕐᒧᓪᓗ OR ᐅᒃᐱᕐᓂᖅ)

  and that a Google search page is opened with that query

Search for word in LATIN - Happy Path
- Enter ukpirniq (= religion) in the search box, then click [Search] button
- Check that the search term is replaced by this in the Google window:

     (ᐅᑉᐱᕐᓂᕐᒥᒃ OR ᐅᑉᐱᕐᓂᖅ OR ᐅᑉᐱᕐᓂᖏᑦ OR ᐅᑉᐱᕐᓂᐅᕗᖅ OR ᐅᑉᐱᓂᕐᒧᓪᓗ OR ᐅᒃᐱᕐᓂᖅ)

  and that a Google search page is opened with that query
- Click the back button and check that the query has been replaced by this in 
  IUTools search engine window (i.e. expansion in Latin, not syllabics):
  
     (ukpirniq OR uppirusuppunga OR uppirusuttunga OR uppirusukkama OR uppirijara OR ukpirusukpugut)
  
Search using an already expanded query
 Enter ᐅᒃᐱᕐᓂᖅ (= religion) in the query text box, then click [Search] 
  button.
- Check that the search term is replaced by this:

     (ᐅᑉᐱᕐᓂᕐᒥᒃ OR ᐅᑉᐱᕐᓂᖅ OR ᐅᑉᐱᕐᓂᖏᑦ OR ᐅᑉᐱᕐᓂᐅᕗᖅ OR ᐅᑉᐱᓂᕐᒧᓪᓗ OR ᐅᒃᐱᕐᓂᖅ)

  and that a Google search page is opened with that query
- Click on the browser's back button to return to the iutools search page
- Click on Search again and make sure that
  - The expanded query remained exactly the same
  - This is the query that is opened in the Google page.  
  
Search in Latin
  
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
- Make sure that  the corrected text, as well as all the suggested spellings
    are  displayed in SYLLABIC
- Make sure that all the words that are mis-spelled are labeled as such and that
    the first suggested spelling is the right one     
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
- Make sure that all the words that are mis-spelled are labeled as such and that
    the first suggested spelling is the right one 
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
    - Hit the cancel button
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
 
Spell check text that contains spaces and newlines
- Spell Check text that contains some newlines and extra spaces
- Make sure that:
  - Extra spaces and newlines are preserved in the spell checke output
  - They are also preserved when you do Copy to clipboard     
    
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
     
     
## Feedback link

- Click Feedback, type a message and make sure it gets sent     