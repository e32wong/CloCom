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

class RunnableDemo implements Callable<ArrayList<Result>> {
    private Thread t;
    private String threadName;

	private ArrayList<Text> projectTextList;
	private Text text1;
    private int mode;
	private int gapSize;
    private int minNumLines;

    private String databaseDir;
    private String projectDir;

	private ArrayList<Result> result;

    @Override
    public ArrayList<Result> call() throws Exception {
        run();
        return result;
    }

    public void run() {
        System.out.println("Running " +  threadName );
		for (int j = 0; j < projectTextList.size(); j++) {
			// inner loop is the project
			Text text2 = projectTextList.get(j);
			result = TextCompare.textCompare(text2, text1, mode, gapSize, minNumLines, projectDir, databaseDir);
		}
        System.out.println("Thread " +  threadName + " exiting.");
    }

    public RunnableDemo( String name, ArrayList<Text> projectTextListIn, Text text1In,
                int modeIn, int gapSizeIn, int minNumLinesIn, String projectDirIn, String databaseDirIn) {
        threadName = name;
        System.out.println("Creating " +  threadName );

		projectTextList = projectTextListIn;
		text1 = text1In;
		mode = modeIn;
		gapSizeIn = gapSize;
        minNumLines = minNumLinesIn;
        projectDir = projectDirIn;
        databaseDir = databaseDirIn;

        System.out.println("Starting " +  threadName );
        //if (t == null) {
        //    t = new Thread (this, threadName);
        //    t.start ();
        //}
    }





}
