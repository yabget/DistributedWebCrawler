package Graph;

import java.util.Collection;
import java.util.Hashtable;

/**
 * Created by ydubale on 2/28/15.
 */
public class Node {

    private Hashtable<String, Node> in;
    private Hashtable<String, Node> out;
    private String value;

    public Node(String value) {
        this.value = value;
        in = new Hashtable<String, Node>();
        out = new Hashtable<String, Node>();
    }

    public String getValue(){
        return value;
    }

    public Collection<Node> getInNodes(){
        return in.values();
    }

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
        String toReturn = value + "--->\n";
        String inS = "\tIn------------------------------------------------------------------------------------\n\t\t\t";
        String outS = "\tOut++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n\t\t\t";

        for(String inNodes : in.keySet()){
            inS += "\t\t\t" + inNodes + "\n";
        }

        for(String outNodes : out.keySet()){
            outS += "\t\t\t" + outNodes + "\n";
        }

        return toReturn + inS + "\n" + outS + "\n";

    }
}
