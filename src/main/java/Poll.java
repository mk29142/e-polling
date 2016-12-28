/**
 * Created by sp5714 on 15/11/16.
 */
class Poll {
    int id;
    String name;

    Poll(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Poll = " + this.name + " with id " + this.id;
    }
}
