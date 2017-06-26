import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import java.io.File;
import org.xml.sax.*;
import org.w3c.dom.*;

import java.util.ArrayList;
import java.io.FileOutputStream;
import java.io.IOException;

public class ConfigFile{
    public int minNumLines = 3;
    public String database = null;
    public int databaseFormat = 0;
    public String project = null;
    public String outputDir = null;
    public int matchAlgorithm = 0;
    public int matchMode = 0;
    public int meshBlockSize = 0;
    public boolean debug = false;
    public boolean removeEmpty = false;
    public boolean forceRetokenization = true;
    public int gapSize = 0;
    public String resultPath = null;
    public boolean exportResults = false;
    public boolean loadResults = false;
    public int similarityRange = 0;
    public boolean enableSimilarity = true;
    public boolean enableRepetitive = false;
    public boolean enableOneMethod = false;
    public boolean buildTFIDF = false;
    public int numberThreads = 0;
    public boolean loadDatabaseFilePaths = false;
    public int aprioriMinSupport = 0;
    public boolean enableQuery = false;

    private String getTextValue(Element doc, String tag) {
        String value = null;
        NodeList nl;
        nl = doc.getElementsByTagName(tag);
        if (nl.getLength() > 0 && nl.item(0).hasChildNodes()) {
            value = nl.item(0).getFirstChild().getNodeValue();
        }
        return value;
    }

    private void loadHeuristic(Element doc) {

        NodeList nl1;
        Element firstNode;

        NodeList nl2;
        Element secondNode;

        String value;

        // process text similarity
        nl1 = doc.getElementsByTagName("similarity");
        firstNode = (Element) nl1.item(0);

        nl2 = firstNode.getElementsByTagName("similarityRange");
        secondNode = (Element) nl2.item(0);
        value = secondNode.getFirstChild().getNodeValue();
        if (value.equals("local")) {
            similarityRange = 0;
        } else if (value.equals("extended")) {
            similarityRange = 1;
        } else {
            System.out.println("Invalid similarityRange option, must be local/extended");
            System.exit(0);
        }
        System.out.println("Text similarity range: " + similarityRange);

        nl2 = firstNode.getElementsByTagName("enableSimilarity");
        secondNode = (Element) nl2.item(0);
        value = secondNode.getFirstChild().getNodeValue();
        if (value.equals("true")) {
            enableSimilarity = true;
        } else if (value.equals("false")) {
            enableSimilarity = false;
        } else {
            System.out.println("Invalid enableSimilarity option, must be true/false");
            System.exit(0);
        }
        System.out.println("Text similarity status: " + enableSimilarity);

        // process other heuristics
        nl1 = doc.getElementsByTagName("others");
        firstNode = (Element) nl1.item(0);

        nl2 = firstNode.getElementsByTagName("enableRepetitive");
        secondNode = (Element) nl2.item(0);
        value = secondNode.getFirstChild().getNodeValue();
        if (value.equals("true")) {
            enableRepetitive = true;
        } else if (value.equals("false")) {
            enableRepetitive = false;
        } else {
            System.out.println("Invalid enableRepetitive option, must be true/false");
            System.exit(0);
        }

        nl2 = firstNode.getElementsByTagName("enableOneMethod");
        secondNode = (Element) nl2.item(0);
        value = secondNode.getFirstChild().getNodeValue();
        if (value.equals("true")) {
            enableOneMethod = true;
        } else if (value.equals("false")) {
            enableOneMethod = false;
        } else {
            System.out.println("Invalid enableOneMethod option, must be true/false");
            System.exit(0);
        }
    }

    private void loadMatching(Element doc) {

        NodeList nl1 = doc.getElementsByTagName("matching");
        Element firstNode = (Element) nl1.item(0);

        NodeList nl2;
        Element secondNode;
        String value;

        nl2 = firstNode.getElementsByTagName("minNumLines");
        secondNode = (Element) nl2.item(0);
        value = secondNode.getFirstChild().getNodeValue();
        minNumLines = Integer.parseInt(value);
        System.out.println("Min num lines: " + minNumLines);

        nl2 = firstNode.getElementsByTagName("matchAlgorithm");
        secondNode = (Element) nl2.item(0);
        value = secondNode.getFirstChild().getNodeValue();
        matchAlgorithm = Integer.parseInt(value);
        if (matchAlgorithm != 0 && matchAlgorithm != 1) {
            System.out.println("Invalid alogrithm choice, must be 1 or 0");
            System.exit(0);
        }
        if (matchAlgorithm == 1) {
            System.out.println("Algorithm: Gapped - " + matchAlgorithm);
        } else {
            System.out.println("Algorithm: Non-gapped - " + matchAlgorithm);
        }

        nl2 = firstNode.getElementsByTagName("matchMode");
        secondNode = (Element) nl2.item(0);
        value = secondNode.getFirstChild().getNodeValue();
        matchMode = Integer.parseInt(value);
        if (matchMode != 0 && matchMode != 1) {
            System.out.println("Invalid matching mode, must be 1 (mesh) or 0 (between)");
            System.exit(0);
        }
        if (matchMode == 0) {
            System.out.println("Match Mode: Between Comparison - " + matchMode);
        } else {
            System.out.println("Match Mode: Full Mesh Comparison - " + matchMode);
        }

        nl2 = firstNode.getElementsByTagName("meshBlockSize");
        secondNode = (Element) nl2.item(0);
        value = secondNode.getFirstChild().getNodeValue();
        meshBlockSize = Integer.parseInt(value);
        System.out.println("Mesh matching block size: " + meshBlockSize);

        nl2 = firstNode.getElementsByTagName("numberThreads");
        secondNode = (Element) nl2.item(0);
        value = secondNode.getFirstChild().getNodeValue();
        numberThreads = Integer.parseInt(value);
        if (numberThreads < 0) {
            System.out.println("Invalid number of threads, must be 0 or larger");
            System.exit(0);
        }
        System.out.println("Number of threads: " + value);

        nl2 = firstNode.getElementsByTagName("gapSize");
        secondNode = (Element) nl2.item(0);
        value = secondNode.getFirstChild().getNodeValue();
        gapSize = Integer.parseInt(value);
        if (gapSize < 1) {
            System.out.println("Invalid gap size, must be 1 or higher");
            System.out.println("A value of 1 means no gaps are allowed");
            System.exit(0);
        }
        System.out.println("Gap size: " + gapSize);

    }

    private void loadFrequencySet(Element doc) {

        NodeList nl1 = doc.getElementsByTagName("frequencySet");
        Element firstNode = (Element) nl1.item(0);

        NodeList nl2;
        Element secondNode;
        String value;

        nl2 = firstNode.getElementsByTagName("aprioriMinSupport");
        secondNode = (Element) nl2.item(0);
        value = secondNode.getFirstChild().getNodeValue();
        aprioriMinSupport = Integer.parseInt(value);
        if (aprioriMinSupport < 0) {
            System.out.println("Invalid minimum apriori support, must be larger than 0");
            System.exit(0);
        }
        System.out.println("Apriori Minimum Support: " + aprioriMinSupport);
    }

    private void loadProjects(Element doc) {

        NodeList nl1 = doc.getElementsByTagName("projects");
        Element firstNode = (Element) nl1.item(0);

        NodeList nl2;
        Element secondNode;
        String value;

        nl2 = firstNode.getElementsByTagName("database");
        secondNode = (Element) nl2.item(0);
        database = secondNode.getFirstChild().getNodeValue();
        System.out.println("Database path: " + database);

        /*
        nl2 = firstNode.getElementsByTagName("databaseFormat");
        secondNode = (Element) nl2.item(0);
        value = secondNode.getFirstChild().getNodeValue();
        databaseFormat = Integer.parseInt(value);
        if (databaseFormat != 0 && databaseFormat != 1) {
            System.out.println("Invalid database format, must be 0 (source) or 1 (autocomment)");
            System.exit(0);
        }
        if (databaseFormat == 0) {
            System.out.println("Database format: source code");
        } else if (databaseFormat == 1) {
            System.out.println("Database format: autocomment");
        } else if (databaseFormat == 2) {
            System.out.println("Database format: source code + autocomment");
        }*/

        nl2 = firstNode.getElementsByTagName("project");
        secondNode = (Element) nl2.item(0);
        project = secondNode.getFirstChild().getNodeValue();
        System.out.println("Project path: " + project);

        nl2 = firstNode.getElementsByTagName("outputDir");
        secondNode = (Element) nl2.item(0);
        outputDir = secondNode.getFirstChild().getNodeValue();
        System.out.println("Output path: " + outputDir);

        nl2 = firstNode.getElementsByTagName("forceRetokenization");
        secondNode = (Element) nl2.item(0);
        value = secondNode.getFirstChild().getNodeValue();
        if (value.equals("true")) { 
            forceRetokenization = true; 
        } else if (value.equals("false")) { 
            forceRetokenization = false; 
        } else { 
            System.out.println("Invalid forceRetokenization option, must be true/false"); 
            System.exit(0); 
        }
        System.out.println("Build database: " + forceRetokenization);

        nl2 = firstNode.getElementsByTagName("buildTFIDF");
        secondNode = (Element) nl2.item(0);
        value = secondNode.getFirstChild().getNodeValue();
        if (value.equals("true")) {
            buildTFIDF = true;
        } else if (value.equals("false")) {
            buildTFIDF = false;
        } else {
            System.out.println("Invalid buildTFIDF option, must be true/false");
            System.exit(0);
        }
        System.out.println("Build tf-idf: " + buildTFIDF);

        nl2 = firstNode.getElementsByTagName("loadDatabaseFilePaths");
        secondNode = (Element) nl2.item(0);
        value = secondNode.getFirstChild().getNodeValue();
        if (value.equals("true")) {
            loadDatabaseFilePaths = true;
        } else if (value.equals("false")) {
            loadDatabaseFilePaths = false;
        } else {
            System.out.println("Invalid loadDatabaseFilePaths option, must be true/false");
            System.exit(0);
        }
        System.out.println("Load cached database path list: " + loadDatabaseFilePaths);

    }

    private void loadOutputSettings(Element doc) {
    
        NodeList nl1 = doc.getElementsByTagName("outputSettings");
        Element firstNode = (Element) nl1.item(0);
        
        NodeList nl2;
        Element secondNode;
        String value;
        
        nl2 = firstNode.getElementsByTagName("debug");
        secondNode = (Element) nl2.item(0);
        value = secondNode.getFirstChild().getNodeValue();
        if (value.equals("true")) {
            debug = true;
        } else if (value.equals("false")) {
            debug = false;
        } else {
            System.out.println("Invalid debug option, must be true/false");
            System.exit(0);
        }
        System.out.println("Debug: " + debug);
        
        nl2 = firstNode.getElementsByTagName("removeEmpty");
        secondNode = (Element) nl2.item(0);
        value = secondNode.getFirstChild().getNodeValue();
        if (value.equals("true")) {
            removeEmpty = true;
        } else if (value.equals("false")) {
            removeEmpty = false;
        } else {
            System.out.println("Invalid removeEmpty option, must be true/false");
            System.exit(0);
        }
        System.out.println("Remove empty: " + removeEmpty);
        
        nl2 = firstNode.getElementsByTagName("exportResults");
        secondNode = (Element) nl2.item(0);
        value = secondNode.getFirstChild().getNodeValue();
        if (value.equals("true")) {
            exportResults = true;
        } else if (value.equals("false")) {
            exportResults = false;
        } else {
            System.out.println("Invalid exportResults option, must be true/false");
            System.exit(0);
        }
        System.out.println("Export results: " + exportResults);

        if (exportResults) {
            nl2 = firstNode.getElementsByTagName("resultPath");
            secondNode = (Element) nl2.item(0);
            resultPath = secondNode.getFirstChild().getNodeValue();
            System.out.println("Result path: " + resultPath);
        }

        nl2 = firstNode.getElementsByTagName("enableQuery");
        secondNode = (Element) nl2.item(0);
        value = secondNode.getFirstChild().getNodeValue();
        if (value.equals("true")) {
            enableQuery = true;
        } else if (value.equals("false")) {
            enableQuery = false;
        } else {
            System.out.println("Invalid enableQuery option, must be true/false");
            System.exit(0);
        }
        System.out.println("Query mode: " + enableQuery);

    }

    public void loadConfig(String filePath) {
        Document dom;
        // Make an  instance of the DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use the factory to take an instance of the document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // parse using the builder to get the DOM mapping of the    
            // XML file
            dom = db.parse(filePath);

            Element doc = dom.getDocumentElement();

            String value = getTextValue(doc, "loadResults");
            if (value.equals("true")) {
                loadResults = true;
            } else if (value.equals("false")) {
                loadResults = false;
            } else {
                System.out.println("Invalid loadResults option, must be true/false");
                System.exit(0);
            }
            System.out.println("Load results: " + loadResults);

            try {
                loadFrequencySet(doc);
                loadMatching(doc);
                loadHeuristic(doc);
                loadProjects(doc);
                loadOutputSettings(doc);
            } catch (Exception e) {
                System.out.println(e);
                System.out.println("Error while parsing the xml config, maybe missing entry");
                System.exit(0);
            }

        } catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
        } catch (SAXException se) {
            System.out.println(se.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }

    }


    public static void writeBaseline(String filePath) {
        /*
        Document dom;
        Element e = null;

        // instance of a DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use factory to get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // create instance of DOM
            dom = db.newDocument();

            // create the root element
            Element rootEle = dom.createElement("configuration");
            // create data elements and place them under root
            e = dom.createElement("numLines");
            e.appendChild(dom.createTextNode("3"));
            rootEle.appendChild(e);

            e = dom.createElement("database");
            e.appendChild(dom.createTextNode("path A"));
            rootEle.appendChild(e);

            e = dom.createElement("project");
            e.appendChild(dom.createTextNode("path B"));
            rootEle.appendChild(e);

            e = dom.createElement("algorithm");
            e.appendChild(dom.createTextNode("A"));
            rootEle.appendChild(e);

            e = dom.createElement("debug");
            e.appendChild(dom.createTextNode("A"));
            rootEle.appendChild(e);

            e = dom.createElement("removeEmpty");
            e.appendChild(dom.createTextNode("false"));
            rootEle.appendChild(e);

            e = dom.createElement("buildDatabase");
            e.appendChild(dom.createTextNode("false"));
            rootEle.appendChild(e);

            e = dom.createElement("gapSize");
            e.appendChild(dom.createTextNode("false"));
            rootEle.appendChild(e);

            e = dom.createElement("resultPath");
            e.appendChild(dom.createTextNode("path C"));
            rootEle.appendChild(e);

            e = dom.createElement("loadResults");
            e.appendChild(dom.createTextNode("false"));
            rootEle.appendChild(e);

            e = dom.createElement("similarityRange");
            e.appendChild(dom.createTextNode("extended"));
            rootEle.appendChild(e);

            dom.appendChild(rootEle);

            try {
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "config.dtd");
                tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                // send DOM to file
                File file = new File(filePath);
                if (file.exists()) {
                    System.out.println("File exists, please remove existing file first");
                    return;
                }
                FileOutputStream fos = new FileOutputStream(file);
                DOMSource source = new DOMSource(dom);
                StreamResult result = new StreamResult(fos);
                tr.transform(source, result);

            } catch (TransformerException te) {
                System.out.println("Transformer exception: " + te.getMessage());
            } catch (IOException ioe) {
                System.out.println("IOException: " + ioe.getMessage());
            }
        } catch (ParserConfigurationException pce) {
            System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
        }
        */
    }
}

