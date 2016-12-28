import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

class NodeGraphBuilder {
    private AnswersUtils au;

    NodeGraphBuilder(Connection connection, String pollId) {
        this.au = new AnswersUtils(connection, pollId);
    }

    ResultGraph createResultGraph() {
        List<GraphData> gd = this.au.getGraphData();
        List<Node> nodes = generateNodes(gd);
        List<Link> links = generateLinks(gd);
        return new ResultGraph(nodes, links);
    }

    private List<Node> generateNodes(List<GraphData> gd) {
        List<Node> nodes = new ArrayList<>();
        for (GraphData node : gd) {
            nodes.add(node.toNode());
        }
        return nodes;
    }

    private List<Link> generateLinks(List<GraphData> gd) {
        List<Link> links = new ArrayList<>();
        for (GraphData node : gd) {
            Link newLink = node.toLink();
            if (newLink != null) {
                links.add(newLink);
            }
        }

        return links;
    }
}
