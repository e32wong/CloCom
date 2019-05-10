# Introduction

This research project proposes a new automatic comment generation approach,
which mines comments from a large programming Question and Answer (Q&A) site.
The tool generates code comments automatically by 
analyzing existing software repositories. 
It applies code clone detection techniques to discover 
similar code segments and use the comments from some code segments 
to describe the other similar code segments. 

It is a hybrid technique that can generate code comments
by mining (1) StackOverflow and (2) open source projects.

# Installation

## Dependencies

sudo apt-get install openjdk-8-jdk

Place the following tars under the folder, lib2,
which can be obtained from Stanford's website
(https://stanfordnlp.github.io/CoreNLP/): 

stanford-corenlp-3.8.0.jar  
stanford-corenlp-3.8.0-models.jar

```lib``` and ```lib2``` contains all the library dependencies that
are required to run the tool.

## Running the Tool

The configuration is done using an XML file.
Configure "config.xml"'s <database> and <project> tag
with the appropriate folder path.
The folders should contain Java source code files.
The 'database' can be a StackOverflow database
and the 'project' can be any Java project's source code.

Code inside the <project> tag represents the target project
that you want to generate comments for.

Code inside the <database> tag represents the database.
CloCom will extract code comments from this folder for
the code inside the <project> folder.

See the "config" folder for xml examples and 
modify the parameters to suit your needs.

### Compilation

Type 'make' to compile.

### Execute

```./cloneDigger.sh config.xml```

The output is a list of clones from the database path and 
the respective generated comment.

Please note the XML parameters for ```config.xml```:

databaseFormat - 0 for standard Java source code, 1 for autocomment format

- minNumLines
  - number of statements a clone should have
- matchAlgorithm 
  - 1 for gapped, 0 for non-gapped
- matchMode 
  - 1 for between comparison (loads files in the project path into memory), 0 for full mesh
- gapSize 
  - set this for gapped comparision
- meshBlockSize 
  - number of source code files to load into memory for full mesh
- numberThreads 
  - 0 to disable multithreading, else specify the number of cores

- debug 
  - turn on debug statements
- removeEmpty 
  - remove the display of clones that doesn't have a comment
- forceRetokenization 
  - set true if we want to retokenize the database directory, else it will only tokenize as needed
- loadDatabaseFilePaths 
  - use the existing cached list of files

# Publication

Please see the ```research``` folder for PDF version of the publication.

- Edmund Wong, Jinqiu Yang, and Lin Tan, AutoComment: Mining Question and Answer Sites for Automatic Comment Generation, International Conference on Automated Software Engineering (ASE) 2013

- Edmund Wong, Taiyue Liu, Lin Tan, CloCom: Mining Existing Source Code for Automatic Comment Generation, International Conference on Software Analysis, Evolution, and Reengineering (SANER) 2015

Please use commit
f89b780bac5ea7322633550c29fffd3c199e1f2e
if you would like to reproduce the results of
CloCom's SANER conference paper.

We no longer support the ASE codebase because it had evolved greatly since 2013.
CloCom is the successer of AutoComment.

## Data

We are thankful to [StackOverflow](https://stackoverflow.com/)
for providing a data dump of 
their website's public data.
Please refer to the Stack Overflow Creative Commons Data Dump for 
further details on how to obtain the data.
We utilized the data dump that was published on Match 2017.
You will have to download "stackoverflow.com-Posts.7z" to obtain "Posts.xml"
if you would like to build a mapping database.

## Mapping Database

We provide a code-comment mapping
 data for the Java and Android tag under the ```database``` folder ```javaAndroidDB.tar.gz```.

It was generated using the newer Match 2017 StackOverflow database with the following criteria:

- Must have a least 3 lines in the code segment
- Must have the ```java``` tag
- Code mappings are only extracted from the answer of the post
- Question and answer must have a score larger than 0
- No NLP parse tree editing other than basic sentence refinements. The reason that the NLP is not processed in this database is because we recently moved the NLP component into the code clone detection tool. You can run it yourself using the Java class (NLP.java).
- Each mapping is named as "[questionID]-[answerID]-[codeSegmentNumber].autocom"

## User Study

### Automatically Generated Comments

Here is the format of the results from the CloCom tool:

1. Match number in this project (excluded matches that contain no comments)
   - xx+yy is the grouping of the source code, together it represents a clone group
   - xx represents the number of code from target project
   - yy represents the number of code from Stack Overflow
2. One or more code from target project
   - File name of the target project's source code and the matched line numbers (xx-xx)
   - Length represents the number of matched statements
   - Source code that had been matched
3. One or more code from Stack Overflow
   - File name of the Stack Overflow database entry and the matched line numbers (xx-xx)
     - The code segment ID consists of two parts:
       1. StackOverflow Answer ID
       2. Index number of the code snippet within the answer
          (there can be more than one code snippet within an answer)
   - Length represents the number of matched statements
   - Text similarity terms that had been matched (local, global and variable)
   - Source code that had been matched
4. List of comment candidates from all sources 
   - size x, where x represents the total number of candidates
5. Ranked output from all sources
   - Ranking is based on the text similarity score

The output of AutoComment for the 16 evaluated Java projects are under the ```research``` folder,
which are then used for the user study questionnaire.

### Questionnaire

Below are two links that contains the full digital questionnaire that we presented to the users:

[Google Form - Group 1](https://docs.google.com/forms/d/e/1FAIpQLSf4pBKisdtIcbgW3MxDpH4XLeiCUmNYc9N64srnW__MH0_8uQ/viewform?usp=sf_link)

[Google Form - Group 2](https://docs.google.com/forms/d/e/1FAIpQLSddELKFj0yzc88VeY__O6G08yyTHdhF_NR50Vnpe-Wtjj-g8w/viewform?usp=sf_link)

Raw CSV Dump of the results are located under the ```research/output``` folder.

There are two files ```group1.csv``` and ```group2.csv``` which
corresponds to the two sets of questions.
Evaulation was performed on
20 participants where each
person received 15 questions (12 from AutoComment
and 3 from previous work SumSlice).

Summarized Results: [Google Sheet Link](https://docs.google.com/spreadsheets/d/1G59FQ8CtKJJAmN6ApZiw5pGDoqGAOKfrvSycSwoA_7I/edit?usp=sharing)

### Comparison Against Previous Work

AutoComment is compared against the work (SumSlice) 
from Breno Dantas Cruz, Paul "Will" McBurney, and Collin McMillan from TSE 2015.
SumSlice's output and intermediate files on the evaluated Java project, NanoXML,
are placed under the ```research/output``` folder.

- Comments (nanoXML.txt)
- SWUM (NanoXML.out)
- PageRank (ND_PageRankFormatter.txt)
- XML SumGen (nd_xmlsumgen.xml)

### Study of Developer Written Comments

We also provide the list of the extracted comments for the study of developer comments
under the ```research/output/developer``` folder.
The original source code files are located on [Jajuk's website](https://sourceforge.net/projects/jajuk/)
under: ```/jajuk-src-1.10.3/src/main/java/org/qdwizard/```

We studied the following randomly selected Java files. 
The generated comments and the original source code had been provided.

- ActionsPanel
- ClearPoint
- Header
- Langpack
- Screen
- ScreenState
- Wizard

The classification of the comments
(type one and type two) are under
```type1-comment.txt``` and ```type2-comment.txt```.




