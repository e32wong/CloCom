import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;

import java.io.PrintWriter;
import java.io.FileNotFoundException;

class FrequencyMap {

    int aprioriMinSupport = 0;

    HashSet<String> masterNameSet = new HashSet<String>();
    ArrayList<HashSet<String>> table = new ArrayList<HashSet<String>>();

    public FrequencyMap (int aprioriMinSupportIn) {

        aprioriMinSupport = aprioriMinSupportIn;

    }

    public void addInstance(HashSet<String> setString) {
        masterNameSet.addAll(setString);
        table.add(setString);
    }

    public void exportTable (String fileName) {

        List<String> orderedList = new ArrayList<String>();
        orderedList.addAll(masterNameSet);

        PrintWriter writer;
        try {
            writer = new PrintWriter("config.txt", "UTF-8");

            writer.println(orderedList.size());
            writer.println(table.size());
            writer.println("2");
            writer.close();

            writer = new PrintWriter(fileName, "UTF-8");

            for (String str : orderedList) {
                writer.print(str + ",");
            }
            writer.println("");

            for (HashSet<String> instance : table) {
                for (String str : orderedList) {
                    if (instance.contains(str)) {
                        writer.print("1 ");
                    } else {
                        writer.print("0 ");
                    }
                }
                writer.println("");
            }

            writer.close();
        } catch (Exception e) {
            System.out.println("Error while exporting: " + e);
        }

        System.out.println("Config:");
        System.out.println("number of items - " + orderedList.size());
        System.out.println("number of transactions - " + table.size());

        try {
            AprioriProcess process = new AprioriProcess("config.txt", "table.txt");
        } catch (Exception e) {
            System.out.println("Exception in Apriori: " + e);
        }

    }

    public void printTable () {

        List<String> orderedList = new ArrayList<String>();
        orderedList.addAll(masterNameSet);

        for (String str : orderedList) {
            System.out.print(str + " ");
        }
        System.out.println("");

        for (HashSet<String> instance : table) {
            for (String str : orderedList) {
                if (instance.contains(str)) {
                    System.out.print("1 ");
                } else {
                    System.out.print("0 ");
                }
            }
            System.out.println("");
        }
    }

}
