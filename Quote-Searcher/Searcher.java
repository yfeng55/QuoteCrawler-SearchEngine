import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;


public class Searcher {


    private static String directoryname = "";

    private static String indexDir = "";
    private static String query = "";
    private static String query_speaker = "";
    private static double min_sentiment = 0;
    private static double max_sentiment = 1;



    public static void main(String[] args) throws IllegalArgumentException, IOException, ParseException, org.apache.lucene.queryparser.classic.ParseException {

        for(int i=0; i<args.length; i+=2){
            switch(args[i]){
                case "-index":
                    indexDir = args[i+1];
                    break;
                case "-q":
                    query = args[i+1];
                    break;
                case "-s":
                    query_speaker = args[i+1];
                    if(query_speaker.toLowerCase().equals("all")){
                        query_speaker = "";
                    }
                    break;
                case "-x":
                    min_sentiment = Double.parseDouble(args[i+1]);
                    break;
                case "-y":
                    max_sentiment = Double.parseDouble(args[i+1]);
                    break;
                default:
                    throw new IllegalArgumentException("Must provide arguments: " + "-index <index dir> -q <query>");
            }
        }
        if (query.equals("")) {
            throw new IllegalArgumentException("Must provide a query and index: " + "-index <index dir> -q <query>");
        }


        search(indexDir, query);
    }


    public static void search(String indexDir, String q) throws IOException, ParseException, org.apache.lucene.queryparser.classic.ParseException {

        Directory dir = FSDirectory.open(Paths.get(new File(indexDir).getPath()));

        IndexReader indexreader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(indexreader);

        //create a new QueryParser
        QueryParser parser = new QueryParser("contents", new StandardAnalyzer());

        Query query = parser.parse(q);
        long start = System.currentTimeMillis();

        TopDocs hits = searcher.search(query, 20);
        long end = System.currentTimeMillis();



        // PRINT OUTPUT (SEARCH RESULTS) //

        System.out.println("\nFound " + hits.totalHits + " document(s) (in " + (end - start) + " milliseconds) that matched query '" + q + "':\n");

        System.out.println("Results for query '" + q + "' in directory '" + directoryname);
        if(!query_speaker.equals("")){
            System.out.println(" by speaker " + query_speaker);
        }
        System.out.println();


        String htmlString = "";


        int i = 0;
        for(ScoreDoc scoreDoc : hits.scoreDocs) {

            Document doc = searcher.doc(scoreDoc.doc);

            //set directoryname
            if(directoryname.equals("")){
                String fullpath = doc.get("fullpath");

                int lastindex = fullpath.lastIndexOf("/");

                String shortened_fullpath = fullpath.substring(0, lastindex);

                int secondlastindex = shortened_fullpath.lastIndexOf("/");
                directoryname = fullpath.substring(secondlastindex+1, lastindex);

                // System.out.println("DIRECTORY NAME: " + directoryname);
            }


            // check for speaker and sentiment filters before appending to results //
            if(doc.get("speaker").toLowerCase().contains(query_speaker.toLowerCase()) &&
                Double.parseDouble(doc.get("sentiment")) >= min_sentiment &&
                Double.parseDouble(doc.get("sentiment")) <= max_sentiment){


                ///// print results /////
                System.out.println(Integer.toString(i) + ". \"" + doc.get("quotetext") + "\"");
                System.out.println("\tspeaker: " + doc.get("speaker"));
                System.out.println("\tsubject: " + doc.get("subject"));
                System.out.println("\tsentiment: " + doc.get("sentiment"));
                System.out.println("\tsource: " + doc.get("pagetitle") + " " + doc.get("source"));
                System.out.println();

                ///// append results  to output file /////
                htmlString += "<p><b><i>" + Integer.toString(i+1) + "</i>. \"" + doc.get("quotetext") + "\"</b><br/> " +
                    "<span style='margin-left:3em'>" + "speaker:\t" + doc.get("speaker") + "</span><br/>" +
                    "<span style='margin-left:3em'>" + "subject:\t" + doc.get("subject") + "</span><br/>" +
                    "<span style='margin-left:3em'>" + "sentiment:\t" + doc.get("sentiment") + "</span><br/>" +
                    "<span style='margin-left:3em'>" + doc.get("pagetitle") + " | <a href='" + doc.get("source") + "'>" + doc.get("source") + "</a></span></p>";



                i++;
            }








        }

        htmlString += "<p><a href='http://cims.nyu.edu/~yf833/cgi-bin/retriever.cgi'> Back to Search Page </a></p></body></html>";


        // PREPEND TITLE

        if(!query_speaker.equals("")){
            htmlString = " <h2>by speaker: \"" + query_speaker + "\"</h2>" + htmlString;
        }

        htmlString = "<h1>" + i + " Results for query <u>" + q + "</u> in directory <u>" + directoryname + "</u> </h1>" + htmlString;

        htmlString = "<title>" + i + " Results for query '" + q + "' in directory '" + directoryname + "</title></head><body>" + htmlString;

        htmlString = "<link rel='stylesheet' type='text/css' href='style.css' media='screen' />" + htmlString;

        htmlString = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" + htmlString;



        //output to HTML file
        File htmlResultsPage = new File("./results.html");
        FileUtils.writeStringToFile(htmlResultsPage, htmlString);

        //close the indexreader
        indexreader.close();
    }



}