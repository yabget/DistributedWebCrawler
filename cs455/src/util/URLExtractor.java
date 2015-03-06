package util;

import net.htmlparser.jericho.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ydubale on 2/17/15.
 */
public class URLExtractor {

    private static String resolveRedirects(String url) {
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection)(new URL(url).openConnection());
            con.setInstanceFollowRedirects(false);
            con.connect();

            int responseCode = con.getResponseCode();
            if(responseCode == 301){
                return con.getHeaderField("Location");
            }

        } catch (IOException e) {
            PrintHelper.printFail("Could not open connection in redirect.");
        }
        return url;
    }

    public static String convertToDirectory(String url){
        String dirURL = url;

        dirURL.replace("https://", "");
        dirURL = dirURL.replace("http://", "");

        if(dirURL.charAt(dirURL.length()-1) == '/'){
            dirURL = dirURL.substring(0, dirURL.length()-1);
        }

        return dirURL;
    }

    //TODO: PLEASE BREAK THIS UP!!!!!!!!!!!!!!!!!!
    public static Set<String> parseURL(String url) {
        Set<String> urlList = new HashSet<String>();
        Config.LoggerProvider = LoggerProvider.DISABLED;
        try {
            String pageURL = resolveRedirects(url);
            Source source = new Source(new URL(pageURL));
            List<Element> aTags = source.getAllElements(HTMLElementName.A);

            for(Element aTag : aTags){
                String href = aTag.getAttributeValue("href");
                //System.out.println(href);

                if(href == null){
                    continue;
                }
                if(href.length() == 0){
                    continue;
                }

                if(href.charAt(0) == '.'){
                    String[] tempURL = pageURL.split("://");
                    //System.out.println("TEMP URL: " + tempURL[1]);
                    String[] slashURL = tempURL[1].split("/");
                    int lastIndex = slashURL.length;
                    while(href.charAt(0) == '.'){
                        if(slashURL[slashURL.length-1].equals("cstop")){
                            break;
                        }
                        //System.out.print("REPLACING "+ pageURL + "\t" + href +" ----------------------------------");
                        href = href.replace("./", "");
                        lastIndex--;
                        if(lastIndex == 0){
                            break;
                        }
                       // System.out.println(href);
                    }
                    String newPrefix = new String();
                    for(int i =0; i < lastIndex; i++){
                        newPrefix += slashURL[i] + "/";
                    }
                    href = tempURL[0] + "://" + newPrefix.toString() + href;
                    //System.out.println("CRAWL THISSSSSSSSSSSSSSSSSSS -- " + href);
                }


                if(href.charAt(href.length()-1) == '/'){
                    href = href.substring(0, href.length()-1);
                }

                for(String validURL : Storage.validURLPrefixes){
                    //System.out.println(validURL + " == " + href);
                    if(href.contains(validURL)){

                        if(validURL == "/"){    //URL refers to current page ex. /LINK
                            if(!href.contains("://")){ //As long as URL is not http://
                                if(!href.equals("/")){
                                    urlList.add(href);
                                }
                            }
                            break;
                        }

                        urlList.add(href);
                        break;
                    }
                    else{
                        //System.out.println("Not added: " + href);
                    }
                }
            }
            //System.out.println("Done parsing url");
        } catch (IOException e) {
            PrintHelper.printFail(PrintHelper.COULD_NOT_READ_URL + "\t" + url);
        }
        return urlList;
    }

}
