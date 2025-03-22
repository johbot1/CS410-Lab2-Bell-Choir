import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TestConductor {
    public static void main(String[] args) throws Exception {
        // Create audio format and line (assuming this is done earlier)
        AudioFormat af = new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);
        SourceDataLine line = AudioSystem.getSourceDataLine(af);
        line.open();
        line.start();

        // Create a shared BlockingQueue
        BlockingQueue<BellNote> playQueue = new LinkedBlockingQueue<>();

        // Create members
        Member member1 = new Member("Alice", line, playQueue);
        member1.assignNotes("left", new BellNote(Note.C4, NoteLength.QUARTER));
        member1.assignNotes("right", new BellNote(Note.E4, NoteLength.QUARTER));

        Member member2 = new Member("Bob", line, playQueue);
        member2.assignNotes("left", new BellNote(Note.G4, NoteLength.QUARTER));

        // Create conductor
        List<Member> members = Arrays.asList(member1, member2);
        Conductor conductor = new Conductor(members, playQueue, 120);  // 120 BPM

        // Start member threads
        member1.start();
        member2.start();

        // Start the performance
        List<BellNote> songNotes = Arrays.asList(
                new BellNote(Note.C4, NoteLength.QUARTER),
                new BellNote(Note.G4, NoteLength.QUARTER),
                new BellNote(Note.E4, NoteLength.QUARTER)
        );

        conductor.startPerformance(songNotes);

        // Allow time for playback
        Thread.sleep(3000);

        // Interrupt members after performance
        member1.interrupt();
        member2.interrupt();
        line.drain();
        line.close();
    }
}
