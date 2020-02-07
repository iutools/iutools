#Manual test plan for Inuktut apps

##Search Engine

Search for word in SYLLABIC -- Happy Path
- Enter ᐅᒃᐱᕐᓂᖅ (= religion) in the query text box, then click [Search] 
  button.
- Check that the search term is replaced by this:

     (ᐅᑉᐱᕐᓂᕐᒥᒃ OR ᐅᑉᐱᕐᓂᖅ OR ᐅᑉᐱᕐᓂᖏᑦ OR ᐅᑉᐱᕐᓂᐅᕗᖅ OR ᐅᑉᐱᓂᕐᒧᓪᓗ OR ᐅᒃᐱᕐᓂᖅ)

  and that we get some hits.
- Check that all hits on the first page contain at least one of those words. 
- Click on the second page of hits and check that:
  - They are different from the first page
  - They all contain one of the query terms
  

Search for word in LATIN - Happy Path
- Enter ukpirniq (= religion) in the search box, then click [Search] button
- Check that the search term is replaced by this:

     (ᐅᑉᐱᕐᓂᕐᒥᒃ OR ᐅᑉᐱᕐᓂᖅ OR ᐅᑉᐱᕐᓂᖏᑦ OR ᐅᑉᐱᕐᓂᐅᕗᖅ OR ᐅᑉᐱᓂᕐᒧᓪᓗ OR ᐅᒃᐱᕐᓂᖅ)

  and that we get some hits.
- Check that all hits contain at least one of those words 
- Click on the second page of hits and check that:
  - They are different from the first page
  - They all contain one of the query terms

  
Search by pressing Enter key
- Enter a search word in the text box, then instead of clicking 
  [Search] button, press Enter key.

##Spell Checker

Spell check LATIN -- Happy Path
- Enter some text in LATIN, some of which are badly spelled, others not. 
- Make sure that the corrected text, as well as all the suggested spellings
    are  displayed in LATIN
- Make sure that all the words that are mis-spelled are labeled as such and that
    the first suggested spelling is the right one 
- Click on a mis-spelled word and choose an alternate spelling
  - Make sure that the chosen spelling is now displayed
    
Spell check SYLLABIC -- Happy Path
- Enter some text in SYLLABIC, some of which are badly spelled, others not. 
- Make sure that  the corrected text, as well as all the suggested spellings
    are  displayed in SYLLABIC
- Make sure that all the words that are mis-spelled are labeled as such and that
    the first suggested spelling is the right one     
- Click on a mis-spelled word and choose an alternate spelling
  - Make sure that the chosen spelling is now displayed
    
    
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
- Click on one of the mis-spelled words, and change its spelling
  - Click on copy to clipboard and paste into a text editor
  - Copy text to text editor, and make sure that the selected spelling is the one that
    appears in the editor
    
##Morpheme Search

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
 