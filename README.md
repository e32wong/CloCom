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

## Requirements

sudo apt-get install openjdk-8-jdk

Place the following tars under the folder, lib2,
which can be obtained from Stanford's website
(https://stanfordnlp.github.io/CoreNLP/): 

stanford-corenlp-3.8.0.jar  
stanford-corenlp-3.8.0-models.jar

```lib``` and ```lib2``` contains all the library dependencies that
are required to run the tool.

## How to Run

- Configure "config.xml"'s <database> and <project> tag
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

### Execute with the provided shell script

```./cloneDigger.sh config.xml```

The output is a list of clones from the database path and 
the respective generated comment.

Please note the XML parameters for ```config.xml```:

databaseFormat - 0 for standard Java source code, 1 for autocomment format

minNumLines - number of statements a clone should have
matchAlgorithm - 1 for gapped, 0 for non-gapped
matchMode - 1 for between comparison (loads files in the project path into memory), 0 for full mesh
gapSize - set this for gapped comparision
meshBlockSize - number of source code files to load into memory for full mesh
numberThreads - 0 to disable multithreading, else specify the number of cores

debug - turn on debug statements
removeEmpty - remove the display of clones that doesn't have a comment
forceRetokenization - set true if we want to retokenize the database directory, else it will only tokenize as needed
loadDatabaseFilePaths - use the existing cached list of files


# Publication

Edmund Wong, Taiyue Liu, Lin Tan, CloCom: Mining Existing Source Code for Automatic Comment Generation", International Conference on Software Analysis, Evolution, and Reengineering (SANER) 2015

Please use commit
f89b780bac5ea7322633550c29fffd3c199e1f2e
if you would like to reproduce the results of
CloCom's SANER conference paper.

# Data Source

We are thankful to [StackOverflow](https://stackoverflow.com/)
for providing a data dump of 
their website's public data.
Please refer to the Stack Overflow Creative Commons Data Dump for 
further details on how to obtain the data.
We utilized the data dump that was published on Match 2017.
You will have to download "stackoverflow.com-Posts.7z" to obtain "Posts.xml"
if you would like to build a mapping database.

# Mapping Database

We provide a mapping data for the Java and Android tag under the ```database``` folder ```javaAndroidDB.tar.gz```.

It was generated using the newer Match 2017 StackOverflow database with the following criteria:

- must have a least 3 lines in the code segment
- must have the ```java``` tag
- code mappings are only extracted from the answer of the post
- question and answer must have a score larger than 0
- no NLP parse tree editing other than basic sentence refinements (see reason below as to why it isn't being applied in this tar file)
- each mapping is named as "[questionID]-[answerID]-[codeSegmentNumber].autocom"

# Evaluation

Below are two links contains the full questionnaire that we presented to the users:

[Google Form - Group 1](https://docs.google.com/forms/d/e/1FAIpQLSf4pBKisdtIcbgW3MxDpH4XLeiCUmNYc9N64srnW__MH0_8uQ/viewform?usp=sf_link)
[Google Form - Group 2](https://docs.google.com/forms/d/e/1FAIpQLSddELKFj0yzc88VeY__O6G08yyTHdhF_NR50Vnpe-Wtjj-g8w/viewform?usp=sf_link)






