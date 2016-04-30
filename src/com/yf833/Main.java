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



    public static void main(String[] args) throws IOException, BoilerpipeProcessingException {

        // read in input file
        File input_file = new File(input_path);

        // get an array of quotes form the file
        QuoteParse.getQuotes(input_file);





    }


}
