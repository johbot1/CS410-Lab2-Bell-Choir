import javax.sound.sampled.*;
import java.util.concurrent.*;

public class TestMember {
    public static void main(String[] args) throws Exception {
        // Create audio format
        AudioFormat af = new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);
        SourceDataLine line = AudioSystem.getSourceDataLine(af);
        line.open();
        line.start();

        // Create a shared BlockingQueue
        BlockingQueue<BellNote> playQueue = new LinkedBlockingQueue<>();

        // Create a test member
        Member member1 = new Member("Alice", line, playQueue);
        //        Debug.printMessage(5,member1.getName(),Note.C4); // Debug
        member1.assignNotes("left", new BellNote(Note.C4, NoteLength.QUARTER));
        //        Debug.printMessage(5,member1.getName(),Note.E4); // Debug
        member1.assignNotes("right", new BellNote(Note.E4, NoteLength.QUARTER));

        // Start the member thread
        member1.start();

        // Simulate Conductor cues
        playQueue.put(new BellNote(Note.C4, NoteLength.QUARTER)); // Should play
        Thread.sleep(500);
        playQueue.put(new BellNote(Note.E4, NoteLength.QUARTER)); // Should play

        // Allow time for playback
        Thread.sleep(1000);
        member1.interrupt(); // Stop the thread
        line.drain();
        line.close();
    }
}
