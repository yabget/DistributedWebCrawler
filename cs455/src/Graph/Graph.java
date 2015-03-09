package Graph;

import java.util.Collection;
import java.util.Hashtable;

/**
 * Created by ydubale on 2/28/15.
 */
public class Graph {

    private String rootURL;
    private Hashtable<String, Node> nodes;

    public Graph(String rootURL){
        this.rootURL = rootURL;
        nodes = new Hashtable<String, Node>();
    }

    public String getRootURL(){
        return rootURL;
    }

    public Collection<Node> getNodes(){
        return nodes.values();
    }

    /**
     * Adds the node to the graph
     * @param urlToAdd - url to add to graph
     */
    public void addNode(String urlToAdd){
        synchronized (nodes){
            if(!nodes.containsKey(urlToAdd)){
                nodes.put(urlToAdd, new Node(urlToAdd));
            }
        }
    }

    /**
     * Creates an edge between two nodes.
     * Adds the destination to the out node of the source
     * Adds the source to the in node of the destination
     * @param source - the source node
     * @param destination - the destination node
     */
    public void addFromTo(String source, String destination){
        synchronized (nodes){
            //If the node to add to DNE, create it
            addNode(source);
            addNode(destination);

            Node toAddTo = nodes.get(source);

            toAddTo.addOut(nodes.get(destination));
            nodes.get(destination).addIn(toAddTo);
        }
    }

    public void printTree(){
        for(Node node : nodes.values()){
            System.out.println(node);
        }
    }
}
