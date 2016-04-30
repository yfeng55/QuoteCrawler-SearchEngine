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
import java.util.ArrayList;
import java.util.Arrays;


public class QuoteParse {

    public static int SAID_SPAN = 6;    // denotes the +/- number of tokens to look for the word "said" when resolving speakers
    public static int QUOTE_TOKEN_MIN = 3; // denotes the minimum number of tokens a quote needs in order to be considered valid

    // return an arraylist of quotes extracted from the given page
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
        System.out.println("QUOTE COUNT (CANDIDATES): " + Util.getNumberOfQuotes(maintext_tokens));


        // (4) initialize a part-of-speech tagger //
        POSModel posModel = new POSModelLoader().load(new File("./en-pos-maxent.bin"));
        POSTaggerME posTagger = new POSTaggerME(posModel);
        String[] maintext_tags = posTagger.tag(maintext_tokens);


        // (5) chunk the text //
        ChunkerModel chunkerModel = new ChunkerModel(new FileInputStream(new File("./en-chunker.bin")));
        Chunker chunker = new ChunkerME(chunkerModel);
        String[] maintext_chunks = chunker.chunk(maintext_tokens, maintext_tags);


        // (6) locate named entities in the text //
        TokenNameFinderModel nameModel = new TokenNameFinderModel(new FileInputStream(new File("./en-ner-person.bin")));
        NameFinderME nameFinder = new NameFinderME(nameModel);
        Span[] namespans = nameFinder.find(maintext_tokens);   //contains start-end indices of names in maintext_tokens[]


        // (6) Iterate through the text and process quotes //
        int iStartQuote=0;
        int iEndQuote;

        //while  maintext_tokens[] contains another opening text character
        while(Util.getNextQuotePosition(maintext_tokens, iStartQuote) != -1) {

            iStartQuote = Util.getNextQuotePosition(maintext_tokens, iStartQuote);
            iEndQuote = Util.getCloseQuotePosition(maintext_tokens, iStartQuote);

            String quotetext = Util.getQuoteText(maintext_tokens, iStartQuote);
            quotetext = quotetext.replace("“", "");
            quotetext = quotetext.replaceAll("\\s+(?=\\p{Punct})", "");
            //System.out.println("QUOTE TEXT: " + quotetext);

            // get text tokens, part-of-speech tags, and chunks //
            String[] quote_tokens = tokenizer.tokenize(quotetext);
            String[] quote_pos = posTagger.tag(quote_tokens);
            String[] quote_chunks = chunker.chunk(quote_tokens, quote_pos);


            // get the subject of the quote //
            String quotesubject = getQuoteSubject(quote_tokens, quote_pos, quote_chunks);
            quotesubject = quotesubject.replace("“", "");
            //System.out.println("QUOTE SUBJECT: " + quotesubject);


            // get the speaker of the quote //
            String quotespeaker = getQuoteSpeaker(maintext_tokens, maintext_chunks, namespans, iStartQuote, iEndQuote);
            quotespeaker = quotespeaker.replace("“", "");
            //System.out.println("QUOTE SPEAKER: " + quotespeaker);


            // create a new quote and store in quotes list (discard quotes that are just two words)//
            if(quotetext.split(" +").length > QUOTE_TOKEN_MIN){
                Quote newquote = new Quote(input_file.getAbsolutePath(), quotespeaker, quotesubject, quotetext);

                System.out.println(newquote);
                quotes_list.add(newquote);
            }

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

        return subject;
    }


    //get the speaker of a quote
    private static String getQuoteSpeaker(String[] maintext_tokens, String[] maintext_chunks, Span[] namespans, int iStart, int iEnd){
        String speaker = "unresolved";

        // (1) if said occurs within x number of tokens of iEnd //
        if(Util.positionOfSaidWithin_x(maintext_tokens, iEnd, SAID_SPAN) != -1){
            //System.out.println("said is within +5 of iEnd");

            int said_position = Util.positionOfSaidWithin_x(maintext_tokens, iEnd, SAID_SPAN);
            speaker = Util.getSpeakerNearSaid(maintext_tokens, namespans, said_position);
        }

        // (2) if said occurs within x number of tokens of iStart //
        else if(Util.positionOfSaidWithin_x(maintext_tokens, iStart, SAID_SPAN*-1) != -1){
            //System.out.println("said is within -5 of iStart");

            int said_position = Util.positionOfSaidWithin_x(maintext_tokens, iStart, SAID_SPAN*-1);
            speaker = Util.getSpeakerNearSaid(maintext_tokens, namespans, said_position);
        }

        // catch-all: select the nearest named entity (in either direction) //
        else{

            speaker = Util.getNearestNamedEntity(maintext_tokens, namespans, iStart, iEnd);

        }

        return speaker;
    }


    //get the sentiment of a quote
    private static double getQuoteSentiment(){



        return 0.0;
    }

}







