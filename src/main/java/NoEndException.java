public class NoEndException extends Exception {
    private static String line = "--------------------------------------------------------------------";
    public NoEndException() {
        super("(・´з`・) Uh oh... please add an end date\n" + line);
    }
}
