import java.util.Set;
import java.util.HashSet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.ArrayList;

public class Utilities {

    public static boolean checkIsJava(String filePath) {
        String extension = filePath.substring(filePath.length()-5);
        if (extension.equals(".java")) {
            return true;
        } else {
            return false;
        }
    }

    public static Set<String> extractTermsFromSentence2(String sentence) {

        Set<String> listTerms = new HashSet<String>();

        Pattern pattern = Pattern.compile("[a-zA-Z_0-9]{3,}");
        Matcher matcher = pattern.matcher(sentence);
        while (matcher.find()) {
            //System.out.println(matcher.group());
            String term = matcher.group();
            listTerms.add(term.toLowerCase());
        }

        return listTerms;
    }

    public static Set<String> extractTermsFromSentence(String sentence, ArrayList<String> banList, boolean debug) {

        Set<String> listTerms = new HashSet<String>();

        Pattern pattern = Pattern.compile("[a-zA-Z_0-9]{3,}");
        Matcher matcher = pattern.matcher(sentence);
        while (matcher.find()) {
            //System.out.println(matcher.group());
            String term = matcher.group();
            if (!banList.contains(term)) {
                listTerms.add(term.toLowerCase());
            } else {
                if (debug) {
                    System.out.println("Removed a similarity term due to ban list:");
                    System.out.println(term);
                }

            }
        }

        return listTerms;
    }


    public static String splitCamelCaseString (String term) {

        StringBuilder strBuilder = new StringBuilder();

        for (String w : term.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
            //System.out.println(w.toLowerCase());
            strBuilder.append(w.toLowerCase() + " ");
        }

        return strBuilder.toString();

    }



    public static Set<String> splitCamelCaseSet (String term) {

        Set<String> camelTerms = new HashSet<String>();

        Pattern p = Pattern.compile("[a-zA-Z]+");
        for (String w : term.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
            w = w.toLowerCase();
            Matcher m = p.matcher(w);
            while (m.find()) {
                camelTerms.add(m.group());
            }
        }

        return camelTerms;

    }

}
