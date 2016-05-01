package com.yf833;

import java.net.URL;



public class Link {

    private URL url;
    private int score;
    private int arrival;

    public Link(URL url, int score, int arrival){
        this.url = url;
        this.score = score;
        this.arrival = arrival;
    }
    public Link(URL url, int arrival){
        this.url = url;
        this.score = 0;
        this.arrival = arrival;
    }



    public URL getURL(){
        return this.url;
    }
    public int getScore(){
        return this.score;
    }
    public int getArrival(){
        return this.arrival;
    }

    public void setScore(int newscore){
        this.score = newscore;
    }


    public String toString(){
        return "url: " + this.url + " " + "score: " + this.score + " " + " | ";


    }


    @Override
    public boolean equals(Object o){
        if(o instanceof Link){
            Link compareto = (Link)o;
            return compareto.url.equals(this.url);
        }
        return false;
    }

}