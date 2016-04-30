package com.yf833;


import opennlp.tools.util.Span;

import java.util.Hashtable;

public class Util {



    // Return a table of most frequently occurring proper nouns //

    public static Hashtable<String, Integer> getProperNounFrequency(String[] maintext_tokens, String[] maintext_tags){
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
        System.out.println(proper_nouns_freq.toString());

        return proper_nouns_freq;
    }


    // print out all named entities in the text
    public static void printNamedEntities(Span[] namespans, String[] maintext_tokens){
        for(Span name : namespans){
            for(int i=name.getStart(); i<name.getEnd(); i++){
                System.out.print(maintext_tokens[i] + " ");
            }
            System.out.println();
        }
    }


}


