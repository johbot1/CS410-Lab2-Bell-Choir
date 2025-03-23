import javax.sound.sampled.SourceDataLine;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Member:
 * Each member will be its own thread,
 * have 1-2 assigned notes,
 * wait for cues from the Conductor,
 * and play ONLY their assigned notes
 * @author johnbotonakis
 */
public class Member extends Thread{
    private final String name; //Name of the Member
    final Map<String, BellNote> assignedNotes = new HashMap<>(); // "left" and "right" hand notes
    private final SourceDataLine line;  // Audio line for playback
    private final BlockingQueue<BellNote> playQueue; // Receives cues from Conductor

    public Member(String name, SourceDataLine line, BlockingQueue<BellNote> playQueue) {
        this.name = name;
        this.line = line;
        this.playQueue = playQueue;
    }

    public void assignNotes(String hand, BellNote note)
    {
        if (!hand.equals("left") && !hand.equals("right")){
            throw new IllegalArgumentException("Invalid hand; Hand must be 'left' or 'right'");
        }
        if (assignedNotes.containsKey(hand)){
            throw new IllegalArgumentException(name + " is already assigned a note in their " + hand + " hand!");
        }
        assignedNotes.put(hand, note);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                BellNote bn = playQueue.take();  // Blocks until a note is available
//                Debug.printMessage(1,name,bn.note); // Debug
                playNote(bn);
//               Debug.printMessage(2,name,bn.note); // Debug
            } catch (InterruptedException e) {
//                Debug.printError(1,name); // Debug
                break;  // Exit loop on interrupt
            }
        }
    }

    /**
     * playNote
     *
     * @param bn The note to be played
     * @implNote The reasoning behind putting playNote here instead of Tone is
     * encapsulation since every member is responsible for their own notes, and
     * concurrency, since if Tone handled all playback, every member would call a shared method,
     * leading to sync issues.
     */
    private void playNote(BellNote bn) {
//        Debug.printMessage(3,name,bn.note); // Debug
        final int ms = Math.min(bn.length.timeMs(), Note.MEASURE_LENGTH_SEC * 1000);
        final int length = Note.SAMPLE_RATE * ms / 1000;
//        Debug.printMessage(4,name,bn.note); // Debug
        line.write(bn.note.sample(), 0, length);
        line.write(Note.REST.sample(), 0, 50); // Small pause
    }
}