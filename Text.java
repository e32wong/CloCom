import org.eclipse.jdt.core.dom.*;

import java.util.List;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;

import java.io.*;

public class Text implements Serializable {

    private String databasePath;  // freecol/xx/xx/xx
	
	/* A collection of Methods */
	ArrayList<Method> methodList = new ArrayList<Method>();
	
    public String toString() {
        return "Path: " + databasePath;
    }

    public static String getDBpath(String fileAbsPath) {
        String pathNoPrefix = FilenameUtils.getPath(fileAbsPath);
        String baseName = FilenameUtils.getBaseName(fileAbsPath);
        return "/" + pathNoPrefix + baseName + ".db";
    } 

	/* Constructor */
	public Text(String fileAbsPath, String baseDir) {
        databasePath = fileAbsPath.substring(baseDir.length());
    }

    public Method getMethod(int i) {
        return methodList.get(i);
    }

    public String getDatabasePath() {
        return databasePath;
    }

    public int getNumMethods() {
        return methodList.size();
    }

    public ArrayList<Statement> getRawStatements(int index) {
        return methodList.get(index).getMethodStatements();
    }

    public int getTotalNumStatements() {
        int totalNumStatements = 0;
        for (Method i : methodList) {
            totalNumStatements = totalNumStatements + i.getNumStatements();
        }
        System.out.println(totalNumStatements);
        return totalNumStatements;
    }

    public ArrayList<String> tokenize(
            int minNumLines, 
            boolean debug, 
            ArrayList<String> fileProcessError,
            String basePath) {
        
        String absolutePath = basePath + databasePath;

        Tokenizer token = Parser.parseAST2Tokens(absolutePath, minNumLines, debug);
        if (token == null) {
            // error at parsing the token list, abort
            fileProcessError.add(absolutePath);
            return fileProcessError;
        }

        ArrayList<Method> methodListAll = token.getTokenizedMethods();
        
        CommentParser cParser = new CommentParser(absolutePath);

        // check and see if there is a comment in each method
        for (Method thisMethod : methodListAll) {
            int startLine = thisMethod.getStart();
            int endLine = thisMethod.getEnd();

            //System.out.println(startLine + " " + endLine + " " + absolutePath);
            
            ArrayList<CommentMap> cMapList = cParser.parseComment(absolutePath, startLine, endLine, 1);
            if (cMapList.size() == 0) {
            } else {
                methodList.add(thisMethod);
            }
        }

        return fileProcessError;
    }
}

