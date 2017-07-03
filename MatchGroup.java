import java.util.Iterator;

import java.io.Serializable;

import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.*;
import java.io.IOException;

import java.util.Set;
import java.util.HashSet;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashSet;

public class MatchGroup implements Serializable {

    HashSet<MatchInstance> masterList = new HashSet<MatchInstance>();
    HashSet<MatchInstance> cloneList = new HashSet<MatchInstance>();
    int matchLength;
    int totalHashValue;

    public MatchGroup (int length) {
        matchLength = length;
    }

    public HashSet<String> dumpTerms () {
        // obtain a list of strings       
        HashSet<String> masterNameSet = new HashSet<String>();
        for (MatchInstance thisMatch : masterList) {
            ArrayList<Statement> listStatements = thisMatch.getStatements();
            for (int i = thisMatch.startIndex; i <= thisMatch.endIndex; i++) {
                Statement s = listStatements.get(i);
                HashSet<String> setWholeString = s.getNameList();

                // break down the strings by camel case
                HashSet<String> setSplittedString = new HashSet<String>();
                for (String str : setWholeString) {
                    Set<String> splitSet = Utilities.splitCamelCaseSet(str);
                    setSplittedString.addAll(splitSet);
                }

                masterNameSet.addAll(setSplittedString);
            }
        }
        return masterNameSet;
    }

    public int getMatchLength() {
        return matchLength;
    }

    public int getMasterNumClonesWithComments() {
        int totalMasterWithComment = 0;
        for (MatchInstance thisMatch : masterList) {
            ArrayList<CommentMap> commentList = thisMatch.getComments();
            if (commentList.size() > 0) {
                totalMasterWithComment++;
            }
        }
        return totalMasterWithComment;
    }

    public int getCloneNumClonesWithComments() {
        int totalCloneWithComment = 0;
        for (MatchInstance thisMatch : cloneList) {
            ArrayList<CommentMap> commentList = thisMatch.getComments();
            if (commentList.size() > 0) {
                totalCloneWithComment++;
            }
        }       
        return totalCloneWithComment;
    }

    public int getMasterSize() {
        return masterList.size();
    }

    public int getCloneSize() {
        return cloneList.size();
    }

    public int getNumberInternalClones() {
        return masterList.size();
    }

    public int getNumberExternalClones() {
        return cloneList.size();
    }

    // adds a match into the matchgroup
    public void addMatch(int mode, String fileName, int startLine, int endLine, 
            ArrayList<Statement> statements, int startIndex, int endIndex, int totalHash) {
        HashSet<MatchInstance> list;

        totalHashValue = totalHash;

        if (mode == 0) {
            // add to master
            list = masterList;
        } else {
            // add to clone
            list = cloneList;
        }
        MatchInstance matchInst = 
            new MatchInstance(fileName, startLine, endLine,
                    statements, startIndex, endIndex);
        list.add(matchInst);

    }

    // mode 0 - master, 1 - clone, 2 - both
    public boolean checkMatchExist(String filePath, int lineStart, int lineEnd, int mode) {

        HashSet<MatchInstance> list;

        if (mode == 2) {
            boolean existMaster = false;
            boolean existClone = false;

            list = masterList;
            MatchInstance matchInstance = new MatchInstance(filePath, lineStart, lineEnd,
                    new ArrayList<Statement>(),  0, 0);
            if (list.contains(matchInstance)) {
                return true;
            }

            list = cloneList;
            matchInstance = new MatchInstance(filePath, lineStart, lineEnd,
                    new ArrayList<Statement>(),  0, 0);
            if (list.contains(matchInstance)) {
                return true;
            } 

            return false;

        } else {
            list = masterList;
            if (mode == 0) {
                // check the master
                list = masterList;
            } else if (mode == 1) {
                // check the clone
                list = cloneList;
            }

            MatchInstance matchInstance = new MatchInstance(filePath, lineStart, lineEnd,
                    new ArrayList<Statement>(),  0, 0);
            if (list.contains(matchInstance)) {
                return true;
            } else {
                return false;
            }
        }
    }

    public void pruneComments(int similarityRange, boolean enableSimilarity, boolean debug) {

        // clone pruning on comments with invalid terms
        for (MatchInstance thisMatch : cloneList) {

            // analyze for invalid terms or too short
            ArrayList<CommentMap> commentMapList = thisMatch.getComments();
            for (CommentMap cMap : commentMapList) {
                String comment = cMap.comment;
                boolean result1 = Analyze.containInvalidTerms(comment, debug);
				boolean result2 = Analyze.checkNumberTerms(comment, 3, debug);
                boolean result3 = Analyze.checkExistNumbers(comment, debug);
                if (result1 == true && result2 == true && result3 == true) {
                    // discard the whole comment list if any is bad
                    // by replacing the list with an empty one
                    commentMapList.remove(cMap);
                }
            }
        }

        // code artifact detection
        //boolean enableArtifactDetection = true;
        //if (enableArtifactDetection) {
        //    Analyze.codeArtifactDetection(masterList, cloneList);
        //}

        // text similarity
        if (enableSimilarity) {
            if (debug) {
                System.out.println("Similarity enabled");
            }
            Analyze.textSimilarity(masterList, cloneList, similarityRange, debug);
        } else {
            System.out.println("Similarity disabled");
        }


    }

    private ArrayList<CommentMap> removeInline(ArrayList<CommentMap> commentList, 
            String filePath) {

        ArrayList<CommentMap> newList = new ArrayList<CommentMap>();

        Pattern pattern = Pattern.compile("^[\\s\t]*\\/\\/.+$");

        for (int i = 0; i < commentList.size(); i++) {
            CommentMap cMap = commentList.get(i);

            int startLine = cMap.startLine;
            int endLine = cMap.endLine;

            if (startLine == endLine && cMap.commentType == 1) {
                BufferedReader reader = null;
                try {
                    File file = new File(filePath);
                    reader = new BufferedReader(new FileReader(file));
                    int currentLine = 1;
                    String line = reader.readLine();
                    while (line != null) {
                        if (currentLine == startLine) {
                            Matcher matcher = pattern.matcher(line);
                            if (matcher.find()) {
                                newList.add(cMap);
                            }
                        }
                        line = reader.readLine();
                        currentLine++;
                    }
                } catch (Exception e) {
                    System.out.println("Error inside remove inline\n" + e);
                    System.exit(0);
                } finally {
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (IOException e) {
                        System.out.println("Error in removeInline()");
                    }
                }
            } else {
                newList.add(cMap);
            }

        }
        return newList;
    }

    // mode is ascending vs desending
    private static HashMap<String, Integer> sortByComparator(HashMap<String, Integer> unsortMap, int mode)
    {

        List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<String, Integer>>()
                {
                public int compare(Entry<String, Integer> o1,
                        Entry<String, Integer> o2)
                {
                if (mode == 0){ 
                return o2.getValue().compareTo(o1.getValue());
                } else {
                return o1.getValue().compareTo(o2.getValue());
                }
                }
                });

        // Maintaining insertion order with the help of LinkedList
        HashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Entry<String, Integer> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    public void printRankedComments(PrintWriter writer) {

        // obtain a list of all the possible code comments from each clone
        HashMap<String, Integer> listComments = new HashMap<String, Integer>();

        for (MatchInstance thisMatch : cloneList) {

            ArrayList<CommentMap> commentList = thisMatch.getComments();
            ArrayList<Integer> scoreList = thisMatch.getScores();

            for (int index = 0; index < commentList.size(); index++) {
                CommentMap cMap = commentList.get(index);
                Integer thisScore = scoreList.get(index);
                listComments.put(cMap.comment, thisScore.intValue());
            }
        }

        // rank it based on the size
        HashMap<String, Integer> sortedList = sortByComparator(listComments, 0);
        Iterator it  = sortedList.entrySet().iterator();
        int displayedNum = 0;
        int lastScore = -1;
        HashMap<String, Integer> listString = new HashMap<String, Integer>();
        while (it.hasNext()) {
            HashMap.Entry pairs = (HashMap.Entry) it.next();

            if (lastScore != (Integer)pairs.getValue()) {
                String str = (String) pairs.getKey();

                // count the number of words inside the string
                Pattern pattern = Pattern.compile("\\w+");
                Matcher matcher = pattern.matcher(str);
                int count = 0;
                while (matcher.find()) {
                    count++;
                }

                listString.put((String)pairs.getKey(), (Integer) count);
                //System.out.println(pairs.getKey() + " = " + pairs.getValue());
                displayedNum++;
            }

            it.remove();

            if (displayedNum == 3) {
                break;
            }
        }

        // display the results
        HashMap<String, Integer> sortedString = sortByComparator(listString, 1);
        it = sortedString.entrySet().iterator();
        displayedNum = 1;
        writer.println("Ranked Result:");
        while (it.hasNext()) {
            HashMap.Entry pairs = (HashMap.Entry) it.next();

            writer.println(displayedNum + ". (size " + pairs.getValue() + ")");
            writer.println(pairs.getKey());

            it.remove();
            displayedNum++;
        }
        writer.println("----");
    }

    private ArrayList<CommentMap> groupNormalizeComment(ArrayList<CommentMap> commentList) {

        ArrayList<CommentMap> newList = new ArrayList<CommentMap>();

        for (int i = 0; i < commentList.size(); i++) {

            CommentMap cMap1 = commentList.get(i);
            int startLine = cMap1.startLine;
            int endLine = cMap1.endLine;

            if (cMap1.commentType == 1) {

                // do not group line comments because it is used to break
                // multiple sentences

                String comment = cMap1.comment.substring(2);

                // try extend it
                while (i + 1 < commentList.size()) {
                    CommentMap cMap2 = commentList.get(i + 1);
                    if (cMap2.startLine == endLine + 1 && cMap2.commentType == 1) {
                        // extend it
                        comment = comment + " " + cMap2.comment.substring(2);

                        // update
                        endLine = cMap2.startLine;
                    } else {
                        break;
                    }
                    i++;
                }

                // remove start of line spaces
                String startSpace = "^\\s*";
                comment = comment.replaceAll(startSpace, "");

                CommentMap cMapNew = new CommentMap(comment, startLine, endLine, 1);
                newList.add(cMapNew);

            } else {

                int commentSize = cMap1.comment.length();
                String comment = cMap1.comment.substring(0, commentSize-2);

                // remove leading opener
                String leadingOpener = "^/\\*(\\*)?\\s*";
                comment = comment.replaceAll(leadingOpener, "");

                // remove start of line annotations
                String pattern = "\\s*\\*\\s*";
                comment = comment.replaceAll(pattern, " ");

                // remove start of line spaces
                String startSpace = "^\\s*";
                comment = comment.replaceAll(startSpace, "");

                // condence the spaces
                String condenceSpace = "\\s{2,}";
                comment = comment.replaceAll(condenceSpace, " ");

                CommentMap cMapNew = new CommentMap(comment, startLine, endLine, 1);
                newList.add(cMapNew);
            }

        }

        return newList;

    }

    public void mapCode2Comment() {

        for (MatchInstance thisMatch : masterList) {
            String filePath = thisMatch.fileName;
            boolean isJavaFile = Utilities.checkIsJava(filePath);
            int startLine = thisMatch.startLine;
            int endLine = thisMatch.endLine;

            // get the list of comments associated
            CommentParser cParser = new CommentParser(filePath);
            //ArrayList<CommentMap> commentList = new ArrayList<CommentMap>();
            ArrayList<CommentMap> commentList = cParser.parseComment(filePath, startLine, endLine, 0, isJavaFile);
            if (isJavaFile == true) {
                // source code format
                //cParser.parseComment(filePath, startLine, endLine, 0, true);
                commentList = removeInline(commentList, filePath);
                commentList = groupNormalizeComment(commentList);
            }

            // remove in-line comments
            //commentList = removeInline(commentList, filePath);

            // group the comments
            //commentList = groupNormalizeComment(commentList);

            //thisMatch.setComments(commentList);
			thisMatch.setComments(commentList);
        }

        for (MatchInstance thisMatch : cloneList) {
            String filePath = thisMatch.fileName;
            boolean isJavaFile = Utilities.checkIsJava(filePath);
            int startLine = thisMatch.startLine;
            int endLine = thisMatch.endLine;

            // get the list of comments associated
            CommentParser cParser = new CommentParser(filePath);
            ArrayList<CommentMap> commentList = cParser.parseComment(filePath, startLine, endLine, 0, isJavaFile);
            //for (CommentMap c : commentList) {
            //    String v = c.comment;
            //}
            // remove in-line comments
            if (isJavaFile == true) {
                // source code format
                commentList = removeInline(commentList, filePath);
                commentList = groupNormalizeComment(commentList);
            }

            thisMatch.setComments(commentList);
        }

    }

    public void pruneDuplicateComments(boolean debug) {

        // get unique list of comments from master and clones
        HashSet<String> masterComments = new HashSet<String>();
        for (MatchInstance thisMatch : masterList) {
            ArrayList<CommentMap> commentList = thisMatch.commentList;
            for (CommentMap thisCMap : commentList) {
                masterComments.add(thisCMap.comment);
            }
        }

        for (MatchInstance thisMatch : cloneList) {

            ArrayList<CommentMap> filteredCommentList = new ArrayList<CommentMap>();

            ArrayList<CommentMap> commentList = thisMatch.commentList;
            for (CommentMap thisCMap : commentList) {

                String thisComment = thisCMap.comment;
                if (!masterComments.contains(thisComment)) {
                    filteredCommentList.add(thisCMap);
                } else {
                    if (debug) {
                        System.out.println("Removed dup comment");
                    }
                }
            }

            thisMatch.setComments(filteredCommentList);
        }
    }

    public boolean hasComment() {
        boolean hasComment = false;
        HashSet<String> cloneComments = new HashSet<String>();
        for (MatchInstance thisMatch : cloneList) {
            ArrayList<CommentMap> commentList = thisMatch.commentList;
            if (commentList.size() > 0) {
                return true;
            }
        }

        return false;
    }

    public void printAllMappings(boolean saveEmpty, int matchMode, int printMode,
            String outputDir, PrintWriter writerComment, PrintWriter writerMaster) {
        boolean enableSimilarity = true;

        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir = new File(outputDir);
            dir.mkdir();
        }

        // first print the master list
        for (MatchInstance thisMatch : masterList) {
            try {
                String filePath = thisMatch.fileName;
                int startLine = thisMatch.startLine;
                int endLine = thisMatch.endLine;

                ArrayList<CommentMap> comments = thisMatch.getComments();

                // print header
                String header = filePath + ": " + startLine + "-" + endLine;
                writerComment.println(header);
                writerMaster.println(header);
                header = "Length: " + matchLength;
                writerComment.println(header);
                writerMaster.println(header);

                // print the comment 
                for (CommentMap cMap : comments) {
                    cMap.print(writerComment);
                    cMap.print(writerMaster);
                }

                // Print the code segment
                List<String> encoded = Files.readAllLines(Paths.get(filePath), Charset.defaultCharset());
                for (int lineNum = startLine - 1; lineNum < endLine; lineNum++) {
                    String line = encoded.get(lineNum);
                    String codeStr = "* " + line;
                    writerComment.println(codeStr);
                    writerMaster.println(codeStr);
                }
                String seperator = "----";
                writerComment.println(seperator);
                writerMaster.println(seperator);
            } catch (IOException e) {
                writerComment.println(e);
                writerMaster.println(e);
            }
        }

        if (printMode == 1) {
            // then print the clone list
            int i = 0;
            HashSet<String> masterCommentList = new HashSet<String>();
            for (MatchInstance thisMatch : cloneList) {
                try {

                    String filePath = thisMatch.fileName;
                    int startLine = thisMatch.startLine;
                    int endLine = thisMatch.endLine;

                    ArrayList<CommentMap> comments = thisMatch.getComments();

                    // print header
                    String headerStr = filePath + ": " + startLine + "-" + endLine;
                    writerComment.println(headerStr);
                    writerMaster.println(headerStr);
                    headerStr = "Length: " + matchLength;
                    writerComment.println(headerStr);
                    writerMaster.println(headerStr);

                    // print text similarity terms
                    if (enableSimilarity) {
                        ArrayList<HashSet<String>> similarityTermsLocal = thisMatch.getSimilarityLocal();
                        String simStr = "local sim: " + similarityTermsLocal;
                        writerComment.println(simStr);
                        writerMaster.println(simStr);

                        ArrayList<HashSet<String>> similarityTermsGlobal = thisMatch.getSimilarityGlobal();
                        simStr = "global sim: " + similarityTermsGlobal;
                        writerComment.println(simStr);
                        writerMaster.println(simStr);

                        ArrayList<HashSet<String>> similarityTermsVariable = thisMatch.getSimilarityVariable();
                        simStr = "variable sim: " + similarityTermsVariable;
                        writerComment.println(simStr);
                        writerMaster.println(simStr);
                    }

                    // print the comment 
                    for (CommentMap cMap : comments) {
                        // print the artifacts
                        if (cMap.artifactSet != null) {
                            String commentStr = cMap.artifactSet + " ";
                            writerComment.print(commentStr);
                            writerMaster.print(commentStr);
                        }
                        cMap.print(writerComment);
                        cMap.print(writerMaster);
                        masterCommentList.add(cMap.comment);
                    }

                    // Print the code segment
                    List<String> encoded = Files.readAllLines(Paths.get(filePath), Charset.defaultCharset());
                    for (int lineNum = startLine - 1; lineNum < endLine; lineNum++) {
                        String line = encoded.get(lineNum);
                        if (matchMode == 0) {
                            String str = "< " + line;
                            writerComment.println(str);
                            writerMaster.println(str);
                        } else {
                            String str = "* " + line;
                            writerComment.println(str);
                            writerMaster.println(str);
                        }
                    }
                    String str = "----";
                    writerComment.println(str);
                    writerMaster.println(str);
                } catch (IOException e) {
                    writerComment.println(e);
                    writerMaster.println(e);
                }
                i++;
            }

            printAllComments(masterCommentList, writerComment, writerMaster);
        }
    }

    private void printAllComments(HashSet<String> masterCommentList, PrintWriter writerComment, PrintWriter writerMaster) {
        String str = "Comments (size " + masterCommentList.size() + "):";
        writerComment.println(str);
        writerMaster.println(str);
        int index = 1;
        for (String thisComment : masterCommentList) {
            str = index + ".\n" + thisComment;
            writerComment.println(str);
            writerMaster.println(str);
            index++;
        }
        str = "----";
        writerComment.println(str);
        writerMaster.println(str);

    }

    public int getHashValue() {
        return totalHashValue;
    }

    public void findClones(HashSet<String> inputTerms, String outputDir) {

        HashSet<String> matchGroupTerms = dumpTerms();
        boolean allExist = true;
        for (String term : inputTerms) {
            if (matchGroupTerms.contains(term) == false) {
                allExist = false;
                break;
            }
        }

        if (allExist) {
            mapCode2Comment();
            try {
                PrintWriter writerComment = new PrintWriter(outputDir + Integer.toString(1) + "-full", "UTF-8");
                PrintWriter writerMaster = new PrintWriter("master.txt", "UTF-8");
                printAllMappings(true, 1, 1, outputDir, writerComment, writerMaster);
                writerComment.close();
                writerMaster.close();
            } catch (Exception e) {
                System.out.println("Error in MatchGroup.java");
            }
        }
    }

}
