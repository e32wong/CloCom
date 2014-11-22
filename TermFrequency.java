import java.util.Comparator;
import java.util.Iterator;
import java.util.Collections;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.Statement;

import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

import java.util.Set;
import java.util.HashSet;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;


class TermFrequency {

    HashMap<String, Integer> termMapList = new HashMap<String, Integer>();
    LinkedHashMap<String, Integer> linkedTermMap;

    public void buildFrequencyMap(String path) {

        NLP nlp = new NLP();

        Set<String> nameSet = new HashSet<String>();

        try {
            List<String> listFiles = Database.getFileList(path);
            System.out.println("Parsing " + listFiles.size() 
                    + " files in the DB for simple names, this is usually fast.");

            for (String filePath : listFiles) {
                parseAST2Tokens(filePath, nameSet);
            }
        } catch (Exception e) {
            System.out.println("Exception while building a set " + e);
            System.exit(0);
        }

        System.out.println("Perfoming NLP on the terms and counting frequency of occurance.");
        for (String term : nameSet) {
            String seperatedTerm = Utilities.splitCamelCaseString(term);
            ArrayList<String> listNouns = nlp.getNouns(seperatedTerm);
            for (String nounTerm : listNouns) {
                Integer counter = termMapList.get(nounTerm);
                if (counter != null) {
                    termMapList.put(nounTerm, counter.intValue() + 1);
                } else {
                    termMapList.put(nounTerm, 1);
                }
            }
        }

        // sort the map
        linkedTermMap = new LinkedHashMap<String, Integer>();
        List<String> keys = new ArrayList<String>(termMapList.keySet());  
        Collections.sort(keys,
                new Comparator() {
                    public int compare(Object left, Object right) {
                        String leftKey = (String) left;
                        String rightKey = (String) right;

                        Integer leftValue = (Integer)termMapList.get(leftKey);
                        Integer rightValue = (Integer)termMapList.get(rightKey);

                        return leftValue.compareTo(rightValue);
                    }
                });
        for (Iterator i = keys.iterator(); i.hasNext();) {
            String k = (String) i.next();
            linkedTermMap.put(k, termMapList.get(k));

            // debug
            System.out.println(k + " " + termMapList.get(k));
        }

        try {
            // Serialize file and write to file
            FileOutputStream fout = new FileOutputStream("./termFreq.out");
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(linkedTermMap);
            oos.close();
        } catch (IOException e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    public void loadFrequencyMap() {
        try {
            // Load serialized file
            FileInputStream fin = new FileInputStream("./termFreq.out");
            ObjectInputStream ois = new ObjectInputStream(fin);
            linkedTermMap = (LinkedHashMap<String, Integer>) ois.readObject();
            ois.close();
        } catch (Exception e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    /* Convert a file into a String */
    private static String fileToString(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

	  /* Convert files that are in String format to AST */
    public void parseAST2Tokens(String absPath, Set<String> nameSet) {
        String source = "";
        try {
            source = fileToString(absPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ASTParser parser = ASTParser.newParser(AST.JLS4);

        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        char[] content = source.toCharArray();
        parser.setSource(content);
        parser.setUnitName(absPath);
        Map<String, String> options = JavaCore.getOptions();
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_7);
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM,
            JavaCore.VERSION_1_7);
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_7);
        
        // new String[] {"UTF-8"}
            String[] sources = {};
          String[] classPaths = {};
        parser.setEnvironment(classPaths, sources,
            null, true);
        //parser.setBindingsRecovery(true);
        parser.setResolveBindings(false);
        parser.setCompilerOptions(options);
        parser.setStatementsRecovery(true);

        //System.out.println(absPath);

        try {
            final CompilationUnit unit = (CompilationUnit) parser.createAST(null);
            final AST ast = unit.getAST();

            // Process the main body
            try {
                unit.accept(new ASTVisitor() {

                    public boolean visit(SimpleName node) {

                        String simpleName = node.toString();
                        if (simpleName.length() >= 3) {
                            nameSet.add(node.toString());
                        }

                        return true;
                    }
       
                });
            } catch (Exception e) {
                System.out.println("Crashed while processing : " + absPath);
                System.out.println("Problem : " + e.toString());
                e.printStackTrace();
                System.exit(0);
            }

        } catch (Exception e) {
            System.out.println("\nError while executing compilation unit : " + e.toString());
        }
    }
}
