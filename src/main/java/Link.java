/**
 * Created by har14 on 25/11/16.
 */
class Link {
    String source;
    String target;
    int value;

    Link(int value, Node source, Node target) {
        this.value = value;
        this.source = source.id;
        this.target = target.id;
    }
}
