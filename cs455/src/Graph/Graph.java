package Graph;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by ydubale on 2/28/15.
 */
public class Graph {

    private Hashtable<String, Node> nodes;
    private int recursionDepth;

    private ArrayList<String> brokenLinks;

    private String rootURL;

    public Graph(String rootURL){
        this.brokenLinks = new ArrayList<String>();
        this.rootURL = rootURL;
        nodes = new Hashtable<String, Node>();
        recursionDepth = 1;
    }

    public void addNode(String urlToAdd){
        synchronized (nodes){
            if(!nodes.containsKey(urlToAdd)){
                nodes.put(urlToAdd, new Node(urlToAdd));
            }
        }
    }

    public synchronized void addBrokenLink(String brokenLink){
        brokenLinks.add(brokenLink);
    }

    public String getRootURL(){
        return rootURL;
    }

    public synchronized void incrementDepth(){
        recursionDepth++;
    }

    public synchronized int getRecursionDepth(){
        return recursionDepth;
    }

    public void addFromTo(String nodeToAddToString, String urlNode){
        synchronized (nodes){
            //If the node to add to DNE, create it
            addNode(nodeToAddToString);
            addNode(urlNode);

            Node toAddTo = nodes.get(nodeToAddToString);

            toAddTo.addOut(nodes.get(urlNode));
            nodes.get(urlNode).addIn(toAddTo);
        }
    }

    public void printTree(){
        for(Node node : nodes.values()){
            System.out.println(node);
        }
    }

    public static void main(String[] args){

        Graph myG = new Graph("B");

        myG.addNode("D");
        myG.addNode("C");
        myG.addFromTo("A", "B");
        myG.addFromTo("C", "A");

        myG.addFromTo("A", "C");


        myG.printTree();

    }

}
