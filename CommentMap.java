import java.io.Serializable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Set;

import java.io.PrintWriter;

public class CommentMap implements Serializable {

    public String comment;
    public int startLine;
    public int endLine;

    public Set<String> artifactSet;

    // comment type
    // 0 - javadoc, 1 - linecomment, 2 - blockcomment
    public int commentType;

    public CommentMap(String cmt, int sLine, int eLine, int cType) {

        comment = cmt;
        startLine = sLine;
        endLine = eLine;
        commentType = cType;
    }

    @Override public String toString() {
        return comment;
    }
}
