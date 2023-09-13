package duke.tasks;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import duke.components.Status;
import duke.exceptions.InvalidStartEndException;

/**
 * Encapsulates an Event. Contains the task description, completion status,
 * start and end date times.
 */
public class Event extends Task {
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private LocalDateTime start;
    private LocalDateTime end;

    /**
     * Class constructor for Event.
     *
     * @param status either DONE or NOT_DONE.
     * @param task   task description.
     * @param start  start datetime.
     * @param end    end datetime.
     * @throws InvalidStartEndException if start after end.
     */
    public Event(Status status, String task, LocalDateTime start, LocalDateTime end) throws InvalidStartEndException {
        super(status, task);
        if (start.isAfter(end)) {
            throw new InvalidStartEndException();
        } else {
            this.start = start;
            this.end = end;
        }
    }

    /**
     * Converts Event to the correct string format to write to data file.
     *
     * @return string to write to data file.
     */
    @Override
    public String convertTask() {
        return "E | " + super.getStatus() + " | " + super.getTask()
                + " | " + this.start.format(formatter) + " | " + this.end.format(formatter);
    }

    /**
     * Returns string representation of an Event object.
     *
     * @return string Event.
     */
    @Override
    public String toString() {
        return "[E] " + super.toString() + " (from: " + this.start.format(formatter)
                + " to: " + this.end.format(formatter) + ")";
    }
}
