package com.yf833;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;



public class QuoteParse {


    // return an arraylist of quote objects extracted from the given page
    public static ArrayList<Quote> getQuotes(File input_file) throws IOException, BoilerpipeProcessingException {

        ArrayList<Quote> quotes_list = new ArrayList<Quote>();

        // parse the file into a Jsoup document
        Document doc = Jsoup.parse(input_file, "UTF-8");

        // get the text from the document
        String fullpagetext = doc.toString();
        String maintext = ArticleExtractor.INSTANCE.getText(fullpagetext);
        System.out.println(maintext);


        // initialize a part-of-speech tagger //

        Tokenizer _tokenizer = null;
        POSTaggerME _posTagger = null;
        POSModel modelIn = null;

        // Loading tokenizer model
//            modelIn = QuoteParse.class.getClassLoader().getResourceAsStream("/en-sent.bin");
            modelIn = new POSModelLoader().load(new File("./en-pos-maxent.bin"));
            final POSModel posModel = modelIn;
            _posTagger = new POSTaggerME(posModel);

        /////////////////////////////////////////




        // search for quotes
        int iStartQuote = 0;    // index of the start quote
        int iEndQuote;          // index of the end quote

        System.out.println(iStartQuote);


        while((iStartQuote = maintext.indexOf("“", iStartQuote+1)) != -1) {
            iEndQuote = maintext.indexOf("”", iStartQuote+1);
            String quotetext = maintext.substring(iStartQuote, iEndQuote);
            System.out.println(quotetext);

            //TODO: use the part-of-speech tagger to tag all tokens in the text
            System.out.println(_posTagger.tag(quotetext));


            //TODO: get the subject of the quotetext


            //TODO: get the subject of the speaker 

        }

        return quotes_list;
    }



}
