# Manual test plan for Inuktut apps

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

IU page whose En page cannot be determined

EN page whose IU page cannot be determined

URL on a server that does not exist
- Enter https://www.gov.nu.ca/blahblah
- Check that
  - System says the page cannot be found
  - ACTUALLY, THIS MESSAGE IS NOT YET IMPLEMENTED

URL on existing server that returns page not found


## Search Engine

Search for word in SYLLABIC -- Happy Path
- Enter ᐅᒃᐱᕐᓂᖅ (= religion) in the query text box, then click [Search] 
  button.
- Check that the search term is replaced by this:

     (ᐅᑉᐱᕐᓂᕐᒥᒃ OR ᐅᑉᐱᕐᓂᖅ OR ᐅᑉᐱᕐᓂᖏᑦ OR ᐅᑉᐱᕐᓂᐅᕗᖅ OR ᐅᑉᐱᓂᕐᒧᓪᓗ OR ᐅᒃᐱᕐᓂᖅ)

  and that a Google search page is opened with that query

Search for word in LATIN - Happy Path
- Enter ukpirniq (= religion) in the search box, then click [Search] button
- Check that the search term is replaced by this:

     (ᐅᑉᐱᕐᓂᕐᒥᒃ OR ᐅᑉᐱᕐᓂᖅ OR ᐅᑉᐱᕐᓂᖏᑦ OR ᐅᑉᐱᕐᓂᐅᕗᖅ OR ᐅᑉᐱᓂᕐᒧᓪᓗ OR ᐅᒃᐱᕐᓂᖅ)

  and that a Google search page is opened with that query
  
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
  - Check that the Progress Wheel is displayed until the very last word has been 
    displayed.
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
    
## Morpheme Search

Happy path
- Enter morpheme 'tut', then click on Search
  - Should see two  morphemes that match 'tut'
  - Click on each of the morphemes and inspect the list of hits for it
    - Words should be sorted in DECREASING order of frequency
    - Clicking on a word should show:
      - Description of the word
      - A list of parallel English-Inuktut sentences using that word
  
Submit form with Enter key
- Enter morpheme 'tut', then PRESS ENTER
  - Form should be submitted 
 