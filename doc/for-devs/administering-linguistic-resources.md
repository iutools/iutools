# Adminstering linguistic resources

IUtools relies on many linguistic resources, namely:
- Parallel corpora in IU-EN (in particular, the hansard)
- Human generated glossaries

The present document explains how to modify/expand those resources.

## _iutools-data_ repository

All linguistic resources are located in the _iutoos-data_ repository
which you can get from:

     https://dagshub.com/iutools/iutools-data
     
On your local IUTools installation, it should have been cloned 
to the location specified by the following config (in your _org_iutools.properties_ file):

    org.iutools.datapath, 

All human generated glossaries are in directory

    iutoools-data/data/glossaries

## Managing glossaries
    
To add a new glossary, just create a new JSON file in _iutoools-data/data/glossaries_.

Use existing JSON files as a model. Make sure you provide the source and possibly 
reference for each glossary entry.

To modify an existing glossary, just edit the corresponding file.

Note that you need to restart Tomcat for these changes to take effect in the web apps.
For the Command Line Interface, the changes will take effect the next time you invoke 
a command.

## Managing Translation Memory files

All TM files are under _iutools-data/translation-memories_. The files have extension 
_.tm.json_ and use a JSON format designed specifically for IUTools. However you can 
generate these JSON files from TMX format (details below).

### Converting a .tmx file

You can convert a _.tmx_ file to the iutools _.tm.json_ format with the CLI command

    iutools_cli tmx2tmjson
    
### Loading a TM file into the iutools database

Whenever you add a new TM file or modify an existing one, you should load it into 
the iutools database with the following command:

    iutools_cli load_translation_memory --data-file /path/to/the/tmfile.tm.json

Note that this may take a few hours to complete.

## Updating the iutools Word Dictionary 

IUtools has a Word Dictionary (aka Compiled Corpus) which contains information about
all the words found in all of its TMs. This dictionary needs to be recompiled whenever
new word occurences are added to TM files. 

This essentially means you have to recompile the dictionary when:
- You add a new TM file
- You expand an existing TM file to add more content to ti (ex: adding new years 
to the Hansard TM)
- You make changes to the morphological analyzer, which means the decomposition of 
  all words need to be recomputed.

Note however that you do _NOT_ need to recompile the dictionary when you just change 
the word-level alignments for an existing TM (since this does not change anything 
to the word occurences).

### Word Dictionary recompilation process

Compilation of the word dictionary is done in 3 steps:

- Step 1: Compile a list of words from the TM files
- Step 2: Compute the morphological decomposition of each word 
- Step 3: Load the decomposed words into the DB

The first and last step can be carried on a single machine in a
few hours, but Step 2 may require several days if carried out on a single machine.

For that reason, we usually perform that step on a cluster of machines running 
in parallel.

Below are details of each step.

####  Step 1: Compile a list of words from the TM files


   




 



If you acquire a new corpus of parallel IU-EN text, you can add it 


## Evaluating a new version of an existing Translation Memory file

## Recompiling the Translation Memories

