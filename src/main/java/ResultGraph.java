import java.util.List;

/**
 * Created by har14 on 25/11/16.
 */
public class ResultGraph {
    List<Node> nodes;
    List<Link> links;

    public ResultGraph(List<Node> nodes, List<Link> links) {
        this.nodes = nodes;
        this.links = links;
    }
}
