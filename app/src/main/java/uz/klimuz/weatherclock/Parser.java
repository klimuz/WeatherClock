package uz.klimuz.weatherclock;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class Parser {
    private static Document getPage() throws IOException {
        String url = "https://yandex.uz/pogoda/"; //"https://yandex.uz/pogoda/tashkent?utm_campaign=informer&utm_content=main_informer&utm_medium=web&utm_source=home&lat=41.311151&lon=69.279737";

        Document page = Jsoup.parse(new URL(url), 10000);
        return page;
    }
    private static String imageDecoder(String imageQuiery){
        String result = "";
        if (imageQuiery.contains("skc-n ")){ //ясно night
            result = "skc_n";
        } else if (imageQuiery.contains("skc-d ")){ //ясно day
            result = "skc_d";
        } else if (imageQuiery.contains("bkn-n ")){ //облачно night
            result = "bkn_n";
        } else if (imageQuiery.contains("bkn-d ")){ //облачно day
            result = "bkn_d";
        } else if (imageQuiery.contains("ovc-sn ")){ //снег
            result = "ovc_sn";
        } else if (imageQuiery.contains("ovc-m-sn ")){ //небольшой снег
            result = "ovc_m_sn";
        } else if (imageQuiery.contains("ovc-m-ra ")){ //небольшой дождь
            result = "ovc_m_ra";
        } else if (imageQuiery.contains("ovc ")){ //пасмурно
            result = "ovc";
        } else if (imageQuiery.contains("ovc-ra-sn ")){ //дождь со снегом
            result = "ovc_ra_sn";
        } else if (imageQuiery.contains("ovc-ra ")){ //дождь
            result = "ovc_ra";
        } else if (imageQuiery.contains("bkn-p-ra-d ")){ //ливень днём
            result = "bkn_p_ra_d";
        } else if (imageQuiery.contains("bkn-p-ra-n ")){ //ливень night
            result = "bkn_p_ra_n";
        } else if (imageQuiery.contains("sunset ")){ //закат
            result = "sunset";
        } else if (imageQuiery.contains("sunrise ")){ //восход
            result = "sunrise";
        }
        return result;
    }
    public static ArrayList mainMethod() throws IOException {
        String tempCurr = "";
        String imgCurr = "";
        String imageCode = "";
        ArrayList<String> hourlyInfo = new ArrayList();
        try {
            Document page = getPage();
            Element informer = page.select("div[class=fact__temp-wrap]").first();
            Element hourly = page.select("div[class=swiper-container fact__hourly-swiper]").first();
            Elements hours = hourly.select("li[class=fact__hour swiper-slide]");
            if (informer != null) {
                tempCurr = informer.select("span[class=temp__value temp__value_with-unit]").first().text();
                imgCurr = imageDecoder(informer.select("img").toString());
                hourlyInfo.add(0, tempCurr);
                hourlyInfo.add(1, imgCurr);
                for (Element element : hours ) {
                    String temperature = element.select("div[class=fact__hour-temp]").text();
                    if (!temperature.contains("Kun")) {
                        hourlyInfo.add(temperature);
                        imageCode = imageDecoder(element.select("img").toString());
                        hourlyInfo.add(imageCode);
                    }
                }
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        return hourlyInfo;
    }
}
