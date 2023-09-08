package duke.components;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

import duke.exceptions.DukeException;
import duke.exceptions.InvalidCommandException;
import duke.exceptions.InvalidDeadlineException;
import duke.exceptions.InvalidEventException;
import duke.exceptions.InvalidFindTaskException;
import duke.exceptions.InvalidStartEndException;
import duke.exceptions.InvalidTaskIdException;
import duke.exceptions.NoDescException;
import duke.exceptions.NoEndException;
import duke.exceptions.NoStartException;
import duke.exceptions.NoTaskIdException;
import duke.tasks.Deadline;
import duke.tasks.Event;
import duke.tasks.Task;
import duke.tasks.ToDo;

/**
 * Interprets user commands, and controls what the user can or cannot do. An object acts as a
 * parser to parse commands and carry them out.
 */
public class Parser {
    private Storage storage;
    private TaskList tasks;
    private Ui ui;

    /**
     * Class constructor for Parser.
     *
     * @param storage storage to where data is stored.
     * @param tasks   tasks in the list.
     * @param ui      ui to be used.
     */
    public Parser(Storage storage, TaskList tasks, Ui ui) {
        this.storage = storage;
        this.tasks = tasks;
        this.ui = ui;
    }

    /**
     * Returns true if the string can be parsed as an integer.
     *
     * @param s the string to be tested.
     * @return true if s can be parsed as an integer.
     */
    //credit: https://www.freecodecamp.org/news/java-string-to-int-how-to-convert-a-string-to-an-integer/
    private static boolean isNumber(String s) {
        return s != null && s.matches("[0-9.]+");
    }

    /**
     * Converts a string of the format YYYY-MM-dd HH:mm to a LocalDateTime object.
     *
     * @param str a datetime string.
     * @return the corresponding LocalDateTime object.
     * @throws DateTimeParseException if str is not of the correct format.
     */
    public static LocalDateTime convertToDateTime(String str) {
        //check if dateTime has correct format: ie. YYYY-MM-DD 00:00
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse(str, formatter);
        return dateTime;
    }

    /**
     * Takes in the user input, and identifies whether it is a valid command.
     * If commands are valid, calls the corresponding methods to carry out
     * them out. Else, will throw an InvalidCommandException.
     * <p>
     * This method catches all DukeExceptions, and displays the error
     * messages using the ui.showError() command.
     *
     * @param input user input.
     */
    public String parseInput(String input) {
        String[] inputs = input.split(" ");
        String command = inputs[0];
        try {
            if (command.equals("list")) {
                return tasks.listTasks();
            } else if (command.equals("delete")) {
                return parseDelete(input);
            } else if (command.equals("mark")) {
                return parseMark(input);
            } else if (command.equals("unmark")) {
                return parseUnMark(input);
            } else if (command.equals("todo")) {
                return parseToDo(input);
            } else if (command.equals("deadline")) {
                return parseDeadline(input);
            } else if (command.equals("event")) {
                return parseEvent(input);
            } else if (command.equals("find")) {
                return findTask(input);
            } else if (command.equals("bye")) {
                return parseBye();
            } else {
                throw new InvalidCommandException();
            }
        } catch (DukeException e) {
            return e.getMessage();
        }
    }

    public String parseBye() {
        return ui.bye();
    }
    /**
     * Handles the deleting of a specified task.
     * Checks if a taskID is provided. Calls tasks.deleteTask(index) function to
     * attempt task deletion.
     *
     * @param input user input.
     * @throws NoTaskIdException      if no taskID is provided.
     * @throws InvalidTaskIdException if a non-numerical id is provided.
     */
    public String parseDelete(String input) throws NoTaskIdException, InvalidTaskIdException {
        String[] inputArr = input.split(" ");
        if (inputArr.length == 1) {
            throw new NoTaskIdException();
        } else {
            String strIndex = inputArr[1];
            if (isNumber(strIndex)) {
                int index = Integer.parseInt(strIndex) - 1; //because index starts from 1
                return tasks.deleteTask(index);
            } else {
                //case where a number was not provided
                throw new InvalidTaskIdException();
            }
        }
    }

    /**
     * Handles the marking of a specified task.
     * Checks if a taskID is provided. Calls the tasks.markTask(index) function to
     * attempt task marking.
     *
     * @param input user input.
     * @throws NoTaskIdException      if no taskID is provided.
     * @throws InvalidTaskIdException If a non-numerical id is provided.
     */
    public String parseMark(String input) throws NoTaskIdException, InvalidTaskIdException {
        String[] inputArr = input.split(" ", 2);
        if (inputArr.length == 1) {
            throw new NoTaskIdException();
        } else {
            String strIndex = inputArr[1];
            int index = Integer.parseInt(strIndex) - 1;
            if (isNumber(strIndex) && tasks.isValidTaskId(index)) {
                return tasks.markTask(index);
            } else {
                throw new InvalidTaskIdException();
            }
        }
    }

    /**
     * Handles the unmarking of a specified task.
     * Checks if a taskID is provided. Calls tasks.unMarkTask(index)
     * to attempt task unmarking.
     *
     * @param input user input.
     * @throws NoTaskIdException      if no taskID is provided.
     * @throws InvalidTaskIdException If a non-numerical id is provided.
     */
    public String parseUnMark(String input) throws NoTaskIdException, InvalidTaskIdException {
        String[] inputArr = input.split(" ", 2);
        if (inputArr.length == 1) {
            throw new NoTaskIdException();
        } else {
            String strIndex = inputArr[1];
            int index = Integer.parseInt(strIndex) - 1;
            if (isNumber(strIndex) && tasks.isValidTaskId(index)) {
                return tasks.unMarkTask(index);
            } else {
                throw new InvalidTaskIdException();
            }
        }
    }

    /**
     * Handles the creation of a ToDo.
     * Creates successfully if a description is provided.
     *
     * @param input user input.
     * @throws NoDescException if no description is provided.
     */
    public String parseToDo(String input) throws NoDescException {
        String[] inputs = input.split(" ", 2);
        if (inputs.length == 1) {
            throw new NoDescException();
        }
        if (inputs[1].isBlank()) {
            throw new NoDescException();
        }
        ToDo toDo = new ToDo(0, inputs[1]);
        return tasks.addTask(toDo);
    }

    /**
     * Handles the creation of a Deadline.
     * Creates successfully if a description and an appropriate datetime is provided
     * in the format 'deadline desc /by datetime' is provided.
     *
     * @param input user input.
     * @throws NoDescException          if no description is provided.
     * @throws InvalidDeadlineException if command is not of the correct format.
     */
    public String parseDeadline(String input) throws NoDescException, InvalidDeadlineException {
        String[] inputs = input.split(" ", 2);
        if (inputs.length == 1) {
            throw new NoDescException();
        }

        String afterCommand = inputs[1];
        String[] details = afterCommand.split(" /by ", 2);
        if (details.length < 2) {
            throw new InvalidDeadlineException();
        }

        String desc = details[0];
        String date = details[1];
        if (desc.isBlank()) {
            throw new NoDescException();
        }
        try {
            LocalDateTime dateTime = convertToDateTime(date);
            Deadline deadline = new Deadline(0, desc, dateTime);
            return tasks.addTask(deadline);

        } catch (DateTimeParseException e) {
            return ui.showInvalidDateFormat();
        }
    }

    /**
     * Handles the creation of an Event.
     * Creates successfully if a description, appropriate start datetime and end
     * date time is provided in the format 'event desc /from start /to end'.
     *
     * @param input user input.
     * @throws NoDescException          if no description provided.
     * @throws InvalidStartEndException if start datetime is after end datetime.
     * @throws InvalidEventException    if command is not of the correct format.
     */
    public String parseEvent(String input) throws NoDescException, NoStartException, NoEndException,
            InvalidStartEndException, InvalidEventException {
        String[] inputs = input.split(" ", 2);
        if (inputs.length == 1) {
            throw new NoDescException();
        }
        if (inputs[1].isBlank()) {
            throw new NoDescException();
        }

        String afterCommand = inputs[1];
        String[] details = afterCommand.split("/from", 2);
        if (details[0].isBlank()) {
            throw new NoDescException();
        }
        if (details.length == 1) {
            throw new InvalidEventException(); //can either be no desc or no start
        }

        String task = details[0].trim();
        String[] dateDetails = details[1].split("/to");
        String start = dateDetails[0].trim();
        if (start.isBlank()) {
            throw new NoStartException();
        }
        if (dateDetails.length == 1) {
            throw new NoEndException();
        }
        String end = dateDetails[1].trim();
        if (end.isBlank()) {
            throw new NoEndException();
        }

        try {
            LocalDateTime startDateTime = convertToDateTime(start);
            LocalDateTime endDateTime = convertToDateTime(end);
            Event event = new Event(0, task, startDateTime, endDateTime);
            return tasks.addTask(event);
        } catch (DateTimeParseException e) {
            return ui.showInvalidDateFormat();
        }
    }

    /**
     * Handles the finding of tasks that match the specified keyword.
     *
     * @param input user input.
     * @throws InvalidFindTaskException if 0 or more than 1 keyword is specified.
     */
    public String findTask(String input) throws InvalidFindTaskException {
        String[] inputs = input.split(" ");
        if (inputs.length != 2) {
            throw new InvalidFindTaskException();
        } else {
            String keyword = inputs[1];
            ArrayList<Task> matches = tasks.findMatches(keyword);
            return ui.showMatches(tasks.listTasks(matches));
        }
    }
}