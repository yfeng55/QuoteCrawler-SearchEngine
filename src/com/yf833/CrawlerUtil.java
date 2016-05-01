package com.yf833;

import org.jsoup.Jsoup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CrawlerUtil {

    //extract text between anchor tags
    public static String getLinkText(String htmltext){

        String link_text = "";

        Pattern titleFinder = Pattern.compile("<a[^>]*>(.*?)</a>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher regexMatcher = titleFinder.matcher(htmltext);
        while (regexMatcher.find()) {
            link_text = regexMatcher.group(1);
        }

        return link_text;
    }


    //extract URL from link text
    public static String getHrefText(String htmltext){

        String url_text = "";

        Pattern titleFinder = Pattern.compile("href=[\\'\"]?([^\\'\" >]+)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher regexMatcher = titleFinder.matcher(htmltext);
        while (regexMatcher.find()) {
            url_text = regexMatcher.group(1);
        }

        return url_text;
    }


    //get a string that's plus/minus five words from the link text (excluding the link itself)
    public static String getLinkText_Five(String page, int linkstart, int linkend){

        String result = "";

        String lefthalf = Jsoup.parse(page.substring(0, linkstart)).text();
        String righthalf = Jsoup.parse(page.substring(linkend)).text();

        String[] lefthalf_page = lefthalf.split(" +");
        String[] righthalf_page = righthalf.split(" +");


        int i=lefthalf_page.length-1; int icount=0;
        while(icount<5 && i>0){
            result += lefthalf_page[i] + " ";
            icount++;
            i--;
        }


        int j=0; int jcount=0;
        while(jcount<5 && j<righthalf_page.length){
            result += righthalf_page[j] + " ";
            jcount++;
            j++;
        }

        return result;

    }

}
