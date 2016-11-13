import java.util.List;

public class DynamicData {

    Integer nextLevel;
    List<List<Box>> dynamicQuestions;
    public DynamicData(List<List<Box>> questions, Integer nextLevel) {
        this.dynamicQuestions = questions;
        this.nextLevel = nextLevel;
    }
}
