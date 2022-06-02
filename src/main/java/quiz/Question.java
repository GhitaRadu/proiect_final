package quiz;
import java.util.List;

public class Question {
    private final String questionText;
    private final List<Answer> answers;
    private final int timer;

    public Question(String questionText, List<Answer> answers, int timer) {
        this.questionText = questionText;
        this.answers = answers;
        this.timer = timer;
    }

    public String getQuestionText() {
        return  questionText;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public int getTimer() {
        return timer;
    }
}