package uz.klimuz.weatherclock;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;

public class CurrencyParser{
private static Document getPage() throws IOException {
        String url = "https://bank.uz/currency";
        Document page = new Document(null);//Jsoup.parse(new URL(url), 10000);
        try {
                page = Jsoup.parse(new URL(url), 10000);
        }catch (IOException e){
                e.printStackTrace();
        }
        return page;
}
public static String findOutCourse() throws  IOException {
        String value = "";
        String uncutValue = "";
        Element element = null;
        try {
                Document page = getPage();
                        if (page != null) {
                        element = page.select("div[class=tabs-a]").first();
                }
                if (element != null){
                        uncutValue = element.text();
                        value = uncutValue.substring(4, 10);
                }

        }catch (NullPointerException e){
                e.printStackTrace();
        }
        if (value.equals("")){
                value = "many";
        }
        return "1 USD = " + value + " UZS";
}

}
