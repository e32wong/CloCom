import org.eclipse.jdt.core.dom.*;

import java.util.List;
import java.util.ArrayList;

import java.util.Set;
import java.util.HashSet;

public class Tokenizer {

    public boolean inMethod = false;

    List<Integer> currentTokenList = new ArrayList<Integer>();

    String currentMethodName = "";
    Method currentMethodObj;

    int startLine;
    int endLine;
    int minNumLines;

    boolean statementHasMethodInvocation = false;
    boolean methodHasMethodInvocation = false;
    boolean debugStatements;

    ArrayList<Method> methodList = new ArrayList<Method>();
    HashSet<String> simpleNameList = new HashSet<String>();
    HashSet<String> varNameList = new HashSet<String>();

    public Tokenizer(int numLines, boolean debug) {
        minNumLines = numLines;
        debugStatements = debug;
    }

    public void insertSimpleName(String name, boolean isVariable) {
        simpleNameList.add(name);
        if (isVariable) {
            varNameList.add(name);
        }
    }

    public void methodStart(String name, int mStartLine) {
        if (currentMethodName == "") {
            currentMethodName = name;
            currentMethodObj = new Method(mStartLine);
            methodHasMethodInvocation = false;

            if (debugStatements == true) {
                System.out.println("\n\n");
            }
        }
    }

    public void methodEnd(String name, int mEndLine) {
        // prevent methods within a method
        if (currentMethodName.equals(name)) {
            currentMethodName = "";

            if (currentMethodObj.getNumStatements() >= minNumLines &&
                    methodHasMethodInvocation == true) {
                //currentMethodObj.buildHash(minNumLines);
                currentMethodObj.setEndLine(mEndLine);
                methodList.add(currentMethodObj);
            } else {
                // clear the statments
                currentTokenList.clear();
            }
        }
    }

    public void statementStart(int sLine, int eLine) {
        startLine = sLine;
        endLine = eLine;
        statementHasMethodInvocation = false;
        simpleNameList = new HashSet<String>();
        varNameList = new HashSet<String>();
    }

    public void statementEnd(int scopeLevel) {
        if (!currentTokenList.isEmpty()) {
            int hash_value = hashLine(currentTokenList);
            currentTokenList.clear();

            if (inMethod == true) {
                currentMethodObj.addStatement(hash_value, startLine, endLine,
                        statementHasMethodInvocation, scopeLevel, simpleNameList,
                        varNameList);

                // debug
                if (debugStatements == true) {
                    System.out.println("added");
                    System.out.print("sim terms : ");
                    for (String simpleName : simpleNameList) {
                        System.out.print(simpleName + " ");
                    }
                    System.out.println("");
                    System.out.print("var terms : ");
                    for (String varName : varNameList) {
                        System.out.print(varName + " ");
                    }
                    System.out.println("");
                }
            }
        }
    }

    public void getHash(int nodeType, String str) {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((str == null) ? 0 : str.hashCode());
        result = prime * result + nodeType;
        currentTokenList.add(result);
    }

    public int hashLine(List<Integer> statementTokens) {

        final int prime = 31;
        int result = 1;   	

        for (Integer tkn : statementTokens) {
            result = prime * result + tkn;
        }

        if (debugStatements == true) {
            System.out.printf("\t>> Hashed statement: %d\n", result);
        }

        return result;
    }

    public void hasMethodInvocation() {
        statementHasMethodInvocation = true;
        methodHasMethodInvocation = true;
    }

    // Add node name
    public void addHash(int type, int lineNum) {
        getHash(type, null);
        if (debugStatements == true) {
            debug(type, null, null, lineNum);
        }
    }

    // Add code element
    public void addHash(int type, String str, int lineNum) {        
        getHash(type, str);
        if (debugStatements == true) {
            debug(type, str, null, lineNum);
        }
    }

    // Add variable element that contains ast type, type and name
    public void addHash(int type, String str1, String str2, int lineNum) {
        getHash(type, str1);
        if (debugStatements == true) {
            debug(type, str1, str2, lineNum);
        }
    }

    private void debug(int type, String str, String str2, int lineNum) {
        
        if (str == null && str2 == null) {
            System.out.printf(" >> \"%s\" at line \"%d\"\n", TokenType.values()[type].toString(), lineNum);
        } else if (str != null && str2 == null) {
            str = str.replaceAll("\\n","");
            System.out.printf(" >> \"%s\" with value \"%s\" at line \"%d\"\n", TokenType.values()[type].toString(), str, lineNum);
        }
        else if (str != null && str2 != null) {
            str = str.replaceAll("\\n","");
            System.out.printf(" >> \"%s\" with value \"%s\" with ignored name \"%s\" at line \"%d\"\n", 
                    TokenType.values()[type].toString(), str, str2, lineNum);
        } else {
            System.out.println("Error in Tokenizer");
            System.exit(1);
        }

    }

    public ArrayList<Method> getTokenizedMethods() {
        return methodList;   
    }
}

