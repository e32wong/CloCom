import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.nio.file.Files;
import java.io.PrintWriter;

import java.io.IOException;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;

import java.util.concurrent.TimeUnit;


public class CloneDigger {

    private static void displayError(ArrayList<String> errorList) {
        if (errorList.size() > 0) {
            System.out.println("Eclipse API error on the following files:");
        }   
        for (String str : errorList) {
            System.out.println(str);
        }
    }

    private static void clearAndSaveConfig(String inputPath, String outputDir) {

        String outputFile = outputDir + "config.xml";

        File from = new File(inputPath);
        File toDir = new File(outputDir);
        File toFile = new File(outputFile);

        try {
            // cleans the directory without deleting it
            FileUtils.cleanDirectory(toDir); 
            Files.copy(from.toPath(), toFile.toPath());
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Error while copying config to output dir");
            System.exit(0);
        }

        System.out.println("Saved config to output dir:");
        System.out.println(outputDir);

    }

    public static void main(String args[]) throws IOException {

        Options options = new Options();
        options.addOption("generateBaseline", true, "generate baseline config file to the provided path");
        options.addOption("configPath", true, "configuration xml file path");
        CommandLineParser parser = new DefaultParser();
        String baseLineOutputPath = null;
        String configPath = null;
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("configPath")) {
                configPath = cmd.getOptionValue("configPath");
            }
            if (cmd.hasOption("generateBaseline")) {
                baseLineOutputPath = cmd.getOptionValue("generateBaseline");
            }
        } catch (ParseException e) {
            System.out.println(e);
        }

        // generate a baseline config file
        if (baseLineOutputPath != null) {
            System.out.println("Writing baseline config file..");
            ConfigFile.writeBaseline(baseLineOutputPath);
            System.out.println("Exiting.");
            System.exit(0);
        }

        // load config file
        ConfigFile config = new ConfigFile();
        config.loadConfig(configPath);

        int gapSize = config.gapSize;
        int matchAlgorithm = config.matchAlgorithm;
        int matchMode = config.matchMode;
        int minNumLines = config.minNumLines;
        int meshBlockSize = config.meshBlockSize;
        String databaseDir = config.database;
        String projectDir = config.project;
        String outputDir = config.outputDir;
        boolean debug = config.debug;
        boolean saveEmpty = config.saveEmpty;
        String resultPath = config.resultPath;
        boolean exportResults = config.exportResults;
        boolean loadResults = config.loadResults;
        int similarityRange = config.similarityRange;
        boolean enableSimilarity = config.enableSimilarity;
        boolean enableRepetitive = config.enableRepetitive;
        boolean enableOneMethod = config.enableOneMethod;
        boolean buildTFIDF = config.buildTFIDF;
        int numberThreads = config.numberThreads;
        int minNumberStatements = config.minNumberStatements;
        boolean enablePercentageMatching = config.enablePercentageMatching;
        boolean forceRetokenization = config.forceRetokenization;
        boolean loadDatabaseFilePaths = config.loadDatabaseFilePaths;
        int aprioriMinSupport = config.aprioriMinSupport;
        boolean enableQuery = config.enableQuery;

        // done parsing
        System.out.println("Finished parsing XML parameters");

        // clear existing output and save the config to output dir
        clearAndSaveConfig(configPath, outputDir);

        // Get current time
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
        Calendar cal = Calendar.getInstance();
        System.out.println("Start @ " +  sdf.format(cal.getTime()) );

        // Measure elapsed time
        long startTime = System.nanoTime();

        // Cached list of files from the database
        String databaseFilePaths = databaseDir + "cachedList.tmp";
        File f = new File(databaseFilePaths);
        List<String> databaseFileList;
        // Check if an existing cache file exists
        if(f.exists() && !f.isDirectory() && loadDatabaseFilePaths == true) {
            // exist, load it
            System.out.println("Path file exists");
            databaseFileList = Database.loadFileList(databaseFilePaths);
        } else {
            // doesn't exist or forced to create new path file, create it
            System.out.println("Path file doesn't exist or specified to regen path file");
            databaseFileList = Database.generateFileList(databaseDir, "cachedList.tmp");
        }

        // td-idf
        /*
        TermFrequency termFreq = new TermFrequency();
        if (buildTFIDF == true) {
            termFreq.buildFrequencyMap(databaseDir);
        } else {
            //termFreq.loadFrequencyMap();
        }*/

        // Start loading main content
        ArrayList<String> errorList = new ArrayList<String>();

        Output output = new Output(matchAlgorithm, enableRepetitive, enableOneMethod, 
                matchMode, outputDir, minNumberStatements, debug, enablePercentageMatching);
        if (matchMode == 1) {
            // full mesh comparison
            if (loadResults == false) {
                System.out.println("Mode: full mesh");
                ArrayList<Text> database_TextList = new ArrayList<Text>();

                // build the database
                ArrayList<String> temp = Database.constructCache(
                        minNumLines, debug, databaseFileList, databaseDir, forceRetokenization);
                errorList.addAll(temp);

                // Capture time
                cal = Calendar.getInstance();
                System.out.println("Start comparison @ " +  sdf.format(cal.getTime()) );

                // perform the comparison
                // full mesh requires both dir paths to be the same
                Compare comp = new Compare(minNumLines, databaseDir, databaseDir);
                comp.installTextFiles(databaseFileList);
                comp.compareMeshed(output, matchAlgorithm, gapSize, meshBlockSize);
                if (exportResults) {
                    output.saveResults(resultPath);
                }
            } else {
                output.loadResults(resultPath);
            }

            // enable the query engine
            if (enableQuery) {
                output.search(outputDir);
            }

            output.printResults(saveEmpty, similarityRange, enableSimilarity, matchMode, debug);
            /*
            // Frequency Map of all terms
            FrequencyMap fMap = new FrequencyMap(aprioriMinSupport);
            output.processOutputTerms(fMap);
            fMap.exportTable("table.txt");
            */
        } else {
            // Between comparison
            if (loadResults == false) {
                // between comparison
                System.out.println("Mode: between comparison");
                ArrayList<Text> db_TextList = new ArrayList<Text>();
                ArrayList<Text> project_TextList = new ArrayList<Text>();

                List<String> projectFilePaths = Database.getFileList(projectDir, false);

                ArrayList<String> temp;
                temp = Database.constructCache(minNumLines, debug, projectFilePaths, projectDir, forceRetokenization);
                errorList.addAll(temp);

                temp = Database.constructCache(minNumLines, debug, databaseFileList, databaseDir, forceRetokenization);
                errorList.addAll(temp);

                // only load the projects into memory
                System.out.println("\nLoading a total of " + projectFilePaths.size() + 
                                                " cached project files from \n" + projectDir);
                Database.loadCache(project_TextList, debug, projectFilePaths, projectDir);

                // Capture time
                cal = Calendar.getInstance();
                System.out.println("Start comparison @ " +  sdf.format(cal.getTime()) );

                // perform the comparison
                Compare comp = new Compare(minNumLines, databaseDir, projectDir);
                comp.installTextFiles(project_TextList, databaseFileList);
                output = comp.compareBetween(output, matchAlgorithm, gapSize, numberThreads);
                if (exportResults) {
                    output.saveResults(resultPath);
                }
            } else {
                output.loadResults(resultPath);
            }

            output.printResults(saveEmpty, similarityRange, enableSimilarity, matchMode, debug);
        }

        // Display all the errors
        displayError(errorList);

        // Display and save elapsed time
        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;
        String msg = ("Elapsed for " +
                TimeUnit.MINUTES.convert(elapsedTime, TimeUnit.NANOSECONDS) +
                " minutes");
        System.out.println(msg);
        // save elapsed time
        PrintWriter writer = new PrintWriter(outputDir + "log", "UTF-8");
        writer.println(msg);
        writer.close();

        // Display finish time
        Calendar cal2 = Calendar.getInstance();
        System.out.println("Finish @ " +  sdf.format(cal2.getTime()) );

        System.out.println("graceful exit...");

    }
}

