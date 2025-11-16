import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBHelper {
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception ignored) {}
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DBConfig.URL, DBConfig.USER, DBConfig.PASSWORD);
    }

    public static int authenticate(String username, String password) throws SQLException {
        String sql = "SELECT user_id FROM users WHERE username = ? AND password_hash = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("user_id");
        }
        return -1;
    }

    public static int registerUser(String username, String password, String fullname) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, full_name) VALUES (?, ?, ?)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, fullname);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    public static List<Question> fetchQuestionsForExam(int examId) throws SQLException {
        String sql = "SELECT qid, question_text, opt_a, opt_b, opt_c, opt_d, correct_opt FROM questions ORDER BY qid";
        List<Question> list = new ArrayList<>();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Question(
                        rs.getInt("qid"),
                        rs.getString("question_text"),
                        rs.getString("opt_a"),
                        rs.getString("opt_b"),
                        rs.getString("opt_c"),
                        rs.getString("opt_d"),
                        rs.getString("correct_opt").charAt(0)
                ));
            }
        }
        return list;
    }

    public static void storeResponsesAndResult(int examId, int userId, List<Question> questions, List<Character> answers) throws SQLException {
        String insertResp = "INSERT INTO responses (exam_id, user_id, qid, selected_opt) VALUES (?, ?, ?, ?)";
        String insertResult = "INSERT INTO results (exam_id, user_id, total_questions, correct_answers, score_percent) VALUES (?, ?, ?, ?, ?)";

        int correct = 0;
        for (int i = 0; i < questions.size(); i++)
            if (answers.get(i) != null && answers.get(i) == questions.get(i).getCorrectOpt()) correct++;

        int total = questions.size();
        double percent = (double) correct / total * 100;

        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement prs = c.prepareStatement(insertResp);
                 PreparedStatement prr = c.prepareStatement(insertResult)) {

                for (int i = 0; i < questions.size(); i++) {
                    prs.setInt(1, examId);
                    prs.setInt(2, userId);
                    prs.setInt(3, questions.get(i).getQid());
                    Character sel = answers.get(i);
                    if (sel == null) prs.setNull(4, Types.CHAR);
                    else prs.setString(4, sel.toString());
                    prs.addBatch();
                }
                prs.executeBatch();

                prr.setInt(1, examId);
                prr.setInt(2, userId);
                prr.setInt(3, total);
                prr.setInt(4, correct);
                prr.setDouble(5, percent);
                prr.executeUpdate();

                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public static int getExamDurationMinutes(int examId) throws SQLException {
        String sql = "SELECT duration_minutes FROM exams WHERE exam_id = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, examId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("duration_minutes");
        }
        return 10;
    }
}
