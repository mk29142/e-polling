class GraphData {
    int id;
    Integer parentId;
    float score;
    float baseScore;
    int yesVotes;
    int noVotes;
    String text;

    GraphData(
            int id,
            Integer parentId,
            float score,
            int yesVotes,
            int noVotes,
            String text) {
        this.id = id;
        this.parentId = parentId;
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

    public Node toNode() {
        return new Node(id, id, score, baseScore, text);
    }

    public Link toLink() {
        if (parentId != null) {
            return new Link(10, parentId, id);
        } else {
            return null;
        }
    }
}
