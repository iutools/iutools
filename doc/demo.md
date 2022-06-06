#Demo plan for Inuktut tools

## Preparation

- Start the Apache service (for webitext)
  - cd [GitRepos]/webitext/admin
  - perl webitextadm.pl apache_services --action=start
    
## WordDict

- Search in the IU-EN direction
  - Query: ammuumajuq
  - Prefered script: LATIN
- The dictionary finds many words that start with that query
- The first one is just 'ammuumajuq'
- A Word info window opens up providing information about the meaning of the word 
  and how to use it. In particular, it shows:
  - The Word Info window displays the word in both LATIN/SYLLABIC 
  - A list of possible ENGLISH translations is provided 
    - Clicking on a translation brings you to a list of bilingual examples 
  - Morphological decomposition 
  - Bilingual examples of use
  
- Search in the EN-IU direction   
  - Query: housing
  - Search in: English
  - Prefered script: LATIN

## Spell checker
- Do a google search for: 
    site:gov.nu.ca
- Choose a page randomly on that list, switch language to Inuktut
  and select a medium sized paragraph
- **Transliterate** it for covenience, then enter the Latin equivalent 
  into the spell checker.
- Find an underlined word and show the alternatives suggested
  - Let Benoit discuss those alternatives
- Talk about its accuracy

## Search engine

- Enter ᐊᖅᑯᑎᒋᔭᖏᓐᓂᒃ ('aqqutigijanginnik' = itinerary) in Google
  - Show that it only finds 3 hits
- Enter ᐊᖅᑯᑎᒋᔭᖏᓐᓂᒃ in iutools Search Engine
  - Show that the query was expanded to:

    (ᐊᖅᑯᑎᒋᓗᒋᑦ OR ᐊᑉᖁᑎᑦ OR ᐊᑉᖁᑎᖓ OR ᐊᑉᖁᓯᐅᕈᓐᓇᕆᐊᒃᓴᖏᓐᓂᒃ OR ᐊᖅᑯᑎᒋᔭᖏᓐᓂᒃ)
    
Which, in Latin is: 

    (aqqutigilugit OR apqutit OR apqutinga OR apqusiurunnariaksanginnik OR aqqutigijanginnik)

  - Notice how it's hard for a non-native speaker to tell if 
    those expansions are about the same "concept" as the original 
   word.
-- The search finds 24 hits
-- Click on a hit and search inside it for the root of all those words

## Morpheme Dictionary

- Examples:
  - siuq
    - Finds a single morpheme, and all word examples are good.
  - gaq 
    - Finds two morphemes: 'gaq/1vn', 'gaq/2vv'

    
- For each example:
  - Enter the morpheme
  - Browse the list of example words
  - Click on an example word and show
    - The gist
    - en-iu examples extracted from the hansard  
    
      
## Reading Assistant

- COPY AND PASTE CONTENT
  - Copy some content from the page: https://www.gov.nu.ca/
  - Show the UNILINGUAL gist with clickable words
  - Good words to show:
    - ????  

- Enter this URL 
     https://www.gov.nu.ca/
  - Show the parallel sentences
    - With clickable gist
  - Good words to show:
     - ????
    

