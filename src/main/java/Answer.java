public class Answer {

    private String text;
    private boolean correctness;

    public Answer(String text, boolean correctness) {
        this.text = text;
        this.correctness = correctness;
    }

    public String getText() {
        return text;
    }

    public boolean isCorrect() {
        return correctness;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setCorrectness(boolean correctness) {
        this.correctness = correctness;
    }
}
