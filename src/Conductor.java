package src;

import javax.sound.sampled.SourceDataLine;
import java.util.List;

/**
 * Conductor
 * The src.Conductor orchestrates the bell choir, telling members when to play their notes.
 * It iterates through the song's notes, finding the corresponding member and instructing them to play.
 * The src.Conductor also manages the tempo of the performance.
 */
public class Conductor extends Thread {

    private final List<Member> members;  // List of all members in the choir.
    private final int tempoBPM;  // Tempo of the performance in Beats Per Minute. Used to calculate sleep durations
    private final List<BellNote> songNotes; // List of notes in the song to be played.
    private final SourceDataLine line; // Audio output line for the members to play on.

    /**
     * Constructor for the src.Conductor class.
     * @param members List of Member objects in the choir.
     * @param tempoBPM Tempo of the performance in BPM.
     * @param songNotes List of notes in the song.
     * @param line Audio output line.
     */
    public Conductor(List<Member> members, int tempoBPM, List<BellNote> songNotes, SourceDataLine line) {
        super("Conductor-Thread");
        this.members = members;
        this.tempoBPM = tempoBPM;
        this.songNotes = songNotes;
        this.line = line;
    }

    /**
     * Overrides the run method of the Thread class.
     * Starts the performance by calling the playSong method.
     */
    @Override
    public void run() {
        try {
            playSong();
        } catch (InterruptedException e) {
            System.err.println("Conductor interrupted during playSong. Stopping members.");
            Thread.currentThread().interrupt(); // Reset interrupt status
            // Ensure members are stopped if conductor is interrupted
            stopMembers();
        } finally {
            System.out.println("Conductor finished conducting members.");
        }
    }

    /**
     * Plays the song by instructing members to play their notes at the correct times.
     * @throws InterruptedException If the thread is interrupted during sleep.
     */
    private void playSong() throws InterruptedException {
        if (songNotes.isEmpty()) {
            System.out.println("Conductor: I have no notes to play!");
            return;
        }
        if (songNotes == null) {
            System.out.println("Conductor: The song has null notes. I cannot play this!.");
            return;
        }
        if (line == null || !line.isOpen()) {
            System.err.println("Conductor: Audio line is not available. Cannot play song.");
            return;
        }

        for (BellNote bellNote : songNotes) {
            // Check for interruption before processing each note
            if (Thread.currentThread().isInterrupted()) {
                System.out.println("Conductor detected interruption. Stopping song.");
                throw new InterruptedException("Conductor interrupted.");
            }

            Note note = bellNote.note;
            NoteLength noteLength = bellNote.length;

            if (note == Note.REST) { // Handle rests explicitly
                // Just wait for the duration of the rest
            } else {
                // Find the member who should play the current note.
                Member targetMember = null;
                for (Member member : members) {
                    if (member.getNote() == note) {
                        targetMember = member;
                        break;
                    }
                }

                if (targetMember != null) {
                    // Tell the member thread to play the note.
                    // This call returns immediately, doesn't wait for the note to finish playing.
                    targetMember.triggerPlay(noteLength, line);
                } else {
                    System.err.println("Error: Conductor found no member for note " + note);
                }
            }

            // Conductor waits for the note's duration before signaling the next note
            // This determines the rhythm/tempo of the song.
            try {
                long sleepTime = getBeatLengthMs(noteLength);
                if (sleepTime > 0) {
                    Thread.sleep(sleepTime);
                } else if (sleepTime < 0) {
                    System.err.println("Warning: Negative sleep time calculated for " + noteLength);
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Error: Invalid note length detected for " + note);
            }
        }
    }

    /**
     * Calculates the duration of a beat in milliseconds based on the note length.
     * @param noteLength The length of the note.
     * @return The duration of the beat in milliseconds.
     */
    private long getBeatLengthMs(NoteLength noteLength) {
        return noteLength.timeMs();
    }

    public void stopMembers() {
        System.out.println("Conductor signaling members to stop.");
        if (members != null) {
            for (Member member : members) {
                if (member != null) {
                    member.stopPlaying();
                }
            }
        }
    }
}