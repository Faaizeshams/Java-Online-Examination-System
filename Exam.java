import java.util.*;
import java.util.concurrent.*;

public class Exam {
    private final int examId;
    private final int durationMinutes;
    private final List<Question> questions;
    private final List<Character> answers;
    private volatile boolean timeUp = false;

    public Exam(int examId, int durationMinutes, List<Question> questions) {
        this.examId = examId;
        this.durationMinutes = durationMinutes;
        this.questions = questions;
        this.answers = new ArrayList<>(Collections.nCopies(questions.size(), null));
    }

    public void startConsoleExam(int userId) {
        Scanner sc = new Scanner(System.in);
        ScheduledExecutorService sched = Executors.newSingleThreadScheduledExecutor();
        long durationSec = durationMinutes * 60L;

        sched.schedule(() -> {
            timeUp = true;
            System.out.println("\nTime's up! Auto-submitting.");
        }, durationSec, TimeUnit.SECONDS);

        int idx = 0;
        while (!timeUp) {
            System.out.println("\n---------------------------------");
            questions.get(idx).printQuestion(idx);
            Character curAns = answers.get(idx);
            System.out.println("Answer: " + (curAns == null ? "Not answered" : curAns));
            System.out.print("Enter option (A/B/C/D) or next/prev/submit: ");
            String input = sc.nextLine().trim();

            if (input.equalsIgnoreCase("next")) {
                idx = Math.min(idx + 1, questions.size() - 1);
                continue;
            }
            if (input.equalsIgnoreCase("prev")) {
                idx = Math.max(idx - 1, 0);
                continue;
            }
            if (input.equalsIgnoreCase("submit")) break;

            if (input.matches("(?i)[abcd]")) {
                answers.set(idx, Character.toUpperCase(input.charAt(0)));
                if (idx < questions.size() - 1) idx++;
                continue;
            }

            System.out.println("Invalid input.");
        }

        sched.shutdownNow();

        try {
            DBHelper.storeResponsesAndResult(examId, userId, questions, answers);
            int correct = 0;
            for (int i = 0; i < questions.size(); i++)
                if (answers.get(i) != null && answers.get(i) == questions.get(i).getCorrectOpt()) correct++;
            double percent = (double) correct / questions.size() * 100;
            System.out.println("\nSubmitted. Score: " + percent + "%");
        } catch (Exception ex) {
            System.out.println("Error saving results.");
        }
    }
}
