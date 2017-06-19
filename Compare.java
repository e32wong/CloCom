import java.util.HashSet;
import java.util.Set;

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

        boolean enableThread = true;
        if (enableThread) {
            // fill up the initial cpu
            int numberCPU = 4;
            int processedNumber = 0;
            while (processedNumber < databasePaths.size()) {
                // try load four cpu
                ExecutorService executor = Executors.newWorkStealingPool();
                Set<Callable<ArrayList<Result>>> callables = new HashSet<Callable<ArrayList<Result>>>();
                
                for (int i = 0; i < numberCPU && processedNumber < databasePaths.size(); i++) {
                    Text text1 = Database.loadSingleFile(databasePaths.get(processedNumber), databaseDir, minNumLines, false);
                    callables.add(new RunnableDemo("Thread-" + Integer.toString(processedNumber), projectTextList,
                                        text1, mode, gapSize, minNumLines, projectDir, databaseDir));
                    processedNumber = processedNumber + 1;
                }

                // invoke and close everything
                try {
                    List<Future<ArrayList<Result>>> futures = executor.invokeAll(callables);
                    for (Future<ArrayList<Result>> future : futures) {
                        ArrayList<Result> threadResult = future.get();
                        result = importResults(result, threadResult);
                    }
                } catch (InterruptedException e) {
                    System.out.println("Interrupted thread exception");
                    System.out.println(e.getMessage());
                } catch (ExecutionException e) {
                    System.out.println("Execution exception in thread");
                    System.out.println(e.getMessage());
                    e.printStackTrace(System.out);
                } finally {
                    executor.shutdownNow();
                }
                executor.shutdown();
                System.out.print((processedNumber+1) + "\r");
            }
        } else {
			for (int i = 0; i < databasePaths.size(); i++) {
				// outer loop is the database
				Text text1 = Database.loadSingleFile(databasePaths.get(i), databaseDir, minNumLines, false);

				for (int j = 0; j < projectTextList.size(); j++) {
					// inner loop is the project
                    
                    Text text2 = projectTextList.get(j);
                    ArrayList<Result> threadResult =
                            TextCompare.textCompare(text2, text1, mode, gapSize, minNumLines, projectDir, databaseDir);
					result = importResults(result, threadResult);
				}
				System.out.print((i+1) + "\r");
			}
        }
    }
}



