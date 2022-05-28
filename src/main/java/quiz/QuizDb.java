package quiz;
import java.sql.*;
import java.util.*;

import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * This is for making the connection between the Java code and the DataBase
 */
public enum QuizDb {
    INSTANCE;

    private static Connection connection;

    static {
        try {
            String quizDbUrl = "jdbc:sqlite:quiz.db";
            connection = DriverManager.getConnection(quizDbUrl);
            createTableForQuizzes();
            createTableForQuestions();
            createTableForAnswers();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    private static void createTableForQuizzes() {
        try {
            String createQuizTableQuery = "CREATE TABLE IF NOT EXISTS Quizzes("
                    + "quiz_id VARCHAR(255) PRIMARY KEY,"
                    + "quiz_name VARCHAR(255)"
                    + ")";
            Statement statement = connection.createStatement();
            statement.execute(createQuizTableQuery);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    private static void createTableForQuestions() {
        try {
            String createQuestionsTableQuery = "CREATE TABLE IF NOT EXISTS Questions("
                    + "question_id VARCHAR(255) PRIMARY KEY,"
                    + "quiz_id VARCHAR(255),"
                    + "question VARCHAR(4096),"
                    + "FOREIGN KEY (quiz_id) REFERENCES Quizzes (quiz_id)"
                    + ")";
            Statement statement = connection.createStatement();
            statement.execute(createQuestionsTableQuery);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    private static void createTableForAnswers() {
        try {
            String createAnswersTablesQuery = "CREATE TABLE IF NOT EXISTS Answers("
                    + "answer_id VARCHAR(255) PRIMARY KEY,"
                    + "question_id VARCHAR(255),"
                    + "answer VARCHAR(4096),"
                    + "correct INTEGER,"
                    + "FOREIGN KEY (question_id) REFERENCES Questions (question_id)"
                    + ")";
            Statement statement = connection.createStatement();
            statement.execute(createAnswersTablesQuery);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    private String indexQuizIntoDb(Quiz quiz) {
        try {
            String quizQuery = "INSERT INTO Quizzes VALUES (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(quizQuery);
            String quizId = UUID.randomUUID().toString();
            preparedStatement.setString(1, quizId);
            preparedStatement.setString(2, quiz.getQuizName());
            preparedStatement.execute();
            return quizId;
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        return "";
    }

    private String indexQuestionIntoDb(Question question, String quizId) {
        try {
            String questionQuery = "INSERT INTO Questions VALUES(?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(questionQuery);
            String questionId = UUID.randomUUID().toString();
            preparedStatement.setString(1, questionId);
            preparedStatement.setString(2, quizId);
            preparedStatement.setString(3, question.getQuestionText());
            preparedStatement.execute();
            return questionId;
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        return "";
    }

    private String indexAnswerIntoDb(Answer answer, String questionID) {
        try{
            String answerQuery = "INSERT INTO Answers VALUES(?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(answerQuery);
            String answerID = UUID.randomUUID().toString();
            preparedStatement.setString(1, answerID);
            preparedStatement.setString(2, questionID);
            preparedStatement.setString(3, answer.getText());
            preparedStatement.setInt(4, answer.isCorrect() ? 1 : 0);
            preparedStatement.execute();
            return answerID;
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        return "";
    }

    public void addQuizToDb(Quiz quiz) {
        String quizId = indexQuizIntoDb(quiz);
        for (Question q: quiz.getQuestions()) {
            String questionId = indexQuestionIntoDb(q, quizId);
            for (Answer a: q.getAnswers()) {
                indexAnswerIntoDb(a, questionId);
            }
        }
    }

    /**
     * ImmutablePair will keep the Quiz name and the Quiz id in one variable
     */
    public List<ImmutablePair<String, String>> getQuizzes() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Quizzes");

            List<ImmutablePair<String, String>> quizzes = new ArrayList<>();
            while (resultSet.next()) {
                quizzes.add(
                        new ImmutablePair<>(resultSet.getString("quiz_name"), resultSet.getString("quiz_id"))
                );
            }
            return quizzes;

        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        return null;
    }

    public List<Answer> getAnswersForQuestion(String question_id) {
        try {
            String getAnswersForQuestionFromDbQuery =
                    "SELECT * FROM Answers " +
                            "INNER JOIN Questions ON Questions.question_id=Answers.question_id " +
                            "WHERE Questions.question_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(getAnswersForQuestionFromDbQuery);
            preparedStatement.setString(1, question_id);
            ResultSet rs = preparedStatement.executeQuery();

            List<Answer> answers = new ArrayList<>();
            while (rs.next()) {
                answers.add(new Answer(rs.getString("answer"), rs.getInt("correct") == 1));
            }
            Collections.shuffle(answers);
            return answers;
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        return null;
    }


    public Quiz getQuizFromDb(String quiz_name, String quiz_id) {
        try {
            String getQuestionsFromDbQuery =
                    "SELECT * FROM Questions " +
                            "INNER JOIN Quizzes ON Questions.quiz_id=Quizzes.quiz_id " +
                            "WHERE Quizzes.quiz_id = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(getQuestionsFromDbQuery);
            preparedStatement.setString(1, quiz_id);
            ResultSet rs = preparedStatement.executeQuery();

            List<Question> questions = new ArrayList<>();
            while (rs.next()) {
                String question_id = rs.getString("question_id");
                String questionText = rs.getString("question");
                List<Answer> answers = getAnswersForQuestion(question_id);
                questions.add(new Question(questionText, answers));
            }
            return new Quiz(quiz_name, questions);
        } catch (Exception ignored) {
            ignored.printStackTrace();
            return null;
        }
    }
}