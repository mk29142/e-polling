import java.util.ArrayList;
import java.util.List;

class DynamicData {
    List<Box> dynamicQuestions;
    Integer nextLevel;

    DynamicData() {
        // This is used when we don't actually want to send any data back
        // i.e. we have stopped.
        this.dynamicQuestions = null;
        this.nextLevel = -1;
    }

    DynamicData(List<Box> questions, Integer nextLevel) {
        this.dynamicQuestions = questions;
        this.nextLevel = nextLevel;
    }

    // Tells us if there is no dynamic questions in here
    // because we used the faux constructor
    boolean isEnd() {
        return nextLevel < 0 || dynamicQuestions.size() <= 1;
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
