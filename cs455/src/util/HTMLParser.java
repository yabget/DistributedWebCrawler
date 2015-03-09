package util;

import net.htmlparser.jericho.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by ydubale on 3/6/15.
 * Singleton class to
 */

public class HTMLParser {

    private static final String HREF = "href";

    private static final HTMLParser ourInstance = new HTMLParser();

    private Map<String, String> crawledURLs = new HashMap<String, String>();
    private Map<String, String> badURLs = new HashMap<String, String>();
    private Map<String, String> relayedURLs = new HashMap<String, String>();

    public static HTMLParser getInstance() {
        return ourInstance;
    }

    private HTMLParser() {
    }

    // True if not sent before
    public boolean addToRelayedURLs(String relayedURL){
        synchronized (relayedURLs){
            if(relayedURLs.containsKey(relayedURL)){
                return false;
            }
            relayedURLs.put(relayedURL, null);
            return true;
        }
    }

    public static String convertToDirectory(String url){
        String dirURL = url;

        dirURL.replace("https://", "");
        dirURL = dirURL.replace("http://", "");

        /*if(dirURL.charAt(dirURL.length()-1) == '/'){
            dirURL = dirURL.substring(0, dirURL.length()-1);
        }*/
        dirURL = dirURL.replaceAll("/", "_");

        return dirURL;
    }

    private String resolveRedirects(String url) {
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
            synchronized (badURLs){
                badURLs.put(url, null);
            }
            PrintHelper.printFail("HTMLParser - Could not open connection in redirect: " + url);
        }
        return url;
    }

    private boolean isValidHREF(String href){
        if(href == null || href.length() == 0){
             //System.out.println("[INVALID]: " + href);
            return false;
        }
        if(href.startsWith("#") || href.startsWith("mailto") ||
                href.startsWith("https") || href.startsWith("ftp")){
            //System.out.println("[INVALID]: " + href);
            return false;
        }
        return true;
    }

    private boolean isValidPage(String page) {
        for(String validpage : Storage.validRedirectDomains){
            if(page.contains(validpage)){
                return true;
            }
        }
        //System.out.println("[INVALID]: " + page);
        return false;
    }

    private String removeHTTP(String url){
        url = url.toLowerCase();
        if(url.contains("https://")){
            return url.replaceAll("https://", "");
        }
        if(url.contains("http://")){
            return url.replaceAll("http://", "");
        }
        return url;
    }

    private String resolveRelativePath(String pageURL, String href){
        if(href.startsWith("./")){
            pageURL = removeSlashAtEndOfURL(pageURL);
            return pageURL + href.replaceAll("\\./", "/");
        }

        String urlPrefix = pageURL.split("://")[0] + "://";
        pageURL = removeHTTP(pageURL);

        String[] slashSeperated = pageURL.split("/");

        int lastIndex = slashSeperated.length-1; //Skip page currently on

        while(href.startsWith("../")){
            if(lastIndex == 0){
                break;
            }
            href = href.replaceFirst("../", "");
            lastIndex--;
        }

        String rebuiltURL = urlPrefix;
        for(int i=0; i < lastIndex; i++){
            rebuiltURL += slashSeperated[i] + "/";
        }
        return rebuiltURL + href;
    }

    private String removeSlashAtEndOfURL(String url){
        if(url.charAt(url.length()-1) == '/'){
            return url.substring(0, url.length()-1);
        }
        return url;
    }

    /**
     * Licensed under http://www.apache.org/licenses/LICENSE-2.0
     */
    public static String normalize(String normalized) {

        if (normalized == null) {
            return null;
        }

        // If the buffer begins with "./" or "../", the "." or ".." is removed.
        if (normalized.startsWith("./")) {
            normalized = normalized.substring(1);
        } else if (normalized.startsWith("../")) {
            normalized = normalized.substring(2);
        } else if (normalized.startsWith("..")) {
            normalized = normalized.substring(2);
        }

        // All occurrences of "/./" in the buffer are replaced with "/"
        int index = -1;
        while ((index = normalized.indexOf("/./")) != -1) {
            normalized = normalized.substring(0, index) + normalized.substring(index + 2);
        }

        // If the buffer ends with "/.", the "." is removed.
        if (normalized.endsWith("/.")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        int startIndex = 0;

        // All occurrences of "/<segment>/../" in the buffer, where ".."
        // and <segment> are complete path segments, are iteratively replaced
        // with "/" in order from left to right until no matching pattern remains.
        // If the buffer ends with "/<segment>/..", that is also replaced
        // with "/".  Note that <segment> may be empty.
        while ((index = normalized.indexOf("/../", startIndex)) != -1) {
            int slashIndex = normalized.lastIndexOf('/', index - 1);
            if (slashIndex >= 0) {
                normalized = normalized.substring(0, slashIndex) + normalized.substring(index + 3);
            } else {
                startIndex = index + 3;
            }
        }
        if (normalized.endsWith("/..")) {
            int slashIndex = normalized.lastIndexOf('/', normalized.length() - 4);
            if (slashIndex >= 0) {
                normalized = normalized.substring(0, slashIndex + 1);
            }
        }

        // All prefixes of "<segment>/../" in the buffer, where ".."
        // and <segment> are complete path segments, are iteratively replaced
        // with "/" in order from left to right until no matching pattern remains.
        // If the buffer ends with "<segment>/..", that is also replaced
        // with "/".  Note that <segment> may be empty.
        while ((index = normalized.indexOf("/../")) != -1) {
            int slashIndex = normalized.lastIndexOf('/', index - 1);
            if (slashIndex >= 0) {
                break;
            } else {
                normalized = normalized.substring(index + 3);
            }
        }
        if (normalized.endsWith("/..")) {
            int slashIndex = normalized.lastIndexOf('/', normalized.length() - 4);
            if (slashIndex < 0) {
                normalized = "/";
            }
        }

        return normalized;
    }

    public Set<String> getUnCrawledURLs(String page){
        //PrintHelper.printAlert("Getting uncrawled pages: " + page);

        Set<String> uncrawledURLs = new HashSet<String>();

        Config.LoggerProvider = LoggerProvider.DISABLED;

        //page = removeSlashAtEndOfURL(page);

        try{
            /*String redirectResolvedPage = resolveRedirects(page);
            if(!redirectResolvedPage.equals(page)){
                PrintHelper.printAlert("Resolved " + page + " to " + redirectResolvedPage);
            }*/
            HttpURLConnection connection = (HttpURLConnection)(new URL(page).openConnection());
            connection.setReadTimeout(5 * 1000);
            connection.setConnectTimeout(5 * 1000);
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            // this is the actual url, the page is redirected to (if there is a redirect).
            String redirectedUrl = connection.getURL().toString();
            // instead of passing the URL, pass the input stream.
            Source source = new Source(inputStream);
            //Source source = new Source(new URL(page));

            // Go through all the anchor tags on the page
            for(Element anchorTag : source.getAllElements(HTMLElementName.A)){
                String href = anchorTag.getAttributeValue(HREF);

                if(!isValidHREF(href)){
                    continue;
                }



                /*if(href.startsWith("/")){
                    href = page + href;
                }
                else if(href.startsWith(".")){
                    //System.out.print(href + " --------------------------------------- ");
                    href = resolveRelativePath(redirectedUrl, href);
                    //System.out.println(href + "\n");
                }*/

                if(!new URI(href).isAbsolute()){
                    //System.out.print(href + " --------------------------------------- ");
                    href = new URI(page).resolve(href).toString();
                    //System.out.println(href + "\n");
                }

                href = normalize(href);

                URL temp = new URL(href);
                //Remove query and fragments
                //URI(String scheme, String authority, String path, String query, String fragment)
                //[scheme:][//authority][path][?query][#fragment]
                href = "http://" + temp.getAuthority() + temp.getPath();

                if(!validEnding(href)){
                    //PrintHelper.printFail("Not valid ending: " + href);
                    continue;
                }

                synchronized (badURLs){
                    if(badURLs.containsKey(href)){
                        //PrintHelper.printAlert("Bad URL " + href);
                        continue;
                    }
                }

                synchronized (crawledURLs){
                    if(crawledURLs.containsKey(href)){
                        //PrintHelper.printAlert("Already crawled " + href);
                        continue;
                    }
                }

                if(!isValidPage(href)){
                    //PrintHelper.printAlert("Not valid " + href);
                    continue;
                }

                //System.out.println("[VALID]: " + href);
                addToCrawledURLs(href);
                uncrawledURLs.add(href);
            }
        }
        catch (MalformedURLException e) {
            //e.printStackTrace();
            //PrintHelper.printFail("HTMLParser - got malformed URL: " + page);
        } catch (IOException e) {
            synchronized (badURLs){
                badURLs.put(page, null);
            }
            //PrintHelper.printFail("HTMLParser - had read exception URL: " + page);
        } catch (URISyntaxException e) {
            synchronized (badURLs){
                badURLs.put(page, null);
            }
        }
        /*catch (URISyntaxException e) {
            //e.printStackTrace();
            synchronized (badURLs){
                badURLs.put(page, null);
            }
        }
        */
        return uncrawledURLs;
    }

    private boolean validEnding(String href) {
        String[] dotDelim = href.split("\\.");
        href = dotDelim[dotDelim.length-1];
        return href.contains("html") || href.contains("htm") || href.contains("php") ||
                href.contains("shtml") || href.contains("/") || href.contains("asp") ||
                href.contains("jsp") || href.contains("cfm");
    }

    public void addToCrawledURLs(String url){
        synchronized (crawledURLs){
            crawledURLs.put(url, null);
        }
    }

    public static void main(String[] args){


        //String page = "http://www.bmb.colostate.edu";
        //String page = "http://www.biology.colostate.edu/";
        //String page = "http://www.chem.colostate.edu/";
        //String page = "http://www.cs.colostate.edu/cstop/index.html";
        //String page = "http://www.math.colostate.edu/";
        //String page = "http://www.physics.colostate.edu/";
        //String page = "http://www.colostate.edu/Depts/Psychology/";
        //String page = "http://www.stat.colostate.edu/";


        String page = "http://www.cs.colostate.edu/";

        Set<String> firstLevel = HTMLParser.getInstance().getUnCrawledURLs(page);
        //System.out.println("\nSize: " + firstLevel.size());
        for(String s : firstLevel){
            System.out.println(s);
        }


    }

}
