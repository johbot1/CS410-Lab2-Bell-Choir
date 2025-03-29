package src;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.Arrays;
import java.util.List;

/**
 * src.TestChoir class - Runs a simple test to make sure our Bell Choir is working!
 */
public class TestChoir {

    /**
     * Main method - Entry point for the test.
     * @param args Command line arguments (not used).
     * @throws LineUnavailableException If the audio line can't be obtained.
     */
    public static void main(String[] args) throws LineUnavailableException {

        // Define the AudioFormat - How the sound is encoded.
        final AudioFormat af = new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);
        final SourceDataLine line = AudioSystem.getSourceDataLine(af); // Get the audio output line.
        line.open(); // Open the line for use.
        line.start(); // Start the line.

        // Create Members with unique notes - Our bell ringers!
        Member member1 = new Member("Alice", line, Note.C4, NoteLength.QUARTER);
        Member member2 = new Member("Bob", line, Note.E4, NoteLength.QUARTER);
        Member member3 = new Member("Charlie", line, Note.G4, NoteLength.QUARTER);

        // Start members (they wait for their turn) - Get them ready to play.
        member1.start();
        member2.start();
        member3.start();

        // Define a simple song sequence - The notes we want to play.
        List<BellNote> song = Arrays.asList(
                new BellNote(Note.C4, NoteLength.QUARTER),
                new BellNote(Note.E4, NoteLength.QUARTER),
                new BellNote(Note.G4, NoteLength.QUARTER)
        );

        // Start the src.Conductor - He tells the members when to play.
        Conductor conductor = new Conductor(Arrays.asList(member1, member2, member3), 120, song, line); // 120 BPM.
        conductor.start(); // Start the conductor's thread.

        // Wait for src.Conductor to finish - Don't exit until the song is done.
        try {
            conductor.join();
        } catch (InterruptedException e) {
            System.err.println("src.Conductor interrupted."); // If something interrupts the conductor.
        }

        // Close the audio line - Clean up.
        line.drain(); // Make sure all sound is played.
        line.close(); // Close the audio output.
        System.out.println("Test complete."); // Tell us the test is done.
    }
}