import org.apache.commons.lang3.tuple.ImmutablePair;
import quiz.Question;
import quiz.Quiz;
import quiz.QuizDb;
import util.InterruptibleInputStream;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

public class QuizApp {
    public static void main(String args[]) throws SQLException {
        System.out.println("What would you like to do?\r\n1 = Take a quiz\r\n2 = Create a new quiz");
        ExecutorService executorService = Executors.newCachedThreadPool();
        Scanner stdio = new Scanner(new InterruptibleInputStream(System.in));
        int userChoice = stdio.nextInt();

        switch (userChoice){

        case 1:
            System.out.println("Please select a quiz that you want to take");
            List<ImmutablePair<String, String>> quizzes = QuizDb.INSTANCE.getQuizzes();

            for (int i = 0; i < quizzes.size(); i++) {
                System.out.println(i + 1 + ") " + quizzes.get(i).getLeft());
            }

            int chosenQuiz = stdio.nextInt() - 1;

            boolean checkQuizChoice = 0 <= chosenQuiz && chosenQuiz < quizzes.size();

            if (checkQuizChoice){
                int score = 0;

                Quiz quiz = QuizDb.INSTANCE.getQuizFromDb(quizzes.get(chosenQuiz).getLeft(), quizzes.get(chosenQuiz).getRight());
                int noOfQuestions = quiz.getQuestions().size();

                for (Question q: quiz.getQuestions()) {
                    Callable<Boolean> promptForAnswerTask = () -> promptForAnswer(q);
                    Future<Boolean> future = executorService.submit(promptForAnswerTask);
                    try {
                        score += future.get(5, TimeUnit.SECONDS) ? 1 : 0;
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        System.out.println("Time to answer has expired");
                    } finally {
                        future.cancel(true);
                    }

                }
                System.out.printf("Quiz finished! Your score is %s/%s!%n", score, noOfQuestions);
                executorService.shutdownNow();
            }

            else {
                System.out.println("Please choose a valid quiz");
            }
            break;

//            case 2:
//                System.out.println("Please choose a name for your quiz");
//                Scanner scanner4 = new Scanner(System.in);
//                System.out.println("Would you like to add a new question to your quiz?");
//                Scanner scanner5 = new Scanner(System.in);
//                String string3 = scanner5.nextLine();
//
//                switch (string3){
//                    case "yes":
//                        List<Question> questionList = new ArrayList<>();
//                        System.out.println("Please insert the question:");
//                        Scanner scanner6 = new Scanner(System.in);
//
//                        System.out.println("How many seconds will be allocated for this question?");
//                        Scanner scanner7 = new Scanner(System.in);
//                        int timer = scanner7.nextInt();
//
//                        System.out.println("Would you like to3 add a new answer for this question?");
//                        Scanner scanner8 = new Scanner(System.in);
//                        String string4 = scanner8.nextLine();
//                        switch (string4){
//
//                            case "yes":
//                                List<Answer> answerList = new ArrayList<>();
//                                System.out.println("Please insert the answer:");
//                                Scanner scanner9 = new Scanner(System.in);
//                                System.out.println("Is this answer true or false?");
//                                Scanner scanner10 = new Scanner(System.in);
//                                String string5 = scanner10.nextLine();
//                                switch (string5){
//                                    case "true":
//                                        Answer answer = new Answer(scanner8.toString(), true);
//                                        answerList.add(answer);
//                                    case "false":
//                                        Answer answer = new Answer(scanner8.toString(), false);
//                                        answerList.add(answer);
//                                    default:
//                                        System.out.println("You need to specify if this answer is 'true' or 'false'.");
//                                }
//
//                            case "no":
//                                Question question = new Question(scanner6.toString(), answerList, timer);
//                                questionList.add(question);
//                            default:
//                                System.out.println("Please select 'yes' if you wish to add a new answer for this question," +
//                                        "or 'no' if you are satisfied with the current answers");
//                        }
//
//                    case "no":
//                        Quiz quiz = new Quiz(scanner4.toString(), questionList);
//                        quiz.addQuizToDb();
//
//                    default:
//                        System.out.println("Please select 'yes' if you wish to add a new question to your quiz, " +
//                                "or 'no' if you are satisfied with the current questions");
//                }
            default:
                System.out.println("Please choose '1' if you wish to take a quiz, or '2' if you wish to create a new quiz");
            }
        }

    private static boolean promptForAnswer(Question q) {
        Scanner stdio = new Scanner(new InterruptibleInputStream(System.in));
        System.out.println(q.getQuestionText());
        for (int i = 0; i < q.getAnswers().size(); i++) {
            System.out.println(i + 1 + ") " + q.getAnswers().get(i).getText());
        }
        if (stdio.hasNextInt()) {
            int chosenAnswer = stdio.nextInt() - 1;
            return 0 <= chosenAnswer && chosenAnswer < q.getAnswers().size() && q.getAnswers().get(chosenAnswer).isCorrect();
        }
        return false;
    }
}