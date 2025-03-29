package src;

import javax.sound.sampled.SourceDataLine;

/**
 * src.Member
 * Represents a member of the bell choir, responsible for playing a specific note.
 * Each member has a name, a note, and a note length.
 * Members play their assigned note when instructed by the src.Conductor.
 */
public class Member extends Thread {

    private final String name; // Name of the src.Member.
    private final Note note; // The note assigned to the src.Member.
    private NoteLength noteLength; // The duration of the note.
    private volatile boolean keepRunning = true; // Flag to control the thread's execution.

    /**
     * Constructor for the src.Member class.
     * @param name The name of the member.
     * @param line The SourceDataLine for audio output.
     * @param note The note the member is assigned to play.
     * @param noteLength The initial note length.
     */
    public Member(String name, SourceDataLine line, Note note, NoteLength noteLength) {
        this.name = name;
        this.note = note;
        this.noteLength = noteLength;
    }

    /**
     * Overrides the run method of the Thread class.
     * This method is executed when the thread starts.
     */
    @Override
    public void run() {
        while (keepRunning) {
            try {
                Thread.sleep(10); // Small sleep to avoid busy-waiting.
            } catch (InterruptedException e) {
                System.err.println("Thread interrupted!");
            }
            keepRunning = false; // Stop the thread after the first iteration.
        }
        System.out.println("src.Member " + name + " has finished"); // Print a message when the thread finishes.
    }

    /**
     * Plays the src.Member's note for the specified duration.
     * @param n The src.NoteLength to play the note for.
     * @param line The SourceDataLine for audio output.
     */
    public void bellTime(NoteLength n, SourceDataLine line) {
        System.out.println(name + " on the... bells: " + note);
        this.noteLength = n;
        final int ms = Math.min(noteLength.timeMs(), Note.MEASURE_LENGTH_SEC * 1000);
        final int length = Note.SAMPLE_RATE * ms / 1000;
        line.write(note.sample(), 0, length); // Write the note's audio data to the line.
//        line.write(src.Note.REST.sample(), 0, 5); // Adds a small rest after the note. Sounds kinda off?
    }

    /**
     * Stops the member from playing.
     */
    private void stopPlaying() {
        keepRunning = false;
    }

    /**
     * Waits for the member's thread to finish.
     */
    public void bellCoalesence() {
        try {
            join();
        } catch (InterruptedException e) {
            System.out.println(name + " interrupted while trying to coalesce");
        }
    }

    /**
     * Gets the note assigned to the member.
     * @return The note assigned to the member.
     */
    public Note getNote() {
        return this.note;
    }
}