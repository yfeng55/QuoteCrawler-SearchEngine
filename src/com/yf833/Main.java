package com.yf833;

import de.l3s.boilerpipe.BoilerpipeProcessingException;

import java.io.File;
import java.io.IOException;


public class Main {

    public static final String input_path = "./sample_articles/hilary-clinton-new-york_nytimes.html";
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



    public static void main(String[] args) throws IOException, BoilerpipeProcessingException {

        //get input values from string arguments
        getInput(args);

        if(showTrace){
            System.out.println("Crawling for " + maxPages + " pages relevant to \"" + query_input + "\" starting from " + url_input);
        }

        WebCrawler crawler = new WebCrawler(url_input, path_input, query_input, maxPages, showTrace);
        crawler.run();


        ////////// PARSE ALL OUTPUT FILES FOR QUOTES //////////


        // read in input file
//        File input_file = new File(input_path);

        // get an array of quotes form the file
//        QuoteParse.getQuotes(input_file);


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
