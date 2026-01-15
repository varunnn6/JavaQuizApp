import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;

public class QuizGUI extends JFrame {

    private ArrayList<QuizQuestion> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;

    // Student Info
    private String studentName;
    private String enrollmentNo;
    private String course;

    // GUI Components
    private JPanel mainPanel;
    private JLabel questionLabel;
    private JRadioButton[] options;
    private ButtonGroup bg;
    private JButton nextButton, backButton, submitButton;

    // Startup panel
    private JPanel startupPanel;
    private JTextField nameField, enrollmentField, courseField;
    private JButton startButton;

    public QuizGUI() {
        setTitle("Java Quiz Application");
        setSize(600, 400);
        setMinimumSize(new Dimension(600, 400));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        initStartupPanel();
        setVisible(true);
    }

    // Startup panel
    private void initStartupPanel() {
        startupPanel = new JPanel();
        startupPanel.setLayout(new BoxLayout(startupPanel, BoxLayout.Y_AXIS));
        startupPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Name
        startupPanel.add(new JLabel("Enter your Name:"));
        nameField = new JTextField();
        nameField.setMaximumSize(new Dimension(300, 30));
        startupPanel.add(nameField);
        startupPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Enrollment No
        startupPanel.add(new JLabel("Enter Enrollment No.:"));
        enrollmentField = new JTextField();
        enrollmentField.setMaximumSize(new Dimension(300, 30));
        startupPanel.add(enrollmentField);
        startupPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Course
        startupPanel.add(new JLabel("Enter Course:"));
        courseField = new JTextField();
        courseField.setMaximumSize(new Dimension(300, 30));
        startupPanel.add(courseField);
        startupPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        startButton = new JButton("Start Quiz");
        startupPanel.add(startButton);

        add(startupPanel);

        startButton.addActionListener(e -> startQuiz());
    }

    private void startQuiz() {
        studentName = nameField.getText().trim();
        enrollmentNo = enrollmentField.getText().trim();
        course = courseField.getText().trim();

        if(studentName.isEmpty() || enrollmentNo.isEmpty() || course.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!");
            return;
        }

        // Save student info to DB
        saveStudentInfo();

        loadQuestions();

        if (questions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No questions found in the database!");
            return;
        }

        remove(startupPanel);
        initQuizUI();
        revalidate();
        repaint();
    }

    private void saveStudentInfo() {
        try {
            Connection con = DBConnection.getConnection();
            Statement stmt = con.createStatement();
            // Create table if not exists
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS students (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT," +
                    "enrollment TEXT," +
                    "course TEXT," +
                    "score INTEGER" +
                    ")");
            // Insert student info
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO students (name, enrollment, course) VALUES (?, ?, ?)"
            );
            ps.setString(1, studentName);
            ps.setString(2, enrollmentNo);
            ps.setString(3, course);
            ps.executeUpdate();
        } catch(Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to save student info!");
            e.printStackTrace();
        }
    }

    private void loadQuestions() {
        try {
            Connection con = DBConnection.getConnection();
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
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database connection failed!");
            e.printStackTrace();
        }
    }

    private void initQuizUI() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Question label
        questionLabel = new JLabel();
        questionLabel.setFont(new Font("Arial", Font.BOLD, 18));
        questionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        questionLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, questionLabel.getPreferredSize().height));
        mainPanel.add(questionLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Options
        options = new JRadioButton[4];
        bg = new ButtonGroup();
        JPanel optionPanel = new JPanel();
        optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
        optionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (int i = 0; i < 4; i++) {
            options[i] = new JRadioButton();
            options[i].setFont(new Font("Arial", Font.PLAIN, 16));
            options[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            bg.add(options[i]);
            optionPanel.add(options[i]);
            optionPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        mainPanel.add(optionPanel);

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        backButton = new JButton("Back");
        nextButton = new JButton("Next");
        submitButton = new JButton("Submit");

        buttonPanel.add(backButton);
        buttonPanel.add(nextButton); // submit added dynamically later
        mainPanel.add(buttonPanel);

        add(mainPanel);

        // Button actions
        nextButton.addActionListener(e -> nextQuestion());
        backButton.addActionListener(e -> previousQuestion());
        submitButton.addActionListener(e -> submitQuiz());

        showQuestion(currentQuestionIndex);
    }

    private void showQuestion(int index) {
        if(index < 0 || index >= questions.size()) return;

        QuizQuestion q = questions.get(index);
        questionLabel.setText((index+1) + ". " + q.getQuestion());
        String[] opts = q.getOptions();
        for(int i = 0; i < 4; i++) {
            options[i].setText(opts[i]);
            options[i].setSelected(false);
        }

        if(q.getSelectedOption() != -1)
            options[q.getSelectedOption()].setSelected(true);

        // Swap Next button with Submit on last question
        JPanel buttonPanel = (JPanel) nextButton.getParent();
        buttonPanel.removeAll();
        buttonPanel.add(backButton);
        if(index == questions.size() - 1) {
            buttonPanel.add(submitButton);
        } else {
            buttonPanel.add(nextButton);
        }
        buttonPanel.revalidate();
        buttonPanel.repaint();
    }

    private void nextQuestion() {
        saveAnswer();
        if(currentQuestionIndex < questions.size()-1) {
            currentQuestionIndex++;
            showQuestion(currentQuestionIndex);
        }
    }

    private void previousQuestion() {
        saveAnswer();
        if(currentQuestionIndex > 0) {
            currentQuestionIndex--;
            showQuestion(currentQuestionIndex);
        }
    }

    private void saveAnswer() {
        for(int i = 0; i < 4; i++) {
            if(options[i].isSelected()) {
                questions.get(currentQuestionIndex).setSelectedOption(i);
                break;
            }
        }
    }

    private void submitQuiz() {
        saveAnswer();
        score = 0;
        for(QuizQuestion q : questions) {
            if(q.getSelectedOption() == q.getCorrectAnswer()-1)
                score++;
        }

        // Update score in database
        saveScoreToDB();

        JOptionPane.showMessageDialog(this, "Student: " + studentName +
                "\nEnrollment: " + enrollmentNo +
                "\nCourse: " + course +
                "\nScore: " + score + "/" + questions.size());
        System.exit(0);
    }

    private void saveScoreToDB() {
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE students SET score=? WHERE enrollment=?"
            );
            ps.setInt(1, score);
            ps.setString(2, enrollmentNo);
            ps.executeUpdate();
        } catch(Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to save score!");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new QuizGUI();
    }
}
