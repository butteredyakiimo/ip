public class InvalidDeadlineException extends DukeException {
    private static String line = "--------------------------------------------------------------------";
    public InvalidDeadlineException() {
        super("(・´з`・) Uh oh... make sure your deadline has a description and date\n" + line);
    }
}