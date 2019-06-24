import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class WSD {
    public IDictionary init() throws IOException {
        String path = "dict";
        URL url = null;
        try{ url = new URL("file", null, path); }
        catch(MalformedURLException e){ e.printStackTrace(); }
        if(url == null) return null;

        IDictionary dict = new Dictionary(url);
        return dict;

    }

    public String simplifiedLesk(String word, String sentence) throws IOException {

        String bestSense = null;
        IWord bestSenseWord = null;
        int maxOverlap = 0;
        IDictionary dict = init();
        dict.open();

        LinkedHashSet<String> context = new LinkedHashSet<String>();
        StringTokenizer tokenizer = new StringTokenizer(sentence);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().replace(".", "");
            context.add(token);
        }

        IIndexWord idxWord = dict.getIndexWord(word, POS.NOUN);

        Formatter formatter1 = new Formatter();
        System.out.println(formatter1.format("%20s | %10s | %50s ","WordId","Overlap","Gloss"));
        System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------------");

        for(int s = 0 ; s < idxWord.getWordIDs().size() ; s++ ){
            LinkedHashSet<String> signature = new LinkedHashSet<String>();
            IWordID wordIDForSense = idxWord.getWordIDs().get(s);
            IWord wordForSense = dict.getWord(wordIDForSense);

            String signatureText = wordForSense.getSynset().getGloss().replaceAll("\\(", "")
                    .replaceAll("\\)", "").replaceAll(";", "").replaceAll("\"", "");

            StringTokenizer signatureTokenizer = new StringTokenizer(signatureText);
            while (signatureTokenizer.hasMoreTokens()) {
                String senseToken = signatureTokenizer.nextToken();
                signature.add(senseToken);
            }

            int overlap = computeOverlap(signature, context);

            if (overlap > maxOverlap) {
                maxOverlap = overlap;
                bestSense = wordForSense.getSynset().getGloss().split(";")[0];
                bestSenseWord = wordForSense;
            }

            Formatter formatter = new Formatter();
            System.out.println(formatter.format("%20s | %10d | %50s ",wordForSense,overlap,wordForSense.getSynset().getGloss() ));
        }

        return bestSense;

    }

    private static int computeOverlap(LinkedHashSet<String> signature, LinkedHashSet<String> context) {
        int overlapCount = 0;

        for (String cs : context) {
            for (String ss : signature) {
                if (cs.equals(ss)) {
                    overlapCount++;
                }
            }
        }

        return overlapCount;
    }

    public static void main(String args[]){
        WSD wsd = new WSD();
        try {
            String bestSense = wsd.simplifiedLesk("bank", "The bank can guarantee deposits will eventually cover future tuition costs because it invests in adjustable-rate mortgage securities.");
            System.out.println("Best sense : "+bestSense);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
