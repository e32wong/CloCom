import org.eclipse.jdt.core.dom.*;

import java.util.List;
import java.util.ArrayList;

import java.io.Serializable;

import java.util.Set;
import java.util.HashSet;

public class Method implements Serializable {

    // list of statements with its respective token values
    ArrayList<Statement> bodyStatements = new ArrayList<Statement>();

    int methodStartLine;
    int methodEndLine;

    public Method(int startLine) {
        methodStartLine = startLine;
    }

    public void setEndLine(int endLine) {
        methodEndLine = endLine;
    }

    public int getStart() {
        return methodStartLine;
    }

    public int getEnd() {
        return methodEndLine;
    }

    public void addStatement(int value, int startLine, int endLine,
            boolean hasMethodInvocation, int scopeLevel,
            HashSet<String> simpleNameList) {

        Statement statement = new Statement(value, startLine, endLine);
        statement.insertScope(scopeLevel);
        statement.insertNameList(simpleNameList);
        if (hasMethodInvocation) {
            statement.enableMethodInvocation();
        }
        bodyStatements.add(statement);

    }

    public ArrayList<Statement> getMethodStatements() {
        return bodyStatements;
    }

    public int getNumStatements() {
        return bodyStatements.size();
    }

}

