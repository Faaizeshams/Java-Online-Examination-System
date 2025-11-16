public class Question {
    private final int qid;
    private final String text;
    private final String a, b, c, d;
    private final char correctOpt;

    public Question(int qid, String text, String a, String b, String c, String d, char correctOpt) {
        this.qid = qid;
        this.text = text;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.correctOpt = Character.toUpperCase(correctOpt);
    }

    public int getQid() { return qid; }
    public String getText() { return text; }
    public String getA() { return a; }
    public String getB() { return b; }
    public String getC() { return c; }
    public String getD() { return d; }
    public char getCorrectOpt() { return correctOpt; }

    public void printQuestion(int index) {
        System.out.println("Q" + (index + 1) + ". " + text);
        System.out.println("  A) " + a);
        System.out.println("  B) " + b);
        System.out.println("  C) " + c);
        System.out.println("  D) " + d);
    }
}
