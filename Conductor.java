import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Conductor
 * The Conductor is responsible for cueing members to play their assigned notes. It loops through
 * the song's notes, and checks which Member has that note. If a Member has the note, it places it
 * in the BlockingQueue(playQueue), and after cueing a note, the conductor waits for the next beat
 */
public class Conductor {
    private final List<Member> members;  // List of all members
    private final BlockingQueue<BellNote> playQueue;  // Shared queue for signaling members
    private final int tempoBPM;  // Tempo in Beats Per Minute

    /**
     * Takes a list of Member objects, a BlockingQueue of BellNotes to signal the members, and the tempo in BPM
     * @param members
     * @param playQueue
     * @param tempoBPM
     */
    public Conductor(List<Member> members, BlockingQueue<BellNote> playQueue, int tempoBPM) {
        this.members = members;
        this.playQueue = playQueue;
        this.tempoBPM = tempoBPM;
    }

    /**
     * getBeatLengthMs
     * This method calculates the length of each beat in milliseconds based on the tempo (BPM).
     * It divides 60,000 milliseconds (1 minute) by the BPM to get the time for each beat.
     * @return
     */
    private int getBeatLengthMs() {
        return 60000 / tempoBPM;  // 60,000 ms in a minute divided by BPM
    }

    /**
     * cueMembers
     * Cues the members to play their assigned notes; It loops thru each note in the song, and for each note,
     * it checks which members are assigned to that note. It places the note into the shared Blocking Queue for
     * that member who has the assignment, then sleeps until the next beat.
     * @param notes
     * @throws InterruptedException
     */
    public void cueMembers(List<BellNote> notes) throws InterruptedException {
        for (BellNote note : notes) {
            System.out.println("Conductor is cuing note: " + note.note);  // Debugging cue
            boolean noteAssigned = false;

            for (Member member : members) {
                if (memberHasNoteAssigned(member, note)) {
                    playQueue.put(note);  // Add note to the queue for the correct member
                    System.out.println("Conductor cues " + member.getName() + " to play: " + note.note);
                    noteAssigned = true;
                    break;  // Break the loop after finding the member assigned to this note
                }
            }

            // If no member was assigned to this note, print a warning message
            if (!noteAssigned) {
                System.err.println("Warning: No member assigned to play note: " + note.note);
            }

            // Wait for the next beat based on tempo
            Thread.sleep(getBeatLengthMs());  // Adjust this for better accuracy
        }
    }


    /**
     * memberHasNoteAssigned
     * A helper method to check if a member has been assigned the note (either in the "left" or "right" hand).
     * @param member
     * @param note
     * @return
     */
    private boolean memberHasNoteAssigned(Member member, BellNote note) {
        // Check if the note is assigned to any of the member's hands (left or right)
        return member.assignedNotes.containsValue(note);
    }

    /**
     * startPerformance
     * This method starts the performance by signaling the members to play the notes in the given order (the song).
     * @param songNotes
     * @throws InterruptedException
     */
    public void startPerformance(List<BellNote> songNotes) throws InterruptedException {
        System.out.println("Conductor starting the performance...");
        cueMembers(songNotes);  // Cue the members to play the notes in sequence
    }
}
