import java.util.Iterator;

import java.io.Serializable;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.File;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.nio.file.Paths;
import java.nio.file.Files;
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

public class MatchGroup implements Serializable {

    HashSet<MatchInstance> masterList = new HashSet<MatchInstance>();
    HashSet<MatchInstance> cloneList = new HashSet<MatchInstance>();
    int matchLength;

    public MatchGroup (int length) {
        matchLength = length;
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
            ArrayList<Statement> statements, int startIndex, int endIndex) {

        HashSet<MatchInstance> list;

        if (mode == 0) {
            // check the master
            list = masterList;
        } else {
            // check the clone
            list = cloneList;
        }

        MatchInstance matchInst = 
            new MatchInstance(fileName, startLine, endLine,
                    statements, startIndex, endIndex);
        list.add(matchInst);

    }

    // mode 0 - master, 1 - clone
    public boolean checkMatchExist(String filePath, int lineStart, int lineEnd, int mode) {

        HashSet<MatchInstance> list;

        if (mode == 0) {
            // check the master
            list = masterList;
        } else {
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

    public void pruneComments(int similarityRange, boolean enableSimilarity) {

        // clone pruning
        for (MatchInstance thisMatch : cloneList) {

            // analyze for invalid terms
            ArrayList<CommentMap> commentMapList = thisMatch.getComments();
            for (CommentMap cMap : commentMapList) {
                String comment = cMap.comment;
                boolean result = Analyze.containInvalidTerms(comment);
                if (result == true) {
                    // discard the whole comment list if any is bad
                    // by replacing the list with an empty one
                    commentMapList.remove(cMap);
                    /*ArrayList<CommentMap> emptyCommentList =
                        new ArrayList<CommentMap>();
                    thisMatch.setComments(emptyCommentList);*/
                    break;
                }
            }
        }

        // code artifact detection
        boolean enableArtifactDetection = true;
        if (enableArtifactDetection) {
            Analyze.codeArtifactDetection(masterList, cloneList);
        }

        // text similarity
        if (enableSimilarity) {
            Analyze.textSimilarity(masterList, cloneList, similarityRange);
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
                try {
                    File file = new File(filePath);
                    BufferedReader reader = new BufferedReader(new FileReader(file));
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
                }
            } else {
                newList.add(cMap);
            }

        }
        return newList;
    }

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

    public void printRankedComments() {

        System.out.println("----");

        // obtain a list of all the possible code comments from each clone
        HashMap<String, Integer> listComments = new HashMap<String, Integer>();

        for (MatchInstance thisMatch : cloneList) {
            
            ArrayList<CommentMap> commentList = thisMatch.getComments();
            
            for (int index = 0; index < commentList.size(); index++) {
                CommentMap cMap = commentList.get(index);
                HashSet<String> termsLocal = thisMatch.getSimilarityLocal().get(index);
                HashSet<String> termsGlobal = thisMatch.getSimilarityGlobal().get(index);

                listComments.put(cMap.comment, termsGlobal.size() + termsLocal.size());
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

        HashMap<String, Integer> sortedString = sortByComparator(listString, 1);
        it = sortedString.entrySet().iterator();
        displayedNum = 1;
        System.out.println("Ranked Result:");
        while (it.hasNext()) {
            HashMap.Entry pairs = (HashMap.Entry) it.next();

            System.out.println(displayedNum + ". (size " + pairs.getValue() + ")");
            System.out.println(pairs.getKey());
            
            it.remove();
            displayedNum++;
        }
        System.out.println("----");
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
            int startLine = thisMatch.startLine;
            int endLine = thisMatch.endLine;

            // get the list of comments associated
            CommentParser cParser = new CommentParser(filePath);
            ArrayList<CommentMap> commentList = cParser.parseComment(filePath, startLine, endLine, 0);

            // remove in-line comments
            commentList = removeInline(commentList, filePath);

            // group the comments
            commentList = groupNormalizeComment(commentList);

            thisMatch.setComments(commentList);
        }

        for (MatchInstance thisMatch : cloneList) {
            String filePath = thisMatch.fileName;
            int startLine = thisMatch.startLine;
            int endLine = thisMatch.endLine;

            // get the list of comments associated
            CommentParser cParser = new CommentParser(filePath);
            ArrayList<CommentMap> commentList = cParser.parseComment(filePath, startLine, endLine, 0);

            // remove in-line comments
            commentList = removeInline(commentList, filePath);

            // group the comments
            commentList = groupNormalizeComment(commentList);

            thisMatch.setComments(commentList);
        }

    }

    public void pruneDuplicateComments() {

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

    public void printAllMappings(boolean removeEmpty) {
        boolean enableSimilarity = true;

        // first print the master list
        for (MatchInstance thisMatch : masterList) {
            try {
                String filePath = thisMatch.fileName;
                int startLine = thisMatch.startLine;
                int endLine = thisMatch.endLine;

                ArrayList<CommentMap> comments = thisMatch.getComments();

                // print header
                System.out.println(filePath + ": " + startLine + "-" + endLine);
                System.out.format("Length: %d \n", matchLength);

                // print the comment 
                for (CommentMap cMap : comments) {
                    cMap.print();
                }
                
                // Print the code segment
                List<String> encoded = Files.readAllLines(Paths.get(filePath), Charset.defaultCharset());
                for (int lineNum = startLine - 1; lineNum < endLine; lineNum++) {
                    String line = encoded.get(lineNum);
                    System.out.println("* " + line);
                }
                System.out.println("----");
            } catch (IOException e) {
                System.out.println(e);
            }           
        }

        // then print the clone list
        int i = 0;
        HashSet<String> masterCommentList = new HashSet<String>();
        for (MatchInstance thisMatch : cloneList) {
            try {

                String filePath = thisMatch.fileName;
                int startLine = thisMatch.startLine;
                int endLine = thisMatch.endLine;

                ArrayList<CommentMap> comments = thisMatch.getComments();
                if (comments.size() > 0 || removeEmpty == false) {

                    // print header
                    System.out.println(filePath + ": " + startLine + "-" + endLine);
                    System.out.format("Length: %d \n", matchLength);

                    // print text similarity terms
                    if (enableSimilarity) {
                        ArrayList<HashSet<String>> similarityTermsLocal = thisMatch.getSimilarityLocal();
                        System.out.println("local sim: " + similarityTermsLocal);

                        ArrayList<HashSet<String>> similarityTermsGlobal = thisMatch.getSimilarityGlobal();
                        System.out.println("global sim: " + similarityTermsGlobal);
                    }

                    // print the comment 
                    for (CommentMap cMap : comments) {
                        // print the artifacts
                        if (cMap.artifactSet != null) {
                            System.out.print(cMap.artifactSet + " ");
                        }
                        cMap.print();
                        masterCommentList.add(cMap.comment);
                    }
                    
                    // Print the code segment
                    List<String> encoded = Files.readAllLines(Paths.get(filePath), Charset.defaultCharset());
                    for (int lineNum = startLine - 1; lineNum < endLine; lineNum++) {
                        String line = encoded.get(lineNum);
                        System.out.println("< " + line);
                    }
                    System.out.println("----");
                }
            } catch (IOException e) {
                System.out.println(e);
            }
            i++;
        }
        
        printAllComments(masterCommentList);
    }

    private void printAllComments(HashSet<String> masterCommentList) {
        System.out.println("Comments (size " +
                masterCommentList.size() + "):");
        int index = 1;
        for (String thisComment : masterCommentList) {
            System.out.println(index + ".\n" + thisComment);
            index++;
        }
        System.out.println("----");
        
    }
}
