import java.util.List;

class DynamicData {
    Integer nextLevel;
    List<Box> dynamicQuestions;

    DynamicData(List<Box> questions, Integer nextLevel) {
        this.dynamicQuestions = questions;
        this.nextLevel = nextLevel;
    }
}
