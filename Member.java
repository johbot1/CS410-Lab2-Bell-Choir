import javax.sound.sampled.SourceDataLine;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

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
    private volatile boolean keepRunning = true;
    private volatile boolean performanceFinished = false;

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

    public void stopRunning(){
        this.keepRunning = false;
    }

    public void setPerformanceFinished() {
        this.performanceFinished = true;
    }

    @Override
    public void run() {
        try {
            while (keepRunning) {
                BellNote noteToPlay = playQueue.poll(10, TimeUnit.MILLISECONDS); // Use poll with timeout
                if (noteToPlay == null) {
                    // Check if conductor has finished adding notes.
                    if (performanceFinished && playQueue.isEmpty()) {
                        System.out.println(name + " detected no more notes and is finishing.");
                        break; // Exit the loop, ending the thread
                    }
                    continue; //if the queue is empty, check again.
                }

                System.out.println(name + " took " + noteToPlay.note + " from queue.");

                if (canPlay(noteToPlay)) {
                    System.out.println(name + " is playing: " + noteToPlay.note);
                    playNote(noteToPlay);
                } else {
                    System.out.println(name + " cannot play: " + noteToPlay.note);
                    playQueue.put(noteToPlay);  // Put it back for another member
                }
            }
        } catch (InterruptedException e) {
            System.out.println(name + " interrupted.");
        } finally {
            System.out.println(name + " thread finished."); // Add this line
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

    public boolean canPlay(BellNote note) {
        boolean assigned = assignedNotes.containsValue(note);
        System.out.println(name + " checking if it can play " + note.note + ": " + assigned);
        return assigned;
    }
}