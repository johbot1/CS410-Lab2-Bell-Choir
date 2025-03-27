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


        Member member2 = new Member("Bob", line, playQueue);
        member2.assignNotes("left", new BellNote(Note.G4, NoteLength.QUARTER));

        Member member3 = new Member("Charlie", line, playQueue);
        member3.assignNotes("right", new BellNote(Note.E4, NoteLength.QUARTER));

        // Create conductor
        List<Member> members = Arrays.asList(member1, member2, member3);
        Conductor conductor = new Conductor(members, playQueue, 120);  // 120 BPM

        // Start member threads
        member1.start();
        member2.start();
        member3.start();

        // Start the performance
        List<BellNote> songNotes = Arrays.asList(
                new BellNote(Note.C4, NoteLength.QUARTER),
                new BellNote(Note.G4, NoteLength.QUARTER),
                new BellNote(Note.E4, NoteLength.QUARTER)
        );

        conductor.startPerformance(songNotes);

        // Signal to stop
        member1.stopRunning();
        member2.stopRunning();
        member3.stopRunning();

        // Wait for finish
        member1.join();
        member2.join();
        member3.join();

        line.drain();
        line.close();
    }
}
