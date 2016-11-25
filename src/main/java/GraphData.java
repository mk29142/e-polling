public class GraphData {

    int id;
    float score;
    float baseScore;
    int yesVotes;
    int noVotes;
    String text;

    public GraphData(int id, float score, int yesVotes, int noVotes, String text) {
        this.id = id;
        this.score = score;
        this.yesVotes = yesVotes;
        this.noVotes = noVotes;
        this.text = text;
        this.baseScore = getBaseScore();
    }

    private float getBaseScore() {
        int totalVotes = this.yesVotes + this.noVotes;
        if (totalVotes == 0) {
            return 0.5f;
        } else {
            return 0.5f + (0.5f * (this.yesVotes - this.noVotes) / totalVotes);
        }
    }
}
