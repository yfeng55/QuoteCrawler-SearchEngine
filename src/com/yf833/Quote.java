package com.yf833;


public class Quote {

    public String subject;      // the person (or thing) that the quote is about
    public String speaker;      // the person (or entity) that is delivering the quote
    public String quote;        // the quote itself
    public String source;       // url to the page that contains the quote

    public Quote(String source, String speaker, String subject, String quote){
        this.subject = subject;
        this.speaker = speaker;
        this.quote = quote;
        this.source = source;
    }



}
