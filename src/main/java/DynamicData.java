import java.util.List;

class DynamicData {
    Integer nextLevel;
    List<List<Box>> dynamicQuestions;

    DynamicData(List<List<Box>> questions, Integer nextLevel) {
        this.dynamicQuestions = questions;
        this.nextLevel = nextLevel;
    }
}
