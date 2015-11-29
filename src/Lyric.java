import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;


public class Lyric {
    public static void main(String[] args) throws IOException {
        String url = "http://amdm.ru/akkordi/picca/101700/pyatnica/";
        Document doc = Jsoup.connect(url).get();
        Element lyric = doc.select("[itemprop=\"chordsBlock\"]").first();
        Element artist = doc.select("[itemprop=\"byArtist\"]").first();
        Element songName = doc.select("[itemprop=\"name\"]").first();

        System.out.println(artist.text());
        System.out.println(songName.text());
        System.out.println(lyric.text());
    }

    private static void print(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }

    private static String trim(String s, int width) {
        if (s.length() > width)
            return s.substring(0, width-1) + ".";
        else
            return s;
    }
}