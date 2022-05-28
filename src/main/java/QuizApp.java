import org.apache.commons.lang3.tuple.ImmutablePair;
import quiz.Answer;
import quiz.Question;
import quiz.Quiz;
import quiz.QuizDb;
import util.InterruptibleInputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

public class QuizApp {
    public static void main(String[] args) {
        System.out.println("What would you like to do?\r\n1 = Take a quiz\r\n2 = Create a new quiz\r\n3 = Generate default quiz");

        /*
          ExecutorService allows the usage of a timer
          (Time allocated to answer a question)
         */
        ExecutorService executorService = Executors.newCachedThreadPool();
        Scanner stdio = new Scanner(new InterruptibleInputStream(System.in)).useDelimiter("\n");
        int userChoice = stdio.nextInt();

        switch (userChoice) {
            case 1 -> {
                System.out.println("Please select a quiz that you want to take");
                List<ImmutablePair<String, String>> quizzes = QuizDb.INSTANCE.getQuizzes();

                /*
                  This will list all the Quizzes for the user to choose
                 */
                for (int i = 0; i < quizzes.size(); i++) {
                    System.out.println(i + 1 + ") " + quizzes.get(i).getLeft());
                }
                int chosenQuiz = stdio.nextInt() - 1;

                /*
                  This will ensure that the user makes a valid choice
                 */
                boolean checkQuizChoice = 0 <= chosenQuiz && chosenQuiz < quizzes.size();
                if (checkQuizChoice) {
                    int score = 0;

                    Quiz quiz = QuizDb.INSTANCE.getQuizFromDb(quizzes.get(chosenQuiz).getLeft(), quizzes.get(chosenQuiz).getRight());
                    int noOfQuestions = quiz.getQuestions().size();

                    /*
                      This will ensure that failing to answer the question before the time expires
                      will not increase the score
                     */
                    for (Question q : quiz.getQuestions()) {
                        Callable<Boolean> promptForAnswerTask = () -> promptForAnswer(q);
                        Future<Boolean> future = executorService.submit(promptForAnswerTask);
                        try {
                            score += future.get(15, TimeUnit.SECONDS) ? 1 : 0;
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
                } else {
                    System.out.println("Please choose a valid quiz");
                }
            }
            case 2 -> {
                System.out.println("Please choose a name for your quiz");
                String quizName = stdio.next();
                List<Question> questions = getQuestionsForNewQuiz(stdio);
                Quiz quiz = new Quiz(quizName, questions);
                QuizDb.INSTANCE.addQuizToDb(quiz);
            }
            case 3 -> {
                Creator.createDefaultQuiz();
            }
            default -> System.out.println("Please choose '1' if you wish to take a quiz, or '2' if you wish to create a new quiz");
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
                List<Answer> answers = getAnswersForNewQuestion(stdio);
                questions.add(new Question(questionText, answers));
            } else {
                System.out.println("Please select 'yes' if you wish to add a new question to your quiz, " +
                        "or 'no' if you are satisfied with the current questions");
            }
        }
        return questions;
    }

    private static List<Answer> getAnswersForNewQuestion(Scanner stdio) {
        List<Answer> answers = new ArrayList<>();
        while (true) {
            System.out.println("Would you like to add a new answer for this question?");
            String willAddNewAnswer = stdio.next();

            if (willAddNewAnswer.equals("no")) break;
            else if (willAddNewAnswer.equals("yes")) {
                System.out.println("Please insert the answer:");
                String answerText = stdio.next();
                System.out.println("Is this answer true or false?");
                boolean correct = stdio.next().equals("true");
                answers.add(new Answer(answerText, correct));
            } else {
                System.out.println("Please select 'yes' if you wish to add a new answer for this question," +
                        "or 'no' if you are satisfied with the current answers");
            }
        }
        if (answers.stream().filter(Answer::isCorrect).toList().size() == 1 && answers.size() >= 2) {
            return answers;
        } else {
            System.out.println("The answer list does not contain exactly one right answer");
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
            return 0 <= chosenAnswer && chosenAnswer < q.getAnswers().size() && q.getAnswers().get(chosenAnswer).isCorrect();
        }
        return false;
    }
}