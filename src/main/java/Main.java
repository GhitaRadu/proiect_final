import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class Main {
    public List<String> Main() throws SQLException {

        List<String> quizzes = QuizDb.getQuizzes();

        System.out.println("What would you like to do?\r\n 1 = Take a quiz \r\n 2 = Create a new quiz");
        Scanner scanner1 = new Scanner(System.in);
        int int1 = scanner1.nextInt();

        switch (int1){

        case 1:
//            for (Quiz q:quizzes) {
//                System.out.println(q.getQuizName());
//            }

            System.out.println("Please select a quiz that you want to take");
            List<String> quizzesList = quizzes;
            Scanner scanner2 = new Scanner(System.in);
            String string1 = scanner2.nextLine();
            boolean checkQuizName = quizzes.contains(string1);

            if (checkQuizName){
                int noOfQuestions = 0;
                int score = 0;

                public List<Question> getListQuestions() {
                    try{
                    Statement statement = statement.getConnection().createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT Questions.question FROM Questions INNER JOIN Quizzes ON Questions.quiz_id=Quizzes.quiz_id");
                        List<String> questions = new ArrayList<>();
                        while (resultSet.next()) {
                            questions.add(resultSet.getString("question"));
                        }
                        return questions;

                        for (String question:questions) {
                            noOfQuestions++;

                            try{
                            Statement statement2 = statement2.getConnection().createStatement();
                            ResultSet resultSet2 = statement2.executeQuery("SELECT Answers.answer FROM Answers INNER JOIN Questions ON Answers.question_id=Questions.question_id");
                            List<String> answers = new ArrayList<>();
                            while (resultSet2.next()){
                                answers.add(resultSet2.getString("answer"));
                            }
                            Collections.shuffle(answers);
                            System.out.println("Please choose the correct answer");

                            for (String answer: answers) {
                                System.out.println(answer);
                            }

                            Scanner scanner3 = new Scanner(System.in);
                            String string2 = scanner3.nextLine();
                            boolean checkAnswer = answers.contains(string2);

                            if(checkAnswer){

                                for (String answer: answers) {

                                    if( (Answer) answer.isCorrect()){
                                        System.out.println("Correct!");
                                        score++;
                                    }

                                    else{
                                        System.out.println("Wrong!");
                                    }
                                }
                            }

                            else{
                                System.out.println("Please select a valid answer");
                            }

                        } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    catch (Exception ignored) {
                        ignored.printStackTrace();
                        // FIXME: exceptions
                    return null;

                    }

                System.out.printf("Quiz finished! Your score is %s/%s!%n", score, noOfQuestions);
                }}

            else {
                System.out.println("Please choose a valid quiz name");
            }

            case 2:
                System.out.println("Please choose a name for your quiz");
                Scanner scanner4 = new Scanner(System.in);
                System.out.println("Would you like to add a new question to your quiz?");
                Scanner scanner5 = new Scanner(System.in);
                String string3 = scanner5.nextLine();

                switch (string3){
                    case "yes":
                        List<Question> questionList = new ArrayList<>();
                        System.out.println("Please insert the question:");
                        Scanner scanner6 = new Scanner(System.in);

                        System.out.println("How many seconds will be allocated for this question?");
                        Scanner scanner7 = new Scanner(System.in);
                        int timer = scanner7.nextInt();

                        System.out.println("Would you like to add a new answer for this question?");
                        Scanner scanner8 = new Scanner(System.in);
                        String string4 = scanner8.nextLine();
                        switch (string4){

                            case "yes":
                                List<Answer> answerList = new ArrayList<>();
                                System.out.println("Please insert the answer:");
                                Scanner scanner9 = new Scanner(System.in);
                                System.out.println("Is this answer true or false?");
                                Scanner scanner10 = new Scanner(System.in);
                                String string5 = scanner10.nextLine();
                                switch (string5){
                                    case "true":
                                        Answer answer = new Answer(scanner8.toString(), true);
                                        answerList.add(answer);
                                    case "false":
                                        Answer answer = new Answer(scanner8.toString(), false);
                                        answerList.add(answer);
                                    default:
                                        System.out.println("You need to specify if this answer is 'true' or 'false'.");
                                }

                            case "no":
                                Question question = new Question(scanner6.toString(), answerList, timer);
                                questionList.add(question);
                            default:
                                System.out.println("Please select 'yes' if you wish to add a new answer for this question," +
                                        "or 'no' if you are satisfied with the current answers");
                        }

                    case "no":
                        Quiz quiz = new Quiz(scanner4.toString(), questionList);
                        quiz.addQuizToDb();

                    default:
                        System.out.println("Please select 'yes' if you wish to add a new question to your quiz, " +
                                "or 'no' if you are satisfied with the current questions");
                }
            default:
                System.out.println("Please choose '1' if you wish to take a quiz, or '2' if you wish to create a new quiz");
            }
        }
}