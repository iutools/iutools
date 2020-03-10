#Demo plan for Inuktut tools ==

## Preparation

- In a terminal, start the following iutools command
  - iutools_console gist
  - iutools_console segment_iu
- Open the inuktitut computing transcoder app
    http://inuktitutcomputing.ca/Transcoder/index.php?lang=en

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
-- Notice how it
   
NEXT STEPS
- Need to sit down with an inuit and try it and have a 
  discussion about specific cases.

## Spell checker
- Do a google search for: 
    site:gov.nu.ca
- Choose a page randomly on that list, switch language to Inuktut
  and select a medium sized paragraph
- Transliterate it for covenience, then enter the Latin equivalent 
  into the spell checker.
- Find an underlined word and show the alternatives suggested
  - Let Benoit discuss those alternatives
- Talk about its accuracy

TODO: ALAIN TO ENTER STATS HERE

- NEXT STEPS
  - Better sorting of the alternatives using an edit distance measure 
    that is aware of "plausible" spelling mistakes (ex: 'i' --> 'ii'
    is more plausible than 'i' --> 'im') 
  - Sit down with an inuit to try it and discuss it 
  
## Morpheme search

- Examples:
  - siuq
    - Finds a single morpheme, and all word examples are good.
  - gaq 
    - Finds two morphemes: 'gaq/1vn', 'gaq/2vv'
    - Words found for 'gaq/1vn' are GOOD
    - Words found for 'gaq/2vv' are MOSTLY BAD
      - Reason: First analysis produced by the morphological 
        analyzer is actually wrong (it emits a gaq/2vv where it
        should not). But the correct analysis is in the list of 
        possible analyses.
- ngaaq
    - Finds two morphemes: 'ngaaq/1vn', 'ngaaq/2vv'
    - Words found for 'ngaaq/1vn' are GOOD
    - Words found for 'ngaaq/2vv' are MOSTLY BAD
      - Reason: First analysis produced by the morphological 
        analyzer is actually wrong (it emits a gaq/2vv where it
        should not). But the correct analysis is in the list of 
        possible analyses.
  
  - 
  
- For each example:
  - Enter the morheme
  - Browse the list of example words
  - Click on an example word and show
    - The gist
    - en-iu examples extracted from the hansard  
  
  