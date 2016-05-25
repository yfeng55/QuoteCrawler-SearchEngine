

import java.io.*;
import java.nio.file.*;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import org.jsoup.Jsoup;

public class Indexer {


    public static void main(String[] args) throws Exception {

        if (args.length != 4) {
            throw new IllegalArgumentException("Must provide arguments: " + "-index <index dir> -docs <data dir>");
        }


        String indexpath = "";
        String datapath = "";

        for(int i=0; i<args.length; i+=2){
            switch(args[i]){
                case "-index":
                    indexpath = args[i+1];
                    break;
                case "-docs":
                    datapath = args[i+1];
                    break;
                default:
                    throw new IllegalArgumentException("Must provide arguments: " + "-index <index dir> -docs <data dir>");
            }
        }

        // String indexpath = "../_indexOutput/";
        // String datapath = "../_webpagesToIndex/";

        long start = System.currentTimeMillis();

        Indexer indexer = new Indexer(indexpath);

        int numIndexed;


        try {
            numIndexed = indexer.index(datapath, new TextFilesFilter());
        } finally {
            indexer.close();
        }

        long end = System.currentTimeMillis();
        System.out.println("Indexing " + numIndexed + " files took " + (end - start) + " milliseconds");
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private IndexWriter writer;

    //constructor
    public Indexer(String indexDir) throws IOException {

        Directory dir = FSDirectory.open(Paths.get(new File(indexDir).getPath()));
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());

        writer = new IndexWriter(dir, config);

    }


    public void close() throws IOException {
        writer.close();
    }


    public int index(String dataDir, TextFilesFilter filter) throws Exception {

        File[] files = new File(dataDir).listFiles();
        for (File f: files) {

//            if (!f.isDirectory() && !f.isHidden() && f.exists() && f.canRead() && (filter == null || filter.accept(f))) {
//                indexFile(f);
//            }

            if (!f.isHidden() || !f.isDirectory() || f.canRead() || f.exists()) {
                indexFile(f);
            }

        }

        return writer.numDocs();
    }



    // PRIVATE METHODS //

    private static class TextFilesFilter implements FileFilter {

        public boolean accept(File path) {

            return path.getName().toLowerCase().endsWith(".txt");

        }
    }



    protected Document getDocument(File f) throws Exception {

        JTidyHTMLHandler handler = new JTidyHTMLHandler();
        Document doc = handler.getDocument(new FileInputStream(f));

        System.out.print("DOC:   ");
        System.out.println(doc.toString());




        doc.add(new Field("contents", new FileReader(f)));

        doc.add(new Field("filename", f.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));

        doc.add(new Field("fullpath", f.getCanonicalPath(), Field.Store.YES, Field.Index.NOT_ANALYZED));


        org.jsoup.nodes.Document jdoc = Jsoup.parse(f, "UTF-8");

//        System.out.println("DOC ------->" + jdoc.toString());

        String quotetext = jdoc.getElementsByClass("quotetext").text();
        String quotesentiment = jdoc.getElementsByClass("quotesentiment").text();
        String quotespeaker = jdoc.getElementsByClass("quotespeaker").text();
        String quotesubject = jdoc.getElementsByClass("quotesubject").text();
        String quotesource = jdoc.getElementsByClass("quotesource").text();
        String quotepage = jdoc.getElementsByClass("quotepagetitle").text();


        System.out.println(quotetext);
        System.out.println(quotesentiment);
        System.out.println(quotespeaker);
        System.out.println(quotesubject);
        System.out.println(quotesource);
        System.out.println(quotepage);

        doc.add(new Field("quotetext", quotetext, Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("sentiment", quotesentiment, Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("speaker", quotespeaker, Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("subject", quotesubject, Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("pagetitle", quotepage, Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("source", quotesource, Field.Store.YES, Field.Index.NOT_ANALYZED));


        return doc;
    }



    private void indexFile(File f) throws Exception {

        System.out.println("Indexing " + f.getCanonicalPath());

        Document doc = getDocument(f);
        writer.addDocument(doc);
    }


}
