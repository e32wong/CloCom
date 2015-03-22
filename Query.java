import java.util.ArrayList;
import java.util.List;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.PrintWriter;
public class Query {

    public static void main (String[] args) {
        
        String firstLine = "";
        try (BufferedReader br = new BufferedReader(new FileReader("table.txt"))) {
            StringBuilder sb = new StringBuilder();

            // first line is the labels
            firstLine = br.readLine();

        } catch (Exception e) {
            System.out.println("Error while loading first line");
        }

        while (true) {
            String[] queryList;

            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                System.out.print("Enter String: ");
                String termList = br.readLine();
                if (termList.equals("")) {
                    continue;
                }
                
                queryList = termList.split(" ");

                createTransactionDb(queryList, firstLine);

            } catch (Exception e) {
                System.out.println("Error while parsing input: " + e);
            }
        }
    }

    private static void createTransactionDb (String[] queryList, String firstLine) {

        String[] listLabels = firstLine.split(",");

        // find out what index are we dealing with
        ArrayList<Integer> listIndexes = new ArrayList<Integer>();
        for (String queryTerm : queryList) {
            // get its index
            int index = 0;
            for (String label : listLabels) {
                if (label.equals(queryTerm)) {
                    listIndexes.add(index);
                    //System.out.println(index);
                }
                index++;
            }
        }

        BufferedReader br = null;
        PrintWriter writer = null;
        PrintWriter writer2 = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream("table.txt"), "UTF8"));
             //       new FileReader("table.txt"));
            writer = new PrintWriter("table2.txt", "UTF-8");
            writer2 = new PrintWriter("config2.txt", "UTF-8");

            int numInstances = 0;
            writer.println(firstLine);
            String line = br.readLine();
            line = br.readLine();
            while (line != null) {
                String[] listValues = line.split(" ");

                boolean allExist = true;
                for (int index : listIndexes) {
                    if (listValues[index].equals("0")) {
                        allExist = false;
                        break;
                    } 
                }

                if (allExist) {
                    writer.println(line);
                    numInstances++;
                }

                line = br.readLine();
            }

            String minSupportStr = "20";
            writer2.println(listLabels.length);
            writer2.println(numInstances);
            writer2.println(minSupportStr);
            System.out.println("Number of items: " + listLabels.length);
            System.out.println("Number of instances: " + numInstances);
            System.out.println("Minimum support: " + minSupportStr);

            if (br != null) {
                br.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (writer2 != null) {
                writer2.close();
            }
        } catch (Exception e) {
            System.out.println("Exception in createTransactionDb: " + e);
        } 

        try {
            AprioriProcess process = new AprioriProcess("config2.txt", "table2.txt");
        } catch (Exception e) {
            System.out.println("Exception in Apriori: " + e);
        }

    }

}
