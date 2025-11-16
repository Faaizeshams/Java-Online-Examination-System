import java.util.List;
import java.util.Scanner;

public class ExamApp {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("=== Online Examination System ===");

        try {
            while (true) {
                System.out.println("\n1) Login\n2) Register\n3) Exit");
                System.out.print("Choose: ");
                String choice = sc.nextLine().trim();

                if (choice.equals("1")) {
                    System.out.print("Username: ");
                    String user = sc.nextLine().trim();
                    System.out.print("Password: ");
                    String pass = sc.nextLine().trim();
                    int userId = DBHelper.authenticate(user, pass);
                    if (userId > 0) {
                        System.out.println("Login successful.");
                        afterLogin(sc, userId);
                    } else System.out.println("Invalid credentials.");
                } 
                else if (choice.equals("2")) {
                    System.out.print("Username: ");
                    String user = sc.nextLine().trim();
                    System.out.print("Password: ");
                    String pass = sc.nextLine().trim();
                    System.out.print("Full name: ");
                    String name = sc.nextLine().trim();
                    int uid = DBHelper.registerUser(user, pass, name);
                    if (uid > 0) System.out.println("Registered. ID: " + uid);
                    else System.out.println("Registration failed.");
                } 
                else if (choice.equals("3")) {
                    break;
                }
            }
        } catch (Exception ignored) {}
    }

    private static void afterLogin(Scanner sc, int userId) {
        try {
            int examId = 1;
            int duration = DBHelper.getExamDurationMinutes(examId);
            List<Question> questions = DBHelper.fetchQuestionsForExam(examId);
            System.out.println("\nExam available: Sample Java Test (" + duration + " mins)");
            System.out.print("Start? (yes/no): ");
            String s = sc.nextLine().trim();
            if (s.equalsIgnoreCase("yes")) {
                Exam exam = new Exam(examId, duration, questions);
                exam.startConsoleExam(userId);
            }
        } catch (Exception ignored) {}
    }
}
