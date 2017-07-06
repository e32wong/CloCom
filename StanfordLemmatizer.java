import java.io.*;
import java.util.*;

import edu.stanford.nlp.coref.CorefCoreAnnotations;

import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.io.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.*;

public class StanfordLemmatizer {

    protected StanfordCoreNLP pipeline;

    public StanfordLemmatizer() {
        // Create StanfordCoreNLP object properties, with POS tagging
        // (required for lemmatization), and lemmatization
        Properties props;
        props = new Properties();
        //props.put("annotators", "tokenize, ssplit, pos, lemma");
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma");

        // StanfordCoreNLP loads a lot of models, so you probably
        // only want to do this once per execution
        this.pipeline = new StanfordCoreNLP(props);
    }

    public String lemmatize(String word) {

		Annotation tokenAnnotation = new Annotation(word);
		pipeline.annotate(tokenAnnotation);  // necessary for the LemmaAnnotation to be set.
		List<CoreMap> list = tokenAnnotation.get(CoreAnnotations.SentencesAnnotation.class);
		String tokenLemma = list
								.get(0).get(CoreAnnotations.TokensAnnotation.class)
								.get(0).get(CoreAnnotations.LemmaAnnotation.class);
		return tokenLemma;
    }
}
