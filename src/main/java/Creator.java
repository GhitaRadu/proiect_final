import quiz.Answer;
import quiz.Question;
import quiz.Quiz;
import quiz.QuizDb;
import java.util.ArrayList;
import java.util.List;

/**
 * This class has been created with the sole purpose of having an initial Quiz to work with,
 * before finishing the "Quiz creator" part of the code
 */
public class Creator {
    private static Quiz createMoviesQuiz() {
        List<Answer> answersForQuestion1 = new ArrayList<>();

        Answer answer1ForQuestion1 = new Answer("The Godfather", false);
        Answer answer2ForQuestion1 = new Answer("Phantasm", true);
        Answer answer3ForQuestion1 = new Answer("Once Upon A Time In America", false);
        Answer answer4ForQuestion1 = new Answer("La Dolce Vita", false);

        answersForQuestion1.add(answer1ForQuestion1);
        answersForQuestion1.add(answer2ForQuestion1);
        answersForQuestion1.add(answer3ForQuestion1);
        answersForQuestion1.add(answer4ForQuestion1);

        List<Answer> answersForQuestion2 = new ArrayList<>();

        Answer answer1ForQuestion2 = new Answer("Morgan Freeman", false);
        Answer answer2ForQuestion2 = new Answer("Brad Pitt", false);
        Answer answer3ForQuestion2 = new Answer("Clint Eastwood", true);
        Answer answer4ForQuestion2 = new Answer("Geoffrey Rush", false);

        answersForQuestion2.add(answer1ForQuestion2);
        answersForQuestion2.add(answer2ForQuestion2);
        answersForQuestion2.add(answer3ForQuestion2);
        answersForQuestion2.add(answer4ForQuestion2);

        List<Answer> answersForQuestion3 = new ArrayList<>();

        Answer answer1ForQuestion3 = new Answer("1994", false);
        Answer answer2ForQuestion3 = new Answer("1995", true);
        Answer answer3ForQuestion3 = new Answer("1996", false);
        Answer answer4ForQuestion3 = new Answer("1997", false);

        answersForQuestion3.add(answer1ForQuestion3);
        answersForQuestion3.add(answer2ForQuestion3);
        answersForQuestion3.add(answer3ForQuestion3);
        answersForQuestion3.add(answer4ForQuestion3);

        Question question1ForQuiz1 = new Question("Which of the following movie was directed by Don Coscarelli?",
                answersForQuestion1);
        Question question2ForQuiz1 = new Question("Which of the following actors has never won an Oscar for acting?",
                answersForQuestion2);
        Question question3ForQuiz1 = new Question("In what year was the movie Se7en released?",
                answersForQuestion3);

        List<Question> questions1 = new ArrayList<>();

        questions1.add(question1ForQuiz1);
        questions1.add(question2ForQuiz1);
        questions1.add(question3ForQuiz1);

        return new Quiz("Movies", questions1);
    }

     /*
     This will add the initial "Movies" Quiz to the DataBase
     One time use
    */
    public static void createDefaultQuiz() {
        QuizDb.INSTANCE.addQuizToDb(createMoviesQuiz());
    }
}