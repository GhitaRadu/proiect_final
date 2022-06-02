package quiz;

public class Score {
    private final String nickname;
    private final int points;

    public Score(String nickname, int points) {
        this.nickname = nickname;
        this.points = points;
    }

    public String getNickname() {
        return nickname;
    }

    public int getPoints() {
        return points;
    }
}