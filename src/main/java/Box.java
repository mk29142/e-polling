class Box {
    int id, parent;
    String text, type, vote;

    Box(int id, int parentId, String text, String type, String vote) {
        this.id = id;
        this.parent = parentId;
        this.text = text;
        this.type = type;
        this.vote = vote;
    }
}
