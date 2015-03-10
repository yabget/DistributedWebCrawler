package util;

import net.htmlparser.jericho.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;

/**
 * Created by ydubale on 3/6/15.
 * Singleton class to
 */

public class HTMLParser {

    private static final HTMLParser ourInstance = new HTMLParser();

    private Map<String, String> crawledURLs = new Hashtable<String, String>();
    private Map<String, String> brokenURLs = new Hashtable<String, String>();
    private Map<String, String> relayedURLs = new Hashtable<String, String>();
    private Map<String, String> ignoredURLs = new Hashtable<String, String>();

    public static HTMLParser getInstance() {
        return ourInstance;
    }

    private HTMLParser() {
    }

    /**
     * Adds to the list of relayed urls, this list is kept so that other crawlers
     * don't get duplicate requests for crawls from one(this) crawler
     * @param relayedURL - the url to relay
     * @return True if url has not been sent before
     */
    public boolean addToRelayedURLs(String relayedURL){
        synchronized (relayedURLs){
            if(relayedURLs.containsKey(relayedURL)){
                return false;
            }
            relayedURLs.put(relayedURL, relayedURL);
            return true;
        }
    }

    /**
     * Checks if the href received is valid.
     * @param href - the href to check
     * @return True if it is a valid href
     */
    private boolean isValidHREF(String href){
        if(href.startsWith("#") || href.startsWith("mailto") || href.startsWith("https") || href.startsWith("ftp")){
            return false;
        }
        return true;
    }

    /**
     * Checks if the page received is within the domain of the 8 crawlers
     * @param page - a web page with http://www...
     * @return True if the page is with in the 8 crawlers domain
     */
    private boolean isValidPage(String page) {
        for(String validPage : Util.validRedirectDomains){
            if(page.contains(validPage)){
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the given page has a valid ending
     * @param page - the page to check
     * @return True if the page has a given ending
     */
    private boolean validEnding(String page) {
        String[] dotDelim = page.split("\\.");
        page = dotDelim[dotDelim.length-1];
        return page.contains("html") || page.contains("htm") || page.contains("php") ||
                page.contains("shtml") || page.contains("/") || page.contains("asp") ||
                page.contains("jsp") || page.contains("cfm");
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

    public Collection<String> getBrokenLinks(){
        return brokenURLs.keySet();
    }

    public Set<String> getUnCrawledURLs(String page){
        Set<String> uncrawledURLs = new HashSet<String>(); // Local set to be returned

        Config.LoggerProvider = LoggerProvider.DISABLED;

        try{
            HttpURLConnection connection = (HttpURLConnection)(new URL(page).openConnection());
            connection.setReadTimeout(20 * 1000);
            connection.setConnectTimeout(20 * 1000);
            connection.connect();

            InputStream inputStream = connection.getInputStream();

            String redirectedUrl = connection.getURL().toString(); // Redirected url if exists

            Source source = new Source(inputStream);

            for(Element anchorTag : source.getAllElements(HTMLElementName.A)){
                String href = anchorTag.getAttributeValue("href");

                if(href == null || href.length() == 0){
                    continue;
                }


                if(href.toLowerCase().contains("%7e")){
                    href = href.replaceAll("%7e", "~");
                    href = href.replaceAll("%7E", "~");
                }


                if(!isValidHREF(href)){
                    ignoredURLs.put(href, href);
                    continue;
                }

                if(!new URI(href).isAbsolute()){
                    href = new URI(redirectedUrl).resolve(href).toString();
                }

                href = normalize(href);

                URL temp = new URL(href);
                // [scheme:][//authority][path][?query][#fragment]
                href = "http://" + temp.getAuthority() + temp.getPath(); //Remove query and fragments

                if(crawledURLs.containsKey(href)){
                    continue;
                }

                if(!validEnding(href)){
                    ignoredURLs.put(href, href);
                    continue;
                }

                if(brokenURLs.containsKey(href)){
                    continue;
                }

                if(!isValidPage(href)){
                    ignoredURLs.put(href, href);
                    continue;
                }

                addToCrawledURLs(href);
                uncrawledURLs.add(href); //Local uncrawled urls to be returned
            }
        }
        catch (MalformedURLException e) {
            brokenURLs.put(page, page);
        } catch (IOException e) {
            brokenURLs.put(page, page);
        } catch (URISyntaxException e) {
            brokenURLs.put(page, page);
        }

        return uncrawledURLs;
    }

    public void addToCrawledURLs(String url){
        crawledURLs.put(url, url);
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


        String page = "http://www.biology.colostate.edu";

        Set<String> firstLevel = HTMLParser.getInstance().getUnCrawledURLs(page);
        for(String s : firstLevel){
            System.out.println(s);
        }


    }

}
