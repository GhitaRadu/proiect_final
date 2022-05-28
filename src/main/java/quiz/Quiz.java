package quiz;

import java.util.List;

public class Quiz {
    private String quizName;
    private List<Question> questions;

    public Quiz(String quizName, List<Question> questions) {
        this.quizName = quizName;
        this.questions = questions;
    }

    public void addQuestion(Question question){
        questions.add(question);
    }
    public List<Question> getQuestions() { return questions; }

    public String getQuizName() {
        return quizName;
    }
}
