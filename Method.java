import org.eclipse.jdt.core.dom.*;

import java.util.List;
import java.util.ArrayList;

import java.io.Serializable;

import java.util.Set;
import java.util.HashSet;

public class Method implements Serializable {

    // list of statements with its respective token values
    ArrayList<Statement> bodyStatements = new ArrayList<Statement>();
    ArrayList<Statement> groupedStatements = new ArrayList<Statement>();

    String methodName;

    int methodStartLine;
    int methodEndLine;

    public Method(String m_name, int startLine) {
    methodName = m_name;
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

    public void addStatement(int value, int startLine, 
            boolean hasMethodInvocation, int scopeLevel,
            HashSet<String> simpleNameList) {

        Statement statement = new Statement(value, startLine);
        statement.insertScope(scopeLevel);
        statement.insertNameList(simpleNameList);
        if (hasMethodInvocation) {
            statement.enableMethodInvocation();
        }
        bodyStatements.add(statement);

    }

    public ArrayList<Statement> getGroupedStatements() {
        return groupedStatements;
    }

    public ArrayList<Statement> getMethodStatements() {
        return bodyStatements;
    }

    public int getNumStatements() {
        return bodyStatements.size();
    }

    public void buildHash(int groupSize) {
        for (int i = 0; i < bodyStatements.size() - groupSize + 1; i++) {

            final int prime = 31;
            int result = 1;
            for (int j = i; j < i + groupSize; j++) {
                int num = bodyStatements.get(j).hashNumber;
                result = prime * result + num;
            }
            Statement statement = new Statement(result, bodyStatements.get(i).startLine,
                    bodyStatements.get(i+groupSize-1).startLine);
            groupedStatements.add(statement);
        }
    }

}

