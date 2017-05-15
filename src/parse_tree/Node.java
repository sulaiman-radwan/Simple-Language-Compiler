package parse_tree;

import java.util.ArrayList;

public class Node {
    private ArrayList<Node> children;
    private String value;

    public Node() {
        children = new ArrayList<>();
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<Node> children) {
        this.children = children;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
