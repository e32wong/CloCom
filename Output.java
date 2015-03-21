import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.charset.Charset;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class Output {

    ArrayList<MatchGroup> matchGroupList = new ArrayList<MatchGroup>();
    int algorithmMode;
    boolean enableRepetitive;
    boolean enableOneMethod;
    int matchMode;

    public Output (int alogrithm, 
            boolean enableRepetitiveIn, 
            boolean enableOneMethodIn,
            int matchModeIn) {
        enableRepetitive = enableRepetitiveIn;
        enableOneMethod = enableOneMethodIn;
        algorithmMode = alogrithm;
        matchMode = matchModeIn;
    }

    // file coverage, start-end line
    // statement hash number, start-end
    // method line coverage, start-end line
    public void addClone(String file1, int lineStart1, int lineEnd1, 
            String file2, int lineStart2, int lineEnd2, int length,
            ArrayList<Statement> statementRaw1, int statementStart1, int statementEnd1,
            ArrayList<Statement> statementRaw2, int statementStart2, int statementEnd2) {
        if (algorithmMode == 0) {
            // check for hashing error during the group hash process
            boolean status = Analyze.hasHashError(
                    statementRaw1.subList(statementStart1, statementEnd1), 
                    statementRaw2.subList(statementStart2, statementEnd2));
            if (status ==  true) {
                return;
            }

            // check for repetitive statements
            if (enableRepetitive) {
                if (Analyze.isRepetitive(statementRaw1.subList(statementStart1, statementEnd1)) == true) {
                    return;
                }
            }

            // require at least one method call
            if (enableOneMethod) {
                if (Analyze.checkNumMethods(statementRaw1.subList(statementStart1, statementEnd1), 1) == false) {
                    return;
                }
            }

            // check for valid scope
            //if (Analyze.hasValidScope(statementRaw1.subList(statementStart1, statementEnd1)) == false) {
            //    return;
            //}
        }

        boolean added = false;
        if (matchMode == 0) {
            for (MatchGroup matchGroup : matchGroupList) {
                boolean status1 = matchGroup.checkMatchExist(file1, lineStart1, lineEnd1, 0);
                boolean status2 = matchGroup.checkMatchExist(file2, lineStart2, lineEnd2, 1);
                if (status1 == true && status2 == false) {
                    // add as a clone
                    matchGroup.addMatch(1, file2, lineStart2, lineEnd2, 
                            statementRaw2, statementStart2, statementEnd2);
                    added = true;
                    break;
                } else if (status1 == false && status2 == true) {
                    // add as a master
                    matchGroup.addMatch(0, file1, lineStart1, lineEnd1,
                            statementRaw1, statementStart1, statementEnd1);
                    added = true;
                    break;
                } else if (status1 == true && status2 == true) {
                    added = true;
                    break;
                }
            }
        
            // status 1 and 2 are false
            if (added == false) {
                MatchGroup matchGroup = new MatchGroup(length);

                matchGroup.addMatch(0, file1, lineStart1, lineEnd1,
                        statementRaw1, statementStart1, statementEnd1);
                matchGroup.addMatch(1, file2, lineStart2, lineEnd2,
                        statementRaw2, statementStart2, statementEnd2);

                matchGroupList.add(matchGroup);
            }
        } else {
            // full mesh
            for (MatchGroup matchGroup : matchGroupList) {
                boolean status1 = matchGroup.checkMatchExist(file1, lineStart1, lineEnd1, 2);
                boolean status2 = matchGroup.checkMatchExist(file2, lineStart2, lineEnd2, 2);

                if (status1 == true && status2 == false) {
                    // add as a clone
                    matchGroup.addMatch(1, file2, lineStart2, lineEnd2,
                            statementRaw2, statementStart2, statementEnd2);
                    added = true; 
                    break;
                } else if (status1 == false && status2 == true) {
                    // add as a clone
                    matchGroup.addMatch(1, file1, lineStart1, lineEnd1,
                            statementRaw1, statementStart1, statementEnd1);
                    added = true;
                    break;
                } else if (status1 == true && status2 == true) {
                    added = true;
                    break;
                }
            }

            // status 1 and 2 are false
            if (added == false) {
                MatchGroup matchGroup = new MatchGroup(length);
            
                matchGroup.addMatch(0, file1, lineStart1, lineEnd1,
                        statementRaw1, statementStart1, statementEnd1);
                matchGroup.addMatch(1, file2, lineStart2, lineEnd2,
                        statementRaw2, statementStart2, statementEnd2);

                matchGroupList.add(matchGroup);
            }
        }
    }
    
    public void saveResults(String path) {
        try {
            // Serialize file and write to file
            FileOutputStream fout = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(matchGroupList);
            oos.close();
        } catch (Exception e) {
            System.out.println("Error while writing results\n" + e);
            System.exit(0);
        }
    }

    public void loadResults(String path) {
        try {
            // Load serialized file
            FileInputStream fin = new FileInputStream(path);
            ObjectInputStream ois = new ObjectInputStream(fin);
            matchGroupList = (ArrayList<MatchGroup>) ois.readObject();
            ois.close();
        } catch (Exception e) {
            System.out.println("Error while loading results\n" + e);
            System.exit(0);
        }
    }

    public void processOutputTerms (FrequencyMap fMap) {
        for (MatchGroup thisMatchGroup : matchGroupList) {
            thisMatchGroup.dumpTerms(fMap);
        }
    }


    public void printResults(boolean removeEmpty, 
            int similarityRange, 
            boolean enableSimilarity,
            int matchMode) {

        DescriptiveStatistics statsInternalClones = new DescriptiveStatistics();
        DescriptiveStatistics statsExternalClones = new DescriptiveStatistics();
        int sumInternalClones = 0;
        int sumExternalClones = 0;

        int sumExternalClonesComment = 0;

        int numMatchesWithComment = 0;
        int matchIndex = 0;
        for (MatchGroup thisMatchGroup : matchGroupList) {
            System.out.println("Match Group " + matchIndex + " of size " + 
                    thisMatchGroup.getMasterSize() + "+" + thisMatchGroup.getCloneSize());
            
            thisMatchGroup.mapCode2Comment();
            thisMatchGroup.pruneComments(similarityRange, enableSimilarity);

            thisMatchGroup.pruneDuplicateComments();

            boolean hasComment = thisMatchGroup.hasComment();

            if (hasComment == false && removeEmpty == true) {
                // do nothing
            } else {
                // ranking alogrithm requires a list of similarity terms
                if (enableSimilarity) {
                    thisMatchGroup.printRankedComments();
                }
                thisMatchGroup.printAllMappings(removeEmpty, matchMode);

                numMatchesWithComment++;
            }

            System.out.println("\n\n");
            matchIndex++;
        }

        System.out.println(numMatchesWithComment + " comment groups has a comment");
    }

}



