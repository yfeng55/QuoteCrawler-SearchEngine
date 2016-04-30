package com.yf833;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import opennlp.tools.chunker.Chunker;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
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
        System.out.println("QUOTE COUNT: " + Util.getNumberOfQuotes(maintext_tokens));


        // (4) initialize a part-of-speech tagger //
        POSModel posModel = new POSModelLoader().load(new File("./en-pos-maxent.bin"));
        POSTaggerME posTagger = new POSTaggerME(posModel);
        String[] maintext_tags = posTagger.tag(maintext_tokens);


        // (5) chunk the text //
        ChunkerModel chunkerModel = new ChunkerModel(new FileInputStream(new File("./en-chunker.bin")));
        Chunker chunker = new ChunkerME(chunkerModel);


        // (6) locate named entities in the text //
        TokenNameFinderModel nameModel = new TokenNameFinderModel(new FileInputStream(new File("./en-ner-person.bin")));
        NameFinderME nameFinder = new NameFinderME(nameModel);
        Span[] namespans = nameFinder.find(maintext_tokens);   //contains start-end indices of names in maintext_tokens[]


        // (6) Iterate through the text and process quotes //
        int iStartQuote=0;

        //while  maintext_tokens[] contains another opening quote character
        while(Util.getNextQuotePosition(maintext_tokens, iStartQuote) != -1) {

            iStartQuote = Util.getNextQuotePosition(maintext_tokens, iStartQuote);


            String quotetext = Util.getQuoteText(maintext_tokens, iStartQuote);
            System.out.println("QUOTE TEXT: " + quotetext);

            String[] quote_tokens = tokenizer.tokenize(quotetext);
            String[] quote_pos = posTagger.tag(quote_tokens);
            String[] quote_chunks = chunker.chunk(quote_tokens, quote_pos);

//            System.out.println();
//            System.out.println(Arrays.toString(quote_tokens));
//            System.out.println(Arrays.toString(quote_pos));
//            System.out.println(Arrays.toString(quote_chunks));


            // get the subject of the quotetext //
            String quotesubject = getQuoteSubject(quote_tokens, quote_pos, quote_chunks);
//            System.out.println("QUOTE SUBJECT: " + quotesubject);


            //TODO: get the speaker
            String quotespeaker = getQuoteSpeaker();
//            System.out.println("QUOTE SPEAKER: " + quotespeaker);



        }



        return quotes_list;
    }



    //get the subject of a quote -- refers to the pos tags and chunks
    private static String getQuoteSubject(String[] quote_tokens, String[] quote_pos, String[] quote_chunks){
        String subject = "unresolved";

        // 1. look for proper nouns (NNP)
        for(int i=0; i<quote_tokens.length; i++){
            String nnp_subject = "";
            if(quote_pos[i].equals("NNP") || quote_pos[i].equals("NNPS")){
                nnp_subject += quote_tokens[i];
                i++;

                try{
                    while (quote_chunks[i].equals("I-NP") && i<quote_tokens.length) {
                        nnp_subject += " " + quote_tokens[i];
                        i++;
                    }
                }catch(ArrayIndexOutOfBoundsException e){
                }

                return nnp_subject;
            }
        }

        // 2. look for singular nouns (NNS)
        for(int i=0; i<quote_tokens.length; i++){
            String nnp_subject = "";
            if(quote_pos[i].equals("NN") || quote_pos[i].equals("NNS")){
                nnp_subject += quote_tokens[i];
                i++;

                try{
                    while(quote_chunks[i].equals("I-NP") && i<quote_tokens.length){
                        nnp_subject += " " + quote_tokens[i];
                        i++;
                    }
                }catch(ArrayIndexOutOfBoundsException e){
                }

                return nnp_subject;
            }
        }


        // TODO: look for pronouns (need to resolve pronouns)


        return subject;
    }


    //get the speaker of a quote
    private static String getQuoteSpeaker(){
        String speaker = "unresolved";

        return speaker;
    }


}
