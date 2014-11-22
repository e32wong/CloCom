import org.eclipse.jdt.core.dom.*;

import java.util.List;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;

import java.io.*;

public class Text implements Serializable {

	  private String fileAbsPath;
    private String baseName;
    private String baseDir;
	
	  /* A collection of Methods */
	  ArrayList<Method> methodList = new ArrayList<Method>();
	
	  /* Constructor */
	  public Text(String filePath, String baseDirIn) {
        baseDir = baseDirIn;
        fileAbsPath = filePath;
        baseName = FilenameUtils.getBaseName(fileAbsPath);
    }

    public void clearDependentPath() {
        fileAbsPath = fileAbsPath.substring(baseDir.length());
        baseDir = "";
    }

    public void setDependentPath(String baseDirIn) {
        baseDir = baseDirIn;
        fileAbsPath = baseDir + fileAbsPath;
    }

    public Method getMethod(int i) {
        return methodList.get(i);
    }

    public static String getDBpath(String fileAbsPath) {
        String pathNoPrefix = FilenameUtils.getPath(fileAbsPath);
        String baseName = FilenameUtils.getBaseName(fileAbsPath);
        return "/" + pathNoPrefix + baseName + ".db";
    }

    public String getAbsPath() {
        return fileAbsPath;
    }

    public int getNumMethods() {
        return methodList.size();
    }

    public ArrayList<Statement> getGroupedStatements(int index) {
        return methodList.get(index).getGroupedStatements();
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

    private boolean hasDB() {
        String baseName = FilenameUtils.getBaseName(fileAbsPath);

        File f = new File(Text.getDBpath(fileAbsPath));
        if (f.exists()) {
            return true;
        } else {
            return false;
        }
    }

    public ArrayList<String> tokenize(int minNumLines, boolean debug, ArrayList<String> fileProcessError) {
        
        Tokenizer token = Parser.parseAST2Tokens(fileAbsPath, minNumLines, debug);
        if (token == null) {
            // error at parsing the token list, abort
            fileProcessError.add(fileAbsPath);
            return fileProcessError;
        }

        ArrayList<Method> methodListAll = token.getTokenizedMethods();
        
        CommentParser cParser = new CommentParser(fileAbsPath);

        // check and see if there is a comment in each method
        for (Method thisMethod : methodListAll) {
            int startLine = thisMethod.getStart();
            int endLine = thisMethod.getEnd();

            //System.out.println(startLine + " " + endLine + " " + fileAbsPath);
            
            ArrayList<CommentMap> cMapList = cParser.parseComment(fileAbsPath, startLine, endLine, 1);
            if (cMapList.size() == 0) {
            } else {
                methodList.add(thisMethod);
            }
        }

        return fileProcessError;
    }
}

