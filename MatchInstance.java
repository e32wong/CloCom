import java.io.Serializable;

import java.util.ArrayList;

import java.util.Set;
import java.util.HashSet;

public class MatchInstance implements Serializable {

    public String fileName;
    public int startLine;
    public int endLine;
    
    public ArrayList<Statement> statements;
    public int startIndex;
    public int endIndex;

    // comment related
    ArrayList<CommentMap> commentList;
    ArrayList<HashSet<String>> similarityTermsLocal = new ArrayList<HashSet<String>>();
    ArrayList<HashSet<String>> similarityTermsGlobal = new ArrayList<HashSet<String>>();

    public MatchInstance (String name, int lineStart, int lineEnd,
            ArrayList<Statement> statements_in, int startIndex_in, int endIndex_in) {
        
        fileName = name;
        startLine = lineStart;
        endLine = lineEnd;

        statements = statements_in;
        startIndex = startIndex_in;
        endIndex = endIndex_in;
    }

    public ArrayList<Statement> getStatements() {
        return statements;
    }

    public void setComments (ArrayList<CommentMap> comments) {
        commentList = comments;
    }

    public ArrayList<CommentMap> getComments () {
        return commentList;
    }

    public void addSimilarityLocal (HashSet<String> terms) {
        similarityTermsLocal.add(terms);
    }

    public ArrayList<HashSet<String>> getSimilarityLocal() {
        return similarityTermsLocal;
    }

    public void addSimilarityGlobal (HashSet<String> terms) {
        similarityTermsGlobal.add(terms);
    }

    public ArrayList<HashSet<String>> getSimilarityGlobal() {
        return similarityTermsGlobal;
    }

    public boolean equals(Object obj) {
        MatchInstance matchInstance = (MatchInstance) obj;
        if (matchInstance.fileName.equals(fileName) &&
                matchInstance.startLine == startLine &&
                matchInstance.endLine == endLine) {
            return true;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return fileName.hashCode() * startLine * endLine;
    }

}
