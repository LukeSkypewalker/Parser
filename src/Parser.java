import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sun.deploy.net.proxy.StaticProxyManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Random;


public class Parser {
    static long counter=0;

    static Random rand = new Random();

    static MongoCollection<org.bson.Document> songs;

    public static void main(String[] args) throws IOException {
        connectToDataBase();
        parseSongs("http://amdm.ru/");
    }

    private static void connectToDataBase() {
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        MongoDatabase database = mongoClient.getDatabase("SyncSong");
        songs = database.getCollection("songs");
    }

    private static void parseSongs(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Elements linksOfArtistsAlphabet = doc.select(".alphabet a");
        for (Element link : linksOfArtistsAlphabet) {
            getLinksOfArtists(link.attr("abs:href"));
        }
    }

    private static void getLinksOfArtists(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Elements linksOfArtists = doc.select("article .artist");
        for (Element link : linksOfArtists) {
            getLinksOfSongs( link.attr("abs:href") );
            try {
                Thread.sleep(rand.nextInt(10000)+10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void getLinksOfSongs(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Elements linksOfSongs = doc.select(".artist-profile-song-list .g-link");
        for (Element link : linksOfSongs) {
            getSong(link.attr("abs:href"));
        }
    }

    private static void getSong(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        String artist = doc.select("[itemprop=\"byArtist\"]").first().text();
        String songName = doc.select("[itemprop=\"name\"]").first().text();
        String lyric = doc.select("[itemprop=\"chordsBlock\"]").first().text();

        System.out.println(counter++ + " " + artist + " - " + songName);

        addSongToDatabase(artist, songName, lyric);

        try {
            Thread.sleep(rand.nextInt(1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void addSongToDatabase(String artist, String songName, String lyric) {
        org.bson.Document songDocument = new org.bson.Document
                ("artist", artist)
                .append("name", songName)
                .append("lyric", lyric);

        songs.insertOne(songDocument);
    }
}