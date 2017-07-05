import re
from HTMLParser import HTMLParser
import time
from xml.etree import ElementTree
from sets import Set

# Clean sentence
def cleanSentence(sentences):

    # No need to process empty sentences
    #if sentences == "":
    #    return ""

    # Replace the "i" with "I"
    sentences = re.sub('\\bi\\b', "I", sentences)

    # Replace "..." with "."
    sentences = re.sub('\s*\.{2,}', ".", sentences)

    # Replace ":" with "."
    sentences = re.sub('\s*\:', ".", sentences)

    # Replace "n't" with "not"
    # http://www.learnenglish.de/grammar/shortforms.html
    sentences = re.sub("can't\\b", "cannot", sentences)
    sentences = re.sub("won't\\b", "will not", sentences)
    sentences = re.sub("shan't\\b", "shall not", sentences)
    sentences = re.sub("n't\\b", " not", sentences)
    # Replace "'ll" with " will"
    sentences = re.sub("'ll\\b", " will", sentences)
    # Replace "I'm" with "I am"
    sentences = re.sub("I'm\\b", "I am", sentences)
    # Replace "'ve" with " have
    sentences = re.sub("'ve\\b", " have", sentences)

    # Ensure last word of the paragraph has a valid ending character
    pattern = re.compile('[a-zA-Z;()/]$')
    if pattern.search(sentences):
        sentences = sentences + "."

    # Split sentences and make first word in each sentence capitalize
    # Split sentence using space after the dot
    listOfSentences = re.findall('(.+?(\.|\?|\!)(\s|$)+)', sentences)
    if listOfSentences:
        sentences = ""
        for thisSentence in listOfSentences:
            # Detect for URLs in sentence and remove them
            # Note we substituded ":" with "." previously
            patternURL = re.compile('(http\.|https\.)')
            if patternURL.search(thisSentence[0]):
                continue

            # Make first letter upper case and append to list
            sentences = sentences + thisSentence[0][0:1].upper() + thisSentence[0][1:]

    return sentences

tagNeed = ["java", "android"]
outputFolder = "./output/"
inputFileName = "Posts.xml"

# set
questionListID = Set([])
# dictionary
questionListTitle = {}
start_time = time.time()
print "Processing:"
context = ElementTree.iterparse(inputFileName, events=('start', 'end', 'start-ns', 'end-ns'))
context = iter(context)
event, root = context.next()
totalNumMappings = 0
for event, node in context:
    if event == "end" and node.tag == "row":
        postType = node.attrib.get('PostTypeId')

        if postType == "1":

            # analyze the tag
            tagList = node.attrib.get('Tags')
            rex = re.compile("<(.+?)>")
            parsedTagList = rex.findall(tagList)
            foundTag = False
            for tagRequest in tagNeed:
                for tagAssigned in parsedTagList:
                    if tagRequest == tagAssigned:
                        foundTag = True
                        break
                if foundTag == True:
                    break
    
            if foundTag:

		score = node.attrib.get('Score')
		score = int(score)

                if score > 0:

		    answerCount = node.attrib.get('AnswerCount')
		    answerCount = int(answerCount)

		    if answerCount > 0:

			id = node.attrib.get('Id')
			title = node.attrib.get('Title')

			intID = int(id)
			questionListID.add(intID)
			questionListTitle[intID] = title

			print id + "\r",

        elif postType == '2':

            parentID = node.attrib.get('ParentId')
            parentID = int(parentID)

            if parentID in questionListID:

		score = node.attrib.get('Score')
		score = int(score)

                if score > 0:
                    id = node.attrib.get('Id')
                    bodyContent = node.attrib.get('Body')

                    # an answer
                    # get all the code snippets
                    h = HTMLParser()
                    bodyContent = h.unescape(bodyContent)
                    #print bodyContent


                    # remove code tag
                    rex = re.compile(r'(<code>([^\s]*?)</code>)')
                    listArtifacts = rex.findall(bodyContent)
                    for artifact in listArtifacts:
                        #print "ffff"
                        #print artifact
                        bodyContent = bodyContent.replace(artifact[0], artifact[1])

                    # remove href tag
                    rex = re.compile(r'(<a href=".+?>(.*?)</a>)')
                    listArtifacts = rex.findall(bodyContent)
                    for artifact in listArtifacts:
                        #print artifact
                        bodyContent = bodyContent.replace(artifact[0], artifact[1])

                    # locate comment code pairs
                    rex = re.compile(r'<p>([^\n]*?)</p>\s\s<pre><code>(.*?)</code></pre>',re.S|re.M)
                    listMapping = rex.findall(bodyContent)
                    counter = 0
                    for mapping in listMapping:
                        mappingSentence = mapping[0]
                        mappingCode = mapping[1]
                        numLines = mappingCode.count("\n")
                        numStatements = mappingCode.count(";")
                        if numLines >= 3 and numLines <= 10 and numStatements >= 3 and "(" in mappingCode:
                            # remove tags
                            rex2 = re.compile("</?[a-z]+?>")
                            listTags = rex2.findall(mappingSentence)
                            for tag in listTags:
                                #print artifact
                                mappingSentence = mappingSentence.replace(tag, "")

                            # get the title of post
                            mappingTitle = questionListTitle[parentID]

                            # clean sentence
                            mappingTitle = cleanSentence(mappingTitle)
                            mappingSentence = cleanSentence(mappingSentence)

                            f = open(outputFolder + str(parentID) + "-" + id + "-" + str(counter) + ".autocom", 'w')
                            f.write('//' + mappingTitle.encode('utf-8') + '\n')
                            f.write('//' + mappingSentence.encode('utf-8') + "\n")
                            f.write(mappingCode.encode('utf-8'))
                            f.close()

                            counter = counter + 1
                            totalNumMappings = totalNumMappings + 1

        root.clear()

print "Largest question number"
print max(questionList)
elapsed_time = time.time() - start_time
print "Execution time: " + str(elapsed_time / 60) + "min"

print "Total Number of Mappings: " + str(totalNumMappings)
