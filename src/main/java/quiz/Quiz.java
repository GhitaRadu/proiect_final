package quiz;

import java.util.List;

public class Quiz {
    private final String quizName;
    private final List<Question> questions;


    public Quiz(String quizName, List<Question> questions) {
        this.quizName = quizName;
        this.questions = questions;
    }

    public List<Question> getQuestions() { return questions; }

    public String getQuizName() {
        return quizName;
    }
}
