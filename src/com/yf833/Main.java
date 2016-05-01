package com.yf833;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
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
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;


public class Main {

//    public static final String input_path = "./sample_articles/hilary-clinton-new-york_nytimes.html";
//    public static final String input_path = "./sample_articles/bernie-sanders-hillary-clinton-dnc_theatlantic.html";
//    public static final String input_path = "./sample_articles/donald-trump-foreign-policy_nytimes.html";
//    public static final String input_path = "./sample_articles/donald-trump-interview-highlights_nytimes.html";
//    public static final String input_path = "./sample_articles/donald-trump-transcript_nytimes.html";
//    public static final String input_path = "./sample_articles/ted-cruzs-phony-concern-for-the-people_nytimes.html";
//    public static final String input_path = "./sample_articles/the-great-republican-revolt_theatlantic.html";


    private static String url_input;                // -u
    private static String path_input;               // -docs
    private static String query_input = "";         // -q
    private static int maxPages = 10;                // -m
    private static boolean showTrace = false;       // -t

    private static ArrayList<Quote> quotes = null;


    public static void main(String[] args) throws IOException, BoilerpipeProcessingException {

        //get input values from string arguments
        getInput(args);

        if(showTrace){
            System.out.println("Crawling for " + maxPages + " pages relevant to \"" + query_input + "\" starting from " + url_input);
        }

        WebCrawler crawler = new WebCrawler(url_input, path_input, query_input, maxPages, showTrace);
        crawler.run();


        ////////// PARSE ALL OUTPUT FILES FOR QUOTES //////////

        // initialize openNLP classifiers //
        TokenizerModel tokenModel = new TokenizerModel(new FileInputStream(new File("./en-token.bin")));
        Tokenizer tokenizer = new TokenizerME(tokenModel);

        POSModel posModel = new POSModelLoader().load(new File("./en-pos-maxent.bin"));
        POSTaggerME posTagger = new POSTaggerME(posModel);

        ChunkerModel chunkerModel = new ChunkerModel(new FileInputStream(new File("./en-chunker.bin")));
        Chunker chunker = new ChunkerME(chunkerModel);

        TokenNameFinderModel nameModel = new TokenNameFinderModel(new FileInputStream(new File("./en-ner-person.bin")));
        NameFinderME nameFinder = new NameFinderME(nameModel);


        File output_directory = new File(path_input);
        File[] outputfiles = output_directory.listFiles();

        quotes = new ArrayList<Quote>();
        for(File f: outputfiles){

            // get an array of quotes from the file
            quotes.addAll(QuoteParse.getQuotes(f, tokenizer, posTagger, chunker, nameFinder));

        }


        ////////// CONVERT QUOTES INTO INDEXABLE DOCUMENTS //////////
        int i=1;
        for(Quote q : quotes){

            String path = "./indexable_docs/quote" + i + ".html";
            File f = new File(path);

            System.out.println("writing..." + path);
            System.out.println(q.toString());

            FileUtils.writeStringToFile(f, q.toString());

            i++;
        }


    }


    private static void getInput(String[] args){
        for(int i=0; i<args.length; i++){
            switch(args[i]){
                case "-u":
                    url_input = args[i+1];
                    i++;
                    break;
                case "-docs":
                    path_input = args[i+1];
                    i++;
                    break;
                case "-q":
                    query_input = args[i+1];
                    i++;
                    break;
                case "-m":
                    maxPages = Integer.parseInt(args[i+1]);
                    i++;
                    break;
                case "-t":
                    showTrace = true;
                    break;
                default:
                    throw new IllegalArgumentException("Must provide arguments: " + "-u <url> -q <query> -docs <path> -m <max pages> -t");
            }
        }
        System.out.println("-u: " + url_input);
        System.out.println("-docs: " + path_input);
        System.out.println("-q: " + query_input);
        System.out.println("-m: " + maxPages);
        System.out.println("-t: " + showTrace);
    }



}
