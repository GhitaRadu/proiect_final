package quiz;
import java.sql.*;
import java.util.*;
import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * This is for making the connection between the Java code and the DataBase
 */
public enum QuizDb {
    INSTANCE;

    private static final Connection connection;

    static {

            String quizDbUrl = "jdbc:sqlite:quiz.db";
        try {
            connection = DriverManager.getConnection(quizDbUrl);
            createTableForQuizzes();
            createTableForQuestions();
            createTableForAnswers();
            createTableForScoreboard();
        } catch (SQLException e) {
            System.out.println("Could not create connection to database");
            throw new RuntimeException(e.getMessage());
        }
    }

    private static void createTableForQuizzes() throws SQLException {
            String createQuizTableQuery = "CREATE TABLE IF NOT EXISTS Quizzes("
                    + "quiz_id VARCHAR(255) PRIMARY KEY,"
                    + "quiz_name VARCHAR(255)"
                    + ")";
            Statement statement = connection.createStatement();
            statement.execute(createQuizTableQuery);
    }

    private static void createTableForQuestions() throws SQLException {
            String createQuestionsTableQuery = "CREATE TABLE IF NOT EXISTS Questions("
                    + "question_id VARCHAR(255) PRIMARY KEY,"
                    + "quiz_id VARCHAR(255),"
                    + "question VARCHAR(4096),"
                    + "timer INTEGER,"
                    + "FOREIGN KEY (quiz_id) REFERENCES Quizzes (quiz_id)"
                    + ")";
            Statement statement = connection.createStatement();
            statement.execute(createQuestionsTableQuery);
    }

    private static void createTableForAnswers() throws SQLException {
            String createAnswersTablesQuery = "CREATE TABLE IF NOT EXISTS Answers("
                    + "answer_id VARCHAR(255) PRIMARY KEY,"
                    + "question_id VARCHAR(255),"
                    + "answer VARCHAR(4096),"
                    + "correct INTEGER,"
                    + "FOREIGN KEY (question_id) REFERENCES Questions (question_id)"
                    + ")";
            Statement statement = connection.createStatement();
            statement.execute(createAnswersTablesQuery);
    }

    public static void createTableForScoreboard() throws SQLException {
            String createScoreboardTablesQuery = "CREATE TABLE IF NOT EXISTS Scoreboard("
                    + "nickname_id VARCHAR(255) PRIMARY KEY,"
                    + "quiz_id VARCHAR(255),"
                    + "nickname VARCHAR(255),"
                    + "points INTEGER,"
                    + "FOREIGN KEY (quiz_id) REFERENCES Quizzes (quiz_id)"
                    + ")";
            Statement statement = connection.createStatement();
            statement.execute(createScoreboardTablesQuery);
    }

    public static void deleteDataFromQuizzes() throws SQLException {
        String deleteTables = "DELETE FROM Quizzes;";
        Statement statement = connection.createStatement();
        statement.execute(deleteTables);
    }

    public static void deleteDataFromQuestions() throws SQLException {
        String deleteTables = "DELETE FROM Questions;";
        Statement statement = connection.createStatement();
        statement.execute(deleteTables);
    }

    public static void deleteDataFromAnswers() throws SQLException {
        String deleteTables = "DELETE FROM Answers;";
        Statement statement = connection.createStatement();
        statement.execute(deleteTables);
    }

    public static void deleteDataFromScoreboard() throws SQLException {
        String deleteTables = "DELETE FROM Scoreboard;";
        Statement statement = connection.createStatement();
        statement.execute(deleteTables);
    }

    public static void deleteQuiz (String quiz_id) throws SQLException {
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM Quizzes WHERE quiz_id = ?");
            preparedStatement.setString(1, quiz_id);
            preparedStatement.executeUpdate();
    }

    public static void deleteQuestions (String quiz_id) throws SQLException {
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM Questions WHERE quiz_id = ?");
            preparedStatement.setString(1, quiz_id);
            preparedStatement.executeUpdate();
    }

    public static void deleteAnswers (List<String> questionList) throws SQLException {
        for (String s:questionList){
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM Answers WHERE question_id=?");
            preparedStatement.setString(1, s);
            preparedStatement.executeUpdate();}
    }

    public static void deleteScores (String quiz_id) throws SQLException {
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM Scoreboard WHERE quiz_id=?");
            preparedStatement.setString(1, quiz_id);
            preparedStatement.executeUpdate();
    }

    private String indexQuizIntoDb(Quiz quiz) throws SQLException {
            String quizQuery = "INSERT INTO Quizzes VALUES (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(quizQuery);
            String quizId = UUID.randomUUID().toString();
            preparedStatement.setString(1, quizId);
            preparedStatement.setString(2, quiz.getQuizName());
            preparedStatement.execute();
            return quizId;
    }

    private String indexQuestionIntoDb(Question question, String quizId) throws SQLException {
            String questionQuery = "INSERT INTO Questions VALUES(?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(questionQuery);
            String questionId = UUID.randomUUID().toString();
            preparedStatement.setString(1, questionId);
            preparedStatement.setString(2, quizId);
            preparedStatement.setString(3, question.getQuestionText());
            preparedStatement.setInt(4, question.getTimer());
            preparedStatement.execute();
            return questionId;
    }

    private void indexAnswerIntoDb(Answer answer, String questionID) throws SQLException {
            String answerQuery = "INSERT INTO Answers VALUES(?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(answerQuery);
            String answerID = UUID.randomUUID().toString();
            preparedStatement.setString(1, answerID);
            preparedStatement.setString(2, questionID);
            preparedStatement.setString(3, answer.getText());
            preparedStatement.setInt(4, answer.isCorrect() ? 1 : 0);
            preparedStatement.execute();
    }

    private void indexScoreIntoDb(Score score, String quizID) throws SQLException {
            String nicknameQuery = "INSERT INTO Scoreboard VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(nicknameQuery);
            String scoreID = UUID.randomUUID().toString();
            preparedStatement.setString(1, scoreID);
            preparedStatement.setString(2, quizID);
            preparedStatement.setString(3, score.getNickname());
            preparedStatement.setInt(4, score.getPoints());
            preparedStatement.execute();
    }

    public void addQuizToDb(Quiz quiz) throws SQLException {
        String quizId = indexQuizIntoDb(quiz);
        for (Question q: quiz.getQuestions()) {
            String questionId = indexQuestionIntoDb(q, quizId);
            for (Answer a: q.getAnswers()) {
                indexAnswerIntoDb(a, questionId);
            }
        }
    }

    public void addScoreToDb(Score score, String quizID) throws SQLException {
        indexScoreIntoDb(score, quizID);
    }

    /**
     * ImmutablePair will keep the Quiz name and the Quiz id in one variable
     * @return the list of available Quizzes
     */
    public List<ImmutablePair<String, String>> getQuizzes() throws SQLException {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Quizzes");

            List<ImmutablePair<String, String>> quizzes = new ArrayList<>();
            while (resultSet.next()) {
                quizzes.add(
                        new ImmutablePair<>(resultSet.getString("quiz_name"), resultSet.getString("quiz_id"))
                );
            }
            return quizzes;
    }

    public List<String> getQuestionsFromDb(String quiz_id) throws SQLException {
        String getQuestions = "SELECT * FROM Questions WHERE quiz_id=?";
        PreparedStatement preparedStatement = connection.prepareStatement(getQuestions);
        preparedStatement.setString(1, quiz_id);
        ResultSet resultSet = preparedStatement.executeQuery();

        List<String> questionList = new ArrayList<>();
        while (resultSet.next()) {
            questionList.add(resultSet.getString("question_id"));
        } return questionList;
    }

    public List<Score> getResultsForQuiz(String quiz_id) throws SQLException {
            String getScoresForQuizFromDbQuery = "SELECT * FROM Scoreboard " +
                    "INNER JOIN Quizzes ON Quizzes.quiz_id=Scoreboard.quiz_id " +
                    "WHERE Quizzes.quiz_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(getScoresForQuizFromDbQuery);
            preparedStatement.setString(1, quiz_id);
            ResultSet rs = preparedStatement.executeQuery();

            List<Score> scores = new ArrayList<>();
            while (rs.next()) {
                scores.add(new Score(rs.getString("nickname"), rs.getInt("points")));
            }
            return scores;
    }

    public List<Answer> getAnswersForQuestion(String question_id) throws SQLException {
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
    }

    public Quiz getQuizFromDb(String quiz_name, String quiz_id) throws SQLException {
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
                int timer = rs.getInt("timer");
                List<Answer> answers = getAnswersForQuestion(question_id);
                questions.add(new Question(questionText, answers, timer));
            }
            return new Quiz(quiz_name, questions);
    }
}