import java.util.List;
import java.util.Optional;

public class Question {
    private String questionText;
    private List<Answer> answers;

    public Question(String questionText, List<Answer> answers) {
        this.questionText = questionText;
        this.answers = answers;
    }

    public String getQuestionText() {
        return  questionText;
    }

    public List<Answer> getAnswers() {
        return answers;
    }
}
