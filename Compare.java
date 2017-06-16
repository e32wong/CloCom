import java.util.List;
import java.util.ArrayList;

import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Compare {

    ArrayList<Text> projectTextList;

    List<String> databasePaths;

    int minNumLines;

    Output result;

    String databaseDir;
    String projectDir;

    public Compare(int numLinesMatch, String databaseDirIn, String projectDirIn) {
        databaseDir = databaseDirIn;
        projectDir = projectDirIn;
        minNumLines = numLinesMatch;
    }   

    public void installTextFiles(List<String> db_PathList) {
        databasePaths = db_PathList;
    }
    public void installTextFiles(ArrayList<Text> projectList, List<String> db_PathList) {
        projectTextList = projectList;
        databasePaths = db_PathList;
    }

    public Output importResults(Output result, ArrayList<Result> threadResult) {
		for (Result r : threadResult) {
			result.addClone(r.file1, r.lineStart1, r.lineEnd1, r.file2,
					r.lineStart2, r.lineEnd2, r.length, r.statementRaw1, r.statementStart1, r.statementEnd1,
					r.statementRaw2, r.statementStart2, r.statementEnd2, r.totalHashValue);
		}
		return result;
	}


    public void compareMeshed (Output outputObject, int mode, int gapSize, int blockSize) {
    
        result = outputObject;

        System.out.println("\nComparing for " + databasePaths.size() + " files");

        // perform comparison within the blocks
        System.out.println("Processing within blocks");
        for (int i = 0; i < databasePaths.size(); i = i + blockSize) {
            int nextMark = i + blockSize;
            if (nextMark > databasePaths.size()) {
                nextMark = databasePaths.size();
            }

            // load up memory first
            ArrayList<Text> thisBlock = new ArrayList<Text>();
            for (int j = i; j < nextMark; j++) {
                thisBlock.add(Database.loadSingleFile(databasePaths.get(j), databaseDir, minNumLines, false));
            }

            // perform local comparison
            for (int j = 0; j < thisBlock.size() - 1; j++) {
                System.out.print(j + "\r");
                Text text1 = thisBlock.get(j);
                for (int k = j + 1; k < thisBlock.size(); k++) {
                    Text text2 = thisBlock.get(k);
                    ArrayList<Result> threadResult = 
							TextCompare.textCompare(text1, text2, mode, gapSize, minNumLines, projectDir, databaseDir);
                    result = importResults(result, threadResult);
                }
            }
        }
        
        // perform comparison between the blocks
        System.out.println("Processing between blocks");
        for (int i = 0; i < databasePaths.size(); i = i + blockSize) {
            int nextMark = i + blockSize;
            if (nextMark > databasePaths.size()) {
                // we are on the last block, terminate
                break;
            }

            System.out.println(nextMark);

            // load this block into memory
            ArrayList<Text> thisBlock = new ArrayList<Text>();
            for (int j = i; j < nextMark; j++) {
                thisBlock.add(Database.loadSingleFile(databasePaths.get(j), databaseDir, minNumLines, false));
            }

            // between comparsion
            for (int j = 0; j < thisBlock.size(); j++) {
                Text text1 = thisBlock.get(j);
                for (int k = nextMark; k < databasePaths.size(); k++) {
                    Text text2 = Database.loadSingleFile(databasePaths.get(k), databaseDir, minNumLines, false);
                    ArrayList<Result> threadResult = 
                            TextCompare.textCompare(text1, text2, mode, gapSize, minNumLines, projectDir, databaseDir);
                    result = importResults(result, threadResult);
                }
            }

        }

        System.out.println("");
    }
    public void compareBetween (Output outputObject, int mode, int gapSize) {

        result = outputObject;

        System.out.println("\nComparing against " + databasePaths.size() + " database files");

        for (int i = 0; i < databasePaths.size(); i++) {
            // outer loop is the database
            Text text1 = Database.loadSingleFile(databasePaths.get(i), databaseDir, minNumLines, false);

            ExecutorService es = Executors.newSingleThreadExecutor();
            Future futureResult = es.submit(new RunnableDemo("Thread-1", projectTextList, 
                    text1, mode, gapSize, minNumLines, projectDir, databaseDir));
            try {
                ArrayList<Result> threadResult = (ArrayList<Result>)futureResult.get();
                result = importResults(result, threadResult);
            } catch (InterruptedException e) {
                System.out.println("Interrupted thread exception");
            } catch (ExecutionException e) {
                System.out.println("Execution exception in thread");
            } finally {
                es.shutdownNow();
            }

            System.out.print((i+1) + "\r");
        }
        System.out.println("");
    }
}



