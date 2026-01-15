import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class QuizGUI extends JFrame {

    private JLabel questionLabel;
    private JRadioButton[] options = new JRadioButton[4];
    private ButtonGroup group;
    private JButton nextButton;
    private JButton backButton;

    private List<QuizQuestion> questions = new ArrayList<>();
    private int index = 0;
    private int[] selectedAnswers; // stores selected answer index for each question
    private int score = 0;

    public QuizGUI() {
        setTitle("Java Quiz Application");
        setSize(700, 450);
        setMinimumSize(new Dimension(700, 450));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Question label
        questionLabel = new JLabel();
        questionLabel.setFont(new Font("Arial", Font.BOLD, 20));
        questionLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(questionLabel, BorderLayout.NORTH);

        // Panel for options
        JPanel optionPanel = new JPanel();
        optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
        optionPanel.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 50));
        group = new ButtonGroup();

        for (int i = 0; i < 4; i++) {
            options[i] = new JRadioButton();
            options[i].setFont(new Font("Arial", Font.PLAIN, 18));
            options[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            group.add(options[i]);
            optionPanel.add(options[i]);
            optionPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        }

        add(optionPanel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        backButton = new JButton("Back");
        backButton.setFont(new Font("Arial", Font.BOLD, 16));
        nextButton = new JButton("Next");
        nextButton.setFont(new Font("Arial", Font.BOLD, 16));

        buttonPanel.add(backButton);
        buttonPanel.add(nextButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load questions
        loadQuestions();
        selectedAnswers = new int[questions.size()]; // default 0 = nothing selected
        displayQuestion();

        // Button actions
        nextButton.addActionListener(e -> nextQuestion());
        backButton.addActionListener(e -> previousQuestion());

        setVisible(true);
    }

    // Load questions from database
    private void loadQuestions() {
        try {
            Connection con = DBConnection.getConnection();
            if (con == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed");
                return;
            }

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM questions");

            while (rs.next()) {
                questions.add(new QuizQuestion(
                        rs.getString("question"),
                        new String[]{
                                rs.getString("option1"),
                                rs.getString("option2"),
                                rs.getString("option3"),
                                rs.getString("option4")
                        },
                        rs.getInt("correct_answer")
                ));
            }

            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Display current question and restore previous selection if any
    private void displayQuestion() {
        if (index < 0) index = 0;
        if (index >= questions.size()) {
            // Quiz finished
            calculateScore();
            JOptionPane.showMessageDialog(this,
                    "Quiz Finished!\nYour Score: " + score + " / " + questions.size());
            System.exit(0);
        }

        QuizQuestion q = questions.get(index);
        questionLabel.setText("Q" + (index + 1) + ": " + q.getQuestion());

        String[] opts = q.getOptions();
        for (int i = 0; i < 4; i++) {
            options[i].setText(opts[i]);
        }

        group.clearSelection();
        int selected = selectedAnswers[index];
        if (selected > 0 && selected <= 4) {
            options[selected - 1].setSelected(true);
        }

        // Enable/disable back button
        backButton.setEnabled(index > 0);
        // Change Next button text to "Finish" on last question
        nextButton.setText(index == questions.size() - 1 ? "Finish" : "Next");
    }

    private void nextQuestion() {
        // Save selected answer
        for (int i = 0; i < 4; i++) {
            if (options[i].isSelected()) {
                selectedAnswers[index] = i + 1;
                break;
            }
        }

        index++;
        displayQuestion();
    }

    private void previousQuestion() {
        // Save current selection before going back
        for (int i = 0; i < 4; i++) {
            if (options[i].isSelected()) {
                selectedAnswers[index] = i + 1;
                break;
            }
        }

        index--;
        displayQuestion();
    }

    private void calculateScore() {
        score = 0;
        for (int i = 0; i < questions.size(); i++) {
            if (selectedAnswers[i] == questions.get(i).getCorrectAnswer()) {
                score++;
            }
        }
    }
}
