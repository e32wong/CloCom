import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.Javadoc;

import java.util.ArrayList;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommentVisitor extends ASTVisitor {

    // http://stackoverflow.com/questions/12971991/eclipse-jdt-core-parser-not-parsing-the-comments
    CompilationUnit compilationUnit;
    ArrayList<CommentMap> cMapList;

    private char[] source;

    int startLine;
    int endLine;

    public CommentVisitor(CompilationUnit compilationUnit, char[] content, ArrayList<CommentMap> commentMap,
            int sLine, int eLine) {

        super();
        this.compilationUnit = compilationUnit;
        this.source = content;

        startLine = sLine;
        endLine = eLine;

        cMapList = commentMap;

    }

    public boolean visit(Javadoc node) {
        int startLineNumber = compilationUnit.getLineNumber(node.getStartPosition());
        int endLineNumber = compilationUnit.getLineNumber(node.getStartPosition() + node.getLength());

        //System.out.println("dd " + startLineNumber + " " + endLineNumber);
        //System.out.println("ee " + startLine + " " + endLine);

        if (startLine == startLineNumber) {

            // Build the comment char by char
            StringBuilder comment = new StringBuilder();
            for (int i = node.getStartPosition(); i < node.getStartPosition() + node.getLength(); i++) {
                comment.append(source[i]);
            }

            if (comment.capacity() > 0) {
                String str = comment.toString().replaceAll("\\n\\s+", "\n ");

                if (str != null) {
                    CommentMap cMap = new CommentMap(str, startLineNumber, endLineNumber, 0);
                    cMapList.add(cMap);
                }
            }
        }
        return true;
    }

    public boolean visit(LineComment node) {

        int startLineNumber = compilationUnit.getLineNumber(node.getStartPosition());
        int endLineNumber = compilationUnit.getLineNumber(node.getStartPosition() + node.getLength());

        if (startLineNumber >= startLine && endLineNumber <= endLine) {
            // Build the comment char by char
            StringBuilder comment = new StringBuilder();
            for (int i = node.getStartPosition(); i < node.getStartPosition() + node.getLength(); i++) {
                comment.append(source[i]);
            }

            if (comment.capacity() > 0) {
                String str = comment.toString();

                if (str != null) {
                    CommentMap cMap = new CommentMap(str, startLineNumber, endLineNumber, 1);
                    cMapList.add(cMap);
                }
            }
        }
        return true;
    }

    public boolean visit(BlockComment node) {

        int startLineNumber = compilationUnit.getLineNumber(node.getStartPosition());
        int endLineNumber = compilationUnit.getLineNumber(node.getStartPosition() + node.getLength());

        //System.out.println("dd "  + startLineNumber + " " + endLineNumber);
        //System.out.println("ee "  + startLine + " " + endLine + "\n");

        if ((startLineNumber >= startLine && endLineNumber <= endLine) || 
                startLine - 1 == endLineNumber) {

            // Build the comment char by char
            StringBuilder comment = new StringBuilder();
            for (int i = node.getStartPosition(); i < node.getStartPosition() + node.getLength(); i++) {
                comment.append(source[i]);
            }

            if (comment.capacity() > 0) {
                String str = comment.toString().replaceAll("\\n\\s+", "\n ");

                if (str != null) {
                    CommentMap cMap = new CommentMap(str, startLineNumber, endLineNumber, 2);
                    cMapList.add(cMap);
                }
            }
        }
        return true;
    }

}
