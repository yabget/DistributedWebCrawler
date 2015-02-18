package util;

import net.htmlparser.jericho.*;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ydubale on 2/17/15.
 */
public class URLExtractor {

    //TODO: Are these the only prefixes we need to worry about?
    private static final String[] validURLPrefixes = {
            "http://www.bmb.colostate.edu", "http://www.biology.colostate.edu",
            "http://www.chm.colostate.edu", "http://www.cs.colostate.edu",
            "http://www.math.colostate.edu", "http://www.physics.colostate.edu",
            "http://www.colostate.edu/Depts/Psychology", "http://www.stat.colostate.edu",
            "/"
    };

    public static Set<String> parseURL(String url) {
        Set<String> urlList = new HashSet<String>();
        Config.LoggerProvider = LoggerProvider.DISABLED;
        try {
            Source source = new Source(new URL(url));
            List<Element> aTags = source.getAllElements(HTMLElementName.A);

            for(Element aTag : aTags){
                String href = aTag.getAttributeValue("href");
                for(String validURL : validURLPrefixes){
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
            e.printStackTrace();
            Error.printErrorExit(Error.COULD_NOT_READ_URL);
        }
        return urlList;
    }

}
