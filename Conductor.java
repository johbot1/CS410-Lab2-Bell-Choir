import javax.sound.sampled.SourceDataLine;
import java.util.List;

/**
 * Conductor
 * The Conductor orchestrates the bell choir, telling members when to play their notes.
 * It iterates through the song's notes, finding the corresponding member and instructing them to play.
 * The Conductor also manages the tempo of the performance.
 */
public class Conductor extends Thread {

    private final List<Member> members;  // List of all members in the choir.
    private final int tempoBPM;  // Tempo of the performance in Beats Per Minute.
    private final List<BellNote> songNotes; // List of notes in the song to be played.
    private final SourceDataLine line; // Audio output line for the members to play on.

    /**
     * Constructor for the Conductor class.
     * @param members List of Member objects in the choir.
     * @param tempoBPM Tempo of the performance in BPM.
     * @param songNotes List of notes in the song.
     * @param line Audio output line.
     */
    public Conductor(List<Member> members, int tempoBPM, List<BellNote> songNotes, SourceDataLine line) {
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
            System.out.println("Conductor interrupted when trying to play song");
        }
    }

    /**
     * Plays the song by instructing members to play their notes at the correct times.
     * @throws InterruptedException If the thread is interrupted during sleep.
     */
    private void playSong() throws InterruptedException {
        System.out.println("Conductor: Beginning performance with the tempo " + tempoBPM);

        for (BellNote bellNote : songNotes) {
            Note note = bellNote.note;
            NoteLength noteLength = bellNote.length;

            // Find the member who should play the current note.
            for (Member member : members) {
                if (member.getNote() == note) {
                    member.bellTime(noteLength, line); // Tell the member to play.
                    break;
                }
            }
            Thread.sleep(getBeatLengthMs(noteLength)); // Wait for the duration of the note.
        }
        System.out.println("Conductor: All notes played. Performance finished");
    }

    /**
     * Calculates the duration of a beat in milliseconds based on the note length.
     * @param noteLength The length of the note.
     * @return The duration of the beat in milliseconds.
     */
    private int getBeatLengthMs(NoteLength noteLength) {
        return noteLength.timeMs();
    }
}