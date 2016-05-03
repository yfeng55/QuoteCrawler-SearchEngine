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


    //get the position of the next text
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

    //get the next closed text position
    public static int getCloseQuotePosition(String[] maintext_tokens, int iStart){
        int i=iStart;

        try{
            while(!maintext_tokens[i].equals("”") && !maintext_tokens[i].contains("”")){
                i++;
            }
        }catch(ArrayIndexOutOfBoundsException e){
        }

        return i;
    }

    //get the text text
    public static String getQuoteText(String[] maintext_tokens, int iStart, int iEnd){

        String quote_text = "";

        int i=iStart;

        try{
            while(i < iEnd){
                quote_text += maintext_tokens[i] + " ";
                i++;
            }
        }catch(ArrayIndexOutOfBoundsException e){
        }

        return quote_text;
    }


    // find the position of said (and synonyms of said) within x of the provided index
    public static int positionOfSaidWithin_x(String[] maintext_tokens, int quoteIndex, int x){

        if(x>0){
            int i=quoteIndex;
            try{
                while(i < (quoteIndex + x)){
                    if(maintext_tokens[i].contains("said") || maintext_tokens[i].contains("replied") || maintext_tokens[i].contains("adds") ||
                            maintext_tokens[i].contains("asked") || maintext_tokens[i].contains("responds"))
                    { return i; }
                    i++;
                }
            }catch(ArrayIndexOutOfBoundsException e){
            }

        }
        else{
            int i=quoteIndex;
            try{
                while(i > (quoteIndex + x)){

                    if(maintext_tokens[i].contains("said") || maintext_tokens[i].contains("replied") || maintext_tokens[i].contains("adds") ||
                            maintext_tokens[i].contains("asked") || maintext_tokens[i].contains("responds"))
                    { return i; }
                    i--;
                }
            }catch(ArrayIndexOutOfBoundsException e){
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


    // get the speaker near the provided position of "said"
    public static String getSpeakerNearSaid(String[] maintext_tokens, Span[] namespans, int saidIndex){
        String speaker = "";

        for(Span name : namespans){

            if(Math.abs(name.getStart()-saidIndex) <= 5 || Math.abs(name.getEnd()-saidIndex) <= 5){
                for(int i=name.getStart(); i<name.getEnd(); i++){
                    speaker += maintext_tokens[i] + " ";
                }
            }
        }

        return speaker;
    }


    // find the nearest named entity in either direction of a quote
    public static String getNearestNamedEntity(String[] maintext_tokens, Span[] namespans, int iStart, int iEnd){
        String speaker = "";


        try{
            int speaker_end=namespans[0].getEnd();

            while((iStart > 0 || iEnd < maintext_tokens.length) && speaker.equals("")){

                for(Span name : namespans){

                    if(name.getStart() == iStart){
                        for(int i=name.getStart(); i<name.getEnd(); i++){
                            speaker += maintext_tokens[i] + " ";
                        }
                        speaker_end = name.getEnd();
                    }
                    else if(name.getStart() == iEnd){
                        for(int i=name.getStart(); i<name.getEnd(); i++){
                            speaker += maintext_tokens[i] + " ";
                        }
                        speaker_end = name.getEnd();
                    }

                }

                iStart--;
                iEnd++;
            }

            if(speaker.equals(("Mr.")) || speaker.equals("Mrs.") || speaker.equals(("Mr. ")) || speaker.equals("Mrs. ")){
//            System.out.println("-- MR. CASE --");
                speaker += maintext_tokens[speaker_end];
            }


        }catch(ArrayIndexOutOfBoundsException e){
        }


        return speaker;
    }


    // get nearest "Mr." chunk
    public static String getNearestMrEntity(String[] maintext_tokens, int said_position, int SAID_SPAN){
        String speaker = "";

        int j = said_position;
        for(int i=said_position; i<said_position + SAID_SPAN; i++){

//            System.out.println(maintext_tokens[j]);
//            System.out.println(maintext_tokens[i]);

            try{
                if(maintext_tokens[i].contains("Mr.") || maintext_tokens[i].contains("Mrs.")){
                    speaker = maintext_tokens[i] + maintext_tokens[i+1];
                }
                else if(maintext_tokens[j].contains("Mr.") || maintext_tokens[j].contains("Mrs.")){
                    speaker = maintext_tokens[j] + maintext_tokens[j+1];
                }
                j--;
            }catch(ArrayIndexOutOfBoundsException e){
            }

        }

        return speaker;
    }



}






