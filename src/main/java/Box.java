public class Box {
    int id, parent;
    String text, type;

    Box(int id, int parentId, String text, String type) {
        this.id = id;
        this.parent = parentId;
        this.text = text;
        this.type = type;
    }
}
