import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;


import java.io.StringReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;


public class NLP {

    MaxentTagger tagger = new MaxentTagger("./models/english-left3words-distsim.tagger");

    public ArrayList<String> getNouns(String str) {

        ArrayList<String> nounList = new ArrayList<String>();

        TokenizerFactory<CoreLabel> ptbTokenizerFactory = PTBTokenizer.factory
            (new CoreLabelTokenFactory(), "untokenizable=noneKeep");

        StringReader stringReader = new StringReader(str);
        DocumentPreprocessor documentPreprocessor = new DocumentPreprocessor(stringReader);
        documentPreprocessor.setTokenizerFactory(ptbTokenizerFactory);
        for (List<HasWord> sentence : documentPreprocessor) {
            List<TaggedWord> tSentence = tagger.tagSentence(sentence);
            //System.out.println(Sentence.listToString(tSentence, false));

            for (TaggedWord tw : tSentence) {
                if (tw.tag().startsWith("NN")) {
                    nounList.add(tw.word());
                }
            }

        }

        return nounList;
    }
}
