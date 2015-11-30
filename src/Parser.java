import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Random;

//TODO reconnect refactoring
//TODO make a links list first, start from previous
//TODO track updates (on firs page ?) or hash

public class Parser {
    static long counter=0;

    static Random rand = new Random();

    static MongoCollection<org.bson.Document> songs;

    public static void main(String[] args) throws IOException {
        connectToDataBase();
//        parseSongs("http://amdm.ru/");
        getLinksOfArtists("http://amdm.ru/chords/1/");
    }

    private static void connectToDataBase() {
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        MongoDatabase database = mongoClient.getDatabase("SyncSong");
        songs = database.getCollection("songs");
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
            getSong(link.attr("abs:href"));
        }
    }

    private static void getSong(String url) throws IOException {
        Connection con = connect(url);
        Document doc = con.get();
        String artist = doc.select("[itemprop=\"byArtist\"]").first().text();
        String songName = doc.select("[itemprop=\"name\"]").first().text();
        String lyric = doc.select("[itemprop=\"chordsBlock\"]").first().text();

        System.out.println(counter++ + " " + artist + " - " + songName);

        addSongToDatabase(artist, songName, lyric);

        try {
            Thread.sleep(2000);
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

    private static Connection connect(String url){
        Connection con = Jsoup.connect(url).userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21").timeout(10000);
        return con;
    }
}