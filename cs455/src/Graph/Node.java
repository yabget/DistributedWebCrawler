package Graph;

import java.util.Collection;
import java.util.Hashtable;

/**
 * Created by ydubale on 2/28/15.
 */
public class Node {

    private String value;
    private Hashtable<String, Node> in;
    private Hashtable<String, Node> out;

    public Node(String value) {
        this.value = value;
        in = new Hashtable<String, Node>();
        out = new Hashtable<String, Node>();
    }

    public String getValue(){
        return value;
    }

    /**
     * Returns all of the nodes that point to this node
     * @return
     */
    public Collection<Node> getInNodes(){
        return in.values();
    }

    /**
     * Returns all of the nodes that this node points to
     * @return
     */
    public Collection<Node> getOutNodes(){
        return out.values();
    }

    public void addIn(Node node){
        in.put(node.getValue(), node);
    }

    public void addOut(Node node){
        out.put(node.getValue(), node);
    }

    public String toString(){
        //todo: Use stringbuffer
        String toReturn = value + " --->\n";
        String inS = "\tIN\n\t\t\t";
        String outS = "\tOut\n\t\t\t";

        for(String inNodes : in.keySet()){
            inS += "\t\t\t" + inNodes + "\n";
        }

        for(String outNodes : out.keySet()){
            outS += "\t\t\t" + outNodes + "\n";
        }

        return toReturn + inS + "\n" + outS + "\n";
    }
}
