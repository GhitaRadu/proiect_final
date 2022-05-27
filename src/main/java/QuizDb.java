import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuizDb {
    private Connection connection;

    private static String quizDbUrl = "jdbc:sqlite:quiz.db";

    public Connection getConnection() {
        return connection;
    }

    public QuizDb() {
        try {
            connection = DriverManager.getConnection(quizDbUrl);
            createTableForQuizzes();
            createTableForQuestions();
            createTableForAnswers();
        } catch (Exception ignored) {
            ignored.printStackTrace();
            // FIXME: handle exception and gracefully exit
        }
    }

    private void createTableForQuizzes() {
        try {
            String createQuizTableQuery = "CREATE TABLE IF NOT EXISTS Quizzes("
                    + "quiz_id VARCHAR(255) PRIMARY KEY,"
                    + "quiz_name VARCHAR(255)"
                    + ")";
            Statement statement = connection.createStatement();
            statement.execute(createQuizTableQuery);
        } catch (Exception ignored) {
            ignored.printStackTrace();
            // FIXME: treat exception
        }
    }

    private void createTableForQuestions() {
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
            // FIXME: treat exception
        }
    }

    private void createTableForAnswers() {
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
            // FIXME: treat exception
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
            // FIXME: exceptions
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
            // FIXME: exceptions
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
            // FIXME: exceptions
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

    public List<String> getQuizzes() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT quiz_name FROM Quizzes");

            List<String> quizzes = new ArrayList<>();
            while (resultSet.next()) {
                quizzes.add(resultSet.getString("quiz_name"));
            }
            return quizzes;

        } catch (Exception ignored) {
            ignored.printStackTrace();
            // FIXME: exceptions
        }
        return null;
    }

    public static void main(String args[]) {
        QuizDb quizDb = new QuizDb();

        // Creator creator = new Creator();
        // quizDb.addQuizToDb(creator.createMoviesQuiz());

        System.out.println(quizDb.getQuizzes());
    }
}