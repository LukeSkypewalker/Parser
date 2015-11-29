import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;


public class Songs {
    public static void main(String[] args) throws IOException {
        String url = "http://amdm.ru/akkordi/picca/";
        Document doc = Jsoup.connect(url).get();
        Elements links = doc.select(".artist-profile-song-list .g-link");

        for (Element link : links) {
            print("<%s>  (%s)", link.attr("abs:href"), trim(link.text(), 35));
        }
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