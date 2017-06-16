import java.util.ArrayList;

class Result {

    public String file1;
    public int lineStart1;
    public int lineEnd1;
    public String file2;
    public int lineStart2; 
    public int lineEnd2; 
    public int length;
    public ArrayList<Statement> statementRaw1;
    public int statementStart1;
    public int statementEnd1;
    public ArrayList<Statement> statementRaw2;
    public int statementStart2;
    public int statementEnd2;
    public int totalHashValue;

    public Result (String file1In, int lineStart1In, int lineEnd1In,
            String file2In, int lineStart2In, int lineEnd2In, int lengthIn,
            ArrayList<Statement> statementRaw1In, int statementStart1In, int statementEnd1In,
            ArrayList<Statement> statementRaw2In, int statementStart2In, int statementEnd2In,
            int totalHashValueIn) {

        file1 = file1In;
        lineStart1 = lineStart1In;
        lineEnd1 = lineEnd1In;
        file2 = file2In;
        lineStart2 = lineStart2In;
        lineEnd2 = lineEnd2In;
        length = lengthIn;
        statementRaw1 = statementRaw1In;
        statementStart1 = statementStart1In;
        statementEnd1 = statementEnd1In;
        statementRaw2 = statementRaw2In;
        statementStart2 = statementStart2In;
        statementEnd2 = statementEnd2In;
        totalHashValue = totalHashValueIn;
    }
}
