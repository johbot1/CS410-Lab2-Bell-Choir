package src;

import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Member
 * Represents a member of the bell choir, responsible for playing a specific note.
 * Each member has a name, a note, and a note length.
 * Members play their assigned note when instructed by the src.Conductor.
 */
public class Member extends Thread {

    private final Note note; // The note assigned to the Member.
    private final String name; // Name of the Member.
    private final Object lock = new Object(); // Dedicated lock for wait/notify
    private volatile NoteLength noteToPlay = null; // The note length to play currently (volatile for visibility)
    private volatile SourceDataLine audioLine = null; // The line to write to (volatile for visibility)
    private final AtomicBoolean keepRunning = new AtomicBoolean(true); // Use AtomicBoolean for safer flag setting

    /**
     * Constructor for the Member class.
     *
     * @param name The name of the member.
     * @param note The note the member is assigned to play.
     */
    public Member(String name, Note note) {
        super(name + "-Thread"); // Gives the thread a descriptive name
        this.name = name;
        this.note = note;
    }

    /**
     * Gets the note assigned to the member.
     *
     * @return The note assigned to the member.
     */
    public Note getNote() {
        return this.note;
    }

    /**
     * Overrides the run method of the Thread class.
     * This method is executed when the thread starts.
     */
    @Override
    public void run() {
        while (keepRunning.get()) {
            NoteLength currentNoteLength = null;
            SourceDataLine currentLine = null;
            // -- Begin Synchronized block --
            synchronized (lock) {
                // Wait while there's no note assigned AND we should keep running
                while (noteToPlay == null && keepRunning.get()) {
                    try {
                        lock.wait(); // Release lock and wait for notification
                    } catch (InterruptedException e) {
                        System.err.println(getName() + " interrupted while waiting. Exiting.");
                        keepRunning.set(false); // Ensure loop termination on interrupt
                        Thread.currentThread().interrupt(); // Re-set interrupt status
                        break; // Exit inner wait loop
                    }
                }

                // If woken up, check if it should still run and if there's a note
                if (!keepRunning.get()) {
                    break; // Exit the main while loop
                }

                if (noteToPlay != null) {
                    // Grab the details needed to play *before* resetting
                    currentNoteLength = this.noteToPlay;
                    currentLine = this.audioLine;

                    // Reset the state *inside* the synchronized block
                    // so the conductor doesn't overwrite before we process
                    this.noteToPlay = null;
                    this.audioLine = null;
                }
            }

            // --- Perform the actual work *outside* the synchronized block ---
            if (currentNoteLength != null && currentLine != null) {
                try {
                    final int ms = Math.min(currentNoteLength.timeMs(), Note.MEASURE_LENGTH_SEC * 1000);
                    final int length = Note.SAMPLE_RATE * ms / 1000;
                    // Ensure the sample data isn't null or invalid.txt before writing
                    byte[] sampleData = note.sample();
                    if (sampleData != null) {
                        currentLine.write(sampleData, 0, length); // Write the note's audio data
                    } else {
                        System.err.println("Error: Sample data is null for note " + note + " in " + getName());
                    }
                } catch (Exception e) { // Catch broader exceptions during audio playback
                    System.err.println("It looks like " + getName() + " went missing!\n " +
                            "I'm just playing, I don't actually know what happened. Here's the message though: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Called by the Conductor to signal this Member to play its note.
     *
     * @param length The length of the note to play.
     * @param line   The audio line to write to.
     */
    public void triggerPlay(NoteLength length, SourceDataLine line) {
        synchronized (lock) {
            if (!keepRunning.get()) return; // Don't accept new notes if trying to stop

            this.noteToPlay = length;
            this.audioLine = line;
            lock.notify();
        }
    }

    /**
     * Signals the Member thread to stop running.
     */
    public void stopPlaying() {
        synchronized (lock) {
            keepRunning.set(false);
            noteToPlay = null; // Ensure no pending note is played
            lock.notify(); // Wake up the thread so it can check keepRunning and exit
        }
    }

    /**
     * This method  called from the main thread *after* Conductor.join()
     * to ensure the member thread itself has terminated cleanly.
     */
    public void joinThread() {
        try {
            this.join();
        } catch (InterruptedException e) {
            System.err.println(getName() + " interrupted while being joined.");
            Thread.currentThread().interrupt();
        }
    }
}