import org.apache.commons.io.FilenameUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;
import java.io.File;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import java.util.Collection;

import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import java.io.File;

import java.lang.Runtime;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder;
import java.io.InputStream;

import java.io.FilenameFilter;

public class Database {

    public static void listf(String directoryName, List<String> files) {
        File directory = new File(directoryName);
        String extName = ".java";
        // get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                //System.out.println(file);
                String strFileName = file.toString();
                if (strFileName.endsWith(extName)) {
                    files.add(strFileName);
                }
            } else if (file.isDirectory()) {
                listf(file.getAbsolutePath(), files);
            }
        }
    }

    public static List<String> generateFileList(String dir_name, String targetDBName) throws IOException {
        List<String> listNames = new ArrayList<String>();

        listf(dir_name, listNames);
        System.out.println("Done obtaining list of files!");

        // Serialize file and write to file
        serializeToFile(dir_name + targetDBName,listNames);

        return listNames;

    }

    private static void serializeToFile(String filePath, Object obj) {
        try {
            FileOutputStream fout = new FileOutputStream(filePath);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(obj);
            fout.close();
            oos.close();
        } catch (Exception e) {
            System.out.println("Error in serializeToFile for: " + filePath);
            System.exit(0);
        }

    }

    public static List<String> loadFileList(String filePath) throws IOException {

        List<String> listNames = null;
        FileInputStream fin = null;
        ObjectInputStream ois = null;

        try {
            // Load serialized file
            fin = new FileInputStream(filePath);
            ois = new ObjectInputStream(fin);
            listNames = (List<String>) ois.readObject();
        } catch (Exception e) {
            System.out.println("Error while loading file list: " + e);
            System.exit(0);
        } finally {
            if (ois != null) {
                ois.close();
            }
            if (fin != null) {
                fin.close();
            }
        }

        return listNames;

    }

    public static List<String> getFileList(String dir_name, boolean wrapCode) throws IOException {
        List<String> fileList = new ArrayList<String>();
        Path dir = FileSystems.getDefault().getPath(dir_name);
        DirectoryStream<Path> stream = Files.newDirectoryStream(dir);
        for (Path path : stream) {

            String absPath = path.toString();
            if (Files.isDirectory(path)) {
                // directory, callback this function to do it recursively
                List<String> new_file_list = getFileList(absPath, wrapCode);
                //System.out.println(new_file_list);
                fileList.addAll(new_file_list);
            } else {
                // file, check for the extension
                if (wrapCode == true) {
                    String extension = FilenameUtils.getExtension(absPath);
                    String java_ext = "map";
                    if (extension.equals(java_ext)) {
                        fileList.add(absPath);
                    }
                } else {
                    String extension = FilenameUtils.getExtension(absPath);
                    String java_ext = "java";
                    if (extension.equals(java_ext)) {
                        fileList.add(absPath);
                    }
                }
            }
        }
        stream.close();

        return fileList;
    }

    public static Text loadSingleFile (String filePath, String databaseDir, int minNumLines, boolean debug) {
        FileInputStream fin = null;
        ObjectInputStream ois = null;
        try {
            String dbPath = Text.getDBpath(filePath);

            // Load serialized file
            fin = new FileInputStream(dbPath);
            ois = new ObjectInputStream(fin);
            Text txt = (Text) ois.readObject();

            return txt;
        } catch (Exception e) {
            System.out.println("Error while loading single file\n" + filePath + "\n" + e);
            Text txt = repairDatabaseFile(minNumLines, debug, filePath, databaseDir);
            return txt;
        } finally {

            try {
                ois.close();
                fin.close();
            } catch (Exception e) {
                System.out.println("Error while closing stream\n" + e);
            }
        }
    }

    public static void loadCache (ArrayList<Text> textList,
            boolean debug, List<String> fileList, String dir_name) {
        FileInputStream fin = null;
        ObjectInputStream ois = null;
        try {
            int counter = 1;
            for (String absPath : fileList) {
                if (debug == true) {
                    System.out.println("\n>> Loading file #" + counter + "\n" + absPath);
                    System.out.println();
                } else {
                    System.out.print(counter + "\r");
                }

                // Load serialized file
                fin = new FileInputStream(Text.getDBpath(absPath));
                ois = new ObjectInputStream(fin);
                Text txt = (Text) ois.readObject();
                ois.close();

                textList.add(txt);

                counter++;
            }
        } catch (Exception e) {
            System.out.println(e);
            System.exit(0);
        } finally {

            try {
                if (fin != null) {
                    fin.close();
                }
                if (ois != null) {
                    ois.close();
                }
            } catch (Exception e) {
                System.out.println(e);
                System.exit(0);
            }
        }
    }

    private static Text repairDatabaseFile(int minNumLines,
            boolean debug, String absPath, String dir_name) {

        ArrayList<String> errorList = new ArrayList<String>();
        int counter = 0;
        Text txt;
        FileInputStream fin = null;
        ObjectInputStream ois = null;
        try {
            // Load serialized file
            fin = new FileInputStream(Text.getDBpath(absPath));
            ois = new ObjectInputStream(fin);
            txt = (Text) ois.readObject();
            ois.close();
        } catch (Exception e) {

            System.out.println("Trying to recover file: " + absPath);
            txt = new Text(absPath, dir_name);
            txt.tokenize(minNumLines, debug, errorList, dir_name);

            // Serialize file and write to file
            serializeToFile(Text.getDBpath(absPath), txt);
            System.out.println("Recovery successful");
        } finally {
            try {
                if (fin != null) {
                    fin.close();
                }
                if (ois != null) {
                    ois.close();
                }
            } catch (Exception e) {
                System.out.println(e);
                System.exit(0);
            }
        }
        return txt;
    }
    
    public static boolean checkTokenized(String fileAbsPath) {
        String fileDbPath = Text.getDBpath(fileAbsPath);
        File f = new File(fileDbPath);
        if (f.exists() && !f.isDirectory()) {
            return true;
        } else {
            return false;
        }
    }

    // tokenizes files
    public static ArrayList<String> constructCache(int minNumLines,
            boolean debug, List<String> fileList, String dir_name, boolean forceToken) {

        ArrayList<String> errorList = new ArrayList<String>();

        System.out.println("\nTokenizing a total of " + fileList.size() + " files from:");
        System.out.println(dir_name);
        int counter = 1;
        for (String absPath : fileList) {

            // check if it exists
            boolean exist = checkTokenized(absPath);
            if (exist == false || forceToken == true) {

                // debug message
                if (debug == true) {
                    System.out.println("\n>> Tokenizing file #" + counter + "\n" + absPath);
                    System.out.println();
                } else {
                    System.out.print(counter + "\r");
                }

                Text txt = new Text(absPath, dir_name);
                errorList = txt.tokenize(minNumLines, debug, errorList, dir_name);

                // Serialize file and write to file
                serializeToFile(Text.getDBpath(absPath), txt);
            } else {
                if (debug == true) {
                    System.out.println("Already tokenized, skipping:");
                    System.out.println(absPath);
                }
            }
            counter++;
        }

        return errorList;
    }


}
