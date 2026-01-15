public class QuizQuestion {

    private String question;
    private String[] options;
    private int correctAnswer;
    private int selectedOption = -1; // store user-selected option

    public QuizQuestion(String question, String[] options, int correctAnswer) {
        this.question = question;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    // Getters
    public String getQuestion() { return question; }
    public String[] getOptions() { return options; }
    public int getCorrectAnswer() { return correctAnswer; }

    // Selected answer methods
    public void setSelectedOption(int index) { selectedOption = index; }
    public int getSelectedOption() { return selectedOption; }
}
