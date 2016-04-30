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


    //count the number of quotes in the text
    public static int getNumberOfQuotes(String maintext_tokens[]){
        int count=0;

        for(int i=0; i<maintext_tokens.length; i++) {
            if(maintext_tokens[i].equals("”")){
                count++;
            }
        }

        return count;
    }


    //get the position of the next quote
    public static int getNextQuotePosition(String[] maintext_tokens, int iStart){

        int nextquote_position = -1;
        for(int i=iStart+1; i<maintext_tokens.length; i++){
            if(maintext_tokens[i].equals("“") || maintext_tokens[i].contains("“")){
                nextquote_position = i;
                break;
            }
        }
        return nextquote_position;
    }

    //get the next closed quote position
    public static int getCloseQuotePosition(String[] maintext_tokens, int iStart){
        int i=iStart;
        while(!maintext_tokens[i].equals("”")){
            i++;
        }
        return i;
    }

    //get the quote text
    public static String getQuoteText(String[] maintext_tokens, int iStart){

        String quote_text = "";

        int i=iStart;
        while(!maintext_tokens[i].equals("”")){
            quote_text += maintext_tokens[i] + " ";
            i++;
        }

        return quote_text;
    }


    // find the position of said within x of the provided index
    public static int positionOfSaidWithin_x(String[] maintext_tokens, int quoteIndex, int x){

        if(x>0){
            int i=quoteIndex;
            while(i < (quoteIndex + x)){
                if(maintext_tokens[i].equals("said") || maintext_tokens[i].contains("said")){ return i; }
                i++;
            }
        }
        else{
            int i=quoteIndex;
            while(i > (quoteIndex - x)){
                if(maintext_tokens[i].equals("said") || maintext_tokens[i].contains("said")){ return i; }
                i--;
            }
        }

        return -1;
    }

    // return a string for the noun-phrase chunk starting at the provided index
    public static String getChunkStringAtIndex(String[] maintext_tokens, String[] maintext_chunks, int index){
        String output = "";
        int i = index+1;

        output += maintext_tokens[i];
        i++;
        try{
            while (maintext_chunks[i].equals("I-NP") && i<maintext_tokens.length) {
                output += " " + maintext_tokens[i];
                i++;
            }
        }catch(ArrayIndexOutOfBoundsException e){
        }

        return output;
    }


}


