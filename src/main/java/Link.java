/**
 * Created by har14 on 25/11/16.
 */
public class Link {
    String source;
    String target;
    int value;

    public Link(int value, Node source, Node target) {
        this.value = value;
        this.source = source.id;
        this.target = target.id;
    }
}
