
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import opennlp.tools.chunker.Chunker;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.util.Span;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;


public class QuoteParse {

    public static int SAID_SPAN = 6;    // denotes the +/- number of tokens to look for the word "said" when resolving speakers
    public static int QUOTE_TOKEN_MIN = 3; // denotes the minimum number of tokens a quote needs in order to be considered valid

    // return an arraylist of quotes extracted from the given page
    public static ArrayList<Quote> getQuotes(File input_file, Tokenizer tokenizer, POSTaggerME posTagger, Chunker chunker, NameFinderME nameFinder, DocumentCategorizerME sentCategorizer) throws IOException, BoilerpipeProcessingException {

        ArrayList<Quote> quotes_list = new ArrayList<Quote>();

        // (1) get the source of the file, parse the file into a Jsoup document //
        BufferedReader brTest = new BufferedReader(new FileReader(input_file));
        String source = brTest .readLine();

        Document doc = Jsoup.parse(input_file, "UTF-8");

        //get title of the page
        String page_title = doc.title();

        // (2) get the text from the document //
        String fullpagetext = doc.toString();
        String maintext = ArticleExtractor.INSTANCE.getText(fullpagetext);
        //System.out.println(maintext);


        // (3) tokenize the text //
        String[] maintext_tokens = tokenizer.tokenize(maintext);
        System.out.println(Arrays.toString(maintext_tokens));
        System.out.println("QUOTE COUNT (CANDIDATES): " + Util.getNumberOfQuotes(maintext_tokens));


        // (4) get part-of-speech tags //
        String[] maintext_tags = posTagger.tag(maintext_tokens);


        // (5) chunk the text //
        String[] maintext_chunks = chunker.chunk(maintext_tokens, maintext_tags);


        // (6) locate named entities in the text //
        Span[] namespans = nameFinder.find(maintext_tokens);   //contains start-end indices of names in maintext_tokens[]
//        System.out.println("----- Named Entities -----");
//        Util.printNamedEntities(namespans, maintext_tokens);

        // (6) Iterate through the text and process quotes //
        int iStartQuote=0;
        int iEndQuote;

        //while  maintext_tokens[] contains another opening text character
        while(Util.getNextQuotePosition(maintext_tokens, iStartQuote) != -1) {

            iStartQuote = Util.getNextQuotePosition(maintext_tokens, iStartQuote);
            iEndQuote = Util.getCloseQuotePosition(maintext_tokens, iStartQuote);

            String quotetext = Util.getQuoteText(maintext_tokens, iStartQuote, iEndQuote);
            quotetext = quotetext.replace("“", "");
            quotetext = quotetext.replaceAll("\\s+(?=\\p{Punct})", "");
//            System.out.println("QUOTE TEXT: " + quotetext);

            // get text tokens, part-of-speech tags, and chunks //
            String[] quote_tokens = tokenizer.tokenize(quotetext);
            String[] quote_pos = posTagger.tag(quote_tokens);
            String[] quote_chunks = chunker.chunk(quote_tokens, quote_pos);


            // get the subject of the quote //
            String quotesubject = getQuoteSubject(quote_tokens, quote_pos, quote_chunks);
            quotesubject = quotesubject.replace("“", "");
//            System.out.println("QUOTE SUBJECT: " + quotesubject);


            // get the speaker of the quote //
            String quotespeaker = getQuoteSpeaker(maintext_tokens, maintext_chunks, namespans, iStartQuote, iEndQuote);
            quotespeaker = quotespeaker.replace("“", "");
//            System.out.println("QUOTE SPEAKER: " + quotespeaker);

            // get the sentiment of the quote //
            double[] outcomes = sentCategorizer.categorize(quotetext);
            double positive_probability = outcomes[1];


            // create a new quote and store in quotes list (discard quotes that are just two words)//
            if(quotetext.split(" +").length > QUOTE_TOKEN_MIN){
                Quote newquote = new Quote(source, quotespeaker, quotesubject, quotetext, positive_probability, page_title);

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

        // 3. look for pronouns (PRP, WP)
        for(int i=0; i<quote_tokens.length; i++){
            String nnp_subject = "";
            if(quote_pos[i].equals("PRP") || quote_pos[i].equals("WP")){
                nnp_subject += quote_tokens[i];
                i++;
                return nnp_subject;
            }
        }

        // 4. look for gerunds (VBG)
        for(int i=0; i<quote_tokens.length; i++){
            String nnp_subject = "";
            if(quote_pos[i].equals("VBG")){
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
        int said_position;

        // (1) if said occurs within x number of tokens of iEnd //
        if(Util.positionOfSaidWithin_x(maintext_tokens, iEnd, SAID_SPAN) != -1){
            //System.out.println("said is within +5 of iEnd");

            said_position = Util.positionOfSaidWithin_x(maintext_tokens, iEnd, SAID_SPAN);
            speaker = Util.getSpeakerNearSaid(maintext_tokens, namespans, said_position);

            //if speaker is empty
            if(speaker.replaceAll("\\s+","").equals("")){
                speaker = Util.getNearestMrEntity(maintext_tokens, said_position, SAID_SPAN);
            }
        }

        // (2) if said occurs within x number of tokens of iStart //
        else if(Util.positionOfSaidWithin_x(maintext_tokens, iStart, SAID_SPAN*-1) != -1){
            //System.out.println("said is within -5 of iStart");

            said_position = Util.positionOfSaidWithin_x(maintext_tokens, iStart, SAID_SPAN * -1);
            speaker = Util.getSpeakerNearSaid(maintext_tokens, namespans, said_position);

            //if speaker is empty
            if(speaker.replaceAll("\\s+","").equals("")){
                speaker = Util.getNearestMrEntity(maintext_tokens, said_position, SAID_SPAN);
            }
        }



        // catch-all: select the nearest named entity (in either direction) if speaker is still empty //
        if(speaker.replaceAll("\\s+","").equals("") || speaker.replaceAll("\\s+","").equals("unresolved")){
            speaker = Util.getNearestNamedEntity(maintext_tokens, namespans, iStart, iEnd);
        }

        // if speaker resolution was unsuccessful for all cases
        if(speaker.replaceAll("\\s+","").equals("")){
            speaker = "unresolved";
        }

        return speaker;
    }


    //get the sentiment of a quote
    private static double getQuoteSentiment(){



        return 0.0;
    }

}







