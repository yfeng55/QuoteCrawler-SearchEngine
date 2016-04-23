package com.yf833;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import opennlp.tools.chunker.Chunker;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;


public class QuoteParse {


    // return an arraylist of quote objects extracted from the given page
    public static ArrayList<Quote> getQuotes(File input_file) throws IOException, BoilerpipeProcessingException {

        ArrayList<Quote> quotes_list = new ArrayList<Quote>();

        // (1) parse the file into a Jsoup document //
        Document doc = Jsoup.parse(input_file, "UTF-8");

        // (2) get the text from the document //
        String fullpagetext = doc.toString();
        String maintext = ArticleExtractor.INSTANCE.getText(fullpagetext);
        //System.out.println(maintext);


        // (3) tokenize the text //

        TokenizerModel tokenModel = new TokenizerModel(new FileInputStream(new File("./en-token.bin")));
        Tokenizer tokenizer = new TokenizerME(tokenModel);
        String[] maintext_tokens = tokenizer.tokenize(maintext);
        System.out.println(Arrays.toString(maintext_tokens));



        // (4) initialize a part-of-speech tagger //

        POSModel posModel = new POSModelLoader().load(new File("./en-pos-maxent.bin"));
        POSTaggerME posTagger = new POSTaggerME(posModel);
        String[] maintext_tags = posTagger.tag(maintext_tokens);



        // (5) chunk the text //

        ChunkerModel chunkerModel = new ChunkerModel(new FileInputStream(new File("./en-chunker.bin")));
        Chunker chunker = new ChunkerME(chunkerModel);

        //String[] maintext_chunks = chunker.chunk(maintext_tokens, maintext_tags);
        //System.out.println(Arrays.toString(maintext_chunks));





        // (6) Create a table of most frequently occurring proper nouns //

        Hashtable<String, Integer> proper_nouns_freq = new Hashtable<>();

        for(int i=0; i<maintext_tokens.length; i++){
            if(maintext_tags[i].equals("NNP")){

                if(proper_nouns_freq.containsKey(maintext_tokens[i])){
                    int oldcount = proper_nouns_freq.get(maintext_tokens[i]);
                    proper_nouns_freq.remove(maintext_tokens[i]);
                    proper_nouns_freq.put(maintext_tokens[i], oldcount + 1);
                }else{
                    proper_nouns_freq.put(maintext_tokens[i], 1);
                }

            }
        }
        //System.out.println(proper_nouns_freq.toString());



        // (7) Iterate through the text and process quotes //

        int iStartQuote=0, iEndQuote;

        while((iStartQuote = maintext.indexOf("“", iStartQuote+1)) != -1) {
            iEndQuote = maintext.indexOf("”", iStartQuote+1);
            String quotetext = maintext.substring(iStartQuote, iEndQuote);
            //System.out.println(quotetext);

            String[] quote_tokens = tokenizer.tokenize(quotetext);
            String[] quote_pos = posTagger.tag(quote_tokens);
            String[] quote_chunks = chunker.chunk(quote_tokens, quote_pos);

            System.out.println();
            System.out.println(Arrays.toString(quote_tokens));
            System.out.println(Arrays.toString(quote_pos));
            System.out.println(Arrays.toString(quote_chunks));


            //TODO: get the subject of the quotetext
            System.out.println("QUOTE SUBJECT: " + getQuoteSubject(quote_tokens, quote_pos, quote_chunks));


            //TODO: get the speaker

        }

        return quotes_list;
    }


    private static String getQuoteSubject(String[] quote_tokens, String[] quote_pos, String[] quote_chunks){
        String subject = "unresolved";

        // look for proper nouns (NNP)
        for(int i=0; i<quote_tokens.length; i++){
            String nnp_subject = "";
            if(quote_pos[i].equals("NNP") || quote_pos[i].equals("NNPS")){
                nnp_subject += quote_tokens[i];
                i++;
                while (quote_chunks[i].equals("I-NP") && i<quote_tokens.length) {
                    nnp_subject += " " + quote_tokens[i];
                    i++;

                    if(i >= quote_tokens.length){
                        break;
                    }
                }

                return nnp_subject;
            }
        }


        // look for singular nouns (NNS)
        for(int i=0; i<quote_tokens.length; i++){
            String nnp_subject = "";
            if(quote_pos[i].equals("NN") || quote_pos[i].equals("NNS")){
                nnp_subject += quote_tokens[i];
                i++;
                while(quote_chunks[i].equals("I-NP") && i<quote_tokens.length){
                    nnp_subject += " " + quote_tokens[i];
                    i++;

                    if(i >= quote_tokens.length){
                        break;
                    }
                }

                return nnp_subject;
            }
        }


        // look for pronouns (need to resolve pronouns)




        return subject;
    }



}
