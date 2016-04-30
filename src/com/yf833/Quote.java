package com.yf833;


public class Quote {

    public String subject;      // the person (or thing) that the text is about
    public String speaker;      // the person (or entity) that is delivering the text
    public String text;         // the text itself
    public String source;       // url to the page that contains the text
    public double sentiment;       // sentiment rating for the quote

    public Quote(String source, String speaker, String subject, String text){
        this.subject = subject;
        this.speaker = speaker;
        this.text = text;
        this.source = source;
        this.sentiment = 0.0;
    }

    public String toString(){
        String output = "";

        output += "QUOTE TEXT: " + this.text;
        output += "\nQUOTE SUBJECT: " + this.subject;
        output += "\nQUOTE SPEAKER: " + this.speaker;
        output += "\nQUOTE SENTIMENT: " + this.sentiment;
        output += "\nQUOTE SOURCE: " + this.source;

        return output;
    }


}
