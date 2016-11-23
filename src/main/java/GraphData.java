public class GraphData {

    int id;
    float score;
    int yesVotes;
    int noVotes;
    String text;

    public GraphData(int id, float score, int yesVotes, int noVotes, String text) {
        this.id = id;
        this.score = score;
        this.yesVotes = yesVotes;
        this.noVotes = noVotes;
        this.text = text;
    }
}
