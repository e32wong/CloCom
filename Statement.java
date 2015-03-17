import java.io.Serializable;

import java.util.ArrayList;

import java.util.Set;
import java.util.HashSet;

public class Statement implements Serializable {

    public int startLine;
    public int endLine;
    public int hashNumber;
    public int scopeLevel;

    public boolean hasMethodInvocation = false;

    public HashSet<String> nameList;

    public Statement(int value, int sLine, int eLine) {
        startLine = sLine;
        endLine = eLine;
        hashNumber = value;
    }

    public void insertNameList (HashSet<String> nameList_in) {
        nameList = nameList_in;
    }

    public void insertScope(int level) {
        scopeLevel = level;
    }

    public void enableMethodInvocation() {
        hasMethodInvocation = true;
    }

    public boolean hasMethodInvocation() {
        return hasMethodInvocation;
    }
}
