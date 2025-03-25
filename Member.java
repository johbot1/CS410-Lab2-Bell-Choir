import javax.sound.sampled.SourceDataLine;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
/**
 * Member
 * Storing it's name and notes assigned ot it's left and right hands, a Member will
 * listen for BellNotes in the BlockingQueue, and play them if they match its assigned
 * notes. It continuously takes notes from the queue (playQueue.take), blocking until
 * a note is available. It interrupts and stops only if it catches an InterruptedException.
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

    public void assignNotes(String hand, BellNote note) {
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
        try {
            while (!Thread.currentThread().isInterrupted()) {
                BellNote bn = playQueue.take();  // Blocks until a note is available

                if (bn == null) {  // Termination signal received
                    System.out.println(name + " received stop signal.");
                    break;
                }

                // Check if the member is assigned to this note
                if (!assignedNotes.containsValue(bn)) {
                    System.err.println(name + " received an unassigned note: " + bn.note + " - Skipping...");
                    continue;  // Ignore and keep waiting for a valid note
                }

                playNote(bn);  // Play the valid assigned note
            }
        } catch (InterruptedException e) {
            System.out.println(name + " interrupted.");
            Thread.currentThread().interrupt(); // Restore interrupt status
        }
        System.out.println(name + " has finished playing.");
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