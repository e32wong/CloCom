import java.util.List;
import java.util.ArrayList;

import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

public class Compare {

    ArrayList<Text> project;

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
        project = projectList;
        databasePaths = db_PathList;
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
                    textCompare(text1, text2, mode, gapSize);
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
                    textCompare(text1, text2, mode, gapSize);
                }
            }

        }

        /*
        for (int i = 0; i < databasePaths.size(); i++) {
            System.out.print((i+1) + "\r");
            Text text1 = Database.loadSingleFile(databasePaths.get(i), databaseDir, minNumLines, false);
            for (int j = i + 1; j < databasePaths.size(); j++) {

                Text text2 = Database.loadSingleFile(databasePaths.get(j), databaseDir, minNumLines, false);

                textCompare(text1, text2, mode, gapSize);
            }
        }
        */
        System.out.println("");
    }
    public void compareBetween (Output outputObject, int mode, int gapSize) {

        result = outputObject;

        System.out.println("\nComparing against " + databasePaths.size() + " database files");

        for (int i = 0; i < databasePaths.size(); i++) {
            // outer loop is the database
            Text text1 = Database.loadSingleFile(databasePaths.get(i), databaseDir, minNumLines, false);

            for (int j = 0; j < project.size(); j++) {
                // inner loop is the project
                Text text2 = project.get(j);
                textCompare(text2, text1, mode, gapSize);

            }

            System.out.print((i+1) + "\r");
        }
        System.out.println("");
    }

    private ArrayList<Coordinate> sortCoordinate(ArrayList<Coordinate> coorList) {
        ArrayList<Coordinate> newList = new ArrayList<Coordinate>();

        for (Coordinate c : coorList) {

        }

        return newList;
    }

    private int distance(int a, int b) {
        return (b-a);
    }

    private void extendChain (
            ArrayList<Chain> builtChain, HashMap<Chain, ArrayList<Chain>> gapMap,
            ArrayList<ArrayList<Chain>> masterChain) {
        
        Chain thisChain = builtChain.get(builtChain.size()-1);
        ArrayList<Chain> connectedList = gapMap.get(thisChain);
        if (connectedList != null) {
            for (int i = 0 ; i < connectedList.size(); i++) {
                ArrayList<Chain> newList = new ArrayList<Chain>(builtChain);
                newList.add(connectedList.get(i));
                extendChain(newList, gapMap, masterChain);
            }
        } else {
            masterChain.add(builtChain);
        }
    }

    private ArrayList<ArrayList<Chain>> buildChains(
            ArrayList<Chain> chainList, HashMap<Chain, ArrayList<Chain>> gapMap) {
        
        ArrayList<ArrayList<Chain>> masterChain = new ArrayList<ArrayList<Chain>>();

        for (int i = 0; i < chainList.size(); i++) {
            Chain thisChain = chainList.get(i);

            ArrayList<Chain> builtChain = new ArrayList<Chain>();
            builtChain.add(thisChain);

            extendChain(builtChain, gapMap, masterChain);
        }

        return masterChain;
    }

    public void textCompare(Text text1, Text text2, int mode, int gapSize) {

        for (int k = 0; k < text1.getNumMethods(); k++) {
            for (int l = 0; l < text2.getNumMethods(); l++) {
                if (mode == 0) {
                    // exact matching
                    ArrayList<Statement> statementRaw1 = text1.getRawStatements(k);
                    ArrayList<Statement> statementRaw2 = text2.getRawStatements(l);
                    int sizeX = statementRaw1.size();
                    int sizeY = statementRaw2.size();
                    
                    // x - statement1
                    // y - statement2
                    // Build matrix
                    HashMap<Coordinate, Integer> coorMap = new HashMap<Coordinate, Integer>();
                    ArrayList<Coordinate> coorList = new ArrayList<Coordinate>();
                    //matrix = new boolean[sizeX][sizeY];
                    for (int x = 0; x < sizeX; x++) {
                        for (int y = 0; y < sizeY; y++) {
                            if (statementRaw1.get(x).hashNumber == statementRaw2.get(y).hashNumber) {
                                Coordinate coor = new Coordinate(x,y);
                                coorMap.put(coor, statementRaw1.get(x).hashNumber);
                                coorList.add(coor);
                            }
                        }
                    }

                    // find the NG-clone
                    while (coorList.size() != 0) {
                        Coordinate thisCoor = coorList.get(0);

                        int longestLength = 1; 

                        int x = thisCoor.x + 1;
                        int y = thisCoor.y + 1;
                        Coordinate coorNext = new Coordinate(x,y);
                        while (coorMap.get(coorNext) != null) {

                            //remove from arrayList
                            coorList.remove(coorNext);

                            longestLength++;

                            x++;
                            y++;
                            coorNext = new Coordinate(x,y);

                        }
                        coorList.remove(thisCoor);

                        if (longestLength >= minNumLines) {
                            result.addClone(projectDir + text1.getDatabasePath(), 
                                    statementRaw1.get(thisCoor.x).startLine,
                                    statementRaw1.get(thisCoor.x + longestLength - 1).endLine,
                                    databaseDir + text2.getDatabasePath(), 
                                    statementRaw2.get(thisCoor.y).startLine,
                                    statementRaw2.get(thisCoor.y + longestLength - 1).endLine,
                                    longestLength,
                                    statementRaw1, thisCoor.x, thisCoor.x + longestLength - 1,
                                    statementRaw2, thisCoor.y, thisCoor.y + longestLength - 1,
                                    0);
                        }
                    } 
                } else {
                    // gapped matching
                    ArrayList<Statement> statementRaw1 = text1.getRawStatements(k);
                    ArrayList<Statement> statementRaw2 = text2.getRawStatements(l);
                    int sizeX = statementRaw1.size();
                    int sizeY = statementRaw2.size();

                    // populate the scatter plot
                    HashMap<Coordinate, Integer> coorMap = new HashMap<Coordinate, Integer>();
                    ArrayList<Coordinate> coorList = new ArrayList<Coordinate>();
                    for (int x = 0; x < sizeX; x++) {
                        for (int y = 0; y < sizeY; y++) {
                            if (statementRaw1.get(x).hashNumber == statementRaw2.get(y).hashNumber) {
                                Coordinate coor = new Coordinate(x,y);
                                coorMap.put(coor, statementRaw1.get(x).hashNumber);
                                coorList.add(coor);
                            }
                        }
                    }

                    // used to store the NG-clones
                    ArrayList<Chain> chainList = new ArrayList<Chain>();

                    // Detect NG-clones
                    while (coorList.size() != 0) {
                        Coordinate thisCoor = coorList.get(0);

                        int longestLength = 1;

                        // see how far we can go
                        Coordinate coorNext;
                        int currentX = thisCoor.x;
                        int currentY = thisCoor.y;
                        while (true) {
                            // try extend the chain
                            coorNext = new Coordinate(currentX + 1, currentY + 1);
                            if (coorMap.get(coorNext) != null) {

                                // remove this since it is 
                                // part of a NG-chain
                                coorList.remove(coorNext);

                                // can extend chain
                                currentX = currentX + 1;
                                currentY = currentY + 1;
                                longestLength++;
                                continue;
                            } else {
                                // there is a gap
                                break;
                            }
                        }

                        // add to chain list
                        Chain c = new Chain(thisCoor.x, thisCoor.y, 
                                currentX, currentY, longestLength);
                        //System.out.println(thisCoor.x + "!" + thisCoor.y);
                        //System.out.println(currentX + "!" + currentY);
                        //System.out.println(longestLength);
                        chainList.add(c);
                        // remove this point
                        coorList.remove(thisCoor);
                    }

                    // now have a list of NG chains
                    // create a hashmap of gaps
                    HashMap<Chain, ArrayList<Chain>> gapMap = new HashMap<Chain, ArrayList<Chain>>();
                    for (int i = 0; i < chainList.size(); i++) {
                        Chain c_i = chainList.get(i);

                        // search for connections
                        for (int j = i + 1; j < chainList.size(); j++) {
                            Chain c_j = chainList.get(j);
                            
                            // check if the two are near
                            int dx = distance(c_i.x2, c_j.x1);
                            int dy = distance(c_i.y2, c_j.y1);
                            if ( ((0 < dx) && (dx <= gapSize)) &&
                                    ((0 < dy) && (dy <= gapSize)) ) {
                                // satistified gap requirement
                                ArrayList<Chain> c_i_list = gapMap.get(c_j);
                                if (c_i_list == null) {
                                    c_i_list = new ArrayList<Chain>();
                                    c_i_list.add(c_j);
                                    gapMap.put(c_i, c_i_list);
                                } else {
                                    c_i_list.add(c_j);
                                }
                            } else {
                                // optimization
                                if (distance(c_i.x2, c_j.y1) > gapSize) {
                                    break;
                                }
                            }
                        }
                    }

                    // now have a list of gaps
                    // construct the longest chain
                    ArrayList<ArrayList<Chain>> masterList = buildChains(chainList, gapMap);
                    //ArrayList<ArrayList<Chain>> filteredList = new ArrayList<ArrayLIst<Chain>>();

                    // check the length
                    for (ArrayList<Chain> list : masterList) {
                        // calculate the total length of the match excluding the gaps
                        // and prune out repetitive matches
                        int totalLength = 0;
                        int totalHashValue = 0;
                        boolean isRepetitive = true;
                        // assume there is at least one line and one chain
                        int baseline = statementRaw1.get(list.get(0).x1).hashNumber;
                        for (Chain c : list) {
                            // sum up the length
                            totalLength = totalLength + c.size;

                            // check on the master side
                            for (int i = c.x1; i < c.x2; i++) {
                                // check repetitiveness
                                if (isRepetitive == true) {
                                    if (statementRaw1.get(i).hashNumber != baseline) {
                                        isRepetitive = false;
                                    }
                                }

                                // sum up the value
                                totalHashValue = totalHashValue + statementRaw1.get(i).hashNumber;
                            }
                        }

                        if (totalLength >= minNumLines && isRepetitive == false) {

                            // get first element for the starting point
                            int file1Start = list.get(0).x1;
                            int file1End = list.get(list.size()-1).x2;

                            // get last element for the ending point
                            int file2Start = list.get(0).y1;
                            int file2End = list.get(list.size()-1).y2;
                            result.addClone(projectDir + text1.getDatabasePath(),
                                    statementRaw1.get(file1Start).startLine,
                                    statementRaw1.get(file1End).startLine,
                                    databaseDir + text2.getDatabasePath(),
                                    statementRaw2.get(file2Start).startLine,
                                    statementRaw2.get(file2End).startLine,
                                    totalLength,
                                    statementRaw1, file1Start, file1End,
                                    statementRaw2, file2Start, file2End,
                                    totalHashValue);
                        }
                    }
                }
            }
        }
    }

    static class Coordinate {
        public int x, y;
        Coordinate(int x, int y) { this.x = x; this.y = y; }
        public boolean equals(Object obj) {
            Coordinate other = (Coordinate) obj;
            return ((this.x == other.x) && (this.y == other.y));
        }
        public int hashCode() {
            return this.x ^ this.y;
        }
    } 

    static class Chain {
        public int x1, y1, x2, y2;
        public int size;
        Chain(int x1, int y1, int x2, int y2, int size) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.size = size;
        }
        public boolean equals(Object obj) {
            Chain other = (Chain) obj;
            return ((this.x1 == other.x1) && (this.y1 == other.y1) &&
                    (this.x2 == other.x2) && (this.y2 == other.y2) &&
                    this.size == other.size);
        }
        public int hashCode() {
            return this.x1 ^ this.y1 + this.x2 ^ this.y2 + size;
        }
    }
    
    static class Gap {
        Chain c1;
        Chain c2;
        Gap(Chain c1, Chain c2) {
            this.c1 = c1;
            this.c2 = c2;
        }
        public boolean equals(Object obj) {
            Gap other = (Gap) obj;
            return ((this.c1 == other.c1) && (this.c2 == other.c2));
        }
        public int hashCode() {
            return c1.hashCode() + c2.hashCode();
        }
    }
}



