import org.apache.commons.lang3.tuple.ImmutablePair;
import quiz.Answer;
import quiz.Question;
import quiz.Quiz;
import quiz.QuizDb;
import util.InterruptibleInputStream;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

/**
 * Main app, used for either playing or creating a Quiz
 */
public class QuizApp {
    public static void main(String[] args) {
        System.out.println("What would you like to do?\r\n1 = Take a quiz\r\n2 = Create a new quiz\r\n3 = Generate default quiz");

//          ExecutorService allows the usage of a timer
//          (Time allocated to answer a question)

        try
        {
        ExecutorService executorService = Executors.newCachedThreadPool();
        Scanner stdio = new Scanner(new InterruptibleInputStream(System.in)).useDelimiter("\n");
        int userChoice = stdio.nextInt();

        switch (userChoice) {
            case 1 -> {
                System.out.println("Please select a quiz that you want to take");
                List<ImmutablePair<String, String>> quizzes = QuizDb.INSTANCE.getQuizzes();

                for (int i = 0; i < quizzes.size(); i++) {
                    System.out.println(i + 1 + ") " + quizzes.get(i).getLeft());
                }
                int chosenQuiz = stdio.nextInt() - 1;

                boolean checkQuizChoice = 0 <= chosenQuiz && chosenQuiz < quizzes.size();
                if (checkQuizChoice) {
                    int score = 0;

                    Quiz quiz = QuizDb.INSTANCE.getQuizFromDb(quizzes.get(chosenQuiz).getLeft(), quizzes.get(chosenQuiz).getRight());
                    int noOfQuestions = quiz.getQuestions().size();

                    for (Question q : quiz.getQuestions()) {
                        Callable<Boolean> promptForAnswerTask = () -> promptForAnswer(q);
                        Future<Boolean> future = executorService.submit(promptForAnswerTask);
                        try {
                            score += future.get(15, TimeUnit.SECONDS) ? 1 : 0;
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        } catch (TimeoutException e) {
                            System.out.println("\r\nTime to answer has expired");
                            for (Answer a:q.getAnswers()){
                                if(a.isCorrect()){
                            System.out.println("The correct answer was: " + a.getText() + "\r\n");}}
                        } finally {
                            future.cancel(true);
                        }
                    }
                    System.out.printf("Quiz finished! Your score is %s/%s!%n", score, noOfQuestions);
                    executorService.shutdownNow();
                } else {
                    System.out.println("Please choose a valid quiz");
                    main(args);
                }
            }
            case 2 -> {
                System.out.println("Please choose a name for your quiz");
                String quizName = stdio.next();
                while (quizName == null || quizName.isEmpty() || quizName.isBlank()){
                    System.out.println("Quiz must have a name! Please choose a name for your quiz");
                    quizName = stdio.next();;
                }
                List<Question> questions = getQuestionsForNewQuiz(stdio);
                Quiz quiz = new Quiz(quizName, questions);
                QuizDb.INSTANCE.addQuizToDb(quiz);
            }
            case 3 ->
                Creator.createDefaultQuiz();

            default -> {System.out.println("Please choose '1' if you wish to take a quiz, '2' if you wish to create a new quiz, or '3' if you wish to generate the default quiz");
            main(args);}
        }} catch (InputMismatchException e){
            System.out.println("Please choose '1' if you wish to take a quiz, '2' if you wish to create a new quiz, or '3' if you wish to generate the default quiz");
            main(args);
        }
    }

    private static List<Question> getQuestionsForNewQuiz(Scanner stdio) {
        List<Question> questions = new ArrayList<>();
        while (true) {
            System.out.println("Would you like to add a new question to your quiz?");
            String willAddNewQuestion = stdio.next();

            if (willAddNewQuestion.equals("no")) break;
            else if (willAddNewQuestion.equals("yes")) {
                System.out.println("Please insert the question:");
                String questionText = stdio.next();
                while (questionText == null || questionText.isEmpty() || questionText.isBlank()){
                    System.out.println("Question must have a text! Please insert the question:");
                    questionText = stdio.next();
                }
                List<Answer> answers = getAnswersForNewQuestion(stdio);
                questions.add(new Question(questionText, answers));
            } else {
                System.out.println("Please select 'yes' if you wish to add a new question to your quiz, " +
                        "or 'no' if you are satisfied with the current questions");
            }
        }
        if (questions.size() >= 1) {
            return questions;
        } else{
            System.out.println("The quiz must contain at least one question");
            return getQuestionsForNewQuiz(stdio);
        }
    }

    private static List<Answer> getAnswersForNewQuestion(Scanner stdio) {
        List<Answer> answers = new ArrayList<>();
        while (true) {
            System.out.println("Would you like to add a new answer for this question?");
            String willAddNewAnswer = stdio.next();

            if (willAddNewAnswer.equals("no")){
                if(answers.stream().filter(Answer::isCorrect).toList().size() == 0){
                    System.out.println("It seems there is no correct answer to this question. Please add a correct answer:");
                    String answerText = stdio.next();
                    while (answerText == null || answerText.isBlank() || answerText.isEmpty()){
                        System.out.println("Answer must have a text! Please insert the answer:");
                        answerText = stdio.next();
                    }
                    answers.add(new Answer(answerText, true));
                }
                if(answers.size() <= 1){
                    System.out.println("This question does not have any incorrect answers. Please add an incorrect answer:");
                    String answerText = stdio.next();
                    while (answerText == null || answerText.isBlank() || answerText.isEmpty()){
                        System.out.println("Answer must have a text! Please insert the answer:");
                        answerText = stdio.next();
                    }
                    answers.add(new Answer(answerText, false));
                }
                break;
            }
            else if (willAddNewAnswer.equals("yes")) {
                System.out.println("Please insert the answer:");
                String answerText = stdio.next();
                while (answerText == null || answerText.isBlank() || answerText.isEmpty()){
                    System.out.println("Answer must have a text! Please insert the answer:");
                    answerText = stdio.next();
                }

                while (true){
                if(answers.stream().filter(Answer::isCorrect).toList().size() == 0) {
                    System.out.println("Is this answer true or false?");
                    String correct = stdio.next();
                    if (correct.equals("true")) {
                        answers.add(new Answer(answerText, true));break;
                    } else {
                        if (correct.equals("false")) {
                            answers.add(new Answer(answerText, false));break;
                        } else {
                            System.out.println("Please specify the correctness of the answer using either 'true' or 'false'");
                        }
                    }
                } else{
                answers.add(new Answer(answerText, false));break;}}

            } else { if (willAddNewAnswer.equals("reset")){
                System.out.println("The answers for the current question have been deleted");
                return getAnswersForNewQuestion(stdio);
            }
                System.out.println("Please select 'yes' if you wish to add a new answer for this question," +
                        "or 'no' if you are satisfied with the current answers");
            }
        }
        if (answers.stream().filter(Answer::isCorrect).toList().size() == 1 && answers.size() >= 2) {
            return answers;
        } else {
            System.out.println("The answer list does not contain exactly one right answer and at least one wrong answer;" +
                    "The answers for the current question have been invalidated, please add them again!");
            return getAnswersForNewQuestion(stdio);
        }
    }

    /**
     * Will prompt for an answer for the given question
     * Will be run on a separate thread and interrupted after the timeout
     * @param q The question to prompt answers for
     * @return true if the answer is correct and false otherwise
     */
    private static boolean promptForAnswer(Question q) {
        Scanner stdio = new Scanner(new InterruptibleInputStream(System.in));
        System.out.println(q.getQuestionText());
        for (int i = 0; i < q.getAnswers().size(); i++) {
            System.out.println(i + 1 + ") " + q.getAnswers().get(i).getText());
        }
        if (stdio.hasNextInt()) {
            int chosenAnswer = stdio.nextInt() - 1;
            try{
            if(q.getAnswers().get(chosenAnswer).isCorrect()){
                System.out.println("\r\nYour answer is correct!\r\n");
            }
            else{
                for (Answer a:q.getAnswers()) {
                                if(a.isCorrect()){
                                    System.out.println("\r\nIncorrect! The correct answer was: " + a.getText() + "\r\n");
                                }}
            }} catch (IndexOutOfBoundsException index){
                System.out.println("\r\nInvalid answer! Please answer again using a number from 1 to "+ q.getAnswers().size() + " corresponding to each question");
                return promptForAnswer(q);
            }
            return 0 <= chosenAnswer && chosenAnswer < q.getAnswers().size() && q.getAnswers().get(chosenAnswer).isCorrect();
        }
        return false;
    }
}