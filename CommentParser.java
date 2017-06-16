import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.charset.*;
import java.io.IOException;

import java.io.BufferedReader;
import java.io.FileReader;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class CommentParser {

    final CompilationUnit unit;
    char[] content;
    List<Comment> commentList;

    /* Convert a file into a String */
    private static String fileToString(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public CommentParser (String absPath) {

        String source = "";
        try {
            source = fileToString(absPath);
        } catch (IOException e) {
            System.out.println("Error at loading file for comment extraction");
            e.printStackTrace();
        }

        ASTParser parser = ASTParser.newParser(AST.JLS4);

        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        content = source.toCharArray();
        parser.setSource(content);

        Map<String, String> options = JavaCore.getOptions();
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_6);
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM,
                JavaCore.VERSION_1_6);
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_6);

        String sourceName = absPath.substring(0, absPath.indexOf("/"));
        String[] sources = {sourceName};
        String[] classPaths = {sourceName};

        parser.setEnvironment(classPaths, sources,
                new String[] { "UTF-8" }, true);
        parser.setBindingsRecovery(true);
        parser.setResolveBindings(true);
        parser.setCompilerOptions(options);
        parser.setStatementsRecovery(true);

        unit = (CompilationUnit) parser.createAST(null);

        commentList = (List<Comment>) unit.getCommentList();
    }

    // mode 0 - default where it grabs all comments
    // mode 1 - stops upon finding a comment
    // format 1 = autocomment, 0 = standard
    public ArrayList<CommentMap> parseComment
        (String absPath, int startLine, int endLine, int mode, int format) {

            final ArrayList<CommentMap> commentMap = new ArrayList<CommentMap>();

            if (format == 1) {

                try (BufferedReader br = new BufferedReader(new FileReader(absPath))) {
                    String line = br.readLine();
                    if (!line.equals("//")) {
                        try {
                            ArrayList<String> listSentences = Analyze.splitParagraph(line.substring(2));
                            for (String s : listSentences) {
                                CommentMap c1 = new CommentMap(s, 0, 0, 1);
                                commentMap.add(c1);
                            }
                        } catch (Exception e) {
                            System.out.println("Exception on first line parsing, expecting // format for the db");
                        }

                    }
                    line = br.readLine();
                    if (!line.equals("//")) {
                        ArrayList<String> listSentences = Analyze.splitParagraph(line.substring(2));
                        for (String s : listSentences) {
                            CommentMap c2 = new CommentMap(s, 1, 1, 1);
                            commentMap.add(c2);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Exception in CommentParser format 1");
                    System.out.println(e);
                }

            } else if (format == 0) {
                // call accept on each comment to retrieve the content of the comment
                for (int i = 0; i < commentList.size(); i++) {

                    Comment comment = commentList.get(i);
                    int startLineNumber = unit.getLineNumber(comment.getStartPosition());
                    int endLineNumber = unit.getLineNumber(comment.getStartPosition() + comment.getLength());

                    // search for line line comment, block comment, javadoc comment within the range
                    if (startLineNumber >= startLine && endLineNumber <= endLine) {
                        comment.accept(new CommentVisitor(unit, content, commentMap, startLineNumber, endLineNumber));
                        continue;
                    }

                    // search for line comment before the range
                    if (startLineNumber >= startLine - 3 && startLineNumber < startLine &&
                            startLineNumber == endLineNumber) {
                        int currentLine = startLineNumber;

                        final ArrayList<CommentMap> dummyMap = new ArrayList<CommentMap>();

                        // keep searching forward one line at a time
                        while (currentLine < startLine && i < commentList.size()) {
                            comment = commentList.get(i);

                            startLineNumber = unit.getLineNumber(comment.getStartPosition());
                            endLineNumber = unit.getLineNumber(comment.getStartPosition() + comment.getLength());

                            if (startLineNumber == endLineNumber && startLineNumber == currentLine) {
                                comment.accept(new CommentVisitor(unit, content,
                                            dummyMap, startLineNumber, endLineNumber));
                                currentLine++;
                                i++;
                            } else {
                                break;
                            }
                        }

                        if (currentLine != startLine) {
                            dummyMap.clear();
                        } else {
                            i = i - 1;
                        }

                        commentMap.addAll(dummyMap);

                        continue;
                            }

                    // stop execution if in mode 1 once we found a comment
                    if (mode == 1) {
                        if (commentMap.size() > 0) {
                            break;
                        }
                    }
                }
            } 

            return commentMap;
        }               

}
