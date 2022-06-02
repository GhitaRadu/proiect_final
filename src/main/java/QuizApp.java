import org.apache.commons.lang3.tuple.ImmutablePair;
import quiz.*;
import util.InterruptibleInputStream;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * Main app, used for either playing or creating a Quiz
 */
public class QuizApp {
    public static void main(String[] args) {
        System.out.println("""
                What would you like to do?\r
                1 = Take a quiz\r
                2 = Create a new quiz\r
                3 = Show scoreboard\r
                4 = Generate default quiz\r
                5 = Clear scoreboard\r
                6 = Delete a quiz\r
                7 = Delete current quizzes\r
                8 = Exit app""");
        try {
        ExecutorService executorService = Executors.newCachedThreadPool();
        Scanner stdio = new Scanner(new InterruptibleInputStream(System.in)).useDelimiter("\n");
        int userChoice = stdio.nextInt();

        switch (userChoice) {
            case 1 -> {
                System.out.println("Please select a quiz that you want to take");
                List<ImmutablePair<String, String>> quizzes = QuizDb.INSTANCE.getQuizzes();

                if (quizzes.isEmpty()){
                    System.out.println("\r\n(List is empty)\r\n");
                    main(args);
                } else {

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
                            score += future.get(q.getTimer(), TimeUnit.SECONDS) ? 1 : 0;
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
                    float percentage = (float) score*100/noOfQuestions;
                    percentage = Float.parseFloat(new DecimalFormat("##.##").format(percentage));

                    if (score == noOfQuestions){
                        System.out.printf("Congratulations! You have a perfect score of %s/%s!%n", score, noOfQuestions);
                        System.out.println("(" + percentage + ")%\r\n");
                    } else {
                    System.out.printf("Quiz finished! Your score is %s/%s!%n", score, noOfQuestions);
                        System.out.println("(" + percentage + ")%\r\n");}
                    executorService.shutdownNow();

                    System.out.println("Please enter your nickname:");
                    Score currentScore =  new Score(stdio.next(), score);
                    while (currentScore.getNickname() == null || currentScore.getNickname().isEmpty() || currentScore.getNickname().isBlank()){
                        System.out.println("Name cannot be blank! Please enter a valid name:");
                        currentScore =  new Score(stdio.next(), score);
                    }
                    if (!currentScore.getNickname().equals("skip")) {
                        QuizDb.INSTANCE.addScoreToDb(currentScore, quizzes.get(chosenQuiz).getRight());
                    }

                    System.out.println("\r\n");
                    main(args);
                } else {
                    System.out.println("\r\nPlease choose a valid quiz!\r\n");
                    main(args);
                }
            }}

            case 2 -> {
                System.out.println("Please choose a name for your quiz");
                String quizName = stdio.next();
                while (quizName == null || quizName.isEmpty() || quizName.isBlank()){
                    System.out.println("Quiz must have a name! Please choose a name for your quiz");
                    quizName = stdio.next();
                }
                List<Question> questions = getQuestionsForNewQuiz(stdio);
                Quiz quiz = new Quiz(quizName, questions);
                QuizDb.INSTANCE.addQuizToDb(quiz);
                System.out.printf("\r\nThe %s quiz has been successfully added to the quiz list!\r\n%n", quizName);
                main(args);
            }

            case  3 -> { try {
                System.out.println("Please select a quiz to see its scoreboard");
                List<ImmutablePair<String, String>> quizzes = QuizDb.INSTANCE.getQuizzes();

                if (quizzes.isEmpty()){
                    System.out.println("\r\n(List is empty)\r\n");
                    main(args);
                } else {
                    for (int i = 0; i < quizzes.size(); i++) {
                        System.out.println(i + 1 + ") " + quizzes.get(i).getLeft());
                    }
                    int chosenQuiz = stdio.nextInt() - 1;

                    boolean checkQuizChoice = 0 <= chosenQuiz && chosenQuiz < quizzes.size();
                    if (checkQuizChoice) {

                        Quiz quiz = QuizDb.INSTANCE.getQuizFromDb(quizzes.get(chosenQuiz).getLeft(), quizzes.get(chosenQuiz).getRight());
                        List<Score> scoreboard = QuizDb.INSTANCE.getResultsForQuiz(quizzes.get(chosenQuiz).getRight());
                        int noOfQuestions = quiz.getQuestions().size();

                        if (scoreboard.isEmpty()){
                            System.out.println("\r\nAs of yet, there are no scores for this quiz\r\n");
                            main(args);
                        }

                        for (Score s:scoreboard) {
                            if (s.getPoints() == noOfQuestions){
                                System.out.printf("%s has a perfect score of %s/%s on the '%s' quiz\r\n", s.getNickname(), s.getPoints(), noOfQuestions, quizzes.get(chosenQuiz).getLeft());
                            } else {
                                System.out.printf("%s has a score of %s/%s on the '%s' quiz\r\n", s.getNickname(), s.getPoints(), noOfQuestions, quizzes.get(chosenQuiz).getLeft());
                            }
                        }

                        System.out.println("\r\n");
                        main(args);
                    } else {System.out.println("\r\nPlease choose a valid quiz to see the scoreboard!\r\n");
                        main(args);}
                }} catch (InputMismatchException e) {System.out.println("\r\nPlease choose a valid quiz to see the scoreboard!\r\n"); main(args);}
            }

            case 4 -> {
                        Creator.createDefaultQuiz();
                        System.out.println("\r\nThe default quiz 'Movies' has been generated\r\n");
                        main(args);
                }

            case 5 -> {
                System.out.println("Please select a quiz to clear its scoreboard");
                List<ImmutablePair<String, String>> quizzes = QuizDb.INSTANCE.getQuizzes();

                if (quizzes.isEmpty()){
                    System.out.println("\r\n(List is empty)\r\n");
                    main(args);
                } else {

                    for (int i = 0; i < quizzes.size(); i++) {
                        System.out.println(i + 1 + ") " + quizzes.get(i).getLeft());
                    }
                    int chosenQuiz = stdio.nextInt() - 1;

                    boolean checkQuizChoice = 0 <= chosenQuiz && chosenQuiz < quizzes.size();
                    if (checkQuizChoice) {
                        if (QuizDb.INSTANCE.getResultsForQuiz(quizzes.get(chosenQuiz).getRight()).isEmpty()) {
                            System.out.println("There are no scores to be deleted for the selected quiz\r\n");
                            main(args);
                        }
                        System.out.printf("Are you sure you want to clear the scoreboard for the '%s' quiz?\r\n", quizzes.get(chosenQuiz).getLeft());
                        String clearChoice = stdio.next().toLowerCase();
                        if (clearChoice.equals("yes")){
                            QuizDb.deleteScores(quizzes.get(chosenQuiz).getRight());
                            System.out.printf("Scoreboard for the '%s' quiz has been cleared\r\n\r\n", quizzes.get(chosenQuiz).getLeft());
                        } else {
                            System.out.println("Scores have not been deleted\r\n");
                        }
                    } else {
                        System.out.println("\r\nPlease choose a valid quiz!\r\n");
                    }}main(args);}

            case  6 -> {
                System.out.println("\r\nPlease enter the number corresponding to the quiz that you wish to remove\r\n");
                List<ImmutablePair<String, String>> quizzes = QuizDb.INSTANCE.getQuizzes();

                if (quizzes.isEmpty()){
                    System.out.println("\r\n(List is empty)\r\n");
                    main(args);
                } else {
                    for (int i = 0; i < quizzes.size(); i++) {
                        System.out.println(i + 1 + ") " + quizzes.get(i).getLeft());
                    }
                    int chosenQuiz = stdio.nextInt() - 1;
                    boolean checkQuizChoice = 0 <= chosenQuiz && chosenQuiz < quizzes.size();
                    if (checkQuizChoice) {
                        System.out.println("Are you sure you want to permanently delete quiz " + quizzes.get(chosenQuiz).getLeft() + " ?");
                        String makeSure = stdio.next().toLowerCase();
                        if (makeSure.equals("yes")){
                            quiz.QuizDb.deleteScores(quizzes.get(chosenQuiz).getLeft());
                            quiz.QuizDb.deleteAnswers(quizzes.get(chosenQuiz).getLeft());
                            quiz.QuizDb.deleteQuestions(quizzes.get(chosenQuiz).getLeft());
                            quiz.QuizDb.deleteQuiz(quizzes.get(chosenQuiz).getLeft());
                            System.out.printf("\r\nQuiz '%s' was successfully deleted\r\n\r\n", quizzes.get(chosenQuiz).getLeft());
                            main(args);
                        } else {
                            System.out.println("\r\nNothing happened, list is intact\r\n");
                            main(args);
                        }} else {
                        System.out.println("\r\nPlease choose a valid quiz to delete\r\n");
                        main(args);
                    }}}

            case 7 -> {
                System.out.println("Are you sure you want to delete all quizzes, along with their questions and answers?");
                String makeSure = stdio.next().toLowerCase();
                if(Objects.equals(makeSure, "yes")){
                    QuizDb.deleteDataFromTables();
                    if (QuizDb.INSTANCE.getQuizzes().isEmpty()){
                    System.out.println("\r\nAll quizzes have been successfully deleted\r\n");}
                    else {
                        System.out.println("\r\nTask failed\r\n");
                    }
                    main(args);
                }
                else {
                    System.out.println("\r\nNothing happened, list is intact\r\n");
                    main(args);
                }
            }

            case 8 -> System.exit(0);

            default -> {System.out.println("Please choose one of the options from 1 - 8\r\n");
            main(args);}
        }} catch (InputMismatchException e){
            System.out.println("Please choose one of the options from 1 - 8\r\n");
            main(args);
        } catch (SQLException sqlException) {
            System.out.println("Something went wrong with the DataBase");
            System.exit(1);
        }
    }

    private static List<Question> getQuestionsForNewQuiz(Scanner stdio) {
            List<Question> questions = new ArrayList<>();
            while (true) {
                System.out.println("Would you like to add a new question to your quiz?");
                String willAddNewQuestion = stdio.next().toLowerCase();

                if (willAddNewQuestion.equals("no")) break;
                else if (willAddNewQuestion.equals("yes")) {
                    System.out.println("Please insert the question:");
                    String questionText = stdio.next();
                    while (questionText == null || questionText.isEmpty() || questionText.isBlank()) {
                        System.out.println("Question must have a text! Please insert the question:");
                        questionText = stdio.next();
                    }

                    System.out.println("Would you like to set a timer? (Standard is 20 seconds)");

                    String willChangeTimer = stdio.next().toLowerCase();

                    while (willChangeTimer.isEmpty() || willChangeTimer.isBlank()) {
                        System.out.println("Please answer using 'yes' or 'no'");
                        willChangeTimer = stdio.next().toLowerCase();
                    }

                    while (!willChangeTimer.equals("no") && !willChangeTimer.equals("yes")){
                        System.out.println("Please answer using 'yes' or 'no'");
                        willChangeTimer = stdio.next().toLowerCase();
                    }

                    int timer = 20;
                    if (willChangeTimer.equals("yes")) {
                        timer = setTimer(timer);
                    } else {
                        System.out.println("Time for this question was set at 20 seconds");
                    }

                    List<Answer> answers = getAnswersForNewQuestion(stdio);
                    questions.add(new Question(questionText, answers, timer));
                } else {
                    System.out.println("Please select 'yes' if you wish to add a new question to your quiz, " +
                            "or 'no' if you are satisfied with the current questions");
                }
            }
            if (questions.size() >= 1) {
                return questions;
            } else {
                System.out.println("The quiz must contain at least one question");
                return getQuestionsForNewQuiz(stdio);
            }
        }

    private static List<Answer> getAnswersForNewQuestion(Scanner stdio) {
        List<Answer> answers = new ArrayList<>();
        while (true) {
            System.out.println("Would you like to add a new answer for this question?");
            String willAddNewAnswer = stdio.next().toLowerCase();

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
                    String correct = stdio.next().toLowerCase();
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
            return answers;
    }

    /**
     * Will prompt for an answer for the given question
     * Will be run on a separate thread and interrupted after the timeout
     * @param q The question to prompt answers for
     * @return true if the answer is correct and false otherwise
     */
    private static boolean promptForAnswer(Question q) { try{
        Scanner stdio = new Scanner(new InterruptibleInputStream(System.in));
        System.out.println(q.getQuestionText());
        for (int i = 0; i < q.getAnswers().size(); i++) {
            System.out.println(i + 1 + ") " + q.getAnswers().get(i).getText());
        }
        try{
            int chosenAnswer = stdio.nextInt() - 1;
            boolean checkChosenAnswer = 0 <= chosenAnswer && chosenAnswer < q.getAnswers().size();

            if (checkChosenAnswer){

                if(q.getAnswers().get(chosenAnswer).isCorrect()){
                    System.out.println("\r\nYour answer is correct!\r\n");
                }
                else{
                    for (Answer a:q.getAnswers()) {
                        if(a.isCorrect()){
                            System.out.println("\r\nIncorrect! The correct answer was: " + a.getText() + "\r\n");
                        }}
                }
            return chosenAnswer < q.getAnswers().size() && q.getAnswers().get(chosenAnswer).isCorrect();} else {
                System.out.println("Please choose a valid answer!");
                return promptForAnswer(q);
            }
        } catch (IndexOutOfBoundsException index){
            System.out.println("\r\nInvalid answer! Please answer again using a number from 1 to "+ q.getAnswers().size() + " corresponding to each question");
            return promptForAnswer(q);
        }
    } catch (InputMismatchException e){
        System.out.println("Please choose a valid answer!"); return promptForAnswer(q);}
    }

    private static int setTimer(int time) { try {
        System.out.println("Please set the timer:");

        Scanner stdio = new Scanner(System.in);
        time = stdio.nextInt();

                if (time < 5) {
                    System.out.println("Timer must be at least 5 seconds!");
                    return setTimer(time);
                }
                return time;
        } catch (Exception e) {
            System.out.println("Please insert a number to set the timer!");
            return setTimer(time);
        }
    }
}