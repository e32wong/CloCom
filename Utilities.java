import java.util.Set;
import java.util.HashSet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilities {

    public static boolean checkIsJava(String filePath) {
        String extension = filePath.substring(filePath.length()-5);
        if (extension.equals(".java")) {
            return true;
        } else {
            return false;
        }
    }

    public static Set<String> extractTermsFromSentence(String sentence) {

        Set<String> listTerms = new HashSet<String>();

        Pattern pattern = Pattern.compile("[a-zA-Z_0-9]{3,}");
        Matcher matcher = pattern.matcher(sentence);
        while (matcher.find()) {
            //System.out.println(matcher.group());
            String term = matcher.group();
            Set<String> camelTerms = Utilities.splitCamelCaseSet(term);
            for (String splittedTerm : camelTerms) {
                listTerms.add(splittedTerm);
            }
        }

        //System.out.println(sentence);

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
