import java.util.List;

class DynamicData {
    List<Box> dynamicQuestions;

    DynamicData(List<Box> questions) {
        this.dynamicQuestions = questions;
    }

    // Tells us if there is no dynamic questions in here
    // because we used the faux constructor
    boolean isEnd() {
        return dynamicQuestions.size() <= 1;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');

        for (Box b : dynamicQuestions) {
            sb.append(b.toString());
            sb.append(',');
        }

        sb.append(']');
        return sb.toString();
    }
}
