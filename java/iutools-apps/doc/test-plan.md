#Manual test plan for Inuktut apps

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