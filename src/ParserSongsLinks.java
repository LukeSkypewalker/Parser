import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import org.bson.conversions.Bson;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

//TODO reconnect refactoring
//TODO speedup: proxy, threads
//TODO unit test with mockito
//Done make a links list first, start from previous
//Done track updates (parse news page)

public class ParserSongsLinks {
    static long counter=0;
    static MongoCollection<org.bson.Document> links;

    public static void main(String[] args) throws IOException {
        connectToDataBase();
        parseSongs("http://amdm.ru/");
//        getLinksOfArtists("http://amdm.ru/chords/28/");
    }

    private static void connectToDataBase() {
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        MongoDatabase database = mongoClient.getDatabase("SyncSong");
        links = database.getCollection("links");
    }

    private static void parseSongs(String url) throws IOException {
        Connection con = connect(url);
        Document doc = con.get();
        Elements linksOfArtistsAlphabet = doc.select(".alphabet a");
        for (Element link : linksOfArtistsAlphabet) {
            getLinksOfArtists(link.attr("abs:href"));
        }
    }

    private static void getLinksOfArtists(String url) throws IOException {
        Connection con = connect(url);
        Document doc = con.get();
        Elements linksOfArtists = doc.select("article .artist");
        for (Element link : linksOfArtists) {
            getLinksOfSongs( link.attr("abs:href") );
        }
    }

    private static void getLinksOfSongs(String url) throws IOException {
        Connection con = connect(url);
        Document doc = con.get();
        Elements linksOfSongs = doc.select(".artist-profile-song-list .g-link");
        for (Element link : linksOfSongs) {
            addSongLinkToDatabase(link.attr("abs:href"));
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void addSongLinkToDatabase(String link) {
        Bson filter = Filters.eq("link", link);

        Bson update =  new org.bson.Document("$set",
                new org.bson.Document()
                .append("link", link)
                .append("isDownloaded", 0));

        UpdateOptions options = new UpdateOptions().upsert(true);

        links.updateOne(filter, update, options);

        System.out.println(counter++ + " " + link);
    }

    private static Connection connect(String url){
        Connection con = Jsoup.connect(url).userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21").timeout(10000);
        return con;
    }
}