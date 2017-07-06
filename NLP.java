import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.io.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.*;
import edu.stanford.nlp.trees.tregex.*;

import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;

public class NLP {

    private static String encryptLine(String line) {
        // Replace "." with "@@" to avoid confusion on the sentence splitter
        Pattern pattern = Pattern.compile("[a-zA-Z](\\.)[^\\s\\.]");
        while (true) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                line = line.substring(0, matcher.start(1)) + "@@" + line.substring(matcher.end(1), line.length());
            } else {
                break;
            }
        }
        return line;
    }


    private static String processLine(String line, StanfordCoreNLP pipeline) {
        // return string
        String processedString = "";

        // Replace "." with "@"
        line = encryptLine(line);

        // Process the line
        Annotation annotation = new Annotation(line);
        pipeline.annotate(annotation);
        //pipeline.prettyPrint(annotation, System.out);

        // Obtain a list of sentences
        boolean firstSentence = true;
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        // Process one sentence at a time
        for(CoreMap sentence: sentences) {

            // this is the parse tree of the current sentence
            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            //tree.pennPrint();

            // regex for matching
            TregexPattern p1 = TregexPattern.compile("VP << (NP < /NN.?/) < /VB.?/");
            TregexMatcher m1 = p1.matcher(tree);
            TregexPattern p2 = TregexPattern.compile("NP !< PRP [<< VP | $ VP]");
            TregexMatcher m2 = p2.matcher(tree);

            // merge the found sub-trees that are matched by the regex
            Tree mergeTree = null;
            if (m1.find()) {
                mergeTree = m1.getMatch();
                while (m1.find()) {
                    mergeTree = tree.joinNode(mergeTree, m1.getMatch());
                }
            }
            if (m2.find()) {
                if (mergeTree == null) {
                    mergeTree = m2.getMatch();
                } else {
                    mergeTree = tree.joinNode(mergeTree, m2.getMatch());
                }
                while (m2.find()) {
                    mergeTree = tree.joinNode(mergeTree, m2.getMatch());
                }
            }

            if (mergeTree == null) {
                // Sentence does not satisfy the NP-VP patterns
                continue;
            }

            // Obtain the sentence on the merged tree from the leaf nodes
            List<Tree> listOfLeaves = new ArrayList<Tree>();
            listOfLeaves = mergeTree.getLeaves();
            //mergeTree.pennPrint();
            //System.out.println(listOfLeaves);

            // Discard sentence if it is too short
            if (listOfLeaves.size() < 3) {
                continue;
            }

            //String sentenceString = sentence.toString();
            //System.out.println(sentenceString);
            //System.out.println(line);
            String thisSentence = "";
            for (int i = 0; i < listOfLeaves.size(); i++) {
                //System.out.println(listOfLeaves.get(i).label());
                int begin = Integer.parseInt(((CoreLabel)listOfLeaves.get(i).label()).get(CharacterOffsetBeginAnnotation.class).toString());
                int end = Integer.parseInt(((CoreLabel)listOfLeaves.get(i).label()).get(CharacterOffsetEndAnnotation.class).toString());
                //System.out.println(begin);
                //System.out.println(end);

                if (begin != -1) {
                    thisSentence += line.substring(begin, end);
                    if (i < listOfLeaves.size()-1) {
                        if (line.substring(end, end+1).equals(" ")) {
                            thisSentence += " ";
                        }
                    }
                }
            }
            // First letter in a sentence should be on upper case
            thisSentence = thisSentence.substring(0,1).toUpperCase() + thisSentence.substring(1);


            //String sentenceString = sentence.toString();
            //System.out.println(sentenceString);

            // First sentence doesn't need space appending
            if (firstSentence == false) {
                // Needs a space in front of sentence
                thisSentence = " " + thisSentence;
            } else {
                firstSentence = false;
            }

            // Check if there is an invalid ending character for this sentence and remove it
            // Then append a fullstop.
            String pattern = "[\\?!]$";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(thisSentence);
            if (m.find()) {
                thisSentence = thisSentence.substring(0,thisSentence.length() - 1);
            }

            // Check if there is a valid ending fullstop for this sentence
            pattern = "[\\.]$";
            p = Pattern.compile(pattern);
            m = p.matcher(thisSentence);
            if (!m.find()) {
                thisSentence = thisSentence.substring(0,thisSentence.length()) +
                    ".";
            }

            // Append to processed string
            processedString += thisSentence;

        }

        // Convert encrypted characters back to original
        processedString = decryptLine(processedString);

        return processedString;
    }

    private static String decryptLine(String line) {
        // Replace "@@" with "." for end user presentation
        Pattern pattern = Pattern.compile("[a-zA-Z](@@)[^\\s]");
        while (true) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                line = line.substring(0, matcher.start(1)) + "." + line.substring(matcher.end(1), line.length());
            } else {
                break;
            }
        }
        return line;
    }


    public static String processString (String inputSentence) {

        // Create a StandfordCoreNLP object
        Properties props = new Properties();
        // props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        props.put("annotators", "tokenize, ssplit, pos, parse");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        String returnSentence = processLine(inputSentence, pipeline);
        System.out.println("processed string:");
        System.out.println(returnSentence);
        return returnSentence;
    }

}
